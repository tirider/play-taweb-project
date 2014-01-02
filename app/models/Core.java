package models;

import play.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;

public class Core {
	public static String readUrl(String urlString) throws Exception {
	    BufferedReader reader = null;
	    try {
	        URL url = new URL(urlString);
	        reader = new BufferedReader(new InputStreamReader(url.openStream()));
	        StringBuffer buffer = new StringBuffer();
	        int read;
	        char[] chars = new char[1024];
	        while ((read = reader.read(chars)) != -1)
	            buffer.append(chars, 0, read); 

	        return buffer.toString();
	    } finally {
	        if (reader != null)
	            reader.close();
	    }
	}
	
	public static String getCityByQuery(String query)
	{
//		String prefixes = "" +
//				"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
//				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +	
//				"PREFIX foaf: <http://xmlns.com/foaf/0.1/> " +
//				"PREFIX dbpedia-owl: <http://dbpedia.org/ontology/> " +
//				"PREFIX dbpprop: <http://dbpedia.org/property/> ";
//		
//		String service_ep = "http://dbpedia.org/sparql";
//		String queryString = 	prefixes +
//								"select distinct ?nomCity ?nomCountry ?nomDep where "
//								+ "{ "
//								+ "{ "
//								+ "?r a <http://dbpedia.org/ontology/Settlement> . "
//								+ "?r rdfs:label ?nomCity . "
//								+ "?r dbpedia-owl:country ?countryResource . "
//								+ "?countryResource rdfs:label ?nomCountry . "
//								+ "FILTER (lang(?nomCity) = \"en\" && lang(?nomCountry) = \"en\" && regex(str(?nomCity), \"^"+ query +"\", \"i\")) "
//								+ "OPTIONAL {?r dbpedia-owl:department ?depResource . ?depResource rdfs:label ?nomDep . FILTER (lang(?nomDep) = \"en\") } "
//								+ "OPTIONAL {?r dbpedia-owl:populationTotal ?pop } "
//								+ "} "
//								+ "UNION "
//								+ "{ "
//								+ "?r a <http://dbpedia.org/ontology/City> . "
//								+ "?r rdfs:label ?nomCity . "
//								+ "?r dbpedia-owl:country ?countryResource . "
//								+ "?countryResource rdfs:label ?nomCountry . "
//								+ "FILTER (lang(?nomCity) = \"en\" && lang(?nomCountry) = \"en\" && regex(str(?nomCity), \"^"+ query +"\", \"i\")) "
//								+ "OPTIONAL {?r dbpedia-owl:department ?depResource . ?depResource rdfs:label ?nomDep . FILTER (lang(?nomDep) = \"en\") } "
//								+ "OPTIONAL {?r dbpedia-owl:populationTotal ?pop } "
//								+ "} "
//								+ "} "
//								+ "ORDER BY DESC(?pop) "
//								+ "LIMIT 10";
//		
//		QueryExecution qexec = QueryExecutionFactory.sparqlService(service_ep, queryString);
//		ResultSet results = qexec.execSelect() ;
		
//		String departmentNameStr = "";
//		
//		for ( ; results.hasNext() ; )
//		{
//			departmentNameStr = "";
//			
//			QuerySolution qsolution = results.nextSolution() ;
//
//			Literal resultCityName = qsolution.getLiteral("nomCity");
//		    String cityName = resultCityName.getString();
//
//		    String departmentName = "";
//		    if(qsolution.contains("nomDep")) {
//		    	Literal resultDepartmentName = qsolution.getLiteral("nomDep");
//		    	departmentName = resultDepartmentName.getString();
//		    }
//
//		    Literal resultCountryName = qsolution.getLiteral("nomCountry");
//		    String countryName = resultCountryName.getString();
//		    
//		    if(departmentName != "") departmentNameStr = departmentName + ", ";
//
//		    
		
		String resultsStr = "{\"cities\":[";
		
		String cityName = "";
		try {
            SAXBuilder sxb = new SAXBuilder();
            URL url = new URL("http://lookup.dbpedia.org/api/search/KeywordSearch?QueryClass=Settlement&MaxHits=10&QueryString=" + query);
            Namespace ns = Namespace.getNamespace("http://lookup.dbpedia.org/");
            Document document = sxb.build(url);
            Element racine = document.getRootElement();
            List<Element> results = racine.getChildren("Result", ns);
            int size = results.size();
            for (Element element : results) {
            	cityName = element.getChildText("Label", ns);
            	resultsStr += "{\"value\" : \"" + cityName + "\",";
    		    resultsStr += "\"name\" : \"" + cityName + getCountryByCityName(cityName) + "\"}";
            	//resultsStr += "\"name\" : \"" + cityName + "\"}";
    		    if (--size > 0) {
    		    	resultsStr += ", ";
    		    }
    		}
    		resultsStr += "]}";
        } catch (Exception ex) {
        	return null;
        }
		return resultsStr;
	}
	
	public static String getCountryByCityName(String cityName)
	{
		String prefixes = "" +
		"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
		"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +	
		"PREFIX foaf: <http://xmlns.com/foaf/0.1/> " +
		"PREFIX dbpedia-owl: <http://dbpedia.org/ontology/> " +
		"PREFIX dbpprop: <http://dbpedia.org/property/> ";

		String service_ep = "http://dbpedia.org/sparql";
		String queryString = 	prefixes
								+ "SELECT ?countryName "
								+ "WHERE "
								+ "{ "
								+ "{ "
								+ "?cityResource rdf:type dbpedia-owl:Settlement. "
								+ "?cityResource rdfs:label \"" + cityName + "\"@en. "
								+ "?cityResource  dbpedia-owl:country ?countryResource. "
								+ "?countryResource  rdfs:label ?countryName. "
								+ "FILTER (lang(?countryName)= 'en')"
								+ "} "
								+ "UNION "
								+ "{ "
								+ "?cityResource rdf:type dbpedia-owl:City. "
								+ "?cityResource rdfs:label \"" + cityName + "\"@en. "
								+ "?cityResource  dbpedia-owl:country ?countryResource. "
								+ "?countryResource  rdfs:label ?countryName. "
								+ "FILTER (lang(?countryName)= 'en')"
								+ "} "
								+ "} "
								+ "LIMIT 1";

		QueryExecution qexec = QueryExecutionFactory.sparqlService(service_ep, queryString);
		ResultSet results = qexec.execSelect() ;
		String countryName = "";
		for ( ; results.hasNext() ; )
		{
			countryName = "";
			QuerySolution qsolution = results.nextSolution() ;
		    if(qsolution.contains("countryName")) {
		    	Literal resultDepartmentName = qsolution.getLiteral("countryName");
		    	countryName = ", " + resultDepartmentName.getString();
		    }
		}

		return countryName;
	}
	
	public static int parseInt(String string) {
		try {
			return Integer.parseInt(string);
		}
		catch(NumberFormatException e) {
			return 0;
		}
	}
}
