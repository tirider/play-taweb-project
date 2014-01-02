package models.crypto;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Encryptor 
{
	public static String ecryptToSha1(String content)
	{
		try 
		{
	        MessageDigest md = MessageDigest.getInstance("SHA1");
	        
	        md.update(content.getBytes());
	        
	        return new BigInteger( 1, md.digest() ).toString(16);
	    }
	    catch (NoSuchAlgorithmException e) { }
		
		return content;
	}
}
