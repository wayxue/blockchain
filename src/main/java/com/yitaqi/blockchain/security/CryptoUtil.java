package com.yitaqi.blockchain.security;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import org.eclipse.jetty.util.security.Credential.MD5;

public class CryptoUtil {

	public static String SHA256(String str) {
		
		MessageDigest messageDigest;
		String encodeStr = "";
		try {
			messageDigest = MessageDigest.getInstance("SHA-256");
			messageDigest.update(str.getBytes("UTF-8"));
			encodeStr = byte2Hex(messageDigest.digest());
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return encodeStr;
		
	}
	
	private static String byte2Hex(byte[] bytes) {
		
		StringBuilder builder = new StringBuilder();
		String tmp;
		for (int i = 0; i < bytes.length; i++) {
			tmp = Integer.toHexString(bytes[i] & 0xFF);
			if (tmp.length() == 1) {
				builder.append("0");
			}
			builder.append(tmp);
		}
		return builder.toString();
	}
	
	public static String MD5(String str) {
		String result = MD5.digest(str);
		return result.substring(4, result.length());
	}
	
	public static String UUID() {
		return UUID.randomUUID().toString().replace("\\-", "");
	}
}
