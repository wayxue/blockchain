package com.yitaqi.blockchain.block;

import java.util.ArrayList;

import org.junit.Test;

import com.alibaba.fastjson.JSON;
import com.yitaqi.blockchain.model.Block;
import com.yitaqi.blockchain.model.Transaction;
import com.yitaqi.blockchain.model.TransactionInput;
import com.yitaqi.blockchain.model.TransactionOutput;
import com.yitaqi.blockchain.model.Wallet;
import com.yitaqi.blockchain.security.CryptoUtil;

public class BlockServiceTest {

	@Test
	public void test() {
		
		// 创建空区块链
		ArrayList<Block> blockChain = new ArrayList<Block>();
		// 创建初始区块
		Block begin = new Block(0, "1", "0", System.currentTimeMillis(), 0, new ArrayList<Transaction>());
		blockChain.add(begin);
		System.out.println(JSON.toJSONString(blockChain));
		
		// 生成两个钱包
		Wallet walletSender = Wallet.generateWallet();
		Wallet walletReciptent = Wallet.generateWallet();
		
		Transaction coinbase = coinbase(walletSender.getPublicKey());
		// 转账交易
		TransactionInput inTx = new TransactionInput(coinbase.getId(), 5, null, walletSender.getPublicKey());
		TransactionOutput outTx = new TransactionOutput(5, walletReciptent.getPublicKey());
		Transaction transaction = new Transaction(CryptoUtil.UUID(), inTx, outTx);
		transaction.sign(walletSender.getPrivateKey(), coinbase);
		
		// 创建系统奖励的交易
//		Transaction ts1 = new Transaction();
//		Transaction ts2 = new Transaction();
//		Transaction ts3 = new Transaction();
		ArrayList<Transaction> list = new ArrayList<Transaction>();
//		list.add(ts1);
//		list.add(ts2);
//		list.add(ts3);
		
		list.add(coinbase);
		list.add(transaction);
		// 获取当前区块链最后一个区块
		Block end = blockChain.get(blockChain.size() -1);
		
		int nonce = 1;
		String hash = "";
		while(true) {
			// 计算新区块的hash值   SHA256-- 最后一个区块的hash + 交易记录信息 + 随机数
			hash = CryptoUtil.SHA256(end.getHash() + JSON.toJSONString(list) + nonce);
			if (hash.startsWith("0123")) {
				System.out.println("======  计算结果正确，计算次数为： " + nonce + "  hash：" + hash);
				break;
			}
			nonce++;
			System.out.println("计算错误： hsah:" + hash);
		}
		
		// 创建新区块，打包进区块链
		Block newBlock = new Block(end.getIndex() + 1, hash, end.getHash(), System.currentTimeMillis(), nonce, list);
		blockChain.add(newBlock);
		System.out.println(JSON.toJSONString(blockChain));
	}
	
	public Transaction coinbase(String publicKey) {
		TransactionInput inTx = new TransactionInput("0", -1, null, null);
		TransactionOutput outTx = new TransactionOutput(10, publicKey);
		return new Transaction(CryptoUtil.UUID(), inTx, outTx);
	}
	
	@Test
	public void testGenerateWallet() throws Exception {
		
		Wallet wallet = Wallet.generateWallet();
		System.out.println(JSON.toJSON(wallet));
		System.out.println(wallet.getAddress().length());
	}
}
