package controllers;

import java.io.File;

import models.semantic.Ontology;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.ontology;

public class OntologyController extends Controller
{
	/**
	 * 
	 * @return
	 */
    public static Result ontology()
    {
    	return ok(ontology.render());
    }
    
    /**
     * 
     * @param type
     * @return
     */
    public static Result ontologyDL(String type)
    {
    	File file = Ontology.generateOntology(type);
    	//response().setContentType("application/x-download");
		return ok(file);
    }
}
