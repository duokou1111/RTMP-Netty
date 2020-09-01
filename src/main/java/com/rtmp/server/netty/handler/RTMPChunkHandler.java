package com.rtmp.server.netty.handler;

import com.rtmp.server.netty.common.Tools;
import com.rtmp.server.netty.model.RTMPChunk;
import com.rtmp.server.netty.model.RTMPChunkBasicHeader;
import com.rtmp.server.netty.model.RTMPChunkMessageHeader;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RTMPChunkHandler extends SimpleChannelInboundHandler<RTMPChunk> {
    private final Logger log= LoggerFactory.getLogger(RTMPChunkHandler.class);
    private final int SET_CHUNK_SIZE = 1;
    private final int ABORT_MESSAGE = 2;
    private final int ACKNOWLEDGEMENT =3;
    private final int SET_ACKNOWLEDGEMENT_SIZE = 5;
    private final int SET_BAND_WIDTH = 6;
    private final int CONNECT = 20;
    private final int PROTOCOL_CHUNK_STREAM_ID = 2;
    private final int PROTOCOL_MESSAGE_STREAM_ID = 0;
    private final byte BAND_WIDTH_TYPE_SOFT = (byte)1;
    private int clientChunkSize = 128;
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RTMPChunk msg) throws Exception {
        switch (msg.getRtmpChunkMessageHeader().getMessageTypeId()){
            case SET_CHUNK_SIZE:{//设置chunk中Data字段所能承载的最大字节数
                log.info("AssumedChunkSize:"+ Tools.toInt(msg.getPayload()));
            }
            case ABORT_MESSAGE:{

            }
            case CONNECT:{
                log.info("CLIENT WANT TO CONNECT");
                RTMPChunk ackSizeChunk = getSetAcknowledgementSizeChunk(5000000);
                RTMPChunk bandWidthChunk = getSetBandWidthChunk(5000000,BAND_WIDTH_TYPE_SOFT);
                RTMPChunk chunkSizeChunk = getSetChunkSizeChunk(5000);
                ctx.writeAndFlush(ackSizeChunk);
                ctx.writeAndFlush(bandWidthChunk);
                ctx.writeAndFlush(chunkSizeChunk);
            }
        }
    }
    private RTMPChunk getSetAcknowledgementSizeChunk(int ackSize){
        RTMPChunkBasicHeader rtmpChunkBasicHeader = new RTMPChunkBasicHeader();
        rtmpChunkBasicHeader.setChunkType(0);
        rtmpChunkBasicHeader.setChunkStreamId(PROTOCOL_CHUNK_STREAM_ID);
        RTMPChunkMessageHeader rtmpChunkMessageHeader = new RTMPChunkMessageHeader();
        rtmpChunkMessageHeader.setType(SET_ACKNOWLEDGEMENT_SIZE);
        rtmpChunkMessageHeader.setMessageLength(4);
        rtmpChunkMessageHeader.setMessageStreamId(PROTOCOL_MESSAGE_STREAM_ID);
        rtmpChunkMessageHeader.setTimeStamp(0);
        RTMPChunk rtmpChunk = new RTMPChunk();
        rtmpChunk.setSize(clientChunkSize);
        rtmpChunk.setPayload(Tools.IntToBytes(ackSize));
        return rtmpChunk;
    }
    private RTMPChunk getSetChunkSizeChunk(int clientChunkSize){
        RTMPChunkBasicHeader rtmpChunkBasicHeader = new RTMPChunkBasicHeader();
        rtmpChunkBasicHeader.setChunkType(0);
        rtmpChunkBasicHeader.setChunkStreamId(PROTOCOL_CHUNK_STREAM_ID);
        RTMPChunkMessageHeader rtmpChunkMessageHeader = new RTMPChunkMessageHeader();
        rtmpChunkMessageHeader.setType(SET_CHUNK_SIZE);
        rtmpChunkMessageHeader.setMessageLength(4);
        rtmpChunkMessageHeader.setMessageStreamId(PROTOCOL_MESSAGE_STREAM_ID);
        rtmpChunkMessageHeader.setTimeStamp(0);
        RTMPChunk rtmpChunk = new RTMPChunk();
        rtmpChunk.setSize(clientChunkSize);
        rtmpChunk.setPayload(Tools.IntToBytes(clientChunkSize));
        this.clientChunkSize = clientChunkSize;
        return rtmpChunk;
    }
    private RTMPChunk getSetBandWidthChunk(int ackSize,byte type){
        RTMPChunkBasicHeader rtmpChunkBasicHeader = new RTMPChunkBasicHeader();
        rtmpChunkBasicHeader.setChunkType(0);
        rtmpChunkBasicHeader.setChunkStreamId(PROTOCOL_CHUNK_STREAM_ID);
        RTMPChunkMessageHeader rtmpChunkMessageHeader = new RTMPChunkMessageHeader();
        rtmpChunkMessageHeader.setType(SET_ACKNOWLEDGEMENT_SIZE);
        rtmpChunkMessageHeader.setMessageLength(5);
        rtmpChunkMessageHeader.setMessageStreamId(PROTOCOL_MESSAGE_STREAM_ID);
        rtmpChunkMessageHeader.setTimeStamp(0);
        RTMPChunk rtmpChunk = new RTMPChunk();
        rtmpChunk.setSize(clientChunkSize);
        byte[] payload = Tools.IntToBytes(ackSize,5);
        payload[4] = type;
        rtmpChunk.setPayload(Tools.IntToBytes(ackSize));
        return rtmpChunk;
    }
}
