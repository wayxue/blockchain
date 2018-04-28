package com.yitaqi.blockchain.model;

import java.util.Map;

import com.yitaqi.blockchain.security.CryptoUtil;
import com.yitaqi.blockchain.security.RSACoder;

public class Wallet {

	/**
	 * 公钥
	 */
	private String publicKey;
	/**
	 * 私钥
	 */
	private String privateKey;
	public String getPublicKey() {
		return publicKey;
	}
	public void setPublicKey(String publicKey) {
		this.publicKey = publicKey;
	}
	public String getPrivateKey() {
		return privateKey;
	}
	public void setPrivateKey(String privateKey) {
		this.privateKey = privateKey;
	}
	public Wallet() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public Wallet(String publicKey) {
		super();
		this.publicKey = publicKey;
	}
	public Wallet(String publicKey, String privateKey) {
		super();
		this.publicKey = publicKey;
		this.privateKey = privateKey;
	}
	
	/**
	 * 获取钱包地址
	 * @return
	 */
	public String getAddress() {
		String hashPubKey = hashPubKey(publicKey);
		return CryptoUtil.MD5(hashPubKey);
	}
	
	/**
	 * 根据公钥，获取钱包地址
	 * @param publicKey
	 * @return
	 */
	public static String getAddress(String publicKey) {
		String hashPubKey = hashPubKey(publicKey);
		return CryptoUtil.MD5(hashPubKey);
	}
	
	/**
	 * 生成公钥hash
	 * @return
	 */
	public String getHashPubKey() {
		return CryptoUtil.SHA256(publicKey);
	}
	
	/**
	 * 生成钱包公钥
	 * @param publickey
	 * @return
	 */
	public static String hashPubKey(String publicKey) {
		return CryptoUtil.SHA256(publicKey);
	}
	
	public static Wallet generateWallet() {
		try {
			Map<String, Object> initKey = RSACoder.initKey();
			String publicKey = RSACoder.getPublicKey(initKey);
			String privateKey = RSACoder.getPrivateKey(initKey);
			return new Wallet(publicKey, privateKey);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
}
