package LPH_diem_danh_nv;

import javacard.framework.*;
import javacardx.crypto.*; 
import javacard.security.*;
import javacard.security.KeyBuilder;


public class RSAApplet extends Applet
{
	private static final byte INS_SIGN	= (byte)0x00; 
	private static final byte INS_VERIFY = (byte)0x01;

	private RSAPrivateKey rsaPrivKey; 
	private RSAPublicKey rsaPubKey; 
	private Signature rsaSig;
	 
	private short sigLen;


	public static void install(byte[] bArray, short bOffset, byte bLength) 
	{
		new RSAApplet().register(bArray, (short) (bOffset + 1), bArray[bOffset]);
	}
	
	private RSAApplet(){
		sigLen = (short)(KeyBuilder.LENGTH_RSA_1024/8); // 128 bytes
		
		rsaSig = Signature.getInstance(Signature.ALG_RSA_SHA_PKCS1,false);
		rsaPrivKey = (RSAPrivateKey)
			KeyBuilder.buildKey(KeyBuilder.TYPE_RSA_PRIVATE, (short)(8*sigLen),false);
		rsaPubKey = (RSAPublicKey)
			KeyBuilder.buildKey(KeyBuilder.TYPE_RSA_PUBLIC, (short)(8*sigLen), false);

		KeyPair keyPair = new KeyPair(KeyPair.ALG_RSA, (short)(8*sigLen));
		keyPair.genKeyPair();
		rsaPrivKey = (RSAPrivateKey)keyPair.getPrivate(); 
		rsaPubKey = (RSAPublicKey)keyPair.getPublic();
	}

	public void process(APDU apdu)
	{
		if (selectingApplet())
		{
			return;
		}

		byte[] buf = apdu.getBuffer();
		
		switch (buf[ISO7816.OFFSET_INS])
		{
		case (byte)0x00:
			sendPublicModulus(apdu);
			break;
		case (byte)0x01:
			sendPublicExponent(apdu);
			break;
		case (byte)0x02:
			rsaSign(apdu);
			break;	
		default:
			ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
		}
	}
	
	private void sendPublicExponent(APDU apdu){
		apdu.setIncomingAndReceive();
		byte[] buf = apdu.getBuffer();
		short lenExponent  = rsaPubKey.getExponent(buf, (short) 0);
		apdu.setOutgoingAndSend((short)0, lenExponent);
	}
	
	private void sendPublicModulus(APDU apdu){
		apdu.setIncomingAndReceive();
		byte[] buf = apdu.getBuffer();
		
		short lenModulus = rsaPubKey.getModulus(buf, (short) 0);
		apdu.setOutgoingAndSend((short)0, lenModulus);
	}

	private void rsaSign(APDU apdu)
	{
		byte[] buffer = apdu.getBuffer();
		short byteRead = (short)(apdu.setIncomingAndReceive());
		
		byte[] data = new byte[byteRead];
		byte[] sig_buffer = new byte[sigLen];
		
		Util.arrayCopy(buffer, ISO7816.OFFSET_CDATA, data, (short)0, byteRead);
		rsaSig.init(rsaPrivKey, Signature.MODE_SIGN); 
		
		rsaSig.sign(data, (short)0, (short)(data.length), sig_buffer, (short)0);
		apdu.setOutgoing(); 
		apdu.setOutgoingLength(sigLen);
		 
		apdu.sendBytesLong(sig_buffer, (short)0, sigLen);
	}
}