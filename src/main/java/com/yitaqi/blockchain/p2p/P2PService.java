/**
 * 
 */
package com.yitaqi.blockchain.p2p;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.java_websocket.WebSocket;

import com.alibaba.fastjson.JSON;
import com.yitaqi.blockchain.block.BlockService;
import com.yitaqi.blockchain.model.Block;
import com.yitaqi.blockchain.model.Transaction;
import com.yitaqi.blockchain.model.Wallet;

/**
 * p2p公用服务类
 * @author xue
 *
 */
public class P2PService {

	private List<WebSocket> sockets;
	private BlockService blockService;
	
	// 查询最新的区块
	public final static int QUERY_LATEST_BLOCK = 0;
	// 查询整个区块链
	public final static int QUERY_BLOCKCHAIN = 1;
	// 查询交易集合
	public final static int QUERY_TRANSACTION = 2;
	// 查询已打包交易集合
	public final static int QUERY_PACKED_TRANSACTION = 3;
	// 查询钱包集合
	public final static int QUERY_WALLET = 4;
	// 返回区块集合
	public final static int RESPONSE_BLOCKCHAIN = 5;
	// 返回交易集合
	public final static int RESPONSE_TRANSACTION = 6;
	// 返回已打包集合
	public final static int RESPONSE_PACKED_TRANSACTION = 7;
	// 返回钱包集合
	public final static int RESPONSE_WALLET = 8;
	
	public P2PService(BlockService blockService) {
		this.blockService = blockService;
		this.sockets = new ArrayList<WebSocket>();
	}
	
	public List<WebSocket> getSockets() {
		return sockets;
	}
	
	public void handleMessage(WebSocket webSocket, String message) {
		try {
			System.out.println("接收到" + webSocket.getRemoteSocketAddress().getPort() + "的p2p消息：" + message);
			Message msg = JSON.parseObject(message, Message.class);
			switch (msg.getType()) {
				case QUERY_LATEST_BLOCK:
					write(webSocket, responseLatestBlockMsg());
					break;
				case QUERY_BLOCKCHAIN:
					write(webSocket, responseBlockChainMsg());
					break;
				case QUERY_TRANSACTION:
					write(webSocket, responseTransactions());
					break;
				case QUERY_PACKED_TRANSACTION:
					write(webSocket, responsePackedTransactions());
					break;
				case QUERY_WALLET:
					write(webSocket, responseWallets());
					break;
				case RESPONSE_BLOCKCHAIN:
					handleBlockChainResponse(msg.getData());
					break;
				case RESPONSE_TRANSACTION:
					handleTransactionResponse(msg.getData());
					break;
				case RESPONSE_PACKED_TRANSACTION:
					handlePackedTransactionResponse(msg.getData());
					break;
				case RESPONSE_WALLET:
					handleWalletResponse(msg.getData());
					break;
			}
		} catch (Exception e) {
			System.err.println("处理p2p消息错误：" + e.getMessage());
		}
	}
	
	private synchronized void handleBlockChainResponse(String message) {
		List<Block> receiveBlockChain = JSON.parseArray(message, Block.class);
		Collections.sort(receiveBlockChain, new Comparator<Block>(){

			@Override
			public int compare(Block o1, Block o2) {
				return o1.getIndex() - o2.getIndex();
			}});
		Block lastestBlockReceived = receiveBlockChain.get(receiveBlockChain.size() - 1);
		Block lastBlock = blockService.getLastBlock();
		if (lastestBlockReceived.getIndex() > lastBlock.getIndex()) {
			if (lastBlock.getHash().equals(lastestBlockReceived.getPrehash())) {
				System.out.println("将新接收到的区块加入到本地的区块链");
				if (blockService.addBlock(lastestBlockReceived)) {
					broatcast(responseLatestBlockMsg());
				}
			} else if (receiveBlockChain.size() == 1) {
				System.out.println("查询所有通讯节点上的区块链");
				broatcast(queryBlockChainMsg());
			} else {
				blockService.replaceChain(receiveBlockChain);
			}
		} else {
			System.out.println("接收到的区块链不比本地区块链长，不处理");
		}
	}
	
	private void handleTransactionResponse(String message) {
		List<Transaction> txs = JSON.parseArray(message, Transaction.class);
		blockService.getAllTransactions().addAll(txs);
	}
	
	private void handlePackedTransactionResponse(String message) {
		List<Transaction> txs = JSON.parseArray(message, Transaction.class);
		blockService.getPackedTransactions().addAll(txs);
	}
	
	private void handleWalletResponse(String message) {
		List<Wallet> wallets = JSON.parseArray(message, Wallet.class);
		wallets.forEach(wallet -> {
			blockService.getOtherWalletMap().put(wallet.getAddress(), wallet);
		});
	}
	
	private String queryBlockChainMsg() {
		return JSON.toJSONString(new Message(QUERY_BLOCKCHAIN));
	}
	
	public String queryLatestBlockMsg() {
		return JSON.toJSONString(new Message(QUERY_LATEST_BLOCK));
	}
	
	public String queryTransactionMsg() {
		return JSON.toJSONString(new Message(QUERY_TRANSACTION));
	}
	
	public String queryPackedTransactionMsg() {
		return JSON.toJSONString(new Message(QUERY_PACKED_TRANSACTION));
	}
	
	public String queryWalletMsg() {
		return JSON.toJSONString(new Message(QUERY_WALLET));
	}
	
	private String responseLatestBlockMsg() {
		Block[] blocks = {blockService.getLastBlock()};
		return JSON.toJSONString(new Message(RESPONSE_BLOCKCHAIN, JSON.toJSONString(blocks)));
	}
	
	private String responseBlockChainMsg() {
		return JSON.toJSONString(new Message(RESPONSE_BLOCKCHAIN, JSON.toJSONString(blockService.getBlockChain())));
	}
	
	private String responseTransactions() {
		return JSON.toJSONString(new Message(RESPONSE_TRANSACTION, JSON.toJSONString(blockService.getAllTransactions())));
	}
	
	private String responsePackedTransactions() {
 		return JSON.toJSONString(new Message(RESPONSE_PACKED_TRANSACTION, JSON.toJSONString(blockService.getPackedTransactions())));
	}
	
	private String responseWallets() {
		List<Wallet> wallets = new ArrayList<Wallet>();
		blockService.getMyWalletMap().forEach((address, wallet) -> {
			wallets.add(new Wallet(wallet.getPublicKey()));
		});
		blockService.getOtherWalletMap().forEach((address, wallet) -> {
			wallets.add(wallet);
		});
		return JSON.toJSONString(new Message(RESPONSE_WALLET, JSON.toJSONString(wallets)));
	}
	
	public void write(WebSocket webSocket, String message) {
		System.out.println("发送给" + webSocket.getRemoteSocketAddress().getPort() + "的p2p消息：" + message);
		webSocket.send(message);
	}
	
	public void broatcast(String message) {
		System.out.println("====广播消息开始====");
		if (sockets.size() > 0) {
			for (WebSocket webSocket : sockets) {
				this.write(webSocket, message);
			}
		}
		System.out.println("====广播消息结束====");
	}
}
