package com.rtmp.server.netty.model;

import java.util.Arrays;

public class RTMPChunk{
    private RTMPChunkBasicHeader rtmpChunkBasicHeader;
    private RTMPChunkMessageHeader rtmpChunkMessageHeader;
    private int extendTimeStamp;
    private byte[] payload;
    private int size;

   public RTMPChunk clone() throws CloneNotSupportedException {
       RTMPChunkBasicHeader cloneRTMPChunkBasicHeader = (RTMPChunkBasicHeader) rtmpChunkBasicHeader.clone();
       RTMPChunkMessageHeader cloneRTMPChunkMessageHeader = (RTMPChunkMessageHeader)rtmpChunkMessageHeader.clone();
       RTMPChunk rtmpChunk = new RTMPChunk();
       rtmpChunk.setPayload(this.payload);
       rtmpChunk.setSize(this.size);
       rtmpChunk.setExtendTimeStamp(this.extendTimeStamp);
       rtmpChunk.setRtmpChunkMessageHeader(cloneRTMPChunkMessageHeader);
       rtmpChunk.setRtmpChunkBasicHeader(cloneRTMPChunkBasicHeader);
       return rtmpChunk;
   }

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

    @Override
    public String toString() {
        return "RTMPChunk{" +
                "rtmpChunkBasicHeader=" + rtmpChunkBasicHeader +
                ", rtmpChunkMessageHeader=" + rtmpChunkMessageHeader +
                ", extendTimeStamp=" + extendTimeStamp +
                ", payload=" + Arrays.toString(payload) +
                ", size=" + size +
                '}';
    }
}
