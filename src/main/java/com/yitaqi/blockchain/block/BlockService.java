package com.yitaqi.blockchain.block;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.alibaba.fastjson.JSON;
import com.yitaqi.blockchain.model.Block;
import com.yitaqi.blockchain.model.Transaction;
import com.yitaqi.blockchain.model.TransactionInput;
import com.yitaqi.blockchain.model.TransactionOutput;
import com.yitaqi.blockchain.model.Wallet;
import com.yitaqi.blockchain.security.CryptoUtil;

/**
 * 区块链核心服务
 * @author xue
 *
 */
public class BlockService {

	/**
	 * 区块链存储结构
	 */
	private List<Block> blockChain = new ArrayList<Block>();
	/**
	 * 当前节点钱包集合
	 */
	private Map<String, Wallet> myWalletMap = new HashMap<String, Wallet>();
	/**
	 * 其他节点钱包集合，只包含公钥
	 */
	private Map<String, Wallet> otherWalletMap = new HashMap<String, Wallet>();
	/**
	 * 转账交易集合
	 */
	private List<Transaction> allTransactions = new ArrayList<Transaction>();
	/**
	 * 已打包转账交易
	 */
	private List<Transaction> packedTransactions = new ArrayList<Transaction>();
	
	public BlockService() {
		// 新建创始区块
		Block genesisBlock = new Block(1, "1", "1", System.currentTimeMillis(), 0, new ArrayList<Transaction>());
		blockChain.add(genesisBlock);
		System.out.println("生成创始区块 ：" + JSON.toJSONString(genesisBlock));
	}
	
	/**
	 * 获取最新的区块，即当前链上最后一个区块
	 * @return
	 */
	public Block getLastBlock() {
		return blockChain.size() > 0 ? blockChain.get(blockChain.size() - 1) : null;
	}
	
	/**
	 * 添加新区块
	 * @param newBlock
	 * @return
	 */
	public boolean addBlock(Block newBlock) {
		if (isValidNewBlock(getLastBlock(), newBlock)) {
			blockChain.add(newBlock);
			// 新区块的交易需要加入已打包的交易集合里去
			packedTransactions.addAll(newBlock.getTransactions());
			return true;
		}
		return false;
	}
	
	/**
	 * 验证新区块是否有效
	 * @param preBlock
	 * @param newBlock
	 * @return
	 */
	private boolean isValidNewBlock(Block preBlock, Block newBlock) {
		if (newBlock.getHash() == null) {
			return false;
		}
		if (!preBlock.getHash().equals(newBlock.getPrehash())) {
			System.err.println("新区块的前一区块hash验证不通过");
			return false;
		}
		if (!isValidHash(newBlock.getHash())) {
			return false;
		}
		String hash = calculateHash(newBlock.getPrehash(), newBlock.getTransactions(), newBlock.getNonce());
		if (!newBlock.getHash().equals(hash)) {
			System.err.println("新区块的hash无效：" + hash + "	" + newBlock.getHash());
			return false;
		}
		return true;
	}
	
	/**
	 * 验证hash是否满足系统条件
	 * @param hash
	 * @return
	 */
	private boolean isValidHash(String hash) {
		return hash.startsWith("00000");
	}
	
	/**
	 * 替换本地区块链
	 * @param chain
	 * @return
	 */
	public boolean replaceChain(List<Block> chain) {
		if (isValidChain(chain) && chain.size() > blockChain.size()) {
			blockChain = chain;
			packedTransactions.clear();
			blockChain.forEach(block -> {
				packedTransactions.addAll(block.getTransactions());
			});
			return true;
		}
		System.err.println("接收的区块链无效");
		return false;
	}
	
	/**
	 * 验证整个区块链是否有效
	 * @param chain
	 * @return
	 */
	private boolean isValidChain (List<Block> chain) {
		if (chain == null || chain.size() == 0) return false;
		Block preBlock = chain.get(0);
		int index = 1;
		Block block = null;
		while (index < chain.size()) {
			block = chain.get(index);
			if (!isValidNewBlock(preBlock, block)) {
				return false;
			}
			preBlock = block;
			index++;
		}
		return true;
	}
	
	/**
	 * 生成区块的奖励交易
	 * @param address
	 * @return
	 */
	public Transaction newCoinBaseTx(String address) {
		
		Wallet wallet = myWalletMap.get(address);
		TransactionInput inTx = new TransactionInput("0", -1, null, null);
		// 指定生成区块的奖励为10
		TransactionOutput outTx = new TransactionOutput(10, wallet.getPublicKey());
		return new Transaction(CryptoUtil.UUID(), inTx, outTx);
	}
	
	/**
	 * 挖矿
	 * @param address
	 * @return
	 */
	public Block mine(String address) {
		
		// 创建系统奖励的交易
		allTransactions.add(newCoinBaseTx(address));
		// 去除已打包进区块交易的区块
		ArrayList<Transaction> transactions = new ArrayList<Transaction>(allTransactions);
		transactions.removeAll(packedTransactions);
		verifyAllTransaction(transactions);
		
		String newBlockHash = "";
		int nonce = 0;
		long start = System.currentTimeMillis();
		System.out.println("开始挖矿 ...");
		while(true) {
			// 计算新区块hash
			newBlockHash = calculateHash(getLastBlock().getHash(), transactions, nonce);
			if (isValidHash(newBlockHash)) {
				System.out.println("挖矿完成，正确的hash ：" + newBlockHash);
				System.out.println("挖矿耗费时间：" + (System.currentTimeMillis() - start));
				break;
			}
			nonce++;
		}
		// 创建新区块
		Block block = createNewBlock(nonce, getLastBlock().getHash(), newBlockHash, transactions);
		return block;
	}
	
	/**
	 * 验证所有交易是否有效，非常重要的一步，可以防止双花（好像不可以防止双花）
	 * @param transactions
	 */
	private void verifyAllTransaction(List<Transaction> transactions) {
		
		List<Transaction> invalidTransations = new ArrayList<Transaction>();
		for (Transaction transaction : transactions) {
			if (!verifyTransaction(transaction)) {
				invalidTransations.add(transaction);
			}
		}
		transactions.removeAll(invalidTransations);
		allTransactions.removeAll(invalidTransations);
	}
	
	private boolean verifyTransaction(Transaction transaction) {
		if (transaction == null) return false;
		if (transaction.coinbaseTx()) {
			return true;
		}
		if (transaction.getId() == null || "".equals(transaction.getId())) {
			return false;
		}
		Transaction preTx = findTransaction(transaction.getTxIn().getTxId());
		return transaction.verify(preTx);
	}
	
	private Transaction findTransaction(String id) {
		for (Block block : blockChain) {
			for (Transaction transaction : block.getTransactions()) {
				if (transaction.getId().equals(id)) {
					return transaction;
				}
			}
		}
		return null;
	}
	
	/**
	 * 计算区块的hash
	 * @param preHash
	 * @param transactions
	 * @param nonce
	 * @return
	 */
	private String calculateHash(String preHash, List<Transaction> transactions, int nonce) {
		return CryptoUtil.SHA256(preHash + JSON.toJSONString(transactions) + nonce);
	}
	
	private Block createNewBlock(int nonce, String preHash, String hash, List<Transaction> transactions) {
		
		Block block = new Block(blockChain.size() + 1, hash, preHash, System.currentTimeMillis(), nonce, transactions);
		if(addBlock(block)) {
			return block;
		}
		return null;
	}
	
	/**
	 * 创建钱包
	 * @param senderWallet
	 * @param recipientWallet
	 * @param amont
	 * @return
	 */
	public Transaction createTransaction(Wallet senderWallet, Wallet recipientWallet, int amont) {
		
		List<Transaction> unspentTxs = findUnspentTransactions(senderWallet.getAddress());
		Transaction preTx = null;
		for (Transaction transaction : unspentTxs) {
			// TODO 找零
			if (transaction.getTxOut().getValue() == amont) {
				preTx = transaction;
				break;
			}
		}
		if (preTx == null) return null;
		TransactionInput txIn = new TransactionInput(preTx.getId(), amont, null, senderWallet.getPublicKey());
		TransactionOutput txOut = new TransactionOutput(amont, recipientWallet.getPublicKey());
		Transaction tx = new Transaction(CryptoUtil.UUID(), txIn, txOut);
		tx.sign(senderWallet.getPrivateKey(), preTx);
		allTransactions.add(tx);
		return tx;
	}
	
	/**
	 * 查找未被消费的交易记录（UTXO）
	 * @param address
	 * @return
	 */
	private List<Transaction> findUnspentTransactions(String address) {
		
		if (address == null) return null;
		List<Transaction> unSpentTransactions = new ArrayList<Transaction>();
		Set<String> spentTransactions = new HashSet<String>();
		for (Transaction transaction : allTransactions) {
			if (transaction.coinbaseTx()) {
				continue;
			}
			if (address.equals(Wallet.getAddress(transaction.getTxIn().getPublicKey()))) {
				spentTransactions.add(transaction.getTxIn().getTxId());
			}
		}
		for (Block block : blockChain) {
			List<Transaction> transactions = block.getTransactions();
			for (Transaction transaction : transactions) {
				if (address.equals(Wallet.getAddress(transaction.getTxOut().getPublicKey()))) {
					if (!spentTransactions.contains(transaction.getId())) {
						unSpentTransactions.add(transaction);
					}
				}
			}
		}
//		blockChain.forEach(block -> {
//			List<Transaction> transactions = block.getTransactions();
//			for (Transaction transaction : transactions) {
//				if (address.equals(transaction.getTxOut().getPublicKey())) {
//					if (!spentTransactions.contains(transaction.getId())) {
//						unSpentTransactions.add(transaction);
//					}
//				}
//			}
//		});
		return unSpentTransactions;
	}
	
	/**
	 * 创建钱包
	 * @return
	 */
	public Wallet createWallet() {
		Wallet wallet = Wallet.generateWallet();
		myWalletMap.put(wallet.getAddress(), wallet);
		return wallet;
	}
	
	/**
	 * 获取钱包余额
	 * @param address
	 * @return
	 */
	public int getWalletBalance(String address) {
		List<Transaction> unspentTransactions = findUnspentTransactions(address);
		if (unspentTransactions == null) return 0;
		int balance = 0;
		for (Transaction transaction : unspentTransactions) {
			balance += transaction.getTxOut().getValue();
		}
		return balance;
	}
	
	public List<Block> getBlockChain() {
		return blockChain;
	}
	public void setBlockChain(List<Block> blockChain) {
		this.blockChain = blockChain;
	}
	public Map<String, Wallet> getMyWalletMap() {
		return myWalletMap;
	}
	public void setMyWalletMap(Map<String, Wallet> myWalletMap) {
		this.myWalletMap = myWalletMap;
	}
	public Map<String, Wallet> getOtherWalletMap() {
		return otherWalletMap;
	}
	public void setOtherWalletMap(Map<String, Wallet> otherWalletMap) {
		this.otherWalletMap = otherWalletMap;
	}
	public List<Transaction> getAllTransactions() {
		return allTransactions;
	}
	public void setAllTransactions(List<Transaction> allTransactions) {
		this.allTransactions = allTransactions;
	}
	public List<Transaction> getPackedTransactions() {
		return packedTransactions;
	}
	public void setPackedTransactions(List<Transaction> packedTransactions) {
		this.packedTransactions = packedTransactions;
	}
	
}
