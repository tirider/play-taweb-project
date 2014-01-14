package controllers;

import java.io.ByteArrayOutputStream;
import java.util.Map;

import models.global.Core;
import models.semantic.Semantic;
import models.semantic.SparqlEndpoint;

import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;

import play.data.DynamicForm;
import play.data.Form;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.namespaceprefixes;
import views.html.sparql;
import views.html.sparqlresults;

public class EndpointsController extends Controller
{
	/**
	 * Retrie access to the SPARQL EndPoints page.
	 * @return
	 */
    public static Result sparql() 
    {
    	return ok(sparql.render());
    }

    /**
     * This method handle sparql request. 
     * @return
     */
    public static Result sparqlresults()
    {
    	DynamicForm dynamicForm = Form.form().bindFromRequest();
    	String query = dynamicForm.get("query");
    	String format = null;
    	format = dynamicForm.get("format");
    	
    	System.out.printf(String.valueOf(query), String.valueOf(format));
    	
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

    /**
     * This method retrieve the namespaces from the system ontology.
     * @return
     */
    public static Result namespaceprefixes()
    {
    	Map<String,String> NS = Semantic.getNamespacePrefixes();
		return ok(namespaceprefixes.render(NS));
    }    
}
