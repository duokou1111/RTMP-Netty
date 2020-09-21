package com.rtmp.server.netty;

import com.rtmp.server.netty.handler.RTMPChunkHandler;
import com.rtmp.server.netty.handler.RTMPDecoder;
import com.rtmp.server.netty.handler.RTMPEncoder;
import com.rtmp.server.netty.handler.RTMPShakeHandHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
@Configuration
public class RTMPServer {
    @Autowired
    @Lazy
    RTMPChunkHandler rtmpChunkHandler;
    @Autowired
    @Lazy
    RTMPShakeHandHandler rtmpShakeHandHandler;
    @Autowired
    @Lazy
    RTMPDecoder rtmpDecoder;
    @Autowired
    @Lazy
    RTMPEncoder rtmpEncoder;
    public void init(int port) throws InterruptedException {
        NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        //创建服务器启动对象配置参数
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class).option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast(rtmpShakeHandHandler)
                                .addLast(rtmpDecoder).addLast(rtmpEncoder)
                                .addLast(rtmpChunkHandler);
                    }
                });//workerGroup的eventLoop对于的管道处理器
        System.out.println("....服务器isReady");
        ChannelFuture cf = serverBootstrap.bind(9999).sync();
        cf.channel().closeFuture().sync();
    }

}
