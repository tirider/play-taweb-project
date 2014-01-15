package controllers;

import models.semantic.Semantic;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.services;

public class ServiceController extends Controller
{
	/**
	 * This method provide access to the services page.
	 * @return
	 */
    public static Result services()
    {
    	// GET SEMANTIC INFORMATION
    	String mostTraveledCities = Semantic.getListMostTraveledCities();
    	System.out.println("t tht "+mostTraveledCities);
    	
    	String mostInteractiveUsers = Semantic.getListMostInteractiveUsers();
    	System.out.println("t tht "+mostInteractiveUsers);
    	
    	String bestRatedCities = Semantic.getListBestRatedCities();
    	System.out.println("t tht "+bestRatedCities);
    	
    	String totalUsers = Semantic.getTotalUsers();
    	System.out.println("t tht "+totalUsers);
    	
    	String numberOfDestinationsSearched = Semantic.getNumberOfDestinationsSearched();
    	System.out.println("t tht "+numberOfDestinationsSearched);
    	
    	// CALLS THE CONCERNED FUNCTION / PAGE
    	return ok(services.render(mostTraveledCities, mostInteractiveUsers, bestRatedCities, totalUsers, numberOfDestinationsSearched));
    }
}
