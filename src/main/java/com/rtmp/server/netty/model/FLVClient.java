package com.rtmp.server.netty.model;

import com.rtmp.server.netty.common.AMF0;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FLVClient {
    private Channel channel;
    private byte[] flvHeader;
    private void setChannel(Channel channel){
        this.channel = channel;
    }
    public FLVClient(Channel channel){
        this.channel = channel;
        this.flvHeader =new byte[] {0x46, 0x4C, 0x56, 0x01, 0x05, 00, 00, 00, 0x09};
    }
    public FLVClient(){
        this.flvHeader =new byte[] {0x46, 0x4C, 0x56, 0x01, 0x05, 00, 00, 00, 0x09};
    }
    public synchronized void update(RTMPChunk rtmpChunk,byte tagType){
       byte[] data = encodeToFlv(rtmpChunk,tagType);
       this.channel.writeAndFlush(Unpooled.wrappedBuffer(data));
        System.out.println(data.length+"!!!");
    }
    public void init(RTMPChunk firstChunk, Map<String,Object> properties,RTMPChunk AVC){
        byte[] metaData = encodeOnMetaData(firstChunk,properties);
        this.channel.writeAndFlush(Unpooled.wrappedBuffer(metaData));
        byte[] avcData = encodeToFlv(AVC, (byte) 0x12);
        this.channel.writeAndFlush(Unpooled.wrappedBuffer(avcData));
    }
    public void init(RTMPChunk firstChunk, Map<String,Object> properties,RTMPChunk AVC,RTMPChunk AAC){
        byte[] metaData = encodeOnMetaData(firstChunk,properties);
        this.channel.writeAndFlush(Unpooled.wrappedBuffer(metaData));
        byte[] avcData = encodeToFlv(AVC, AVC.getRtmpChunkMessageHeader().getMessageTypeId());
        this.channel.writeAndFlush(Unpooled.wrappedBuffer(avcData));
        byte[] aacData = encodeToFlv(AAC, AAC.getRtmpChunkMessageHeader().getMessageTypeId());
        this.channel.writeAndFlush(Unpooled.wrappedBuffer(aacData));
    }
    private byte[] encodeToFlv(RTMPChunk rtmpChunk,byte tagType){
        ByteBuf buf = Unpooled.buffer();
        byte[] data = rtmpChunk.getPayload();
        buf.writeByte(tagType);
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
    private byte[] encodeOnMetaData(RTMPChunk chunk, Map<String,Object> properties){
        ByteBuf encodeMetaData = encodeProperties(properties);
        ByteBuf buf = Unpooled.buffer();
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
    public ByteBuf encodeProperties( Map<String,Object> properties){
        ByteBuf buffer = Unpooled.buffer();
        List<Object> list = new ArrayList<>();
        list.add("onMetaData");
        list.add(properties);
        AMF0.encode(buffer,list);
        return buffer;
    }
}
