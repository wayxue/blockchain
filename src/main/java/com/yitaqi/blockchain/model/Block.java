package com.yitaqi.blockchain.model;

import java.util.List;

public class Block {

	private int index;
	private String hash;
	private String prehash;
	private long timestamp;
	private int nonce;
	private List<Transaction> transactions;
	
	public Block() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public Block(int index, String hash, String prehash, long timestamp,
			int nonce, List<Transaction> transactions) {
		super();
		this.index = index;
		this.hash = hash;
		this.prehash = prehash;
		this.timestamp = timestamp;
		this.nonce = nonce;
		this.transactions = transactions;
	}
	public int getIndex() {
		return index;
	}
	public void setIndex(int index) {
		this.index = index;
	}
	public String getHash() {
		return hash;
	}
	public void setHash(String hash) {
		this.hash = hash;
	}
	public String getPrehash() {
		return prehash;
	}
	public void setPrehash(String prehash) {
		this.prehash = prehash;
	}
	public long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	public int getNonce() {
		return nonce;
	}
	public void setNonce(int nonce) {
		this.nonce = nonce;
	}
	public List<Transaction> getTransactions() {
		return transactions;
	}
	public void setTransactions(List<Transaction> transactions) {
		this.transactions = transactions;
	}
}
