package com.rtmp.server.netty.handler;

import com.rtmp.server.netty.common.AMF0;
import com.rtmp.server.netty.common.AMF0Project;
import com.rtmp.server.netty.common.Tools;
import com.rtmp.server.netty.model.RTMPChunk;
import com.rtmp.server.netty.model.RTMPChunkBasicHeader;
import com.rtmp.server.netty.model.RTMPChunkMessageHeader;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class RTMPChunkHandler extends SimpleChannelInboundHandler<RTMPChunk> {
    private final Logger log= LoggerFactory.getLogger(RTMPChunkHandler.class);
    private final byte SET_CHUNK_SIZE = 0x01;
    private final byte ABORT_MESSAGE = 0x02;
    private final byte ACKNOWLEDGEMENT =0x03;
    private final byte SET_ACKNOWLEDGEMENT_SIZE = 0x05;
    private final byte SET_BAND_WIDTH = 0x06;
    private final byte RTMP_COMMAND_MESSAGE = 0x14;
    private final int PROTOCOL_CHUNK_STREAM_ID = 2;
    private final int PROTOCOL_MESSAGE_STREAM_ID = 5;
    private final byte BAND_WIDTH_TYPE_SOFT = 0x01;
    private final String COMMAND_CONNECT ="connect";
    private int clientChunkSize = 128;
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RTMPChunk msg) throws Exception {
        switch (msg.getRtmpChunkMessageHeader().getMessageTypeId()){
            case SET_CHUNK_SIZE:{//设置chunk中Data字段所能承载的最大字节数
                log.info("AssumedChunkSize:"+ Tools.toInt(msg.getPayload()));
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
                        break;
                    }

                }
                break;
            }
            default:
                log.info("MessageTypeIdUnSupported!:"+msg.getRtmpChunkMessageHeader().getMessageTypeId());
        }
    }
    private RTMPChunk getRTMPMessageResponse(List<Object> list){
        RTMPChunkBasicHeader rtmpChunkBasicHeader = new RTMPChunkBasicHeader();
        rtmpChunkBasicHeader.setChunkType(0);
        rtmpChunkBasicHeader.setChunkStreamId(PROTOCOL_CHUNK_STREAM_ID);
        RTMPChunkMessageHeader rtmpChunkMessageHeader = new RTMPChunkMessageHeader();
        rtmpChunkMessageHeader.setMessageTypeId(RTMP_COMMAND_MESSAGE);
        rtmpChunkMessageHeader.setMessageLength(4);
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
