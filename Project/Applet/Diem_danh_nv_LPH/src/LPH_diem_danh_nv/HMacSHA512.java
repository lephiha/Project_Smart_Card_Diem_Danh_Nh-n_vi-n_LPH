package LPH_diem_danh_nv;

import javacard.framework.ISOException;
import javacard.framework.JCSystem;
import javacard.framework.Util;
public class HMacSHA512 {
	public static final short BLOCKSIZE=128; // 128 bytes 
	private static final short SW_UNSUPPORTED_KEYSIZE = (short) 0x9c0E;
	private static final short SW_UNSUPPORTED_MSGSIZE = (short) 0x9c0F;
	private static byte[] data;
	
	
	public static void init(byte[] tmp){
		data= tmp;
	}
	
	public static byte[] computeHmacSha512(byte[] key, short key_offset, short key_length, 
			byte[] mac, short mac_offset){
		
		if (key_length>BLOCKSIZE || key_length<0){
			ISOException.throwIt(SW_UNSUPPORTED_KEYSIZE); // don't accept keys bigger than block size 
		}
		for (short i=0; i<key_length; i++){
			 data[i]= (byte) (key[(short)(key_offset+i)] ^ (0x36));
		}
		Util.arrayFillNonAtomic(data, key_length, (short)(BLOCKSIZE-key_length), (byte)0x36);
		//Sha512.reset();
		Sha512.resetUpdateDoFinal(data, (short)0, (short)(BLOCKSIZE), data, (short)BLOCKSIZE); // copy hash result to data buffer!
		
		// compute outer hash
		for (short i=0; i<key_length; i++){
			data[i]= (byte) (key[(short)(key_offset+i)] ^ (0x5c));
		}
		Util.arrayFillNonAtomic(data, key_offset, (short)(BLOCKSIZE-key_length),(byte)0x5c);
		// previous hash already copied to correct offset in data
		Sha512.resetUpdateDoFinal(data, (short)0, (short)(BLOCKSIZE), mac, mac_offset);
		
		return mac;
	}	
}
