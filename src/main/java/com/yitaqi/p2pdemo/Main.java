/**
 * 
 */
package com.yitaqi.p2pdemo;

/**
 * websocket 启动入口
 * @author xue
 *
 */
public class Main {

	public static void main(String[] args) {
		P2PServer server = new P2PServer();
		P2PClient client = new P2PClient();
		int p2pPort = Integer.parseInt(args[0]);
		// 启动P2P服务端
		server.initP2PServer(p2pPort);
		if (args.length > 1 && args[1] != null) {
			// 作为p2p客户端连接p2p服务端
			client.connect2Peer(args[1]);
		}
	}
}
