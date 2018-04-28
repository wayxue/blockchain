package com.yitaqi.blockchain.model;

/**
 * 交易输出
 * @author xue
 *
 */
public class TransactionOutput {

	/**
	 * 交易金额
	 */
	private int value;
	/**
	 * 接收方钱包公钥
	 */
	private String publicKey;
	public TransactionOutput() {
		super();
		// TODO Auto-generated constructor stub
	}
	public TransactionOutput(int value, String publicKey) {
		super();
		this.value = value;
		this.publicKey = publicKey;
	}
	public int getValue() {
		return value;
	}
	public void setValue(int value) {
		this.value = value;
	}
	public String getPublicKey() {
		return publicKey;
	}
	public void setPublicKey(String publicKey) {
		this.publicKey = publicKey;
	}
	
}
