package com.yitaqi.blockchain.security;

import static org.junit.Assert.*;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;

/**
 * 非对称加密测试
 * @author xue
 *
 */
public class RSACoderTest {

	private String privateKey;
	private String publicKey;
	
	@Before
	public void setUp() throws Exception {
		Map<String, Object> keyMap = RSACoder.initKey();
		privateKey = RSACoder.getPrivateKey(keyMap);
		publicKey = RSACoder.getPublicKey(keyMap);
		System.out.println("私钥： \r\n" + privateKey);
		System.out.println("公钥： \r\n" + publicKey);
	}
	
	@Test
	public void testEncrypt() throws Exception {
		System.out.println("公钥加密--私钥解密");
		String input = "去留无意，闲看庭前花开花落";
		System.out.println("加密前：" + input);
		byte[] data = input.getBytes();
		byte[] encodeData = RSACoder.encryptByPublicKey(data, publicKey);
		System.out.println("密文：" + new String(encodeData));
		byte[] decodeData = RSACoder.decryptByPrivateKey(encodeData, privateKey);
		System.out.println("解密后：" + new String(decodeData));
		assertEquals(input, new String(decodeData));
	}
	
	@Test
	public void testSign() throws Exception {
		System.out.println("私钥签名--公钥认证");
		String input = "宠辱不惊，漫随天外云卷云舒";
		System.out.println("原文：" + input);
		byte[] data = input.getBytes();
		String sign = RSACoder.sign(data, privateKey);
		System.out.println("签名后：" + sign);
		boolean verify = RSACoder.verify(data, publicKey, sign);
		System.out.println("验证结果：" + verify);
		assertTrue(verify);
	}
}
