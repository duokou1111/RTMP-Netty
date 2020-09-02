package com.rtmp.server.netty.handler;

import com.rtmp.server.netty.common.Tools;
import com.rtmp.server.netty.model.RTMPChunk;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RTMPEncoder extends MessageToByteEncoder<RTMPChunk> {
    private final Logger log= LoggerFactory.getLogger(RTMPEncoder.class);
    @Override
    protected void encode(ChannelHandlerContext ctx, RTMPChunk msg, ByteBuf out) throws Exception {
        int fmt = msg.getRtmpChunkBasicHeader().getChunkType();
        if (fmt == 0)
            handleFmtType0(msg,out);
    }
    private void handleFmtType0(RTMPChunk chunk,ByteBuf out){
        log.info("Message TypeId:{}"+chunk.getRtmpChunkMessageHeader().getMessageTypeId());
        ByteBuf buffer = Unpooled.buffer(chunk.getSize());
        ByteBuf payload =Unpooled.buffer(chunk.getPayload().length);
        while (payload.readableBytes()>0) {
            boolean useExtraTimeStamp = false;
            final int TIME_STAMP_MAX_SIZE = 16777215; //equals 0x00ffffff
            byte[] b = Tools.encodeFmtAndCsid(chunk.getRtmpChunkBasicHeader().getChunkType(), chunk.getRtmpChunkBasicHeader().getChunkStreamId());
            //写入fmt和chunkStreamId(1 byte)
            buffer.writeBytes(b);
            //写入时间戳(3 bytes)
            if (chunk.getRtmpChunkMessageHeader().getTimeStamp() >= TIME_STAMP_MAX_SIZE) {
                useExtraTimeStamp = true;
                buffer.writeMedium(TIME_STAMP_MAX_SIZE);
            } else {
                buffer.writeMedium(chunk.getRtmpChunkMessageHeader().getTimeStamp());
            }
            //写入MessageLength
            buffer.writeMedium(chunk.getRtmpChunkMessageHeader().getMessageLength());
            //写入messageType
            buffer.writeBytes(new byte[]{chunk.getRtmpChunkMessageHeader().getMessageTypeId()});
            //写入messageStreamId
            buffer.writeInt(chunk.getRtmpChunkMessageHeader().getMessageStreamId());
            //写入ExtendedTimeStamp
            if (useExtraTimeStamp) {
                buffer.writeInt(chunk.getRtmpChunkMessageHeader().getTimeStamp());
            }
            buffer.writeBytes(payload,Math.min(chunk.getRtmpChunkMessageHeader().getMessageLength(),buffer.readableBytes()));
            out.writeBytes(buffer);
        }

    }
}
