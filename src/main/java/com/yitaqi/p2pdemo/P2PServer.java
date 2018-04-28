/**
 * 
 */
package com.yitaqi.p2pdemo;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

/**
 * p2p服务端
 * @author xue
 *
 */
public class P2PServer {

	private List<WebSocket> sockets = new ArrayList<WebSocket>();
	
	public List<WebSocket> getSockets() {
		return sockets;
	}
	
	public void initP2PServer(int port) {
		final WebSocketServer socketServer = new WebSocketServer(new InetSocketAddress(port)) {

			@Override
			public void onClose(WebSocket arg0, int arg1, String arg2,
					boolean arg3) {
				System.out.println("connection closed to peer: " + arg0.getRemoteSocketAddress());
				sockets.remove(arg0);
			}

			@Override
			public void onError(WebSocket arg0, Exception arg1) {
				System.out.println("connection failed to peer: " + arg0.getRemoteSocketAddress());
				System.err.println(arg1);
				sockets.remove(arg0);
			}

			@Override
			public void onMessage(WebSocket arg0, String arg1) {
				System.out.println("接收到客户端消息：" + arg1);
				write(arg0, "服务器收到消息");
			}

			@Override
			public void onOpen(WebSocket arg0, ClientHandshake arg1) {
				write(arg0, "服务端连接成功");
				sockets.add(arg0);
			}

			@Override
			public void onStart() {
				System.out.println("webSocketServer started success");
			}};
			socketServer.start();
			System.out.println("listening websocket p2p port on: " + port);
	}
	
	public void write(WebSocket webSocket, String message) {
		System.out.println("发送给" + webSocket.getRemoteSocketAddress().getPort() + "的p2p消息：" + message);
		webSocket.send(message);
	}
	
	public void broatcast(String message) {
		System.out.println("====广播消息开始====");
		if (sockets.size() != 0) {
			for (WebSocket webSocket : sockets) {
				this.write(webSocket, message);
			}
		}
		System.out.println("====广播消息开始====");
		
	}
}
