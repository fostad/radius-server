package se.radius.server;

import java.io.*;
import java.net.*;

public class UDPServer {
	
	private DatagramSocket serverSocket;
	private int packetMaxLenght;
	private InetAddress clientAddress;
	private int clientPort;
	

	public UDPServer(int port, int packetMaxlenght) throws SocketException {
		this.serverSocket = new DatagramSocket(8080);
		this.packetMaxLenght = packetMaxlenght;
	}
	
	public DatagramPacket receive() throws IOException {
        byte[] receiveData = new byte[packetMaxLenght]; 
		DatagramPacket receivedPacket = new DatagramPacket(receiveData, receiveData.length);
		serverSocket.receive(receivedPacket);
		clientAddress = receivedPacket.getAddress();
		clientPort = receivedPacket.getPort();
		return receivedPacket;
	}

	public void send(ByteArrayOutputStream bytes) throws IOException {
		byte[] sendData = new byte[bytes.size()];
		sendData = bytes.toByteArray();
		DatagramPacket datagramPacket = new DatagramPacket(sendData,sendData.length, 
				clientAddress,clientPort);
		serverSocket.send(datagramPacket);
	}
	
	public DatagramSocket getServerSocket() {
		return serverSocket;
	}

	public void setServerSocket(DatagramSocket serverSocket) {
		this.serverSocket = serverSocket;
	}

	public int getPacketMaxLenght() {
		return packetMaxLenght;
	}

	public void setPacketMaxLenght(int packetMaxLenght) {
		this.packetMaxLenght = packetMaxLenght;
	}
	
	public void close() {
		serverSocket.close();
	}

}
