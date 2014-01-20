package controllers;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

import models.beans.City;
import models.beans.Destination;
import models.beans.Photo;
import models.beans.Review;
import models.beans.Weather;
import models.query.QueryRunner;
import models.semantic.Semantic;
import models.service.DBPediaService;
import models.service.PhotoService;
import models.service.WeatherService;
import play.data.DynamicForm;
import play.data.Form;
import play.mvc.Controller;
import controllers.ResultController;
import play.mvc.Result;
import views.html.notFoundPage;
import views.html.results;
import views.html.user;
import views.html.dbpediaoffline;

public class ResultController extends Controller 
{
	/**
	 * 
	 * @return
	 * @throws Exception
	 */
    public static Result search() throws Exception
    {
    	// GET DATA FROM CLIENT FORM
    	DynamicForm dynamicForm = Form.form().bindFromRequest();
        
    	String destination = dynamicForm.get("destination-city");
    	
    	return redirect(routes.ResultController.results(destination));
    }
    
	/**
	 * This method 
	 * @param destination
	 * @return
	 * @throws ParseException
	 */
    public static Result results(String destination) throws ParseException 
    {
    	if(session("username") != null)
    	{
	        // CITY INFORMATION -- FIRST CHECK IF CITY ALREADY EXISTS IN TDB 
	        City city = Semantic.getCityDetailsFromTDB(destination);
	        
	        if(city == null) 
	        {
	        	// TESTS WHETHER DBPEDIA SERVICE IS RUNNING
	        	if(QueryRunner.isServiceUp()) { city = DBPediaService.parse(destination); }
	        	
	        	// OTHERWISE THE CITY WAS NOT FOUND ON DBPEDIA
	        	else { redirect(routes.ResultController.dbpediaoffline()); }
	        }
	        
	        // IF NO CITY FOUND FROM TDB AND DBPEDIA -- THEN 404
	        if(city == null) { return notFound(notFoundPage.render()); }
	        
	        // WEATHER INFORMATION -- DON'T CHECK METEO IN TDB, LET'S UPDATE AGAIN
	        List<Weather> weather = WeatherService.getWeatherByLatLongOnDate(city.getLatitude(),city.getLongitude());
	        
	        // PHOTOS INFORMATION - FIRST CHECK IF PHOTOS ALREADY EXISTS IN TDB
	        List<Photo> photos = Semantic.getPhotosByCity(destination);
	        
	        // CHECKS FOR PHOTOS IN http://api.flickr.com
	        if(photos == null) { photos = PhotoService.getPhotosByLatLong(city.getLatitude(), city.getLongitude()); }
	
	        // GET CITY RATING / NB OF VOTES / TIMES TRAVELED / REVIEWS
	        int rating = Semantic.getRatingByCity(city.getName());
	        int nbrating = Semantic.getNumberOfVotesByCity(city.getName());
	        int nbtimes = Semantic.getNumberOfTimesTraveled(city.getName(), session("username"));
	        
	        // ABOUT COMMENTS (REVIEW, OPPINION)
	        List<Review> reviews = Semantic.getReviewsByCity(city.getName());
	        
	        // CAN USER VOTE AND COMMENT
	        Boolean canVote = Semantic.canUserVote(city.getName(), session("username"));
	        
	        //  ADD NEW INFORMATION FROM SERVICES TO TDB -- UPDATE SEMANTIC
	        Semantic.updateCityAndCountryTDB(city);
	        Semantic.updateDestinationPhotos(photos, city.getName());
	        Semantic.updateUserDestinationInterestedTDB(session("username"), city.getName());
	        Semantic.updateWeatherForecastTDB(weather, city.getName());
	        
	        // CALLS THE ASSIATED FUNCTION VIEW
	        return ok(results.render(city, new Date(), weather, rating, nbrating, canVote, photos, reviews, nbtimes));
    	}
    	else return redirect(routes.Application.index(0));
    }
    
    /**
     * 
     * @param nick
     * @return
     */
    public static Result user(String nick)
    {
    	// CHECK IF USER EXISTS
    	if(!Semantic.isUser(nick)) 
    	{
    		return notFound(notFoundPage.render());
    	}
    	
    	// GET USER INFO
    	List<Destination> destinations = Semantic.getUserDestinations(nick);
    	
    	return ok(user.render(nick, destinations));
    }
    
    public static Result dbpediaoffline()
    {
		return ok(dbpediaoffline.render());
    }
}
