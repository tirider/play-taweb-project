package models.dao;

import java.util.List;
import models.beans.*;

/**  **/
public interface IUserDAO
{
	public User find(String email);
	
	public boolean save(User user);
	
	public boolean remove(String email);
	
	public boolean update(String email, String newpassword);
	
	public List<User> findAll();	
	
	public boolean exists(String email, String password);	
}
