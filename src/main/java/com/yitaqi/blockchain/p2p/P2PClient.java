/**
 * 
 */
package com.yitaqi.blockchain.p2p;

import java.net.URI;
import java.net.URISyntaxException;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

/**
 * p2p客户端
 * @author xue
 *
 */
public class P2PClient {

	private P2PService p2pService;

	public P2PClient(P2PService p2pService) {
		super();
		this.p2pService = p2pService;
	}
	
	public void connect2Peer(String peer) {
		try {
			final WebSocketClient socketClient = new WebSocketClient(new URI(peer)) {

				@Override
				public void onClose(int arg0, String arg1, boolean arg2) {
					System.out.println("connection closed");
					p2pService.getSockets().remove(this);
				}

				@Override
				public void onError(Exception arg0) {
					System.err.println("connection failed");
					System.err.println(arg0.getMessage());
					p2pService.getSockets().remove(this);
				}

				@Override
				public void onMessage(String arg0) {
					p2pService.handleMessage(this, arg0);
				}

				@Override
				public void onOpen(ServerHandshake arg0) {
					p2pService.write(this, p2pService.queryLatestBlockMsg());
					p2pService.write(this, p2pService.queryTransactionMsg());
					p2pService.write(this, p2pService.queryPackedTransactionMsg());
					p2pService.write(this, p2pService.queryWalletMsg());
					p2pService.getSockets().add(this);
				}};
			socketClient.connect();
		} catch (URISyntaxException e) {
			System.err.println("p2p connection is errro:" + e.getMessage());
		}
	}
}
