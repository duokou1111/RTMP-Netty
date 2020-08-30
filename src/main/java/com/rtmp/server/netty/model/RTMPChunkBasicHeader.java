package com.rtmp.server.netty.model;

public class RTMPChunkBasicHeader {
    private int chunkType;
    private int chunkStreamId;

    public int getChunkType() {
        return chunkType;
    }

    public void setChunkType(int chunkType) {
        this.chunkType = chunkType;
    }

    public int getChunkStreamId() {
        return chunkStreamId;
    }

    public void setChunkStreamId(int chunkStreamId) {
        this.chunkStreamId = chunkStreamId;
    }
}
