package com.rtmp.server.netty.common;

import java.util.Random;

public class Tools {
	public static Integer toInt(byte[] data){
		return  (data[0] & 0xff) << 24| (data[1] & 0xff) << 16 | (data[2] & 0xff) << 8 | data[3] & 0xff;
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
}
