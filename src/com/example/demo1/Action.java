package com.example.demo1;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class Action {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Action.requestType(Type.UDP)
				.toIp("192.168.31.225")
				.toPort(23233)
				.requestMsg("hello udp")
				.request(new Observer() {

					@Override
					public void onError(Throwable t) {
						System.out.println("udp request fail");
						t.printStackTrace();
						
					}

					@Override
					public void onReback(String s) {
						System.out.println("udp request success: "+s);
					}
				});
		
		Action.requestType(Type.TCP)
				.toIp("192.168.31.225")
				.toPort(10086)
				.requestMsg("hello tcp")
				.request(new Observer() {

					@Override
					public void onError(Throwable t) {
						System.out.println("tcp request fail");
						t.printStackTrace();
					}
		
					@Override
					public void onReback(String s) {
						System.out.println("tcp request success: "+s);
					}
				});
		
		System.out.println("game over");
	}

	private String toIp;
	private int toPort;
	private byte[] requestBytes;
	private Observer observer;
	private Type requestType;

	private Runnable udpRequest = new Runnable() {

		@Override
		public void run() {
			DatagramSocket dgs = null;
			try {
				// 第一步: 发送数据
				// 创建udp socket
				dgs = new DatagramSocket();
				// 创建数据包, 数据包 包含了要发送的数据, 要送达的ip, 要送达的端口
				byte[] bytes = requestBytes;
				DatagramPacket sendDgp = new DatagramPacket(bytes, 0,
						bytes.length, // 要发送的数据
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
				System.out.println("re: "+receiveMsg);
				if (observer != null) {
					observer.onReback(receiveMsg);
				}
			} catch (Exception e) {
				if (observer != null) {
					observer.onError(e);
				}
			} finally {
				if (dgs != null) {
					dgs.close();
				}
			}
		}
	};

	private Runnable tcpRequest = new Runnable() {

		@Override
		public void run() {
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
				fos.write(requestBytes);
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
				if (fis != null) {
					try {
						fis.close();
					} catch (IOException e) {
					}
					fis = null;
				}
				if (fos != null) {
					try {
						fos.close();
					} catch (IOException e) {
					}
					fos = null;
				}
				if (socket != null) {
					try {
						socket.close();
					} catch (IOException e) {
					}
					socket = null;
				}
			}
		}
	};

	public static Action requestType(Type type) {
		Action action = new Action();
		action.requestType = type;
		return action;
	}

	public Action toIp(String toIp) {
		this.toIp = toIp;
		return this;
	}

	public Action toPort(int toPort) {
		this.toPort = toPort;
		return this;
	}

	public Action requestMsg(String msg) {
		requestBytes = msg.getBytes();
		return this;
	}

	public void request(Observer observer) {
		this.observer = observer;

		Runnable r;
		if (Type.TCP.equals(requestType)) {
			r = tcpRequest;
		} else {
			r = udpRequest;
		}
		new Thread(r).start();
	}

	enum Type {
		UDP, TCP
	}

	interface Observer {
		void onError(Throwable t);

		void onReback(String s);
	}
}
