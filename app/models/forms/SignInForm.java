package models.forms;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

public class SignInForm {

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

	public static boolean isValidPassword(String password)
	{
		return password.matches("^.{6,}$"); 
	}	
}
