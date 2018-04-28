package com.yitaqi.blockchain.security;

import java.io.IOException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;

/**
 * RSA安全编码组件
 * @author xue
 *
 */
public abstract class RSACoder extends Coder {

	public static final String KEY_ALGORITHM = "RSA";
	public static final String SIGNATURE_ALGORITHM = "MD5withRSA";
	
	private static final String PUBLIC_KEY = "RSAPublicKey";
	private static final String PRIVATE_KEY = "RSAPrivateKey";
	
	/**
	 * 用私钥对信息生成技术加密
	 * @param data
	 * @param privateKey
	 * @return
	 * @throws Exception
	 */
	public static String sign(byte[] data, String privateKey) throws Exception {
		// 解密私钥
		byte[] keyBytes = decryptBASE64(privateKey);
		PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(keyBytes);
		KeyFactory factory = KeyFactory.getInstance(KEY_ALGORITHM);
		PrivateKey pKey = factory.generatePrivate(pkcs8EncodedKeySpec);
		Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
		signature.initSign(pKey);
		signature.update(data);
		return encryptBASE64(signature.sign());
	}
	
	/**
	 * 校验数字签名
	 * @param data
	 * @param key
	 * @param sign
	 * @return
	 * @throws Exception
	 */
	public static boolean verify(byte[] data, String key, String sign) throws Exception {
		// 解密公钥
		byte[] keyBytes = decryptBASE64(key);
		X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(keyBytes);
		KeyFactory factory = KeyFactory.getInstance(KEY_ALGORITHM);
		PublicKey publicKey = factory.generatePublic(x509EncodedKeySpec);
		Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
		signature.initVerify(publicKey);
		signature.update(data);
		return signature.verify(decryptBASE64(sign));
	}
	
	/**
	 * 解密<br>
	 * 用私钥解密
	 * @param data
	 * @param key
	 * @return
	 * @throws IOException
	 * @throws Exception
	 */
	public static byte[] decryptByPrivateKey(byte[] data, String key) throws IOException, Exception {
		// 对私钥解密
		byte[] keyBytes = decryptBASE64(key);
		// 取得私钥
		PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(keyBytes);
		KeyFactory factory = KeyFactory.getInstance(KEY_ALGORITHM);
		Key privateKey = factory.generatePrivate(pkcs8EncodedKeySpec);
		// 对数据解密
		Cipher cipher = Cipher.getInstance(factory.getAlgorithm());
		cipher.init(Cipher.DECRYPT_MODE, privateKey);
		return cipher.doFinal(data);
	}

	/**
	 * 解密<br>
	 * 用公钥解密
	 * @param data
	 * @param key
	 * @return
	 * @throws Exception
	 */
	public static byte[] decryptByPublicKey(byte[] data, String key) throws Exception {
		// 对公钥解密
		byte[] keyBytes = decryptBASE64(key);
		// 取得公钥
		X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(keyBytes);
		KeyFactory factory = KeyFactory.getInstance(KEY_ALGORITHM);
		Key publicKey = factory.generatePublic(x509EncodedKeySpec);
		// 对数据解密
		Cipher cipher = Cipher.getInstance(factory.getAlgorithm());
		cipher.init(Cipher.DECRYPT_MODE, publicKey);
		return cipher.doFinal(data);
	}
	
	/**
	 * 加密<br>
	 * 用公钥加密
	 * @param data
	 * @param key
	 * @return
	 * @throws Exception
	 */
	public static byte[] encryptByPublicKey(byte[] data, String key) throws Exception {
		// 对公钥解密
		byte[] keyBytes = decryptBASE64(key);
		// 取得公钥
		X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(keyBytes);
		KeyFactory factory = KeyFactory.getInstance(KEY_ALGORITHM);
		Key publicKey = factory.generatePublic(x509EncodedKeySpec);
		// 对数据加密
		Cipher cipher = Cipher.getInstance(factory.getAlgorithm());
		cipher.init(Cipher.ENCRYPT_MODE, publicKey);
		return cipher.doFinal(data);
	}
	
	/**
	 * 加密<br>
	 * 用私钥加密
	 * @param data
	 * @param key
	 * @return
	 * @throws Exception
	 */
	public static byte[] encryptByPrivateKey(byte[] data, String key) throws Exception {
		// 对私钥解密
		byte[] keyBytes = decryptBASE64(key);
		// 取得私钥
		PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(keyBytes);
		KeyFactory factory = KeyFactory.getInstance(KEY_ALGORITHM);
		Key privateKey = factory.generatePrivate(pkcs8EncodedKeySpec);
		// 对数据加密
		Cipher cipher = Cipher.getInstance(factory.getAlgorithm());
		cipher.init(Cipher.ENCRYPT_MODE, privateKey);
		return cipher.doFinal(data);
	}
	
	/**
	 * 获取私钥
	 * @param keyMap
	 * @return
	 */
	public static String getPrivateKey(Map<String, Object> keyMap) {
		Key key = (Key)keyMap.get(PRIVATE_KEY);
		return encryptBASE64(key.getEncoded());
	}
	
	/**
	 * 获取公钥
	 * @param keyMap
	 * @return
	 */
	public static String getPublicKey(Map<String, Object> keyMap) {
		Key key = (Key)keyMap.get(PUBLIC_KEY);
		return encryptBASE64(key.getEncoded());
	}
	
	public static Map<String, Object> initKey() throws Exception {
		KeyPairGenerator generator = KeyPairGenerator.getInstance(KEY_ALGORITHM);
		generator.initialize(1024);
		KeyPair keyPair = generator.generateKeyPair();
		RSAPublicKey publicKey = (RSAPublicKey)keyPair.getPublic();
		RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
		Map<String, Object> keyMap = new HashMap<String, Object>(2);
		keyMap.put(PUBLIC_KEY, publicKey);
		keyMap.put(PRIVATE_KEY, privateKey);
		return keyMap;
	}
}
