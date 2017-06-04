package com.example.demo1;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;

/**
 * 
 * @author sin
 * 
 *         普通写法
 */
public class CommonWrite {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		final String toIp = "192.168.31.225";
		final int toPort = 23233;
		final String msg = "我是要被发送的消息";

		new Thread(new Runnable() {

			@Override
			public void run() {
				requestUdp(toIp, toPort, "hi udp", new Observer() {

					@Override
					public void onReback(String s) {
						System.out.println("request udp success: "+s);
					}

					@Override
					public void onError(Throwable t) {
						System.out.println("request udp fail");
						t.printStackTrace();
					}
				});
			}
		}).start();

		new Thread(new Runnable() {
			@Override
			public void run() {
				requestTcp(toIp, 10086, "hi tcp", new Observer() {

					@Override
					public void onReback(String s) {
						System.out.println("request tcp success: "+s);
					}

					@Override
					public void onError(Throwable t) {
						System.out.println("request tcp fail");
						t.printStackTrace();
					}
				});
			}
		}).start();
	}

	public static void requestUdp(String toIp, int toPort, String msg,
			Observer observer) {
		DatagramSocket dgs = null;
		try {
			// 第一步: 发送数据
			// 创建udp socket
			dgs = new DatagramSocket();
			// 创建数据包, 数据包 包含了要发送的数据, 要送达的ip, 要送达的端口
			byte[] bytes = msg.getBytes();
			DatagramPacket sendDgp = new DatagramPacket(bytes, 0, bytes.length, // 要发送的数据
					InetAddress.getByName(toIp), // 要送达的ip
					toPort); // 要送达的端口号
			dgs.send(sendDgp);
			// 第二步: 准备接收数据
			byte[] buffer = new byte[1024 * 8];
			DatagramPacket receiveDgp = new DatagramPacket(buffer,
					buffer.length);
			dgs.receive(receiveDgp); // 这个方法会一直阻塞, 直到接收到udp报文

			// 第三步: 处理接收到的数据
			byte[] data = receiveDgp.getData();
			int offset = receiveDgp.getOffset();
			int length = receiveDgp.getLength();
			String receiveMsg = new String(data, offset, length); // 这是接收到的消息
			if (observer != null) {
				observer.onReback(receiveMsg);
			}
		} catch (Exception e) {
			if (observer != null) {
				observer.onError(e);
			}
		}finally{
			if(dgs != null){
				dgs.close();
			}
		}
	}

	public static void requestTcp(String toIp, int toPort, String requestMsg,
			Observer observer) {
		Socket socket = null;
		OutputStream fos = null;
		InputStream fis = null;
		try {
			// 创建连接
			socket = new Socket();
			InetSocketAddress serverAddress = new InetSocketAddress(
					InetAddress.getByName(toIp), toPort);
			socket.connect(serverAddress);
			// 写出数据
			fos = socket.getOutputStream();
			fos.write(requestMsg.getBytes());
			fos.flush();
			// 接收数据
			fis = socket.getInputStream();
			byte[] buffer = new byte[1024 * 8];
			int len = -1;
			len = fis.read(buffer);
			if (len == -1) {
				throw new IllegalStateException("read reback msg fail");
			}
			String rebackMsg = new String(buffer, 0, len);
			if (observer != null) {
				observer.onReback(rebackMsg);
			}
		} catch (Exception e) {
			if (observer != null) {
				observer.onError(e);
			}
		} finally {
			if (socket != null) {
				try {
					socket.close();
				} catch (IOException e) {
				}
				socket = null;
			}
		}
	}

	interface Observer {
		void onError(Throwable t);

		void onReback(String s);
	}
}
