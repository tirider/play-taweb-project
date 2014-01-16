package models.forms;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

public class SignUpForm 
{
	/**
	 * 
	 * @param username
	 * @return
	 */
	public static boolean isValidUserName(String username)
	{
    	return (username.matches("[a-zA-Z]{2,}") && !username.matches("[\\s]+"));
	}

	/**
	 * 
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
    	catch (AddressException ex) 
    	{  
    		return false;
        }
	}
	
	/**
	 * 
	 * @param city
	 * @return
	 */
	public static boolean isValidCity(String city)
	{
    	return !city.isEmpty();
	}
	
	/**
	 * 
	 * @param password
	 * @param passwordConfirm
	 * @return
	 */
	public static boolean isValidPasswords(String password, String passwordConfirm)
	{
		return (password.matches("^.{6,}$") &&  password.equals(passwordConfirm)); 
	}
}
