/**
 * 
 */
package com.yitaqi.blockchain.p2p;

import java.net.InetSocketAddress;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

/**
 * p2p服务端
 * @author xue
 *
 */
public class P2PServer {

	private P2PService p2pService;

	public P2PServer(P2PService p2pService) {
		super();
		this.p2pService = p2pService;
	}
	
	public void initP2PServer(int port) {
		final WebSocketServer socketServer = new WebSocketServer(new InetSocketAddress(port)){

			@Override
			public void onClose(WebSocket arg0, int arg1, String arg2,
					boolean arg3) {
				System.out.println("connection closed to peer: " + arg0.getRemoteSocketAddress());
				p2pService.getSockets().remove(arg0);
			}

			@Override
			public void onError(WebSocket arg0, Exception arg1) {
				System.err.println("connection failed to peer: " + arg0.getRemoteSocketAddress());
				System.err.println(arg1.getMessage());
				p2pService.getSockets().remove(arg0);
			}

			@Override
			public void onMessage(WebSocket arg0, String arg1) {
				p2pService.handleMessage(arg0, arg1);
			}

			@Override
			public void onOpen(WebSocket arg0, ClientHandshake arg1) {
				p2pService.getSockets().add(arg0);
			}

			@Override
			public void onStart() {
				System.out.println("webSocketServer start succed on port:" + port);
			}};
		socketServer.start();
		System.out.println("listening webSocket p2p port on: " + port);
	}
}
