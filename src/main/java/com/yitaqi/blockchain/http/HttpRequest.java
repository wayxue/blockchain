package com.yitaqi.blockchain.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;

/**
 * Http 工具类
 * 没用
 * @author xue
 *
 */
public class HttpRequest {

	/**
	 * 向指定url发送get方法
	 */
	public static String sendGet(String url, String param) {
	
		String result = "";
		BufferedReader in = null;
		String urlNameString = url + "?" + param;
		try {
			URL realUrl = new URL(urlNameString);
			// 打开url 之间的连接
			URLConnection connection = realUrl.openConnection();
			// 设置通用的请求属性
			connection.setRequestProperty("accept", "*/*");
			connection.setRequestProperty("connection", "Keep-Alive");
			connection.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
			// 建立实际连接
			connection.connect();
			// 获取所有响应头字段
			Map<String, List<String>> map = connection.getHeaderFields();
			for (String key : map.keySet()) {
				System.out.println(key + "--->" + map.get(key));
			}
			// 定义BufferReader 输出流来读取url的响应
			in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String line = null;
			while ((line = in.readLine()) != null) {
				result += line;
			}
		} catch (Exception e) {
			System.err.println("发送get请求异常：" + e);
			e.printStackTrace();
		} finally {
			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}
	
	public static String sendPost(String url, String param) {
		PrintWriter out = null;
		BufferedReader in = null;
		String result = "";
		try {
			URL realUrl = new URL(url);
			// 打开和url之间的连接
			URLConnection connection = realUrl.openConnection();
			// 设置请求的通用属性
			connection.setRequestProperty("accept", "*/*");
			connection.setRequestProperty("connection", "Keep-Alive");
			connection.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
			// 发送post请求必须设置如下两行
			connection.setDoOutput(true);
			connection.setDoInput(true);
			// 获取URLConnection 对象对应的输入流
			out = new PrintWriter(connection.getOutputStream());
			// 发送请求
			out.print(param);
			// flush 输入流的缓冲
			out.flush();
			// 定义BufferedReader输出流，读取url的响应
			in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String line = null;
			while((line = in.readLine()) != null) {
				result += line;
			}
		} catch (Exception e) {
			System.err.println("发送post请求失败：" + e);
			e.printStackTrace();
		} finally {
			try {
				if (out != null) out.close();
				if (in != null) in.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return result;
	}
}
