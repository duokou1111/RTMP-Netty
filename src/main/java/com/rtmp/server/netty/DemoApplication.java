package com.rtmp.server.netty;

import com.rtmp.server.netty.common.ApplicationContextUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
@EnableCaching
public class DemoApplication {

    public static void main(String[] args) throws InterruptedException {
        ConfigurableApplicationContext configurableApplicationContext = SpringApplication.run(DemoApplication.class, args);
        HTTPServer httpServer = new HTTPServer();
        httpServer.init(8080);
        RTMPServer rtmpServer = configurableApplicationContext.getBean(RTMPServer.class);
        rtmpServer.init(9999,configurableApplicationContext);
    }
}
