package se.radius.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import se.radius.packet.RadiusPacket;

public class Response {

	private RadiusPacket radiusPacket;
	
	public Response(RadiusPacket radiusPacket) {
		this.radiusPacket = radiusPacket;
	}
	
	//create response packet
	public ByteArrayOutputStream createResponse() 
			throws IOException {

		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		byte[] length = new byte[2];
		length[0] = 0; //TODO
		length[1] = 20;
		radiusPacket.setLenght(length);
		
		byteArrayOutputStream.write(radiusPacket.getCode());
		byteArrayOutputStream.write(radiusPacket.getIdentifier());
		byteArrayOutputStream.write(radiusPacket.getLenght()); 
		byteArrayOutputStream.write(encrypt());
		
		return byteArrayOutputStream; 
	}
	
	//encrypt authenticator 
	public byte[] encrypt() {
		byte[] authenticator = new byte[16];
		try {
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			md5.update(radiusPacket.getCode());
	        md5.update(radiusPacket.getIdentifier());
	        md5.update(radiusPacket.getLenght());
	        md5.update(radiusPacket.getAuthenticator());
	        md5.update(RadiusPacket.sharedSecret.getBytes());
	        authenticator = md5.digest();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		return authenticator; 
	}
	

}
