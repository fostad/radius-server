package se.radius.server;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import se.radius.packet.*;


public class Request {

	private InetAddress clientAddress;
	private int clientPort;
	private int packetLenght;
	private int prevIdentifier;
	private RadiusPacket radiusPacket;
	
	public Request() throws IOException {
		this.radiusPacket = new RadiusPacket();
	}
	
	public boolean decodeRequest(DatagramPacket datagramPacket) throws IOException {
		
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(datagramPacket.getData());		
		int availableBytes = byteArrayInputStream.available();
		
		//if radius code is not access request drop the packet
		if(byteArrayInputStream.read() != RadiusCode.ACCESS_REQUEST)  {
			System.out.println("Radius code is not access request.");
			return false;
		}
		
		//if received same packet in short time drop it
		byte identifier = (byte) byteArrayInputStream.read();
		if(clientAddress != null && clientPort != 0 && 
				(clientAddress == datagramPacket.getAddress()) && 
				(clientPort == datagramPacket.getPort())
				&& identifier == prevIdentifier) { //TODO time span
			System.out.println("Same request from same client.");
			return false;
		}
		
		prevIdentifier = identifier;
		radiusPacket.setIdentifier(identifier);
		clientAddress = datagramPacket.getAddress();
		clientPort = datagramPacket.getPort();
		
		//received packet length
		int packetLength = byteArrayInputStream.read();
		packetLength = packetLength << 8;
		packetLength = packetLength + byteArrayInputStream.read();
		
		//if did not received complete packet drop it
		//if packet length is not between max an min drop it
		if(packetLength > availableBytes || 
				packetLength < RadiusPacket.MIN_PACKET_LENGHT || 
				packetLength > RadiusPacket.MAX_PACKET_LENGHT) {
			System.out.println("Lenght is not in range.");
			return false;
		}
		
		byte[] authenticator = new byte[16];
		byteArrayInputStream.read(authenticator);
		radiusPacket.setAuthenticator(authenticator);
		
		String userName = "";
		packetLength = packetLength - RadiusPacket.PACKET_HEADER_LENGTH;
		radiusPacket.setCode(RadiusCode.ACCESS_REJECT); 
		while(byteArrayInputStream.available() != 0 && packetLength != 0) {
			byte attributeType = (byte) byteArrayInputStream.read();
			packetLength--;
			if(attributeType == Attribute.USER_NAME) { //read user name
				int attributeLength = byteArrayInputStream.read();
				packetLength--;
				attributeLength = attributeLength - Attribute.ATTRIBUTE_HEADER_LENGTH;
				byte[] userNameByte = new byte[attributeLength];
				byteArrayInputStream.read(userNameByte);
				packetLength = packetLength - attributeLength;
				userName = new String(userNameByte);
			} else if(attributeType == Attribute.USER_PASSWORD &&  //read password
					(userName.equals(Attribute.DB_USER_NAME1) || 
							userName.equals(Attribute.DB_USER_NAME2)) ) {
				int attributeLength = byteArrayInputStream.read();
				packetLength--;
				attributeLength = attributeLength - Attribute.ATTRIBUTE_HEADER_LENGTH;
				byte[] passwordEncrpt = new byte[attributeLength];
				byteArrayInputStream.read(passwordEncrpt);
				packetLength = packetLength - attributeLength;
				//decrypt password
				String password = decryptPassword(passwordEncrpt);
				
				//check if password is ok
				if(userName.equals(Attribute.DB_USER_NAME1) && 
						password.equals(Attribute.DB_PASS_WORD1))
					radiusPacket.setCode(RadiusCode.ACCESS_ACCEPT);
				else if(userName.equals(Attribute.DB_USER_NAME2) && 
						password.equals(Attribute.DB_PASS_WORD2))
					radiusPacket.setCode(RadiusCode.ACCESS_ACCEPT);
			} 
		}
		return true;
	}
	
	public String decryptPassword(byte[] encryptedPass)  {
				
			byte[] encryptedPassOrig = encryptedPass.clone();
	        
			try {
				MessageDigest md5;
				md5 = MessageDigest.getInstance("MD5");
				md5.reset();
		        md5.update(RadiusPacket.sharedSecret.getBytes());
		        md5.update(radiusPacket.getAuthenticator());
		        byte bn[] = md5.digest();
		        for (int i = 0; i < 16; i++) {
		            encryptedPass[i] = (byte) (bn[i] ^ encryptedPass[i]);
		        }
		        if (encryptedPass.length > 16) {
		            for (int i = 16; i < encryptedPass.length; i += 16) {
		                md5.reset();
		                md5.update(RadiusPacket.sharedSecret.getBytes());
		                md5.update(encryptedPassOrig, i - 16, 16);
		                bn = md5.digest();
		                for (int j = 0; j < 16; j++) {
		                    encryptedPass[i + j] = (byte) (bn[j] ^ encryptedPass[i + j]);
		                }
		            }
		        }
		        int len = encryptedPass.length;
		        while (len > 0 && encryptedPass[len - 1] == 0) {
		            len--;
		        }

		        byte[] passtrunc = new byte[len];
		        System.arraycopy(encryptedPass, 0, passtrunc, 0, len);
		        return new String(passtrunc);
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			}
			return null;
	}
	
	
	public InetAddress getClientAddress() {
		return clientAddress;
	}

	public void setClientAddress(InetAddress clientAddress) {
		this.clientAddress = clientAddress;
	}

	public int getClientPort() {
		return clientPort;
	}

	public void setClientPort(int clientPort) {
		this.clientPort = clientPort;
	}

	public int getPacketLenght() {
		return packetLenght;
	}

	public void setPacketLenght(int packetLenght) {
		this.packetLenght = packetLenght;
	}

	public RadiusPacket getRadiusPacket() {
		return radiusPacket;
	}

	public void setRadiusPacket(RadiusPacket radiusPacket) {
		this.radiusPacket = radiusPacket;
	}
	
	
	
}
