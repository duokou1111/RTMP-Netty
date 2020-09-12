package com.rtmp.server.netty.model;
import com.rtmp.server.netty.common.AMF0;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class RTMPStream {
    private Map<String,Object> properties;
    private byte[] flvHeader;
    private String app;
    private String name;
    private String secret;
    private String streamType;
    private List<RTMPChunk> content = new LinkedList<>();
    private int basicTimeStamp = 0;
    private RTMPVedioChunk AVCDecoderConfigurationRecord;
    public RTMPStream(){
        flvHeader =new byte[] {0x46, 0x4C, 0x56, 0x01, 0x05, 00, 00, 00, 0x09};
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

    public void addContent(RTMPChunk rtmpChunk){
        if (rtmpChunk.getRtmpChunkBasicHeader().getChunkType() == 0){
            if (rtmpChunk.getRtmpChunkMessageHeader().getTimeStamp()!=0x00ffffff){
                basicTimeStamp = rtmpChunk.getRtmpChunkMessageHeader().getTimeStamp();
            }else {
                basicTimeStamp = rtmpChunk.getExtendTimeStamp();
            }
        }else{
            basicTimeStamp = basicTimeStamp + rtmpChunk.getRtmpChunkMessageHeader().getTimeStamp();
        }
        rtmpChunk.getRtmpChunkMessageHeader().setTimeStamp(basicTimeStamp);
        if (rtmpChunk.getRtmpChunkMessageHeader().getMessageTypeId() == 0x09) {
            RTMPVedioChunk rtmpVedioChunk = (RTMPVedioChunk) rtmpChunk;
            if (rtmpVedioChunk.isKeyFrame()){
                content.clear();
            }
            if (rtmpVedioChunk.isAVCDecoderConfigurationRecord()){
                AVCDecoderConfigurationRecord = rtmpVedioChunk;
            }
        }
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
        this.app = app;this.name=app;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public void addSubcriber(Channel channel){
        byte[] firstTag = encodeOnMetaData();
        channel.writeAndFlush(Unpooled.wrappedBuffer(firstTag));
        AVCDecoderConfigurationRecord.getRtmpChunkMessageHeader().setTimeStamp(content.get(0).getRtmpChunkMessageHeader().getTimeStamp());
        byte[] avcArr = encodeToFlv(AVCDecoderConfigurationRecord, (byte) 0x12);
        channel.writeAndFlush(Unpooled.wrappedBuffer(avcArr));
        for (RTMPChunk chunk : content) {
            channel.writeAndFlush(Unpooled.wrappedBuffer(encodeToFlv(chunk,chunk.getRtmpChunkMessageHeader().getMessageTypeId())));
        }
    }
    private byte[] encodeToFlv(RTMPChunk rtmpChunk,byte tagType){
        ByteBuf buf = Unpooled.buffer();
        byte[] data = rtmpChunk.getPayload();
        buf.writeByte(tagType);//TAG TYPE(SCRIPT TAG)
        buf.writeMedium(data.length);//DATA SIZE
        int timestamp = rtmpChunk.getRtmpChunkMessageHeader().getTimeStamp() & 0xffffff;
        int timestampExtended = ((rtmpChunk.getRtmpChunkMessageHeader().getTimeStamp()  & 0xff000000) >> 24);
        buf.writeMedium(timestamp);
        buf.writeByte(timestampExtended);
        buf.writeMedium(0);// streamId
        buf.writeBytes(data);
        buf.writeInt(data.length + 11); //prevousTagSize
        byte[] arr = new byte[buf.readableBytes()];
        buf.readBytes(arr);
        return arr;
    }
    private byte[] encodeOnMetaData(){
        ByteBuf encodeMetaData = encodeProperties();
        ByteBuf buf = Unpooled.buffer();
        RTMPChunk chunk = content.get(0);
        int timestamp = chunk.getRtmpChunkMessageHeader().getTimeStamp() & 0xffffff;
        int timestampExtended = ((chunk.getRtmpChunkMessageHeader().getTimeStamp() & 0xff000000) >> 24);
        //flv header
        buf.writeBytes(flvHeader);
        //previous tag size
        buf.writeInt(0);
        //tag header
        buf.writeByte(0x12);//script tag
        int dataSize = encodeMetaData.readableBytes();
        buf.writeMedium(dataSize);//Data size;
        buf.writeMedium(timestamp);
        buf.writeByte(timestampExtended);
        buf.writeMedium(0);//streamId
        buf.writeBytes(encodeMetaData);//data
        buf.writeInt(dataSize + 11);//previous tag size(this chunk)
        byte[] firstTag = new byte[buf.readableBytes()];
        buf.readBytes(firstTag);
        return firstTag;
    }
    public ByteBuf encodeProperties(){
        ByteBuf buffer = Unpooled.buffer();
        List<Object> list = new ArrayList<>();
        list.add("onMetaData");
        list.add(properties);
        AMF0.encode(buffer,list);
        return buffer;
    }
}
