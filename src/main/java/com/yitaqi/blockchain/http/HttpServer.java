package com.yitaqi.blockchain.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;



import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;



import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;


import org.java_websocket.WebSocket;

import com.alibaba.fastjson.JSON;
import com.yitaqi.blockchain.block.BlockService;
import com.yitaqi.blockchain.model.Block;
import com.yitaqi.blockchain.model.Transaction;
import com.yitaqi.blockchain.model.TransactionParam;
import com.yitaqi.blockchain.model.Wallet;
import com.yitaqi.blockchain.p2p.Message;
import com.yitaqi.blockchain.p2p.P2PService;

/**
 * 区块链对外http服务
 * @author xue
 *
 */
public class HttpServer {

	private BlockService blockService;
	private P2PService p2pService;
	public HttpServer(BlockService blockService, P2PService p2pService) {
		super();
		this.blockService = blockService;
		this.p2pService = p2pService;
	}
	
	public void initHttpServer(int port) {
		try {
			Server server = new Server(port);
			System.out.println("listening http port on " + port);
			ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
			context.setContextPath("/");
			server.setHandler(context);
			// 查询区块链
			context.addServlet(new ServletHolder(new ChainServlet()), "/chain");
			// 创建钱包
			context.addServlet(new ServletHolder(new CreateWalletServlet()), "/wallet/create");
			// 查询钱包
			context.addServlet(new ServletHolder(new GetWalletsServlet()), "/wallet/get");
			// 挖矿
			context.addServlet(new ServletHolder(new MineServlet()), "/mine");
			// 转账交易
			context.addServlet(new ServletHolder(new NewTransactionServlet()), "/transaction/new");
			// 查询未打包交易
			context.addServlet(new ServletHolder(new GetUnpackedTransactionServlet()), "/transaction/unpacked");
			// 查询钱包余额
			context.addServlet(new ServletHolder(new GetWalletBalanceServlet()), "/wallet/balance");
			// 查询所有socket节点
			context.addServlet(new ServletHolder(new PeersServlet()), "/socket");
			
			server.start();
			server.join();
		} catch (Exception e) {
			System.err.println("init http server is error ：" + e);
		}
	}
	
	private class ChainServlet extends HttpServlet {
		
		@Override
		protected void doGet(HttpServletRequest req, HttpServletResponse resp)
				throws ServletException, IOException {
			resp.setCharacterEncoding("UTF-8");
			resp.getWriter().print("当前区块链：" + JSON.toJSONString(blockService.getBlockChain()));
		}
	}
	
	private class MineServlet extends HttpServlet {
		
		@Override
		protected void doPost(HttpServletRequest req, HttpServletResponse resp)
				throws ServletException, IOException {
			resp.setCharacterEncoding("utf-8");
			String address = req.getParameter("address");
			Wallet wallet = blockService.getMyWalletMap().get(address);
			if (wallet == null) {
				resp.getWriter().print("挖矿指定的钱包不存在");
				return;
			}
			Block newBlock = blockService.mine(address);
			if (newBlock == null) {
				resp.getWriter().print("挖矿失败，可能其他节点已挖出该区块");
				return;
			}
			Block[] blocks = {newBlock};
			String msg = JSON.toJSONString(new Message(P2PService.RESPONSE_BLOCKCHAIN, JSON.toJSONString(blocks)));
			p2pService.broatcast(msg);
			resp.getWriter().print(JSON.toJSONString(newBlock));
		}
	}
	
	private class CreateWalletServlet extends HttpServlet {
		
		@Override
		protected void doPost(HttpServletRequest req, HttpServletResponse resp)
				throws ServletException, IOException {
			resp.setCharacterEncoding("utf-8");
			Wallet wallet = blockService.createWallet();
			Wallet[] wallets = {new Wallet(wallet.getPublicKey())};
			String message = JSON.toJSONString(new Message(P2PService.RESPONSE_WALLET, JSON.toJSONString(wallets)));
			p2pService.broatcast(message);
			resp.getWriter().print("创建钱包成功，钱包地址：" + wallet.getAddress());
		}
	}
	
	private class GetWalletsServlet extends HttpServlet {

		@Override
		protected void doGet(HttpServletRequest req, HttpServletResponse resp)
				throws ServletException, IOException {
			resp.setCharacterEncoding("utf-8");
			resp.getWriter().print("当前节点钱包：" + JSON.toJSONString(blockService.getMyWalletMap().values()));
		}
	}
	
	private class NewTransactionServlet extends HttpServlet {
		
		@Override
		protected void doPost(HttpServletRequest req, HttpServletResponse resp)
				throws ServletException, IOException {
			resp.setCharacterEncoding("utf-8");
			TransactionParam param = JSON.parseObject(getRequestBody(req), TransactionParam.class);
			Wallet senderWallet = blockService.getMyWalletMap().get(param.getSender());
			if (senderWallet == null) {
				resp.getWriter().print("输入方钱包不存在");
				return;
			}
			Wallet recipientWallet = blockService.getMyWalletMap().get(param.getRecipient());
			if (recipientWallet == null) {
				recipientWallet = blockService.getOtherWalletMap().get(param.getRecipient());
				if (recipientWallet == null) {
					resp.getWriter().print("接受方钱包不存在");
				}
			}
			Transaction transaction = blockService.createTransaction(senderWallet, recipientWallet, param.getAmont());
			if (transaction == null) {
				resp.getWriter().print("钱包" + param.getSender() + "余额不足或找不到一笔等于" + param.getAmont() + "BTC的UTXO");
			} else {
				Transaction[] txs = {transaction};
				String message = JSON.toJSONString(new Message(P2PService.RESPONSE_TRANSACTION, JSON.toJSONString(txs)));
				p2pService.broatcast(message);
				resp.getWriter().print("生成新交易：" + JSON.toJSONString(transaction));
			}
		}
	}
	
	private class GetWalletBalanceServlet extends HttpServlet {
		
		@Override
		protected void doGet(HttpServletRequest req, HttpServletResponse resp)
				throws ServletException, IOException {
			resp.setCharacterEncoding("utf-8");
			String address = req.getParameter("address");
			int balance = blockService.getWalletBalance(address);
			resp.getWriter().print("钱包余额为：" + balance + "BTC");
		}
	}
	
	private class GetUnpackedTransactionServlet extends HttpServlet {
		
		@Override
		protected void doGet(HttpServletRequest req, HttpServletResponse resp)
				throws ServletException, IOException {
			resp.setCharacterEncoding("utf-8");
			List<Transaction> allTransactions = blockService.getAllTransactions();
			List<Transaction> list = new ArrayList<Transaction>(allTransactions);
			List<Transaction> packedTransactions = blockService.getPackedTransactions();
			list.removeAll(packedTransactions);
			resp.getWriter().print(JSON.toJSONString(list));
		}
	}
	
	private class PeersServlet extends HttpServlet {
		
		@Override
		protected void doGet(HttpServletRequest req, HttpServletResponse resp)
				throws ServletException, IOException {
			resp.setCharacterEncoding("utf-8");
			for (WebSocket webSocket : p2pService.getSockets()) {
				InetSocketAddress remoteSocketAddress = webSocket.getRemoteSocketAddress();
				resp.getWriter().println(remoteSocketAddress.getHostName() + ": " + remoteSocketAddress.getPort());
			}
		}
	}
	
	private String getRequestBody(HttpServletRequest req) throws IOException {
		String request = "";
		String line = null;
		BufferedReader in = req.getReader();
		while ((line = in.readLine()) != null) {
			request += line;
		}
		System.out.println(request);
		return request;
	}
}
