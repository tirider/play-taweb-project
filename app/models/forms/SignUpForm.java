package models.forms;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

public class SignUpForm 
{
	public static boolean isValidUserName(String username)
	{
    	return (username.matches("[a-zA-Z]{2,}") && !username.matches("[\\s]+"));
	}

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
	
	public static boolean isValidCity(String city)
	{
    	return !city.isEmpty();
	}
	
	public static boolean isValidPasswords(String password, String passwordConfirm)
	{
		return (password.matches("^.{6,}$") &&  password.equals(passwordConfirm)); 
	}
}
