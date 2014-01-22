package models.semantic;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import models.beans.City;
import models.beans.Destination;
import models.beans.Photo;
import models.beans.Review;
import models.beans.Weather;
import models.global.Core;
import models.query.QueryRunner;
import models.semantic.SparqlEndpoint;
import models.service.DBPediaService;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class Semantic {
	
	private static final String rdf = RDF.getURI();
	private static final String rdfs = RDFS.getURI();
	private static final String foaf = FOAF.getURI();
	private static final String dc = DC.getURI();
	private static final String met = "http://purl.org/ns/meteo#";
	private static final String geo = "http://www.w3.org/2003/01/geo/wgs84_pos#";
	private static final String dbpedia = "http://dbpedia.org/resource/";
	private static final String dbpediaowl = "http://dbpedia.org/ontology/";
	private static final String dbpprop = "http://dbpedia.org/property/";
	private static final String rev = "http://purl.org/stuff/rev#";
	private static final String trvl = "http://4travelers.org/";
	private static final String trvlowl = "http://4travelers.org/ontology/";
	
	/**
	 * Get unique dataset
	 * @return
	 */
	public static Dataset getDataset() 
	{
		// GET SINGLETON INSTANCE
		TDBDataset instance = new TDBDataset();
		
		return instance.getDataset();
	}
	
	/**
	 * Closes dataset
	 * @param dataset
	 */
	public static void closeDataset(Dataset dataset) {
		dataset.end();
	}
	
	/**
	 * Return TDB Model
	 * @return
	 */
	public static Model getTDBModel()
	{
		// GET DATASET AND MODEL
        Model taweb = getDataset().getNamedModel("taweb");
        
        taweb.setNsPrefix("rdf", rdf);
        taweb.setNsPrefix("rdfs", rdfs);
        taweb.setNsPrefix("foaf", foaf);
        taweb.setNsPrefix("dc", dc);
        taweb.setNsPrefix("met", met);
        taweb.setNsPrefix("geo", geo);
        taweb.setNsPrefix("dbpedia", dbpedia);
        taweb.setNsPrefix("dbpedia-owl", dbpediaowl);
        taweb.setNsPrefix("dbpprop", dbpprop);
        taweb.setNsPrefix("rev", rev);
        taweb.setNsPrefix("trvl", trvl);
        taweb.setNsPrefix("trvl-owl", trvlowl);
		
        // RETRIEVE THE NEWEST SYSTEM MODEL
        return taweb;
	}
	
	/**
	 * Returns all namespaces used on model
	 * @return
	 */
	public static Map<String,String> getNamespacePrefixes()
	{
		Model taweb = getTDBModel();
		
		Map<String, String> NS = taweb.getNsPrefixMap();
		
		taweb.close();
        Dataset dataset = getDataset();
        closeDataset(dataset);
        
        return NS;
	}

	/**
	 * When user is registered is also registered to TDB without email
	 * @param nick
	 * @param email
	 */
	public static void insertUserTDB(String nick, String email, String cityname) 
	{
		Model taweb = getTDBModel();
		
		City city = getCityDetailsFromTDB(cityname);
        if(city == null) {
        	if(QueryRunner.isServiceUp()) {
        		city = DBPediaService.parse(cityname);
        	}
        }
        
        updateCityAndCountryTDB(city);
        Resource CityR = taweb.getResource(trvl + cityname);

        Resource Person = taweb.getResource(trvl + "user/" + nick);
        if(!taweb.containsResource(Person))
        {
	    	// CREATE USER RESSOURCE
        	Person = taweb.createResource(trvl + "user/" + nick);
			Person.addProperty(FOAF.nick, nick);
			Person.addProperty(FOAF.based_near, CityR);
			taweb.add(Person, RDF.type, FOAF.Person);
        }
        
        // CLOSE THE CURRENT MODEL
        taweb.close();
        closeDataset(getDataset());
	}
	
	/**
	 * Updates destination and country information if needed.
	 * @param cityname
	 * @param description
	 * @param latitude
	 * @param longitude
	 * @param population
	 * @param countryname
	 * @param currencyCodeStr
	 * @param photoList
	 */
	public static void updateCityAndCountryTDB(City city) 
	{
		Model model = getTDBModel();
		
		String latitudeStr  = String.valueOf(city.getLatitude());
		String longitudeStr = String.valueOf(city.getLongitude());
		String cityname     = city.getName();
		String countryname  = city.getCountry();
		
		// CREATE REQUIRED PROPERTIES
    	Property populationProp = model.createProperty(dbpediaowl + "populationTotal");
		Property country = model.createProperty(dbpediaowl + "country");
		Property latitudeProp = model.createProperty(geo + "lat");
		Property longitudeProp = model.createProperty(geo + "lon");
		Property latlong = model.createProperty(geo + "lat_long");
		Property currencyCode = model.createProperty(dbpprop + "currencyCode");
		
		// CHECK IF DESTINATION RESOURCE EXISTS
        Resource DestinationR = model.getResource(trvl + cityname);
        if(!model.containsResource(DestinationR))
        {
        	Resource Destination = model.createResource(trvl + "Destination");
        	DestinationR = model.createResource(trvl + cityname);
        	DestinationR.addProperty(DC.description, city.getOverview());
        	
        	if(String.valueOf(city.getPopulationTotal()) != null || String.valueOf(city.getPopulationTotal()) != "0" || String.valueOf(city.getPopulationTotal()) != "") {
        		DestinationR.addProperty(populationProp, String.valueOf(city.getPopulationTotal()));
        	}
        	DestinationR.addProperty(RDFS.label, cityname);
        	DestinationR.addProperty(latitudeProp, latitudeStr);
        	DestinationR.addProperty(longitudeProp, longitudeStr);
        	DestinationR.addProperty(latlong, latitudeStr + "," + longitudeStr);
        	DestinationR.addProperty(RDF.type, Destination);
        }

		// CREATE COUNTRY RESSOURCE
		Resource CountryName = model.getResource(dbpedia + countryname);
        if(!model.containsResource(CountryName))
        {
        	CountryName = model.createResource(dbpedia + countryname);
        	Resource CountryR = model.createResource(dbpediaowl + "Country");
    		model.add(CountryName, RDF.type, CountryR);
    		CountryName.addProperty(RDFS.label, countryname);
    		CountryName.addProperty(currencyCode, city.getCurrencyCode());
        }

        DestinationR.addProperty(country, CountryName);
		
		model.close();
		Dataset dataset = getDataset();
        closeDataset(dataset);
	}

	/**
	 * Add related images to destination
	 * @param photoList
	 * @param cityname
	 */
	public static void updateDestinationPhotos(List<Photo> photoList, String cityname) 
	{
		Model model = getTDBModel();
		
		Resource DestinationR = model.getResource(trvl + cityname);
		if(photoList.size() > 0) 
		{
			for(Photo ph : photoList) 
			{
				// RETRIEVE IMG URL STRING
				String imgUrl = ph.getImgLargeUrl();
				
				// ADD IN TDB GRAPH
				DestinationR.addProperty(FOAF.img, imgUrl);
			}
        }
		
		model.close();
		Dataset dataset = getDataset();
        closeDataset(dataset);
	}
	
	/**
	 * Updates user interest for a destination. The information timesIterested is implicit for the user. However it can be user from the SPARQL EndPoind.
	 * @param nick
	 * @param cityname
	 */
	public static void updateUserDestinationInterestedTDB(String nick, String cityname) 
	{
		Model model = getTDBModel();

		Property toProp = model.createProperty(trvl + "to");
        Property timesInterestedProp = model.createProperty(trvl + "timesInterested");
        Resource Destination = model.getResource(trvl + cityname);

        // CHECK IF USER DESTINATION RESOURCE EXISTS
        Resource userDestination = model.getResource(trvl + "user/" + nick + "#" + cityname);
        if(!model.containsResource(userDestination))
        {
        	userDestination = model.createResource(trvl + "user/" + nick + "#" + cityname);
        	
        	userDestination.addProperty(timesInterestedProp, "1");
        	
        	Resource userDestinationR = model.createResource(trvl + "PersonDestination");
        	userDestination.addProperty(RDF.type, userDestinationR);
        }
        else
        {
        	// INCREASE TIMES INTERESTED
        	Statement stmt = userDestination.getProperty(timesInterestedProp);
        	int timesInterestedInt = Core.parseInt(stmt.getLiteral().toString());
        	timesInterestedInt = timesInterestedInt + 1;
        	userDestination.removeAll(timesInterestedProp);
        	userDestination.addProperty(timesInterestedProp, String.valueOf(timesInterestedInt));
        }

        Property destinationProp = model.createProperty(trvl + "destination");
        Resource Person = model.getResource(trvl + "user/" + nick);
        
        Person.addProperty(toProp, userDestination);
        userDestination.addProperty(destinationProp, Destination);
        
        model.close();
        Dataset dataset = getDataset();
        closeDataset(dataset);
	}
	
	/**
	 * Updates how many times a user has travelled to a destination
	 * @param nick
	 * @param timesTraveled
	 * @param cityname
	 * @return
	 */
	public static String updateUserDestinationTravelledTDB(String nick, String timesTraveled, String cityname)
	{
		Model taweb = getTDBModel();
        
		Property timesTraveledProp = taweb.createProperty(trvl + "timesTraveled");
		
		Resource userDestination = taweb.getResource(trvl + "user/" + nick + "#" + cityname);
		userDestination.removeAll(timesTraveledProp);
    	userDestination.addProperty(timesTraveledProp, timesTraveled);
    	
    	taweb.close();
    	Dataset dataset = getDataset();
        closeDataset(dataset);
    	
    	return "{\"d\":\"1\"}";
	}
	
	/**
	 * Updates rating for a destination. User can change his rating.
	 * @param nick
	 * @param rating
	 * @param cityname
	 * @return
	 */
	public static String updateUserDestinationRatingTDB(String nick, String rating, String cityname)
	{
		Model taweb = getTDBModel();
        
		Property ratingProp = taweb.createProperty(rev + "rating");
		
		Resource userDestination = taweb.getResource(trvl + "user/" + nick + "#" + cityname);
		userDestination.removeAll(ratingProp);
    	userDestination.addProperty(ratingProp, rating);
    	
    	int ratingPercentage = getRatingByCity(cityname);
    	int numberOfVotes = getNumberOfVotesByCity(cityname);
    	
    	taweb.close();
    	Dataset dataset = getDataset();
        closeDataset(dataset);
    	
    	return "{\"rating\":\"" + ratingPercentage + "\", \"numberOfVotes\":\"" + numberOfVotes + "\"}";
	}
	
	/**
	 * Inserts new review.
	 * @param nick
	 * @param review
	 * @param cityname
	 * @return
	 */
	public static String updateUserDestinationReviewTDB(String nick, String review, String cityname)
	{
		Model taweb = getTDBModel();
		Date date = Core.getDate();
		String dateStr = Core.convertDateForURIs(date);

		Resource Destination = taweb.getResource(trvl + cityname);
		Resource Person = taweb.getResource(trvl + "user/" + nick);
		Resource userDestination = taweb.getResource(trvl + "user/" + nick + "#" + cityname);
		Resource userReviewR = taweb.createResource(trvl + "user/" + nick + "#" + cityname + "_review_date" + dateStr);
		Resource reviewR = taweb.createResource(rev + "Review");
		userReviewR.addProperty(RDF.type, reviewR);
		
		Property reviewProp = taweb.createProperty(trvl + "review");
		Property destinationProp = taweb.createProperty(trvl + "destination");
		Property reviewerProp = taweb.createProperty(rev + "reviewer");
		Property textProp = taweb.createProperty(rev + "text");
		
		userDestination.addProperty(reviewProp, userReviewR);

		userReviewR.addProperty(textProp, review);
		userReviewR.addProperty(DC.date, Core.getDateForRDF(date));
		userReviewR.addProperty(reviewerProp, Person);
		userReviewR.addProperty(destinationProp, Destination);
		
		taweb.close();
		Dataset dataset = getDataset();
        closeDataset(dataset);
    	
    	return "{\"date\":\"" + Core.formatDate(date) + "\", \"nick\":\"" + nick + "\"}";
	}
	
	/**
	 * Update weather forecast to TDB
	 * @param weather
	 * @param cityname
	 */
	public static void updateWeatherForecastTDB(List<Weather> weather, String cityname)
	{
		if(!weather.isEmpty()) 
		{
			Model model = getTDBModel();
			
			Property temperatureMinProp = model.createProperty(met + "celcius");
			Property temperatureMaxProp = model.createProperty(met + "celcius");
			Property iconProp = model.createProperty(met + "category");
			Property forecastProp = model.createProperty(met + "forecast");
			Resource DestinationR = model.getResource(trvl + cityname);
			
			for(Weather w : weather)
			{
				Resource ForecastCityR = model.getResource(trvl + "meteo/" + cityname + "#" + Core.convertDateForURIs(w.date));
				if(!model.containsResource(ForecastCityR)) {
					ForecastCityR = model.createResource(trvl + "meteo/" + cityname + "#" + Core.convertDateForURIs(w.date));
					Resource ForecastR = model.getResource(met + "Forecast");
					ForecastCityR.addProperty(RDF.type, ForecastR);
					DestinationR.addProperty(forecastProp, ForecastCityR);
					ForecastCityR.addProperty(DC.description, w.description);
					ForecastCityR.addProperty(DC.date, Core.getDateForRDF(w.date));
					ForecastCityR.addProperty(iconProp, w.icon);
					ForecastCityR.addProperty(temperatureMinProp, String.valueOf(w.temperatureMin));
					ForecastCityR.addProperty(temperatureMaxProp, String.valueOf(w.temperatureMax));
				}
			}
			
			model.close();
			Dataset dataset = getDataset();
	        closeDataset(dataset);
		}
	}
	
	/**
	 * Return average rating by city name given.
	 * @param cityname
	 * @return
	 */
	public static int getRatingByCity(String cityname)
	{
		String query = "SELECT (AVG(xsd:integer(?o)) AS ?avg) " +
				       "WHERE { "
						+ "?s rdf:type trvl:PersonDestination  ."
						+ "?s rev:rating ?o ."
						+ "?s trvl:destination ?destinationR ."
						+ "?destinationR rdfs:label \"" + cityname + "\" "
						+ "}";

		ResultSet results = SparqlEndpoint.queryData(query);
		
		int resultStr = 0;
		
		for ( ; results.hasNext() ; )
		{
			QuerySolution qsolution = results.nextSolution();

			double resultAvg = qsolution.getLiteral("avg").getDouble();
			
			resultStr = (int) Math.round(((resultAvg * 100)/5));
		}
        
    	return resultStr;
	}
	
	/**
	 * Return the number of votes by city name given.
	 * @param cityname
	 * @return
	 */
	public static int getNumberOfVotesByCity(String cityname)
	{
		String query = "SELECT (COUNT(xsd:integer(?o)) AS ?count) " +
				       "WHERE { "
						+ "?s rdf:type trvl:PersonDestination  ."
						+ "?s rev:rating ?o ."
						+ "?s trvl:destination ?destinationR ."
						+ "?destinationR rdfs:label \"" + cityname + "\" "
						+ "}";
		
		ResultSet results = SparqlEndpoint.queryData(query);
		
		int resultStr = 0;
		
		for ( ; results.hasNext() ; )
		{
			QuerySolution qsolution = results.nextSolution();

			int resultCount = qsolution.getLiteral("count").getInt();
			resultStr = resultCount;
		}
        
    	return resultStr;
	}
	
	/**
	 * Returns how many times a user traveled to a destination
	 * @param cityname
	 * @param nick
	 * @return
	 */
	public static int getNumberOfTimesTraveled(String cityname, String nick)
	{
		String query = "SELECT ?o " +
				 "WHERE { "
				+ "?userResource rdf:type foaf:Person ."
				+ "?userResource foaf:nick \"" + nick + "\" ."
				+ "?userResource trvl:to ?userDestinationResource ."
				+ "?userDestinationResource trvl:timesTraveled ?o ."
				+ "?userDestinationResource trvl:destination ?destinationR ."
				+ "?destinationR rdfs:label \"" + cityname + "\" "
				+ "}";

		ResultSet results = SparqlEndpoint.queryData(query);
		
		int resultStr = 0;
		
		for ( ; results.hasNext() ; )
		{
			QuerySolution qsolution = results.nextSolution();
		
			int resultCount = qsolution.getLiteral("o").getInt();
			resultStr = resultCount;
		}
		
		return resultStr;
	}
	
	/**
	 * Return a list of reviews by city name.
	 * @param cityname
	 * @return
	 */
	public static List<Review> getReviewsByCity(String cityname)
	{
		Model model = getTDBModel();
		List<Review> reviews = null;
		
		Resource resourceCity = model.getResource(trvl + cityname);
		
		if(model.containsResource(resourceCity)) 
		{
			reviews = new ArrayList<Review>();
			
			String query = "SELECT ?text ?date ?nick " +
					"WHERE { "
    				+ "?reviewResource rdf:type rev:Review ."
    				+ "?reviewResource rev:text ?text ."
					+ "?reviewResource dc:date ?date ."
					+ "?reviewResource rev:reviewer ?userResource . "
					+ "?userResource foaf:nick ?nick . "
					+ "?reviewResource trvl:destination ?destinationResource  ."
					+ "?destinationResource rdfs:label \"" + cityname + "\" "
					+ "}";
    	
	    	ResultSet results = SparqlEndpoint.queryData(query);
	    	
			for ( ; results.hasNext() ; )
			{
				QuerySolution qsolution = results.nextSolution();
				Review rev = new Review();
				rev.setNick(qsolution.getLiteral("nick").toString());
				rev.setReview(qsolution.getLiteral("text").toString());
				rev.setReviewDate(Core.getDateFromRDF(qsolution.getLiteral("date").toString()));
				reviews.add(rev);
			}
		}

		model.close();
        Dataset dataset = getDataset();
        closeDataset(dataset);
		
		return reviews;
	}
	
	/**
	 * Return if user can vote or review a destination by checking the timesTraveled property.
	 * @param cityname
	 * @param nick
	 * @return
	 */
	public static boolean canUserVote(String cityname, String nick)
	{
		Model taweb = getTDBModel();
		
		Property timesTraveledProp = taweb.createProperty(trvl + "timesTraveled");
		Resource userDestination = taweb.getResource(trvl + "user/" + nick + "#" + cityname);
		boolean hasTimesTraveledProp = userDestination.hasProperty(timesTraveledProp);
		
		taweb.close();
		Dataset dataset = getDataset();
        closeDataset(dataset);
        
    	return hasTimesTraveledProp;
	}
	
	/**
	 * Check if user exists in TDB.
	 * @param nick
	 * @return
	 */
	public static boolean isUser(String nick)
	{
		Model taweb = getTDBModel();
		boolean result = false;
		
		Resource Person = taweb.getResource(trvl + "user/" + nick);
        if(taweb.containsResource(Person))
        {
        	result = true;
        }
        
        taweb.close();
        Dataset dataset = getDataset();
        closeDataset(dataset);
        
        return result;
	}
	
	/**
	 * Return destination details from TDB.
	 * @param cityname
	 * @return
	 */
	public static City getCityDetailsFromTDB(String cityname) 
	{
		// GET TDB MODEL
		Model model = getTDBModel();
		
		// INITIALISATION
		City city = new City();		
		
		// RESOURCE RE-CONSTRUCTION
		Resource resourceCity = model.getResource(trvl + cityname);
		
		// TEST WHETHER RE-BUIT RESOURCE EXISTS
		if(model.containsResource(resourceCity)) 
		{
			// BUILDING QUERY STRING
			String query = "SELECT ?countryName ?overview ?lat ?long ?population ?currencyCode " 
					+ "WHERE "
					+ "{ "
	    				+ "?cityResource rdf:type trvl:Destination . "
	    				+ "?cityResource rdfs:label \"" + cityname + "\" ."
						+ "?cityResource dbpedia-owl:country ?countryResource . "
						+ "?countryResource rdfs:label ?countryName . "
						+ "?cityResource dc:description ?overview . "
						+ "OPTIONAL { ?countryResource dbpprop:currencyCode ?currencyCode } "
						+ "OPTIONAL { ?cityResource dbpedia-owl:populationTotal ?population } "
						+ "OPTIONAL { ?cityResource geo:lat ?lat } "
						+ "OPTIONAL { ?cityResource geo:lon ?long } "
					+ "}";
    	
			// PREPARING THE QUERY EXECUTION
	    	ResultSet results = SparqlEndpoint.queryData(query);
	
	    	// LOOPING ON RESULTS
			for ( ; results.hasNext() ; )
			{
				QuerySolution qsolution = results.nextSolution();
				
				// ABOUT CITY NAME
				city.setName(cityname);
				
				// ABOUT CITY COUNTRY
				city.setCountry(qsolution.getLiteral("countryName").toString());
				
				// ABOUT CITY SUMMARY
				city.setOverview(qsolution.getLiteral("overview").toString());
				
				// ABOUT CITY CURRENCY CODE
				if(qsolution.contains("currencyCode")) 
				{
					city.setcurrencyCode(qsolution.getLiteral("currencyCode").toString());
				}
				
				// ABOUT CITY POPULATION
				if(qsolution.contains("population")) 
				{
					city.setPopulationTotal(qsolution.getLiteral("population").toString());
				}
				
				// ABOUT CITY LAT
				if(qsolution.contains("lat")) 
				{
					city.setLatitude(qsolution.getLiteral("lat").toString());
				}
				
				// ABOUT CITY LONGITUDE
				if(qsolution.contains("long")) 
				{
					city.setLogitude(qsolution.getLiteral("long").toString());
				}
			}

			// CLOSE DATA MODEL
			model.close();
	        Dataset dataset = getDataset();
	        closeDataset(dataset);
			
	        // RETURN CITY OBJECT
			return city;			
		}

		// NO CITY INTO TDB GRAPH
		return null;
	}
	
	/**
	 * Return a list of photos stored in TDB by city name given.
	 * @param cityname
	 * @return
	 */
	public static List<Photo> getPhotosByCity(String cityname)
	{
		Model taweb = getTDBModel();
		
		List<Photo> photos = null;
		
		Resource City = taweb.getResource(trvl + cityname);
		if(taweb.containsResource(City)) {
			
			photos = new ArrayList<Photo>();
			
			String query = "SELECT ?img WHERE { "
    				+ "?cityResource rdf:type trvl:Destination . "
    				+ "?cityResource rdfs:label \"" + cityname + "\" ."
					+ "?cityResource foaf:img ?img "
					+ "}";
    	
	    	ResultSet results = SparqlEndpoint.queryData(query);
	    	String largePhoto = "";
	    	String thumbPhoto = "";
	    	
			for ( ; results.hasNext() ; )
			{
				QuerySolution qsolution = results.nextSolution();
				largePhoto = qsolution.getLiteral("img").toString();
				thumbPhoto = largePhoto.replace("_z.jpg", "_q.jpg");
				photos.add(new Photo(thumbPhoto, largePhoto));
			}
		}

		taweb.close();
        Dataset dataset = getDataset();
        closeDataset(dataset);
        
		return photos;
	}
	
	/**
	 * Returns all user's destinations information. This function is used for users' profile.
	 * @param nick
	 * @return
	 */
	public static List<Destination> getUserDestinations(String nick) 
	{
    	String query = "SELECT ?cityName ?countryName ?timesInterested ?timesTraveled ?latlong ?rating WHERE { "
    				+ "?personResource rdf:type foaf:Person . "
    				+ "?personResource foaf:nick \"" + nick + "\" ."
					+ "?personResource trvl:to ?userDestinationResource . "
					+ "?userDestinationResource trvl:destination ?destinationResource . "
    				+ "?userDestinationResource trvl:timesInterested ?timesInterested . "
					+ "?destinationResource rdfs:label ?cityName . "
					+ "?destinationResource dbpedia-owl:country ?countryResource . "
					+ "?countryResource rdfs:label ?countryName . "
					+ "OPTIONAL { ?userDestinationResource trvl:timesTraveled ?timesTraveled } "
					+ "OPTIONAL { ?userDestinationResource rev:rating ?rating } "
					+ "OPTIONAL { ?destinationResource geo:lat_long ?latlong } "
					+ "}";
    	
    	ResultSet results = SparqlEndpoint.queryData(query);

        List<Destination> destinations = new ArrayList<Destination>();
        
        String cityName = "";
		String countryName = "";
		int timesInterested = 0;
		int timesTraveled;
		int rating;
		List<Review> reviews =  new ArrayList<Review>();
		String latlong = "";
        		
		for ( ; results.hasNext() ; )
		{
			timesTraveled = 0;
			rating = 0;
			reviews.clear();
			latlong = "";
			
			QuerySolution qsolution = results.nextSolution();

			cityName = qsolution.getLiteral("cityName").toString();
			countryName = qsolution.getLiteral("countryName").toString();
			timesInterested = qsolution.getLiteral("timesInterested").getInt();
			if(qsolution.contains("timesTraveled")) {
				timesTraveled = qsolution.getLiteral("timesTraveled").getInt();
			}
			if(qsolution.contains("rating")) {
				rating = qsolution.getLiteral("rating").getInt();
				rating = (rating * 100) / 5;
			}
			if(qsolution.contains("latlong")) {
				latlong = qsolution.getLiteral("latlong").toString();
			}
			
			destinations.add(new Destination(cityName, countryName, timesInterested, timesTraveled, rating, null, latlong));
		}
	
		// Get reviews for each city
		for(Destination destination : destinations) {
			destination.reviews = getUserReviewsByCity(nick, destination.cityName);
		}
		
		return destinations;
	}
	
	/**
	 * Return a list of reviews by user and city name given.
	 * @param nick
	 * @param cityname
	 * @return
	 */
	public static List<Review> getUserReviewsByCity(String nick, String cityname) 
	{
		String query = "SELECT ?review ?reviewDate WHERE { "
				+ "?reviewResource rdf:type rev:Review . "
				+ "?reviewResource rev:reviewer ?userResource . "
				+ "?userResource foaf:nick \"" + nick + "\" . "
				+ "?reviewResource trvl:destination ?destinationResource . "
				+ "?destinationResource rdfs:label \"" + cityname + "\" . "
				+ "OPTIONAL { ?reviewResource rev:text ?review . ?reviewResource dc:date ?reviewDate } "
				+ "}";
	
		ResultSet results = SparqlEndpoint.queryData(query);
	
		List<Review> reviews = new ArrayList<Review>();
	    
		String review;
		String reviewDate;
	    		
		for ( ; results.hasNext() ; )
		{
			review = "";
			reviewDate = "";
			
			QuerySolution qsolution = results.nextSolution();
	
			if(qsolution.contains("review")) {
				review = qsolution.getLiteral("review").toString();
			}
			if(qsolution.contains("reviewDate")) {
				reviewDate = qsolution.getLiteral("reviewDate").toString();
				reviewDate = Core.getDateFromRDF(reviewDate);
			}
			
			if(review != "" && reviewDate != "") {
				reviews.add(new Review(nick, review, reviewDate));
			}
		}
	
		return reviews;
	}
	
	/**
	 * Fetch most interested destinations by counting timesTraveled
	 * @return
	 */
	public static String getMostInterestedCities()
	{
		String query = "SELECT (COUNT(xsd:integer(?o)) AS ?count) ?destination " +
				"WHERE { "
				+ "?s rdf:type trvl:PersonDestination . "
				+ "?s trvl:timesTraveled ?o . "
				+ "?s trvl:destination ?destinationResource . "
				+ "?destinationResource rdfs:label ?destination ."
				+ "?destinationResource foaf:img ?img "
				+ " } "
				+ "GROUP BY ?destination "
				+ "ORDER BY DESC(?count) "
				+ "LIMIT 8";

		ResultSet results = SparqlEndpoint.queryData(query);
		
		if(results == null) {
			return null;
		}

		String cityName = "";
		String cityNameUrl = "";
		String cityDescription = "";
		String cityImage = "";
		
        String resultHtml = "";
        int i = 0;
        int x = 0;

		for ( ; results.hasNext() ; )
		{
			if(i % 4 == 0) {
				resultHtml += "<li><div class='row'>";
				x = 0;
			}
			
			QuerySolution qsolution = results.nextSolution();
			if(!qsolution.contains("destination")) {
				return null;
			}
			cityName =  qsolution.getLiteral("destination").toString();
			String[] cityDetails = getCityDetailsByCityName(cityName);
			cityDescription = cityDetails[0];
			cityImage = cityDetails[1];
			cityNameUrl = cityName.replace(" ", "%20");

			resultHtml += "<div class='span3 featured-item-wrapper'>"
				        + "<div class='featured-item'>"
				            + "<div class='top'>"
				               + "<div class='inner-border'>"
				                   + "<div class='inner-padding'>"
				                       + "<figure>"
			                       		+ " <img src='" + cityImage + "' alt='' />"
			                       		+ " <a href='" + cityImage + "' class='figure-hover fancybox'>Zoom</a>"
		                       		+ " </figure>";

			resultHtml += "<h3><a href='" + cityNameUrl + "'>" + cityName + "</a></h3>";
			
			resultHtml += "<p class='item-text index-description'>";
			resultHtml += cityDescription;
		    resultHtml += "</p>"
		                     + "</div>"
		                 + "</div>"
		                 + "<i class='bubble'> </i>"
		             + "</div>"
		         + "</div>"
		     + "</div>";
					
			if(x == 3) {
				resultHtml += "</div></li>";
			}
			i++;x++;
		}
		
		return resultHtml;
	}
	
	/**
	 * Return destination details by city name given. getMostInterestedCities function uses this function.
	 * @param cityname
	 * @return
	 */
	public static String[] getCityDetailsByCityName(String cityname) {
		
		String query = "SELECT ?img ?description WHERE { "
				+ "?s rdf:type trvl:Destination ."
				+ "?s rdfs:label \"" + cityname + "\" . "
				+ "?s dc:description ?description . "
				+ "?s foaf:img ?img "
				+ " } "
				+ "LIMIT 1";
		
		ResultSet results = SparqlEndpoint.queryData(query);
		if(results == null) {
			return null;
		}
		
		String[] resultArr = new String[2];
		
		for ( ; results.hasNext() ; )
		{
			QuerySolution qsolution = results.nextSolution() ;

			resultArr[0] = qsolution.getLiteral("description").toString();
			resultArr[1] = qsolution.getLiteral("img").toString();
		}
		
		return resultArr;
	}
	
	/**
	 * Returns the number of total users stored in TDB.
	 * @return
	 */
	public static String getTotalUsers()
	{
		String query = "SELECT (COUNT(?userResource) AS ?totalUsers) WHERE { "
						+ "?userResource rdf:type foaf:Person } ";

		ResultSet results = SparqlEndpoint.queryData(query);
		if(results == null) {
			return null;
		}
		
        String resultStr = "";
        		
		for ( ; results.hasNext() ; )
		{
			QuerySolution qsolution = results.nextSolution() ;

			Literal resulttotalUsers = qsolution.getLiteral("totalUsers") ;
			resultStr = resulttotalUsers.getString();
		}

		return resultStr;
	}
	
	/**
	 * Returns list of most traveled cities for service page
	 * @return
	 */
	public static String getListMostTraveledCities()
	{
		String query = "SELECT (COUNT(xsd:integer(?o)) AS ?count) ?destination " +
				"WHERE " +
				"{ "
				+ "?s rdf:type trvl:PersonDestination . "
				+ "?s trvl:timesTraveled ?o . "
				+ "?s trvl:destination ?destinationResource . "
				+ "?destinationResource rdfs:label ?destination "
				+ " } "
				+ "GROUP BY ?destination "
				+ "ORDER BY ?count "
				+ "LIMIT 10";

		ResultSet results = SparqlEndpoint.queryData(query);

		if(results == null) {
			return null;
		}
        String resultHtml = "";
        String cityname = "";

        resultHtml += "<table>";
		for ( ; results.hasNext() ; )
		{
			QuerySolution qsolution = results.nextSolution() ;
			cityname = qsolution.getLiteral("destination").toString();
			resultHtml += "<tr><td><a href=\"" + cityname.replace(" ", "%20") + "\">" + cityname + "</a></td></tr>";
		}
		resultHtml += "</table>";
		
		return resultHtml;
	}
	
	/**
	 * Returns list of most interactive users by reviews
	 * @return
	 */
	public static String getListMostInteractiveUsers()
	{
		String query = "SELECT ?nick (COUNT(?reviewResource) AS ?reviews) " +
				"WHERE { "
				+ "?reviewResource rdf:type rev:Review ."
				+ "?reviewResource rev:reviewer ?userResource ."
				+ "?userResource foaf:nick ?nick  "
				+ " } "
				+ "GROUP BY ?nick "
				+ "ORDER BY DESC(?reviews) "
				+ "LIMIT 10";

		ResultSet results = SparqlEndpoint.queryData(query);
		
		if(results == null) { return null; }

        String resultHtml = "<table>";
		for ( ; results.hasNext() ; )
		{
			QuerySolution qsolution = results.nextSolution() ;
			String nick = qsolution.getLiteral("nick").toString();
			resultHtml += "<tr><td><a href=\"user/" + nick + "\">" + nick + "</a></td></tr>";
		}
		resultHtml += "</table>";
		
		return resultHtml;
	}
	
	/**
	 * Returns best rated cities by the average of users
	 * @return
	 */
	public static String getListBestRatedCities()
	{
		String query = "SELECT (AVG(xsd:integer(?rate)) AS ?r) ?cityname ?countryname WHERE { "
				+ "?userDestinationResource rdf:type trvl:PersonDestination ."
				+ "?userDestinationResource rev:rating ?rate . "
				+ "?userDestinationResource trvl:destination ?destinationResource ."
				+ "?destinationResource rdfs:label ?cityname . "
				+ "?destinationResource dbpedia-owl:country ?countryResource . "
				+ "?countryResource rdfs:label ?countryname"
				+ " } "
				+ "GROUP BY ?cityname ?countryname "
				+ "ORDER BY DESC(?r) "
				+ "LIMIT 10";

		ResultSet results = SparqlEndpoint.queryData(query);
		if(results == null) {
			return null;
		}
        String resultHtml = "";
        String cityname = "";
        String countryname = "";
        String rate = "";

        resultHtml += "<table>";
		for ( ; results.hasNext() ; )
		{
			QuerySolution qsolution = results.nextSolution() ;
			cityname = qsolution.getLiteral("cityname").toString();
			countryname = qsolution.getLiteral("countryname").toString();
			rate = String.valueOf(qsolution.getLiteral("r").getDouble());
			resultHtml += "<tr><td><a href=\"" + cityname.replace(" ", "%20") + "\">" + cityname + ", " + countryname + "</a></td><td>" + rate + "</td></tr>";
		}
		resultHtml += "</table>";
		
		return resultHtml;
	}
	
	/**
	 * Returns the number of destination searched from our user for service page
	 * @return
	 */
	public static String getNumberOfDestinationsSearched()
	{
		String query = "SELECT (COUNT(?destinationResource) AS ?d) WHERE { "
				+ "?destinationResource rdf:type trvl:Destination ."
				+ " } ";

		ResultSet results = SparqlEndpoint.queryData(query);
		if(results == null) {
			return null;
		}
        String resultHtml = "";

		for ( ; results.hasNext() ; )
		{
			QuerySolution qsolution = results.nextSolution() ;
			resultHtml = String.valueOf(qsolution.getLiteral("d").getInt());
		}
		
		return resultHtml;
	}
}
