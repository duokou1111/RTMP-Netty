package com.rtmp.server.netty.model;

public class RTMPChunkMessageHeader {
    private int type;
    private int timeStamp;
    private int messageLength;
    private byte messageTypeId;
    private int messageStreamId;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(int timeStamp) {
        this.timeStamp = timeStamp;
    }

    public int getMessageLength() {
        return messageLength;
    }

    public void setMessageLength(int messageLength) {
        this.messageLength = messageLength;
    }

    public byte getMessageTypeId() {
        return messageTypeId;
    }

    public void setMessageTypeId(byte messageTypeId) {
        this.messageTypeId = messageTypeId;
    }

    public int getMessageStreamId() {
        return messageStreamId;
    }

    public void setMessageStreamId(int messageStreamId) {
        this.messageStreamId = messageStreamId;
    }
}
