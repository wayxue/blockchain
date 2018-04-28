/**
 * 
 */
package com.yitaqi.blockchain;

import com.yitaqi.blockchain.block.BlockService;
import com.yitaqi.blockchain.http.HttpServer;
import com.yitaqi.blockchain.p2p.P2PClient;
import com.yitaqi.blockchain.p2p.P2PServer;
import com.yitaqi.blockchain.p2p.P2PService;

/**
 * 区块链节点启动入口
 * @author xue
 *
 */
public class Main {

	public static void main(String[] args) {
		
		if (args != null && args.length != 0) {
			try {
				BlockService blockService = new BlockService();
				P2PService p2pService = new P2PService(blockService);
				startP2PServer(args, p2pService);
				HttpServer server = new HttpServer(blockService, p2pService);
				int port = Integer.parseInt(args[0]);
				server.initHttpServer(port);
			} catch (Exception e) {
				System.err.println("startup was failed : " + e.getMessage());
			}
		} else {
			System.err.println("usage : java -jar blockChain.jar 8081 7001");
		}
	}
	
	/**
	 * 启动p2p服务
	 * @param args
	 * @param p2pService
	 */
	private static void startP2PServer(String args[], P2PService p2pService) {
		P2PServer server = new P2PServer(p2pService);
		P2PClient client = new P2PClient(p2pService);
		int port = Integer.parseInt(args[1]);
		// 启动p2p服务端
		server.initP2PServer(port);
		if (args.length == 3 && args[2] != null) {
			// 作为p2p客户端连接p2p服务端
			client.connect2Peer(args[2]);
		}
	}
}
