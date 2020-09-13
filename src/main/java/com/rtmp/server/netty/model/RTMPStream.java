package com.rtmp.server.netty.model;
import com.rtmp.server.netty.common.Tools;
import io.netty.channel.Channel;

import java.util.*;

public class RTMPStream {
    private Map<String,Object> properties;
    private String app;
    private String name;
    private String secret;
    private String streamType;
    private List<RTMPChunk> cache = new LinkedList<>();
    private List<FLVClient> subcriber = new ArrayList<>();
    private int basicTimeStamp = 0;
    private RTMPChunk CacheAVCDecoderConfigurationRecord;
    private RTMPChunk CacheAacAudioSpecificConfig;
    public RTMPStream(String name,String secret){
        this.name = name;
        this.app = app;
        this.secret = secret;
    }
    public RTMPStream(){

    }
    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getStreamType() {
        return streamType;
    }

    public void setStreamType(String streamType) {
        this.streamType = streamType;
    }

    public synchronized  void addContent(RTMPChunk rtmpChunk){
        if (rtmpChunk.getRtmpChunkBasicHeader().getChunkType() == 0){
            if (rtmpChunk.getRtmpChunkMessageHeader().getTimeStamp()!=0x00ffffff){
                basicTimeStamp = rtmpChunk.getRtmpChunkMessageHeader().getTimeStamp();
            }else {
                basicTimeStamp = rtmpChunk.getExtendTimeStamp();
            }
        }else{
            basicTimeStamp = basicTimeStamp + rtmpChunk.getRtmpChunkMessageHeader().getTimeStamp();
        }
        System.out.println("basicTimeStamp:"+basicTimeStamp);
        rtmpChunk.getRtmpChunkMessageHeader().setTimeStamp(basicTimeStamp);
        if (rtmpChunk.getRtmpChunkMessageHeader().getMessageTypeId() == 0x09) {
            if (Tools.isKeyFrame(rtmpChunk.getPayload())){
                System.out.println("KEY FRAME");
                cache.clear();
            }
            if (Tools.isAVCDecoderConfigurationRecord(rtmpChunk.getPayload())){
                CacheAVCDecoderConfigurationRecord = rtmpChunk;
                System.out.println(CacheAacAudioSpecificConfig.getRtmpChunkMessageHeader().getTimeStamp());
            }

        }
        if (rtmpChunk.getRtmpChunkMessageHeader().getMessageTypeId() == 0x08){
                if (Tools.isAACAudioSpecificConfig(rtmpChunk.getPayload())) {
                    CacheAacAudioSpecificConfig = rtmpChunk;
                }
        }
        cache.add(rtmpChunk);
        notifyAllSubscribers(rtmpChunk);
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
        this.app = app;this.name=app;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public void notifyAllSubscribers(RTMPChunk rtmpChunk){
        for (FLVClient flv:subcriber){
            flv.update(rtmpChunk,rtmpChunk.getRtmpChunkMessageHeader().getMessageTypeId());
        }
        System.out.println("notify subcriber");
    }
    public void removeSubscriber(Channel channel){
        subcriber.remove(channel);
    }
    public   void registerSubscriber(Channel channel){
        if (CacheAVCDecoderConfigurationRecord == null){
            System.out.println("AVCDecorderConfigurationRecord还未收到!");
            return;
        }
        FLVClient flvClient = new FLVClient(channel);
        subcriber.add(flvClient);
        CacheAVCDecoderConfigurationRecord.getRtmpChunkMessageHeader().setTimeStamp(cache.get(0).getRtmpChunkMessageHeader().getTimeStamp());
        if (CacheAacAudioSpecificConfig != null) {
            CacheAacAudioSpecificConfig.getRtmpChunkMessageHeader().setTimeStamp(cache.get(0).getRtmpChunkMessageHeader().getTimeStamp());
            flvClient.init(cache.get(0), this.properties, CacheAVCDecoderConfigurationRecord,CacheAacAudioSpecificConfig);
        }else {
            flvClient.init(cache.get(0), this.properties, CacheAVCDecoderConfigurationRecord);
        }
       /* for (RTMPChunk chunk:cache){
         flvClient.update(chunk,chunk.getRtmpChunkMessageHeader().getMessageTypeId());
        }
*/

    }
}
