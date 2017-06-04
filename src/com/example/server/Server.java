package com.example.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new Thread(udpServer).start();
		new Thread(tcpServer).start();
		
	}
	
	static boolean udpServerFlag;
	static Runnable udpServer = new Runnable() {
		
		@Override
		public void run() {
			
			try {
				DatagramSocket serverDgs = new DatagramSocket(23233);
				byte[] buffer = new byte[1024 * 8];
				DatagramPacket serverReceivePacket = new DatagramPacket(buffer, buffer.length);
				udpServerFlag = true;
				while(udpServerFlag){
					serverDgs.receive(serverReceivePacket);
					reback(serverDgs,serverReceivePacket);
				}
			} catch (Exception e) {
			}
		}
		
		public void reback(DatagramSocket dgs,DatagramPacket packet) throws IOException{
			InetAddress fromAddress = packet.getAddress();
			int fromPort = packet.getPort();
			byte[] data = packet.getData();
			int offset = packet.getOffset();
			int length = packet.getLength();
			
			String receiveMsg = new String(data,offset,length);
			String rebackMsg = "server already received client udp msg: "+receiveMsg;
			System.out.println(rebackMsg);
			
			byte[] buffer = rebackMsg.getBytes();
			DatagramPacket rebackDgp = new DatagramPacket(buffer,0,buffer.length,fromAddress,fromPort);
			dgs.send(rebackDgp);
		}
	};
	
	static boolean tcpServerFlag;
	static Runnable tcpServer = new Runnable() {
		
		@Override
		public void run() {
			try {
				ServerSocket serverSocket = new ServerSocket(10086);
				tcpServerFlag = true;
				while(tcpServerFlag){
					Socket socket = serverSocket.accept();
					rebackClient(socket);
				}
			} catch (IOException e) {
			}
		}

		private void rebackClient(Socket socket) throws IOException {
			InputStream is = socket.getInputStream();
			byte[] buffer = new byte[1024 * 8];
			int len = -1;
			
			len = is.read(buffer);
			
			if(len == -1){
				System.out.println("read client msg fail");
				socket.close();
				return;
			}
			
			String receiveMsg = new String(buffer,0,len);
			String rebackMsg = "server already receive client tcp msg: " + receiveMsg;
			System.out.println(rebackMsg);
			
			OutputStream os = socket.getOutputStream();
			os.write(rebackMsg.getBytes());
			os.flush();
			socket.close();
		}
	};

}
