package models.query;

import java.util.StringTokenizer;

import models.beans.City;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.sparql.engine.http.QueryExceptionHTTP;

public class QueryRunner 
{	
	private static final String SERVICE = "http://dbpedia.org/sparql";
	
	private static final String FIELD1  = "cityAbstract";
	private static final String FIELD2  = "cityLat";
	private static final String FIELD3  = "cityLong";
	private static final String FIELD4  = "cityPopulationTotal";
	private static final String FIELD5  = "countryName";
	private static final String FIELD6  = "currencyCode";
	
	/**
	 * This method checks whether dbpedia service is running up.
	 * @return
	 */
	public static boolean isServiceUp()
	{
		String query = "ASK { }";
		QueryExecution qexec = QueryExecutionFactory.sparqlService(SERVICE, query);
		
		try 
		{
			return qexec.execAsk() == true;
        } 
		catch (QueryExceptionHTTP e) 
		{
        	System.err.println("Sorry, dbpedia service is not working rigth now...");
        } 
		finally 
		{
        	qexec.close();
        }
		
		return false;
	}
	
	/**
	 * This method check if the given city exists
	 * @param queryString
	 * @param cityName
	 * @return
	 */
	public static boolean exists(String queryString, String cityName)
	{
		String query = String.format(queryString, cityName);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(SERVICE, query);
		
		return qexec.execAsk();
	}
	
	/**
	 * Usuful for query execution
	 * @param queryString
	 * @param cityName
	 * @return
	 */
	public static City execute(String queryString, String cityName)
	{
		City city = null;
		
		String query = String.format(queryString, cityName);
		
		QueryExecution qexec = QueryExecutionFactory.sparqlService(SERVICE, query);
		ResultSet results = qexec.execSelect() ;

		for ( ; results.hasNext() ; )
		{
			city = new City();
			
			QuerySolution qsolution = results.nextSolution() ;
		    
			Literal result = qsolution.getLiteral(FIELD1) ;
		    String cityAbstract = result.getString();	
		    
			result = qsolution.getLiteral(FIELD2) ;
		    String cityLat = result.getString();
		    
			result = qsolution.getLiteral(FIELD3) ;
		    String cityLong = result.getString();		    

			result = qsolution.getLiteral(FIELD4) ;
		    String cityPopulationTotal = result.getString();	
		    
			result = qsolution.getLiteral(FIELD5) ;
		    String countryName = result.getString();
		    
			result = qsolution.getLiteral(FIELD6) ;
		    String currencyCode = result.getString();
		    
		    city.setName(cityName);
		    city.setOverview(cityAbstract);
		    city.setLatitude(cityLat);
		    city.setLogitude(cityLong);
		    city.setPopulationTotal(cityPopulationTotal);
		    city.setCountry(countryName);
		    
		    // Remove additional information on currency
		    if(currencyCode.indexOf(",") >= 0) 
		    {
				StringTokenizer st = new StringTokenizer(currencyCode, ",");
				currencyCode = st.nextToken();
			}
		    
		    city.setcurrencyCode(currencyCode);
		}
		
		return  city;
	}	
}

