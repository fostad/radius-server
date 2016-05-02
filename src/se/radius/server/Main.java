package se.radius.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.SocketException;

import se.radius.packet.RadiusPacket;


public class Main {

	private static final int PACKET_MAX_LENGTH = 4096;

	
	public static void main(String[] args) {
		
		if(args.length < 2) {
			System.out.println("Enter port and shared secret.");
			return;
		}
		 
		int port = Integer.parseInt(args[0]);
		RadiusPacket.sharedSecret = args[1];
		UDPServer udpServer = null;
		
		try {
			udpServer = new UDPServer(port, PACKET_MAX_LENGTH);	
			
			while(true) {		
				DatagramPacket recievedPacket = udpServer.receive();
				Request request = new Request();
							
				if(request.decodeRequest(recievedPacket)) {
					Response response = new Response(request.getRadiusPacket());
					ByteArrayOutputStream responseBytes = response.createResponse();
					udpServer.send(responseBytes);
				}
			}
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(udpServer != null)
				udpServer.close();
		}
		
	}

}
