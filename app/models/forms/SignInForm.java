package models.forms;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

public class SignInForm 
{
	/**
	 * This method verify whether the given mail is valid
	 * @param email
	 * @return
	 */
	public static boolean isValidEmail(String email)
	{
    	try 
    	{  
    		InternetAddress e = new InternetAddress(email);
    		
        	e.validate();  
        	
        	return true;
    	} 
    	catch (AddressException ex) {  
    		return false;
        }
	}

	/**
	 * This method verify whether the given password is valid
	 * @param password
	 * @return
	 */
	public static boolean isValidPassword(String password)
	{
		return password.matches("^.{6,}$"); 
	}	
}
