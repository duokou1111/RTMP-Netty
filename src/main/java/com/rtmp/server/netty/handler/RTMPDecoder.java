package com.rtmp.server.netty.handler;

import com.rtmp.server.netty.model.RTMPChunkBasicHeader;
import com.rtmp.server.netty.model.RTMPChunkMessageHeader;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class RTMPDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        System.out.println("readable:"+in.readableBytes());
        RTMPChunkBasicHeader rtmpChunkBasicHeader= readChunkBasicHeader(in);
        System.out.println("chunkType:"+rtmpChunkBasicHeader.getChunkType());
        System.out.println("streamID:"+rtmpChunkBasicHeader.getChunkStreamId());
        RTMPChunkMessageHeader rtmpChunkMessageHeader = readChunkMessageHeader(in,rtmpChunkBasicHeader.getChunkType());
        System.out.println("rtmpChunkMessageHeader.getTimeStamp() = " + rtmpChunkMessageHeader.getTimeStamp());
        System.out.println("rtmpChunkMessageHeader.getMessageStreamId() = " + rtmpChunkMessageHeader.getMessageStreamId());
        System.out.println("rtmpChunkMessageHeader.getMessageLength() = " + rtmpChunkMessageHeader.getMessageLength());
        System.out.println("rtmpChunkMessageHeader.getMessageTypeId() = " + rtmpChunkMessageHeader.getMessageTypeId());
        if (rtmpChunkMessageHeader.getTimeStamp() == 0x0fff){
            int extendTimeStamp = in.readInt();
        }

        System.out.println(in.readableBytes()+"chunkSize:");
    }
    private RTMPChunkBasicHeader readChunkBasicHeader(ByteBuf in){
        byte b = in.readByte();
        RTMPChunkBasicHeader rtmpChunkBasicHeader = new RTMPChunkBasicHeader();
        rtmpChunkBasicHeader.setChunkType(b>>6 & 0x03);
        int isreserved = b & 0x3f;
        if(isreserved == 0){
            rtmpChunkBasicHeader.setChunkStreamId(in.readByte() & 0xff + 64);
        }
        if (isreserved == 1){
            byte secondByte = in.readByte();
            byte thirdByte = in.readByte();
            rtmpChunkBasicHeader.setChunkStreamId((thirdByte & 0xff) << 8 + (secondByte & 0xff) + 64);
        }
        if (isreserved == 2){
            rtmpChunkBasicHeader.setChunkStreamId(isreserved);
        }
        return rtmpChunkBasicHeader;
    }
    private RTMPChunkMessageHeader readChunkMessageHeader(ByteBuf in,int fmt){

        RTMPChunkMessageHeader rtmpChunkMessageHeader = new RTMPChunkMessageHeader();
        switch (fmt){
            case 0: {
                int timeStamp = in.readMedium();
                int messageLength = in.readMedium();
                byte messageTypeId = in.readByte();
                int streamId = in.readIntLE();
                rtmpChunkMessageHeader.setMessageLength(messageLength);
                rtmpChunkMessageHeader.setMessageStreamId(streamId);
                rtmpChunkMessageHeader.setTimeStamp(timeStamp);
                rtmpChunkMessageHeader.setMessageTypeId(messageTypeId);
                rtmpChunkMessageHeader.setType(0);
                break;
            }
            case 1: {
                int timeStamp = in.readMedium();
                int messageLength = in.readMedium();
                byte messageTypeId = in.readByte();
                rtmpChunkMessageHeader.setMessageLength(messageLength);
                rtmpChunkMessageHeader.setTimeStamp(timeStamp);
                rtmpChunkMessageHeader.setMessageTypeId(messageTypeId);
                rtmpChunkMessageHeader.setType(1);
                break;
            }
            case 2:{
                int timeStamp = in.readMedium();
                rtmpChunkMessageHeader.setType(2);
                rtmpChunkMessageHeader.setTimeStamp(timeStamp);
                break;
            }
            case 3:{
                rtmpChunkMessageHeader.setType(3);
                break;
            }
            default:
                System.out.println("Illegal Chunk Type:"+fmt);
        }
        return rtmpChunkMessageHeader;
    }

}
