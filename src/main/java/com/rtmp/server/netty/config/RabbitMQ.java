package com.rtmp.server.netty.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMQ {
    @Bean
    public FanoutExchange toolExchange(){
        return new FanoutExchange("toolExchange",true,false);
    }
    @Bean
    public Queue toolQueue(){
        Map<String,Object> args = new HashMap();
        args.put("x-message-ttl",600000);
        args.put("x-dead-letter-exchange","disconnect");
        args.put("x-dead-letter-routing-key","disconnectKey");
        return new Queue("toolQueue",true,false,false,args);
    }
    @Bean
    Binding bindingNormal(Queue toolQueue,FanoutExchange toolExchange){
        return BindingBuilder.bind(toolQueue).to(toolExchange);
    }
    @Bean
    public DirectExchange deadLetterExchange(){
        return new DirectExchange("disconnect",true,false);
    }
    @Bean
    public Queue directDisconnectQueue(){

        return new Queue("disconnect",true,false,false,null);
    }
    @Bean
    Binding bindingDisconnect(Queue directDisconnectQueue,DirectExchange deadLetterExchange){
        return BindingBuilder.bind(directDisconnectQueue).to(deadLetterExchange).with("disconnectKey");
    }
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
    Binding bindingLive(Queue directLiveQueue, DirectExchange directExchange){
        return BindingBuilder.bind(directLiveQueue).to(directExchange).with("direct");
    }
    @Bean
    Binding bindingDown(Queue directDownQueue, DirectExchange directExchange){
        return BindingBuilder.bind(directDownQueue).to(directExchange).with("direct");
    }
}
