package com.rtmp.server.netty.handler;

import com.alibaba.fastjson.JSONObject;
import com.rtmp.server.netty.common.AMF0;
import com.rtmp.server.netty.common.AMF0Project;
import com.rtmp.server.netty.common.Tools;
import com.rtmp.server.netty.model.*;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
@Component
@Scope("prototype")
public class RTMPChunkHandler extends SimpleChannelInboundHandler<RTMPChunk> {
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    RabbitTemplate rabbitTemplate;
    private static final String REDIS_PREFIX = "STREAM:";
    private final Logger log= LoggerFactory.getLogger(RTMPChunkHandler.class);
    private final byte SET_CHUNK_SIZE = 0x01;
    private final byte ABORT_MESSAGE = 0x02;
    private final byte ACKNOWLEDGEMENT =0x03;
    private final byte DATA_MESSAGE = 0x12;
    private final byte AUDIO_MESSAGE = 0x08;
    private final byte VIDEO_MESSAGE = 0x09;
    private final byte SET_ACKNOWLEDGEMENT_SIZE = 0x05;
    private final byte SET_BAND_WIDTH = 0x06;
    private final byte RTMP_COMMAND_MESSAGE = 0x14;
    private final int PROTOCOL_CHUNK_STREAM_ID = 2;
    private final int PROTOCOL_MESSAGE_STREAM_ID = 5;
    private final byte BAND_WIDTH_TYPE_SOFT = 0x01;
    private final String COMMAND_CONNECT ="connect";
    private final String COMMAND_PUBLISH = "publish";
    private final String COMMAND_FCPUBLISH = "FCPublish";
    private final String COMMAND_RELEASE_STREAM= "releaseStream";
    private final String COMMAND_CREATE_STREAM = "createStream";
    private final String COMMAND_DELETE_STREAM = "deleteStream";
    private final String COMMAND_UNPUBLISH_STREAM = "FCUnpublish";
    private int clientChunkSize = 128;
    private RTMPStream rtmpStream = new RTMPStream();
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RTMPChunk msg) throws Exception {
        switch (msg.getRtmpChunkMessageHeader().getMessageTypeId()){
            case AUDIO_MESSAGE:{
                log.info("SERVER RECEIVED A AUDIO MESSAGE");
                rtmpStream.addContent(msg);
                break;
            }
            case VIDEO_MESSAGE:{
                log.info("SERVER RECEIVED A VIDEO MESSAGE");
                rtmpStream.addContent(msg);
                break;
            }
            case DATA_MESSAGE:{
                log.info("SERVER RECEIVED A DATA MESSAGE");
                List<Object> list = AMF0.decodeAll(Unpooled.copiedBuffer(msg.getPayload()));
                String name = (String) list.get(0);
                if ("@setDataFrame".equals(name)) {
                    rtmpStream.setProperties((Map<String, Object>) list.get(2));
                }
                break;
            }
            case SET_CHUNK_SIZE:{
                int chunkSize = Tools.toInt(msg.getPayload());
                log.info("AssumedChunkSize:"+ chunkSize);
                RTMPDecoder rtmpDecoder =  ctx.pipeline().get(RTMPDecoder.class);
                rtmpDecoder.setChunkSize(chunkSize);
                break;
            }
            case ABORT_MESSAGE:{
                break;
            }
            case RTMP_COMMAND_MESSAGE:{
                List<Object> list = AMF0.decodeAll(Unpooled.copiedBuffer(msg.getPayload()));
                String command = list.get(0).toString();
                log.info("SERVER RECEIVED A COMMAND MESSAGE:"+command);
                switch (command){
                    case COMMAND_CONNECT:{
                        log.info("CONNECT COMMAND:");
                        List<Object> result = new ArrayList<Object>();
                        result.add("_result");
                        result.add(list.get(1));// transaction id
                        result.add(new AMF0Project().addProperty("fmsVer", "FMS/3,0,1,123").addProperty("capabilities", 31));
                        result.add(new  AMF0Project().addProperty("level", "status").addProperty("code", "NetConnection.Connect.Success")
                                .addProperty("description", "Connection succeeded").addProperty("objectEncoding", 0));
                        RTMPChunk ackSizeChunk = getSetAcknowledgementSizeChunk(5000000);
                        RTMPChunk bandWidthChunk = getSetBandWidthChunk(5000000,BAND_WIDTH_TYPE_SOFT);
                        RTMPChunk chunkSizeChunk = getSetChunkSizeChunk(5000);
                        RTMPChunk response = getRTMPMessageResponse(result);
                        ctx.writeAndFlush(ackSizeChunk);
                        ctx.writeAndFlush(bandWidthChunk);
                        ctx.writeAndFlush(chunkSizeChunk);
                        ctx.writeAndFlush(response);
                        rtmpStream.setApp((String) ((Map) list.get(2)).get("app"));
                        System.out.println("rtmpStream.getApp() = " + rtmpStream.getApp());
                        break;
                    }
                    case COMMAND_DELETE_STREAM:{
                        log.info("STREAM DELETE");
                        break;
                    }
                    case COMMAND_UNPUBLISH_STREAM:{
                        if (rtmpStream.getApp() == null){
                            ctx.close();
                            return;
                        }
                        redisTemplate.watch(REDIS_PREFIX+rtmpStream.getApp());
                        RedisStreamSettings redisStreamSettings = JSONObject.parseObject((String)redisTemplate.opsForValue().get(REDIS_PREFIX+rtmpStream.getApp()), RedisStreamSettings.class);
                        if(redisStreamSettings== null || !redisStreamSettings.getSecret().equals(rtmpStream.getSecret())){
                            ctx.close();
                            return;
                        }
                        SessionCallback sessionCallback = new SessionCallback() {
                            @Override
                            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                                redisOperations.multi();
                                redisOperations.delete(REDIS_PREFIX+rtmpStream.getApp());
                                try {
                                    redisOperations.exec();
                                    rabbitTemplate.convertAndSend("down",rtmpStream.getApp());
                                }catch (Exception e){
                                    ctx.close();
                                }
                                return null;
                            }
                        };
                        redisTemplate.execute(sessionCallback);
                        ctx.close();
                        break;
                    }
                    case COMMAND_FCPUBLISH:{
                        log.info("INTO THE COMMAND FCPUBLISH");
                        break;
                    }
                    case COMMAND_RELEASE_STREAM:{
                        log.info("INTO THE COMMAND RELEASE STREAM;");
                        break;
                    }
                    case COMMAND_CREATE_STREAM:{
                        log.info("INTO THE COMMAND CREATE STREAM!");
                        List<Object> result = new ArrayList<Object>();
                        result.add("_result");
                        result.add(list.get(1));// transaction id
                        result.add(null);// properties
                        result.add(5);// stream id
                        RTMPChunk response = getRTMPMessageResponse(result);
                        ctx.writeAndFlush(response);
                        break;
                    }
                    case COMMAND_PUBLISH:{
                        log.info("INTO THE COMMAND PUBLISH");
                        String streamType = (String) list.get(4);
                        String secret = (String) list.get(3);//串流密钥
                        List<Object> result = new ArrayList<>();
                        result.add("onStatus");
                        result.add(0);// always 0
                        result.add(null);
                        result.add(new AMF0Project().addProperty("level", "status").addProperty("code", "NetStream.Publish.Start").addProperty("description",
                                "Start publishing"));
                        RTMPChunk response = getRTMPMessageResponse(result);
                        ctx.writeAndFlush(response);
                        rtmpStream.setSecret(secret);
                        rtmpStream.setStreamType(streamType);
                        StreamManager.getInstance().addStream(rtmpStream.getApp(),rtmpStream);
                        redisTemplate.watch(REDIS_PREFIX+rtmpStream.getApp());
                        String jsonStr = (String) redisTemplate.opsForValue().get(REDIS_PREFIX+rtmpStream.getApp());
                        RedisStreamSettings redisStreamSettings =  JSONObject.parseObject(jsonStr,RedisStreamSettings.class);
                        if(redisStreamSettings == null || !rtmpStream.getSecret().equals(redisStreamSettings.getSecret())){
                            log.info("密钥错误，关闭连接");
                            ctx.close();
                        }
                        redisStreamSettings.setStatus("LIVE");
                        SessionCallback sessionCallback = new SessionCallback() {
                            @Override
                            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                                redisOperations.multi();
                                redisOperations.opsForValue().set(REDIS_PREFIX+rtmpStream.getApp(),JSONObject.toJSONString(redisStreamSettings));
                                try {
                                    redisOperations.exec();
                                    rabbitTemplate.convertAndSend("live",rtmpStream.getApp());
                                }catch (Exception e){
                                    log.info("房间号被占领，关闭连接");
                                    return ctx.close();
                                }
                                return null;
                            }
                        };
                        redisTemplate.execute(sessionCallback);

                        break;
                    }
                    default:{
                        log.info("RTMO COMMAND MESSAGE DO NOT ADAPT!");
                    }

                }
                break;
            }
            default:
                log.info("MessageTypeIdUnSupported!:"+msg.getRtmpChunkMessageHeader().getMessageTypeId());
        }
    }
    private void sendToServer(){

    }
    private RTMPChunk getRTMPMessageResponse(List<Object> list){
        RTMPChunkBasicHeader rtmpChunkBasicHeader = new RTMPChunkBasicHeader();
        rtmpChunkBasicHeader.setChunkType(0);
        rtmpChunkBasicHeader.setChunkStreamId(PROTOCOL_CHUNK_STREAM_ID);
        RTMPChunkMessageHeader rtmpChunkMessageHeader = new RTMPChunkMessageHeader();
        rtmpChunkMessageHeader.setMessageTypeId(RTMP_COMMAND_MESSAGE);
        rtmpChunkMessageHeader.setMessageStreamId(PROTOCOL_MESSAGE_STREAM_ID);
        rtmpChunkMessageHeader.setTimeStamp(0);
        RTMPChunk rtmpChunk = new RTMPChunk();
        ByteBuf buffer = Unpooled.buffer();
        AMF0.encode(buffer,list);
        byte[] payload = new byte[buffer.readableBytes()];
        rtmpChunkMessageHeader.setMessageLength(payload.length);
        buffer.readBytes(payload);
        rtmpChunk.setPayload(payload);
        rtmpChunk.setSize(clientChunkSize);
        System.out.println("clientChunkSizeAtResponse = " + clientChunkSize);
        rtmpChunk.setRtmpChunkBasicHeader(rtmpChunkBasicHeader);
        rtmpChunk.setRtmpChunkMessageHeader(rtmpChunkMessageHeader);
        return rtmpChunk;
    }
    private RTMPChunk getSetAcknowledgementSizeChunk(int ackSize){
        RTMPChunkBasicHeader rtmpChunkBasicHeader = new RTMPChunkBasicHeader();
        rtmpChunkBasicHeader.setChunkType(0);
        rtmpChunkBasicHeader.setChunkStreamId(PROTOCOL_CHUNK_STREAM_ID);
        RTMPChunkMessageHeader rtmpChunkMessageHeader = new RTMPChunkMessageHeader();
        rtmpChunkMessageHeader.setMessageTypeId(SET_ACKNOWLEDGEMENT_SIZE);
        rtmpChunkMessageHeader.setMessageLength(4);
        rtmpChunkMessageHeader.setMessageStreamId(PROTOCOL_MESSAGE_STREAM_ID);
        rtmpChunkMessageHeader.setTimeStamp(0);
        RTMPChunk rtmpChunk = new RTMPChunk();
        rtmpChunk.setSize(clientChunkSize);
        rtmpChunk.setRtmpChunkBasicHeader(rtmpChunkBasicHeader);
        rtmpChunk.setRtmpChunkMessageHeader(rtmpChunkMessageHeader);
        rtmpChunk.setPayload(Tools.IntToBytes(ackSize));
        return rtmpChunk;
    }
    private RTMPChunk getSetChunkSizeChunk(int clientChunkSize){
        RTMPChunkBasicHeader rtmpChunkBasicHeader = new RTMPChunkBasicHeader();
        rtmpChunkBasicHeader.setChunkType(0);
        rtmpChunkBasicHeader.setChunkStreamId(PROTOCOL_CHUNK_STREAM_ID);
        RTMPChunkMessageHeader rtmpChunkMessageHeader = new RTMPChunkMessageHeader();
        rtmpChunkMessageHeader.setMessageTypeId(SET_CHUNK_SIZE);
        rtmpChunkMessageHeader.setMessageLength(4);
        rtmpChunkMessageHeader.setMessageStreamId(PROTOCOL_MESSAGE_STREAM_ID);
        rtmpChunkMessageHeader.setTimeStamp(0);
        RTMPChunk rtmpChunk = new RTMPChunk();
        rtmpChunk.setSize(this.clientChunkSize);
        rtmpChunk.setPayload(Tools.IntToBytes(clientChunkSize));
        rtmpChunk.setRtmpChunkBasicHeader(rtmpChunkBasicHeader);
        rtmpChunk.setRtmpChunkMessageHeader(rtmpChunkMessageHeader);
        this.clientChunkSize = clientChunkSize;
        return rtmpChunk;
    }
    private RTMPChunk getSetBandWidthChunk(int ackSize,byte type){
        RTMPChunkBasicHeader rtmpChunkBasicHeader = new RTMPChunkBasicHeader();
        rtmpChunkBasicHeader.setChunkType(0);
        rtmpChunkBasicHeader.setChunkStreamId(PROTOCOL_CHUNK_STREAM_ID);
        RTMPChunkMessageHeader rtmpChunkMessageHeader = new RTMPChunkMessageHeader();
        rtmpChunkMessageHeader.setMessageTypeId(SET_BAND_WIDTH);
        rtmpChunkMessageHeader.setMessageLength(5);
        rtmpChunkMessageHeader.setMessageStreamId(PROTOCOL_MESSAGE_STREAM_ID);
        rtmpChunkMessageHeader.setTimeStamp(0);
        RTMPChunk rtmpChunk = new RTMPChunk();
        rtmpChunk.setSize(clientChunkSize);
        rtmpChunk.setRtmpChunkBasicHeader(rtmpChunkBasicHeader);
        rtmpChunk.setRtmpChunkMessageHeader(rtmpChunkMessageHeader);
        byte[] payload = Tools.IntToBytes(ackSize,5);
        payload[4] = type;
        rtmpChunk.setPayload(payload);
        return rtmpChunk;
    }
}
