package se.radius.packet;


public class RadiusPacket {
	
	public static final int MAX_PACKET_LENGHT = 4096;
	public static final int MIN_PACKET_LENGHT = 20;
	public static final int PACKET_HEADER_LENGTH = 20;
	public static String sharedSecret;
	
	private byte identifier;
	private byte code;
	private byte[] lenght;
	private byte[] authenticator;
	
	public RadiusPacket() {
		this.lenght = new byte[2];
		this.authenticator = new byte[4];
	}

	public byte getIdentifier() {
		return identifier;
	}

	public void setIdentifier(byte identifier) {
		this.identifier = identifier;
	}

	public byte getCode() {
		return code;
	}

	public void setCode(byte code) {
		this.code = code;
	}

	public byte[] getLenght() {
		return lenght;
	}

	public void setLenght(byte[] lenght) {
		this.lenght = lenght;
	}

	public byte[] getAuthenticator() {
		return authenticator;
	}

	public void setAuthenticator(byte[] authenticator) {
		this.authenticator = authenticator;
	}


}
