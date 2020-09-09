package com.rtmp.server.netty.model;

public class RTMPVedioChunk extends RTMPChunk{
    private int timeStampDelta;
    private int timeStamp;

    public int getTimeStampDelta() {
        return timeStampDelta;
    }

    public void setTimeStampDelta(int timeStampDelta) {
        this.timeStampDelta = timeStampDelta;
    }

    public int getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(int timeStamp) {
        this.timeStamp = timeStamp;
    }
}
