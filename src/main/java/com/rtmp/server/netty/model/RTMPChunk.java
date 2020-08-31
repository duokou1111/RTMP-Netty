package com.rtmp.server.netty.model;

public class RTMPChunk {
    private RTMPChunkBasicHeader rtmpChunkBasicHeader;
    private RTMPChunkMessageHeader rtmpChunkMessageHeader;
    private int extendTimeStamp;
    private byte[] payload;
    private int size;

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public RTMPChunkBasicHeader getRtmpChunkBasicHeader() {
        return rtmpChunkBasicHeader;
    }

    public void setRtmpChunkBasicHeader(RTMPChunkBasicHeader rtmpChunkBasicHeader) {
        this.rtmpChunkBasicHeader = rtmpChunkBasicHeader;
    }

    public RTMPChunkMessageHeader getRtmpChunkMessageHeader() {
        return rtmpChunkMessageHeader;
    }

    public void setRtmpChunkMessageHeader(RTMPChunkMessageHeader rtmpChunkMessageHeader) {
        this.rtmpChunkMessageHeader = rtmpChunkMessageHeader;
    }

    public int getExtendTimeStamp() {
        return extendTimeStamp;
    }

    public void setExtendTimeStamp(int extendTimeStamp) {
        this.extendTimeStamp = extendTimeStamp;
    }

    public byte[] getPayload() {
        return payload;
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
    }
}
