package controllers;

import java.io.ByteArrayOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;

import models.CityParser;
import models.Core;
import models.CurrencyService;
import models.WeatherData;
import models.WeatherForecast;
import models.beans.City;
import models.beans.User;
import models.dao.DAOFactory;
import models.dao.IUserDAO;
import models.dao.UserDAO;
import models.endpoint.SparqlEndpoint;
import play.data.DynamicForm;
import play.data.Form;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.index;
import views.html.results;
import views.html.cityInformationByQuery;
import views.html.sparql;
import views.html.sparqlresults;

public class Application extends Controller 
{
    public static Result index() 
    {
        return ok( index.render() );
    }

    // LOGIN USER
	public static Result login() 
    {
    	DynamicForm loginForm = Form.form().bindFromRequest();
    	String email = loginForm.get("login-email");
        String password = loginForm.get("login-password");
    	
        System.out.println(email+" "+password);
        
        IUserDAO userDAO = new UserDAO(new DAOFactory().createMongodbConnection());
        if(userDAO.exists(email, password))
        {
        	String user = session("connected");
        	if(user == null) 
        	{
        		session("connected", email);
        	}
            return redirect(controllers.routes.Application.index());        		
        }
        return unauthorized("Oops, need the right credentials");
    }
	
	// LOGGING OUT USER
    public static Result logout() 
    {
		String user = session("connected");
		if(user != null) 
		{
			session().clear();
		    flash("success", "You've been logged out");
		    
		}
		return redirect(routes.Application.index());
    }	
    
    // REGISTER USER
    public static Result register() 
    {
    	IUserDAO userDAO = new UserDAO(new DAOFactory().createMongodbConnection());
    	
    	DynamicForm loginForm = Form.form().bindFromRequest();
    	
    	String name = loginForm.get("register-name");
    	String email = loginForm.get("register-email");
    	String password = loginForm.get("register-password");
    	String passwordConfirm = loginForm.get("register-password-confirm");
    	
    	if(!name.isEmpty() && !email.isEmpty() && !password.isEmpty())
    	{
    		User user = new User();
    		user.setName(name);
    		user.setEmail(email);
    		user.setPassword(password);
    		user.setInscriptiondate(new Date());
    		
    		if(userDAO.save(user))
    		{
        		session("connected", email);
        		return redirect(controllers.routes.Application.index());
        	}
    	}
    			
		return ok("You are not logged");
    }  

    public static Result cityInformationByQuery(String query) 
    {
    	return ok(cityInformationByQuery.render(Core.getCityByQuery(query)));
    }
    
    public static Result results() throws ParseException 
    {
    	// GET FORM DATA
    	DynamicForm dynamicForm = Form.form().bindFromRequest();
        
    	String destination = dynamicForm.get("destination-city");
    	String arrivalDateStr = dynamicForm.get("search-date");
    	
        Date ArrivalDate = new SimpleDateFormat("dd/MM/yyyy hh:mm").parse(arrivalDateStr);

        // GET CITY DATA
        City city = CityParser.parse(destination);
        
        // GET WEATHER INFORMATION
        List<WeatherData> weatherData = WeatherForecast.getWeatherByLatLongOnDate(city.getLatitude(),city.getLongitude(), ArrivalDate);
        
        // GET CURRENCY INFORMATION
        String currency = CurrencyService.getCurrency(city.getCurrencyCode());
        
        // GET GOOGLE MAP API KEY --- NO NEED FROM NOW ON
        //String GMAPIKEY = Play.application().configuration().getString("GMAPIKEY");
        
    	return ok(results.render(city, ArrivalDate, weatherData, currency, null));
    }
    
    public static Result sparql() 
    {
    	return ok(sparql.render());
    }
    
    public static Result sparqlresults()
    {
    	DynamicForm dynamicForm = Form.form().bindFromRequest();
    	String query = dynamicForm.get("query");
    	String format = null;
    	format = dynamicForm.get("format");
    	
    	int formatInt = 0;
    	if(format == null) {
    		if(request().accepts("text/html")) {
        		formatInt = 0;
        	}
        	else if(request().accepts("application/json")) {
        		formatInt = 1;
        	}
        	else if (request().accepts("text/xml")) {
    	    	formatInt = 2;
    	    }
        	else if (request().accepts("application/rdf+xml")) {
    	    	formatInt = 3;
    	    }
    	}
    	else {
    		formatInt = Core.parseInt(format);
    	}
    	
    	ResultSet results = SparqlEndpoint.queryData(query);
    	if(results == null) {
    		return ok(sparqlresults.render("Error on SPARQL query"));
    	}

		switch(formatInt) {
			case 0:
				String resultsHtml = SparqlEndpoint.outputHtml(results);
				return ok(sparqlresults.render(resultsHtml));
			case 1:
				ByteArrayOutputStream baosJson = new ByteArrayOutputStream();
				ResultSetFormatter.outputAsJSON(baosJson, results);
				return ok(Json.parse(baosJson.toString()));
			case 2:
				ByteArrayOutputStream baosXml = new ByteArrayOutputStream();
				ResultSetFormatter.outputAsXML(baosXml, results);
				return ok(baosXml.toString());
			case 3:
				ByteArrayOutputStream baosRDF = new ByteArrayOutputStream();
				ResultSetFormatter.outputAsRDF(baosRDF, "RDF/XML-ABBREV", results);
				return ok(baosRDF.toString());
		}
		return null;
    }
}