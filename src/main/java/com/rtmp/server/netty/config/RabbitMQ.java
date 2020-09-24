package com.rtmp.server.netty.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQ {
    @Bean
    public Queue directLiveQueue(){
        return new Queue("live",true); //队列名字，是否持久化
    }
    @Bean
    public DirectExchange directExchange(){
        return new DirectExchange("direct",true,false);//交换器名称、是否持久化、是否自动删除
    }
    @Bean
    public Queue directDownQueue(){
        return new Queue("down",true); //队列名字，是否持久化
    }
    @Bean
    Binding bindingLive(Queue directLiveQueue, DirectExchange exchange){
        return BindingBuilder.bind(directLiveQueue).to(exchange).with("direct");
    }
    @Bean
    Binding bindingDown(Queue directDownQueue, DirectExchange exchange){
        return BindingBuilder.bind(directDownQueue).to(exchange).with("direct");
    }
}
