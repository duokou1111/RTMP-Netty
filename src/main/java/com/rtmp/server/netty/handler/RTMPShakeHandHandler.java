package com.rtmp.server.netty.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Date;
@Component
public class RTMPShakeHandHandler extends ChannelInboundHandlerAdapter {
    private final Logger log= LoggerFactory.getLogger(RTMPShakeHandHandler.class);
    private static final byte S0 = 0x03;
    private byte[] S1;
    private byte[] S2;
    private boolean success = false;
    private Boolean isReceivedC0;
    private Boolean isReceivedC1;
    private ByteBuf heapBuf = ByteBufAllocator.DEFAULT.heapBuffer(1,6074);
    private int i= 1;
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        isReceivedC0 = false;
        isReceivedC1 = false;
        super.channelActive(ctx);
    }
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        heapBuf.writeBytes((ByteBuf)msg);
        if(heapBuf.readableBytes()>=1 && isReceivedC0 == false){
            log.info("RTMPServer Received C0");
            byte[] C0 = new byte[1];
            heapBuf.readBytes(C0);
            if(C0[0]!=S0){
                log.info("RTMP协议版本错误");
                ctx.close();
            }
            isReceivedC0 = true;
            ctx.writeAndFlush(Unpooled.copiedBuffer(new byte[]{S0}));
            log.info("RTMP版本验证正确");
        }
        if(heapBuf.readableBytes() >= 1536 && isReceivedC1 == false){
            byte[] C1 = new byte[1536];
            S1 = new byte[1536];
            heapBuf.readBytes(C1);
            S1 = setNowTime(S1);
            S1[1535] = (byte) 0xff;
            ctx.writeAndFlush(ctx.writeAndFlush(Unpooled.copiedBuffer(S1)));
            S2 = C1;
            S2[4] = S1[0];
            S2[5] = S1[1];
            S2[6] = S1[2];
            S2[7] = S1[3];
            ctx.writeAndFlush(ctx.writeAndFlush(Unpooled.copiedBuffer(S2)));
            isReceivedC1 = true;
            log.info("RTMPSever Received C1");
        }
        if(isReceivedC1 == true && heapBuf.readableBytes() >= 1536 && success == false){
            byte[] C2 = new byte[1536];
            heapBuf.readBytes(C2);
            success = true;
            log.info("RTMPServer Received C2");
        }
        if (success == true){
            log.info("RTMPServer HandShake Completed!");
            ctx.fireChannelRead(heapBuf);
            ctx.channel().pipeline().remove(RTMPShakeHandHandler.class);
        }

    }


    private byte[] setNowTime(byte[] arr){
        int time = (int) (new Date().getTime() / 1000);
        arr[0] = (byte) (time >>24 & 0xff);
        arr[1] = (byte) (time >>16 & 0xff);
        arr[2] = (byte) (time >>8 & 0xff);
        arr[3] = (byte) (time & 0xff);
        return arr;
    }
}
