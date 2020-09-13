package com.rtmp.server.netty.handler;

import com.rtmp.server.netty.model.StreamManager;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;

import static io.netty.handler.codec.http.HttpHeaderNames.*;

public class HTTPHandler extends SimpleChannelInboundHandler<HttpObject> {
    StreamManager streamManager = StreamManager.getInstance();
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
        if (msg instanceof HttpRequest) {
            HttpRequest req = (HttpRequest) msg;
            String uri = req.uri();
            System.out.println("uri = " + uri);
            streamManager.showAll();
            String appName = uri.substring(1);
            if (streamManager.hasKey(appName)){
                DefaultHttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
                response.headers().set(CONTENT_TYPE, "video/x-flv");
                response.headers().set(ACCESS_CONTROL_ALLOW_ORIGIN, "*");
                response.headers().set(ACCESS_CONTROL_ALLOW_METHODS, "GET, POST, PUT,DELETE");
                response.headers().set(ACCESS_CONTROL_ALLOW_HEADERS, "Origin, X-Requested-With, Content-Type, Accept");
                ctx.writeAndFlush(response);
                streamManager.getStream(appName).registerSubscriber(ctx.channel());
            }else {
                ByteBuf body = Unpooled.wrappedBuffer(("stream [" + appName + "] not exist").getBytes());
                DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                        HttpResponseStatus.NOT_FOUND, body);
                response.headers().set(CONTENT_TYPE, "text/plain");
                ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);

            }

        }

    }
}
