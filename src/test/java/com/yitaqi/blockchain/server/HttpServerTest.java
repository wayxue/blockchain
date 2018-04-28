/**
 * 
 */
package com.yitaqi.blockchain.server;

import java.io.BufferedReader;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.junit.Test;

import com.alibaba.fastjson.JSON;
import com.yitaqi.blockchain.model.TransactionParam;

/**
 * 此种情况下，使用postman提交json时，设置postman请求中
 * header：Content-Type ： application/json
 * body：raw：手写json 如下
 * {
	"sender" : "123",
	"recipient" : "123",
	"amont" : 1
	}
 * @author xue
 *
 */
public class HttpServerTest {

	private String getRequestBody(HttpServletRequest request) throws IOException {
		BufferedReader reader = request.getReader();
		String line = null;
		String res = "";
		while ((line = reader.readLine()) != null) {
			res += line;
		}
		return res;
	}
	
	@Test
	public void init() throws Exception {
		Server server = new Server(8081);
		ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
		context.setContextPath("/");
		server.setHandler(context);
		context.addServlet(new ServletHolder(new JSONTestServlet()), "/json");
		server.start();
		server.join();
	}
	
	private class JSONTestServlet extends HttpServlet {
		
		@Override
		protected void doPost(HttpServletRequest req, HttpServletResponse resp)
				throws ServletException, IOException {
			String body = getRequestBody(req);
			TransactionParam param = JSON.parseObject(body, TransactionParam.class);
			resp.setCharacterEncoding("utf-8");
			resp.getWriter().write(JSON.toJSONString(param));
		}
	}
}
