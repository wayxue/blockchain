/**
 * 
 */
package com.yitaqi.p2pdemo;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

/**
 * p2p客户端
 * @author xue
 *
 */
public class P2PClient {

	private List<WebSocket> sockets = new ArrayList<>();
	
	public List<WebSocket> getSockets() {
		return sockets;
	}
	
	public void connect2Peer(String peer) {
		
		try {
			final WebSocketClient socketClient = new WebSocketClient(new URI(peer)) {

				@Override
				public void onClose(int arg0, String arg1, boolean arg2) {
					System.out.println("connection closed");
					sockets.remove(this);
				}

				@Override
				public void onError(Exception arg0) {
					System.out.println("connection failed");
					System.err.println(arg0);
					sockets.remove(this);
				}

				@Override
				public void onMessage(String arg0) {
					System.out.println("收到服务端发送的消息：" + arg0);
				}

				@Override
				public void onOpen(ServerHandshake arg0) {
					write(this, "客户端连接成功");
					sockets.add(this);
				}
				
			};
			socketClient.connect();
		} catch (Exception e) {
			System.err.println("p2p connection is error : " + e.getMessage());
		}
	}
	
	public void write(WebSocket ws, String message) {
		
		System.out.println("发送给" + ws.getRemoteSocketAddress().getPort() + "的p2p消息：" + message);
		ws.send(message);
	}
	
	public void broatcast(String message) {
		
		System.out.println("====广播消息开始====");
		if (sockets.size() != 0) {
			for (WebSocket webSocket : sockets) {
				this.write(webSocket, message);
			}
		}
		System.out.println("====广播消息结束====");
	}
}
