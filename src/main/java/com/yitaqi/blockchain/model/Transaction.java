package com.yitaqi.blockchain.model;

import com.alibaba.fastjson.JSON;
import com.yitaqi.blockchain.security.CryptoUtil;
import com.yitaqi.blockchain.security.RSACoder;

/**
 * 交易
 * @author xue
 *
 */
public class Transaction {

	/**
	 * 交易唯一标识
	 */
	private String id;
	/**
	 * 交易输入
	 */
	private TransactionInput txIn;
	/**
	 * 交易输出
	 */
	private TransactionOutput txOut;
	public Transaction() {
		super();
		// TODO Auto-generated constructor stub
	}
	public Transaction(String id, TransactionInput txIn, TransactionOutput txOut) {
		super();
		this.id = id;
		this.txIn = txIn;
		this.txOut = txOut;
	}
	
	/**
	 * 用私钥生成签名
	 * @param privateKey
	 * @param preTx
	 */
	public void sign(String privateKey, Transaction preTx) {
		
		if (coinbaseTx()) return;
		if (!txIn.getTxId().equals(preTx.getId())) {
			System.err.println("交易签名失败：当前交易输入引用的前一笔交易与引入的前一笔交易不匹配");
			return ;
		}
		Transaction tx = cloneTx();
		tx.getTxIn().setPublicKey(preTx.getTxOut().getPublicKey());
		try {
			String sign = RSACoder.sign(tx.hash().getBytes(), privateKey);
			txIn.setSignature(sign);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * 验证交易签名
	 * @param preTx
	 * @return
	 */
	public boolean verify(Transaction preTx) {
		
		if (coinbaseTx()) return true;
		if (!txIn.getTxId().equals(preTx.getId())) {
			System.err.println("验证交易签名失败：当前交易输入引用的前一笔交易与传入的前一笔交易不匹配");
			return false;
		}
		Transaction tx = cloneTx();
		tx.getTxIn().setPublicKey(preTx.getTxOut().getPublicKey());
		try {
			return RSACoder.verify(tx.hash().getBytes(), txIn.getPublicKey(), txIn.getSignature());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * 是否是系统生成区块的奖励交易
	 * @return
	 */
	public boolean coinbaseTx() {
		return txIn.getTxId().equals("0") && txIn.getValue() == -1;
	}
	
	/**
	 * 生成用于交易签名的交易记录副本
	 * @return
	 */
	public Transaction cloneTx() {
		TransactionInput transactionInput = new TransactionInput(txIn.getTxId(), txIn.getValue(), null, null);
		TransactionOutput transactionOutput = new TransactionOutput(txOut.getValue(), txOut.getPublicKey());
		return new Transaction(id, transactionInput, transactionOutput);
	}
	
	/**
	 * 生成交易的hash
	 * @return
	 */
	public String hash() {
		return CryptoUtil.SHA256(JSON.toJSONString(this));
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = result * prime + (id == null ? 0 : id.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (obj.getClass() != getClass()) return false;
		Transaction tx = (Transaction) obj;
		if (id == null) {
			if (tx.id != null) return false;
		} else if (!id.equals(tx.id)) {
			return false;
		}
		return true;
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public TransactionInput getTxIn() {
		return txIn;
	}
	public void setTxIn(TransactionInput txIn) {
		this.txIn = txIn;
	}
	public TransactionOutput getTxOut() {
		return txOut;
	}
	public void setTxOut(TransactionOutput txOut) {
		this.txOut = txOut;
	}
	
}
