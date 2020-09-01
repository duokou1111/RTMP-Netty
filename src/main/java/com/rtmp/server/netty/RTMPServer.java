package com.rtmp.server.netty;

import com.rtmp.server.netty.handler.RTMPChunkHandler;
import com.rtmp.server.netty.handler.RTMPDecoder;
import com.rtmp.server.netty.handler.RTMPShakeHandHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
public class RTMPServer {
    public void init(int port) throws InterruptedException {
        NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        //创建服务器启动对象配置参数
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup,workerGroup)
                .channel(NioServerSocketChannel.class).option(ChannelOption.SO_BACKLOG,128)
                .childOption(ChannelOption.SO_KEEPALIVE,true)
                .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast(new RTMPShakeHandHandler())
                       .addLast(new RTMPDecoder()).addLast(new RTMPChunkHandler());
                    }
                });//workerGroup的eventLoop对于的管道处理器
        System.out.println("....服务器isReady");
        ChannelFuture cf= serverBootstrap.bind(port).sync();
        cf.channel().closeFuture().sync();
    }
}
