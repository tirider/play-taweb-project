package controllers;

import java.util.Date;

import models.beans.User;
import models.dao.DAOFactory;
import models.dao.IUserDAO;
import models.dao.UserDAO;
import models.semantic.Semantic;
import models.forms.SignInForm;
import models.forms.SignUpForm;
import models.global.Core;
import play.data.DynamicForm;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.index;
import views.html.about;

public class Application extends Controller 
{
	/**
	 * This method provide access to the welcome page.
	 * @param auth : Holds access to the authentication frame.
	 * @return
	 */
    public static Result index(Integer auth) 
    {
    	int authentication = Core.parseInt(String.valueOf(auth));
    	
    	if(authentication == 0) 
    		return ok(index.render(Semantic.getMostInterestedCities(),0));
		return ok(index.render(Semantic.getMostInterestedCities(),1));    		
    }
 
    /**
     * This method log the an user into the system.
     * @return
     */
	public static Result login() 
    {
		// GET PARAM VALUES
    	DynamicForm loginForm = Form.form().bindFromRequest();
    	String email = loginForm.get("email");
        String password = loginForm.get("password");
        
        if(SignInForm.isValidEmail(email) && SignInForm.isValidPassword(password))
        {
	    	// DAO
	        IUserDAO userDAO = new UserDAO(new DAOFactory().createMongodbConnection());
	        
	        // AUTHENTICATION CHECKER
	        if(userDAO.exists(email, password))
	        {
	        	User user = userDAO.find(email);
	        	
	        	// CREATE SESSION DATA
	        	session().clear();
	        	session("username", user.getName());
				session("email", user.getEmail());
				
				// RETRIEVE JSON TO CHECK EVERYTHING OK WHEN LOGIN
				return ok("{\"username\":\""+user.getName()+"\",\"email\":\"" + user.getEmail() + "\" }");
	        }
	        else return ok("{\"error\":\"1\" }"); 
	     }
	     return ok("{\"error\":\"1\" }");
    }
	
	/**
	 * This method log the connected user out. 
	 * @return
	 */
    public static Result logout() 
    {
		if(session("username") != null) 
		{
			session().clear();
		    flash("success", "You've been logged out");
		}
		// PARAM=1 AVOIDING THE LOGIN QUESTION
		return redirect(routes.Application.index(1));
    }	
    
    /**
     * This method register an user into the application.
     * @return
     */
    public static Result register() 
    {
    	DynamicForm loginForm = Form.form().bindFromRequest();
    	String username = loginForm.get("username");
    	String email = loginForm.get("email");
    	String city = loginForm.get("city");
    	String password = loginForm.get("password");
    	String passwordConfirm = loginForm.get("passwordConfirm");
    	
    	if(SignUpForm.isValidUserName(username) && SignUpForm.isValidEmail(email) && 
    	   SignUpForm.isValidCity(city) && SignUpForm.isValidPasswords(password,passwordConfirm) )
        {
			User user = new User();
			user.setName(username);
			user.setEmail(email);
			user.setPassword(password);
			user.setInscriptiondate(new Date());
			
			IUserDAO userDAO = new UserDAO(new DAOFactory().createMongodbConnection());
			
			if(userDAO.exists(user.getName()))
			{
				return ok("{\"error\":\"2\" }");		
			}
			else if (userDAO.existsEmail(user.getEmail()))
			{
				return ok("{\"error\":\"3\" }");
			}
			else
			{
				if(userDAO.save(user))
				{
					// CREATE SESSION DATA
					session().clear();
					session("username", user.getName());
					session("email", user.getEmail());
					
					// REGISTER USER ON TDB
					Semantic.insertUserTDB(user.getName(), user.getEmail(), city);
					
					// RETRIEVE JSON TO CHECK EVERYTHING OK WHEN SIGN UP
					return ok("{\"error\":\"0\",\"username\":\""+user.getName()+"\",\"email\":\"" + user.getEmail() + "\" }");
				}
				else return badRequest();
			}
		}
		return ok("{\"error\":\"1\" }");
    }

    /**
     * This method provide information about developers team.
     * @return
     */
    public static Result about()
    {
    	return ok(about.render());
    }
}
