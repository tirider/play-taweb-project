package controllers;


import models.global.Core;
import models.semantic.Semantic;
import com.fasterxml.jackson.databind.JsonNode;
import play.mvc.Controller;
import play.mvc.Result;

public class AjaxController extends Controller 
{
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
    
}
