package com.rtmp.server.netty.model;

public class RTMPVedioChunk extends RTMPChunk{

    public boolean isKeyFrame(){
        return this.getPayload()[0] == 0x17;
    }
    public boolean isAVCDecoderConfigurationRecord(){
        return this.getPayload()[1] == 0 && isKeyFrame();
    }
}
