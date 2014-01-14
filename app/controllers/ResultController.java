package controllers;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

import play.api.mvc.*;
import models.beans.City;
import models.beans.Destination;
import models.beans.Photo;
import models.beans.Review;
import models.beans.Weather;
import models.global.Core;
import models.semantic.Semantic;
import models.service.CityParser;
import models.service.PhotoService;
import models.service.WeatherForecast;

import com.fasterxml.jackson.databind.JsonNode;

import play.data.DynamicForm;
import play.data.Form;
import play.mvc.Controller;
import controllers.ResultController;
import play.mvc.Result;
import views.html.notFoundPage;
import views.html.results;
import views.html.user;

public class ResultController extends Controller 
{
	/**
	 * 
	 * @return
	 * @throws Exception
	 */
    public static Result search() throws Exception
    {
    	// GET FORM DATA
    	DynamicForm dynamicForm = Form.form().bindFromRequest();
        
    	String destination = dynamicForm.get("destination-city");
    	
    	return redirect(routes.ResultController.results(destination));
    }
    
	/**
	 * 
	 * @param destination
	 * @return
	 * @throws ParseException
	 */
    public static Result results(String destination) throws ParseException 
    {
    	if(session("username") != null)
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
    
    /**
     * 
     * @return
     */
    public static Result cityInformationByQuery() 
    {
    	JsonNode parameters = request().body().asJson();
        String query = parameters.get("cityname").asText();
        
    	return ok(Core.getCityByQuery(query));
    }
    
    /***
     * 
     * @return
     */
    public static Result submitTimesTraveled() 
    {
    	JsonNode parameters = request().body().asJson();
        String cityname = parameters.get("cityname").asText();
        String timestraveled = parameters.get("timestraveled").asText();
        
    	return ok(Semantic.updateUserDestinationTravelledTDB(session("username"), timestraveled, cityname));
    }
    
    /**
     * 
     * @return
     */
    public static Result submitRating() 
    {
    	JsonNode parameters = request().body().asJson();
        String cityname = parameters.get("cityname").asText();
        String rating = parameters.get("rating").asText();
        
    	return ok(Semantic.updateUserDestinationRatingTDB(session("username"), rating, cityname));
    }
    
    /**
     * 
     * @return
     */
    public static Result submitReview() 
    {
    	JsonNode parameters = request().body().asJson();
        String cityname = parameters.get("cityname").asText();
        String review = parameters.get("review").asText();
        
    	return ok(Semantic.updateUserDestinationReviewTDB(session("username"), review, cityname));
    }    
    
    /**
     * 
     * @param nick
     * @return
     */
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
}
