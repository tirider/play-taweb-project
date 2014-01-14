package controllers;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;

import models.CityParser;
import models.Core;
import models.beans.Photo;
import models.PhotoService;
import models.WeatherForecast;
import models.beans.City;
import models.beans.Destination;
import models.beans.Review;
import models.beans.User;
import models.beans.Weather;
import models.dao.DAOFactory;
import models.dao.IUserDAO;
import models.dao.UserDAO;
import models.query.QueryRunner;
import models.semantic.Ontology;
import models.semantic.SparqlEndpoint;
import models.semantic.Semantic;
import models.forms.SignInForm;
import models.forms.SignUpForm;
import play.Play;
import play.mvc.Http;
import play.api.mvc.Session;
import play.data.DynamicForm;
import play.data.Form;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.index;
import views.html.notFoundPage;
import views.html.results;
import views.html.sparql;
import views.html.sparqlresults;
import views.html.services;
import views.html.user;
import views.html.about;
import views.html.ontology;
import views.html.namespaceprefixes;
import views.html.dbpediaoffline;

public class Application extends Controller 
{
	// Home page
    public static Result index(Integer auth) 
    {
    	int authentication = Core.parseInt(String.valueOf(auth));
    	
    	if(authentication == 0) 
    		return ok(index.render(Semantic.getMostInterestedCities(),0));
		return ok(index.render(Semantic.getMostInterestedCities(),1));    		
    }
 
    // LOGIN USER
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
	        	
	        	session().clear();
	        	session("username", user.getName());
				session("email", user.getEmail());
				
				return ok("{\"email\":\"" + user.getEmail() + "\" }");
	        }
	        else return ok("{\"error\":\"1\" }"); 
	     }
	     return ok("{\"error\":\"1\" }");
    }
	
	// LOGGING OUT USER
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
    
    // REGISTER USER
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
			
			if(userDAO.exists(username))
			{
				return ok("{\"error\":\"2\" }");		
			}
			else if (userDAO.existsEmail(email))
			{
				return ok("{\"error\":\"3\" }");
			}
			else
			{
				if(userDAO.save(user))
				{
					System.out.println("inside");
					session().clear();
					session("username", username);
					session("email", email);
					Semantic.insertUserTDB(username, email);
					return ok("{\"error\":\"0\",\"email\":\"" + user.getEmail() + "\" }");
				}
				else return badRequest();
			}
		}
		return ok("{\"error\":\"1\" }");
    }  

    public static Result cityInformationByQuery() 
    {
    	JsonNode parameters = request().body().asJson();
        String query = parameters.get("cityname").asText();
        
    	return ok(Core.getCityByQuery(query));
    }
    
    public static Result submitTimesTraveled() 
    {
    	JsonNode parameters = request().body().asJson();
        String cityname = parameters.get("cityname").asText();
        String timestraveled = parameters.get("timestraveled").asText();
        
    	return ok(Semantic.updateUserDestinationTravelledTDB(session("username"), timestraveled, cityname));
    }
    
    public static Result submitRating() 
    {
    	JsonNode parameters = request().body().asJson();
        String cityname = parameters.get("cityname").asText();
        String rating = parameters.get("rating").asText();
        
    	return ok(Semantic.updateUserDestinationRatingTDB(session("username"), rating, cityname));
    }
    
    public static Result submitReview() 
    {
    	JsonNode parameters = request().body().asJson();
        String cityname = parameters.get("cityname").asText();
        String review = parameters.get("review").asText();
        
    	return ok(Semantic.updateUserDestinationReviewTDB(session("username"), review, cityname));
    }
    
    public static Result search() throws Exception
    {
    	// GET FORM DATA
    	DynamicForm dynamicForm = Form.form().bindFromRequest();
        
    	String destination = dynamicForm.get("destination-city");
    	
    	return redirect(routes.Application.results(destination));
    }
    
    public static Result results(String destination) throws ParseException 
    {
    	if(session("username") != null)
    	{
	        // GET CITY DATA - FIRST CHECK IF CITY ALREADY EXISTS IN TDB
	        City city = Semantic.getCityDetails(destination);
	        if(city == null) {
	        	// CHECK DBPEDIA SERVICE FIRST
	        	if(QueryRunner.isServiceUp()) {
	        		city = CityParser.parse(destination);
	        	}
	        	else {
	        		return redirect(routes.Application.dbpediaoffline());
	        	}
	        }
	        if(city == null) { // NO CITY FOUND FROM TDB AND DBPEDIA
	        	return notFound(notFoundPage.render());
	        }
	        
	        // GET WEATHER INFORMATION -- DON'T CHECK METEO IN TDB, LET'S UPDATE AGAIN
	        Date ArrivalDate = new Date();
	        List<Weather> weatherData = WeatherForecast.getWeatherByLatLongOnDate(city.getLatitude(),city.getLongitude(), ArrivalDate);
	        
	        // GET PHOTOS - FIRST CHECK IF PHOTOS ALREADY EXISTS IN TDB
	        List<Photo> photos = Semantic.getPhotosByCity(destination);
	        if(photos == null) {
	        	photos = PhotoService.getPhotosByLatLong(city.getLatitude(), city.getLongitude());
	        }
	
	        // GET CITY RATING / NB OF VOTES / TIMES TRAVELED / REVIEWS
	        int rating = Semantic.getRatingByCity(city.getName());
	        int nbrating = Semantic.getNumberOfVotesByCity(city.getName());
	        int nbtimes = Semantic.getNumberOfTimesTraveled(city.getName(), session("username"));
	        List<Review> reviews = Semantic.getReviewsByCity(city.getName());
	        
	        // CAN USER VOTE AND COMMENT
	        Boolean canVote = Semantic.canUserVote(city.getName(), session("username"));
	        
	        // UPDATE SEMANTIC
	        Semantic.updateCityAndCountryTDB(city.getName(), city.getOverview(), city.getLatitude(), city.getLongitude(), city.getPopulationTotal(), city.getCountry(), city.getCurrencyCode(), photos);
	        Semantic.updateUserDestinationInterestedTDB(session("username"), city.getName());
	        
	        return ok(results.render(city, ArrivalDate, weatherData, rating, nbrating, canVote, photos, reviews, nbtimes));
    	}
    	return redirect(routes.Application.index(0));
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
    
    public static Result user(String nick)
    {
    	// CHECK IF USER EXISTS
    	if(!Semantic.isUser(nick)) {
    		return notFound(notFoundPage.render());
    	}
    	
    	// GET USER INFO
    	List<Destination> destinations = Semantic.getUserDestinations(nick);
    	
    	return ok(user.render(nick, destinations));
    }
    
    public static Result services()
    {
    	return ok(services.render(Semantic.getListMostTraveledCities(), Semantic.getListMostInteractiveUsers(), Semantic.getListBestRatedCities(), Semantic.getTotalUsers(), Semantic.getNumberOfDestinationsSearched()));
    }
    
    public static Result about()
    {
    	return ok(about.render());
    }
    
    public static Result ontology()
    {
    	return ok(ontology.render());
    }
    
    public static Result ontologyDL(String type)
    {
    	File file = Ontology.generateOntology(type);
    	//response().setContentType("application/x-download");
		return ok(file);
    }
    
    public static Result namespaceprefixes()
    {
    	Map<String,String> NS = Semantic.getNamespacePrefixes();
		return ok(namespaceprefixes.render(NS));
    }
    
    public static Result dbpediaoffline()
    {
		return ok(dbpediaoffline.render());
    	
    }
}
