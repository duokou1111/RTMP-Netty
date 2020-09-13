package com.rtmp.server.netty.model;

import java.util.HashMap;
import java.util.Map;

public class StreamManager {
    private Map<String,RTMPStream> streams;
    private static StreamManager instance = new StreamManager();
    private StreamManager(){
        streams = new HashMap<>();
    }
    public void showAll(){
        for (String str:streams.keySet()){
            System.out.println(str);
        }
    }
    public boolean hasKey(String name){
        return streams.containsKey(name);
    }
    public static StreamManager getInstance(){
        return instance;
    }
    public void addStream(String name,RTMPStream stream){
        streams.put(name,stream);
    }
    public RTMPStream getStream(String name){
        return streams.get(name);
    }
}
