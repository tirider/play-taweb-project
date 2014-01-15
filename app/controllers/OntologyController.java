package controllers;

import java.io.File;

import models.semantic.Ontology;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.ontology;

public class OntologyController extends Controller
{
	/**
	 * This method provide access to the related ontology page.
	 * @return
	 */
    public static Result ontology()
    {
    	return ok(ontology.render());
    }
    
    /**
     * This method give you possibility to download the complete traveler's ontology.
     * @param type
     * @return
     */
    public static Result ontologyDL(String type)
    {
    	if(!type.equals("") && type != null)
    	{
    		if(type.toUpperCase().equals("RDF") || type.toUpperCase().equals("N3"))
    		{
	    		File file = Ontology.generateOntology(type);
	    		//response().setContentType("application/x-download");
	    		return ok(file);
    		}
    		else return redirect(routes.OntologyController.ontology());
    	}
    	else return redirect(routes.OntologyController.ontology());
    }
}
