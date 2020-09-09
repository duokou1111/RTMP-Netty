package com.rtmp.server.netty.model;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class RTMPStream {
    private Map<String,Object> properties;
    private String app;
    private String name;
    private List<RTMPChunk> content = new LinkedList<>();
    public void addContent(RTMPChunk rtmpChunk){
        content.add(rtmpChunk);
    }
    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
