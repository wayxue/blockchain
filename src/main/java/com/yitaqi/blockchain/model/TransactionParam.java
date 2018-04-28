/**
 * 
 */
package com.yitaqi.blockchain.model;

/**
 * 交易接口参数
 * @author xue
 *
 */
public class TransactionParam {

	/**
	 * 发送方钱包地址
	 */
	private String sender;
	/**
	 * 接受方钱包地址
	 */
	private String recipient;
	/**
	 * 发送金额
	 */
	private int amont;
	public String getSender() {
		return sender;
	}
	public void setSender(String sender) {
		this.sender = sender;
	}
	public String getRecipient() {
		return recipient;
	}
	public void setRecipient(String recipient) {
		this.recipient = recipient;
	}
	public int getAmont() {
		return amont;
	}
	public void setAmont(int amont) {
		this.amont = amont;
	}
	
}
