package com.rtmp.server.netty.common;

import java.util.Random;

public class Tools {
	public static Integer toInt(byte[] data){
		return  (data[0] & 0xff) << 24| (data[1] & 0xff) << 16 | (data[2] & 0xff) << 8 | data[3] & 0xff;
	}
	public static int calculateMessageLength(byte fmt,int chunksize,int payloadLength,boolean useExtendTimeStamp){
		int basicHeader;
		int messageHeader;
		int extendTimeStamp =0;
		if (useExtendTimeStamp){
			extendTimeStamp = 4;
		}
		if (fmt == 0x00){
			basicHeader = 1;
			messageHeader = 11;
		}else {
			basicHeader =-10000000;
			messageHeader = -10000000;
			System.out.println("The Format has not been designed,please Check;");
		}
		int payloadSpareSpace = chunksize - basicHeader - messageHeader -extendTimeStamp;
		return Math.min(payloadLength,payloadSpareSpace);
	}
	public static byte[] IntToBytes(int data){
		byte[] bytes = new byte[4];
		bytes[0] = (byte) ((data >> 24) &0xff);
		bytes[1] = (byte) ((data >> 16) &0xff);
		bytes[2] = (byte) ((data >> 8) &0xff);
		bytes[3] = (byte) ((data) &0xff);
		return bytes;
	}
	public static byte[] IntToBytes(int data,int length){
		byte[] bytes = new byte[length];
		bytes[0] = (byte) ((data >> 24) &0xff);
		bytes[1] = (byte) ((data >> 16) &0xff);
		bytes[2] = (byte) ((data >> 8) &0xff);
		bytes[3] = (byte) ((data) &0xff);
		return bytes;
	}
	public static byte[] encodeFmtAndCsid(final int fmt, final int csid) {
		if (csid <= 63) {
			return new byte[] { (byte) ((fmt << 6) + csid) };
		} else if (csid <= 320) {
			return new byte[] { (byte) (fmt << 6), (byte) (csid - 64) };
		} else {
			return new byte[] { (byte) ((fmt << 6) | 1), (byte) ((csid - 64) & 0xff), (byte) ((csid - 64) >> 8) };
		}
	}
	public static boolean isAACAudioSpecificConfig(byte[] payload){
		return payload.length>1 && payload[1]==0;
	}
	public static boolean isKeyFrame(byte[] payload){
		return payload[0] == 0x17;
	}
	public static boolean isAVCDecoderConfigurationRecord(byte[] payload){
		return payload[1] == 0 && isKeyFrame(payload);
	}
}
