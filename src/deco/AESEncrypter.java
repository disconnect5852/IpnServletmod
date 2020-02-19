package deco;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.interfaces.PBEKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class AESEncrypter 
{ 
	Cipher ecipher; 
	Cipher dcipher; 

	public AESEncrypter(String passphrase) 
	{  
		byte[] iv = new byte[] 
				{ 
				0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09,0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f 
				}; 

		AlgorithmParameterSpec paramSpec = new IvParameterSpec(iv); 
		try 
		{
			byte[] salt ={78,-52,-36,78,136,12,167,17,65,76,94,34,24,-65,34,31};  
			//rand.nextBytes(salt);  
			//PBEKeySpec password = new PBEKeySpec(passphrase.toCharArray(), new byte[16], 1, 128);  
			//SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");  
			PBEKey key = (PBEKey) SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1").generateSecret(new PBEKeySpec(passphrase.toCharArray(), salt, 7, 128));  
			SecretKey encKey = new SecretKeySpec(key.getEncoded(), "AES"); 
			ecipher = Cipher.getInstance("AES/CBC/PKCS5Padding"); 
			dcipher = Cipher.getInstance("AES/CBC/PKCS5Padding"); 

			// CBC requires an initialization vector 
			ecipher.init(Cipher.ENCRYPT_MODE, encKey, paramSpec); 
			dcipher.init(Cipher.DECRYPT_MODE, encKey, paramSpec); 
		} 
		catch (Exception e) 
		{ 
			e.printStackTrace(); 
		} 
	} 

	// Buffer used to transport the bytes from one stream to another 
	byte[] buf = new byte[1024]; 

    public OutputStream encryptstream(OutputStream out) {
    	return new CipherOutputStream(out, ecipher);
    }
    public InputStream decryptstream(InputStream in) {
    	return new CipherInputStream(in, dcipher);
    }
}