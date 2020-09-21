package com.rtmp.server.demo;

import com.rtmp.server.netty.RTMPServer;
import com.rtmp.server.netty.HTTPServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class DemoApplication {

    public static void main(String[] args) throws InterruptedException {
        SpringApplication.run(DemoApplication.class, args);

        HTTPServer httpServer = new HTTPServer();
        httpServer.init(8080);
        RTMPServer rtmpServer = new RTMPServer();
        rtmpServer.init(9999);
    }
}
