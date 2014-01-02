package models.beans;

import java.util.Date;

import play.data.validation.Constraints.Required;

public class User 
{
	@Required
	private String id;
	private String name;
	private String email;
	private String password;
	private Date inscriptiondate;
	
	public User()
	{
		this.id = new String();
		this.name = new String();
		this.email = new String();
		this.password = new String();
		this.inscriptiondate = new Date();
	}
	
	public String getId() {return id;}
	public void setId(String id) { this.id = id; }
	
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }

	public String getPassword() { return password; }
	public void setPassword(String userpassword) { this.password = userpassword; }
	
	public String getEmail() { return email; }
	public void setEmail(String email) { this.email = email; }

	public Date getInscriptiondate() { return inscriptiondate; }
	public void setInscriptiondate(Date inscriptiondate) { this.inscriptiondate = inscriptiondate; }
}