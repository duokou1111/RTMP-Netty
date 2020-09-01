package com.rtmp.server.netty.handler;

import com.rtmp.server.netty.common.Tools;
import com.rtmp.server.netty.model.RTMPChunk;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class RTMPEncoder extends MessageToByteEncoder<RTMPChunk> {
    @Override
    protected void encode(ChannelHandlerContext ctx, RTMPChunk msg, ByteBuf out) throws Exception {
        int fmt = msg.getRtmpChunkBasicHeader().getChunkType();
        if (fmt == 0)
            handleFmtType0(msg);
    }
    private void handleFmtType0(RTMPChunk chunk){
        ByteBuf buffer = Unpooled.buffer(chunk.getSize());
        byte[] b = Tools.encodeFmtAndCsid(chunk.getRtmpChunkBasicHeader().getChunkType(),chunk.getRtmpChunkBasicHeader().getChunkStreamId());
        buffer.writeBytes(b);
    }
}
