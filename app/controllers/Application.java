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
import models.semantic.Ontology;
import models.semantic.SparqlEndpoint;
import models.semantic.Semantic;
import play.Play;
import play.data.DynamicForm;
import play.data.Form;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.index;
import views.html.notFoundPage;
import views.html.results;
import views.html.cityInformationByQuery;
import views.html.sparql;
import views.html.sparqlresults;
import views.html.services;
import views.html.user;
import views.html.about;
import views.html.ontology;
import views.html.namespaceprefixes;

public class Application extends Controller 
{
    public static Result index() 
    {
        return ok( index.render(Semantic.getMostInterestedCities()) );
    }

    // LOGIN USER
	public static Result login() 
    {
    	DynamicForm loginForm = Form.form().bindFromRequest();
    	String email = loginForm.get("login-email");
        String password = loginForm.get("login-password");
        
        IUserDAO userDAO = new UserDAO(new DAOFactory().createMongodbConnection());
        if(userDAO.exists(email, password))
        {
        	String user = session("connected");
        	if(user == null) 
        	{
        		User usr = userDAO.find(email);
        		session("connected", usr.getName());
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
        		session("connected", name);
        		Semantic.insertUserTDB(name, email);
        		return redirect(controllers.routes.Application.index());
        	}
    	}
    			
		return ok("You are not logged");
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
        
    	return ok(Semantic.updateUserDestinationTravelledTDB(session("connected"), timestraveled, cityname));
    }
    
    public static Result submitRating() 
    {
    	JsonNode parameters = request().body().asJson();
        String cityname = parameters.get("cityname").asText();
        String rating = parameters.get("rating").asText();
        
    	return ok(Semantic.updateUserDestinationRatingTDB(session("connected"), rating, cityname));
    }
    
    public static Result submitReview() 
    {
    	JsonNode parameters = request().body().asJson();
        String cityname = parameters.get("cityname").asText();
        String review = parameters.get("review").asText();
        
    	return ok(Semantic.updateUserDestinationReviewTDB(session("connected"), review, cityname));
    }
    
    public static Result search() throws Exception
    {
    	// GET FORM DATA
    	DynamicForm dynamicForm = Form.form().bindFromRequest();
        
    	String destination = dynamicForm.get("destination-city");
    	String arrivalDateStr = dynamicForm.get("search-date");
    	
    	return redirect(routes.Application.results(destination));
    }
    
    public static Result results(String destination) throws ParseException 
    {
        // GET CITY DATA - FIRST CHECK IF CITY ALREADY EXISTS IN TDB
        City city = Semantic.getCityDetails(destination);
        if(city == null) {
        	city = CityParser.parse(destination);
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

        // GET CITY RATING / NB OF VOTES / REVIEWS
        int rating = Semantic.getRatingByCity(city.getName());
        int nbrating = Semantic.getNumberOfVotesByCity(city.getName());
        List<Review> reviews = Semantic.getReviewsByCity(city.getName());
        
        // CAN USER VOTE AND COMMENT
        Boolean canVote = Semantic.canUserVote(city.getName(), session("connected"));
        
        // UPDATE SEMANTIC
        Semantic.updateCityAndCountryTDB(city.getName(), city.getOverview(), city.getLatitude(), city.getLongitude(), city.getPopulationTotal(), city.getCountry(), city.getCurrencyCode(), photos);
        Semantic.updateUserDestinationInterestedTDB(session("connected"), city.getName());
        
        return ok(results.render(city, ArrivalDate, weatherData, rating, nbrating, canVote, photos, reviews));
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
    	return ok(services.render(Semantic.getMostInterestedCities(), Semantic.getTotalUsers()));
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
}