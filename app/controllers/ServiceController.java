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
    	return ok(services.render(Semantic.getListMostTraveledCities(), Semantic.getListMostInteractiveUsers(), Semantic.getListBestRatedCities(), Semantic.getTotalUsers(), Semantic.getNumberOfDestinationsSearched()));
    }
}
