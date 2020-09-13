package com.rtmp.server.demo;

import com.rtmp.server.netty.RTMPServer;
import com.rtmp.server.netty.HTTPServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) throws InterruptedException {
        SpringApplication.run(DemoApplication.class, args);

        HTTPServer httpServer = new HTTPServer();
        httpServer.init(8080);
        RTMPServer rtmpServer = new RTMPServer();
        rtmpServer.init(9999);
    }
}
