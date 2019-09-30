package au.com.michaelpage.gap.common.util;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

public class CryptUtil {
	
	static final byte[] key = ("mp" + java.lang.StrictMath.E).substring(0, 16).getBytes();
	
	public static String decrypt(String encrypted) throws Exception {
		Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"));
		return new String(cipher.doFinal(DatatypeConverter.parseBase64Binary(encrypted)), "UTF8");
	}
	
	public static String encrypt(String message) throws Exception {
		Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"));
		return DatatypeConverter.printBase64Binary(cipher.doFinal(message.getBytes("UTF8")));
	}
	
}
