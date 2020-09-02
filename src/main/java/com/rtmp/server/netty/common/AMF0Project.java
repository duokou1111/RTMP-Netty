package com.rtmp.server.netty.common;

import java.util.LinkedHashMap;

public class AMF0Project extends LinkedHashMap<String, Object> {

    private static final long serialVersionUID = 1L;
    public AMF0Project addProperty(String key,Object value) {
        put(key, value);
        return this;
    }
}
