package models.global;

import play.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.StringTokenizer;

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

public class Core 
{
	/**
	 * Downloads data from url given
	 * @param urlString
	 * @return
	 * @throws Exception
	 */
	public static String readUrl(String urlString) throws Exception {
	    BufferedReader reader = null;
	    try {
	        URL url = new URL(urlString);
	        if(!checkUrl(url)) {
	        	return null;
	        }
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
	
	/**
	 * Returns cities found by user's query. This method uses a json file dump from dbpedia.
	 * @param query
	 * @return cityName, countryName
	 */
	public static String getCityByQuery(String query)
	{
		query = query.toLowerCase();

		LinkedHashMap<String, String> resultArray = new LinkedHashMap<String, String>(); // LinkedHashMap preserves order insertion
		
		File file = Play.application().getFile("/public/json/cities_en.json");
		String resultsStr = "{\"cities\":[";
		String jsonStr;
		try {
			jsonStr = Files.toString(file, Charsets.UTF_8);
			if(jsonStr == null) {
				return null;
			}
			ObjectMapper mapper = new ObjectMapper();
			JsonNode actualObj = mapper.readTree(jsonStr);
			JsonNode results = actualObj.get("cities");
			int size = 12;
			outer:
			for (JsonNode element: results) {
				if(element.get("city").asText().toLowerCase().equals(query)) {
					if (--size > 0) {
						resultArray.put(element.get("city").asText(), element.get("country").asText());
	    		    }
					else {
						break outer;
					}
				}
			}
			outer2:
			for (JsonNode element: results) {
				if(element.get("city").asText().toLowerCase().startsWith(query)) {
					if (--size > 0) {
						if(!resultArray.containsKey(element.get("city").asText())) {
							resultArray.put(element.get("city").asText(), element.get("country").asText());
						}
	    		    }
					else {
						break outer2;
					}
				}
			}
			
			for (String key : resultArray.keySet()) {
				resultsStr += "{\"value\" : \"" + key + "\",";
				resultsStr += "\"name\" : \"" + key + ", " + resultArray.get(key) + "\"},";
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
		resultsStr += "]}";
		resultsStr = resultsStr.replace(",]}", "]}"); // remove last comma
		return resultsStr;
	}
	
	/**
	 * Parse integer
	 * @param string
	 * @return
	 */
	public static int parseInt(String string) {
		try {
			return Integer.parseInt(string);
		}
		catch(NumberFormatException e) {
			return 0;
		}
	}
	
	/**
	 * Checks url by HTTP HEAD method
	 * @param url
	 * @return
	 */
	public static boolean checkUrl(URL url) {

        HttpURLConnection huc;
		try {
			huc = ( HttpURLConnection )  url.openConnection();
			huc.setRequestMethod("HEAD");
	        if(huc.getResponseCode() != HttpURLConnection.HTTP_OK)
	        {
	        	return false;
	        }
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} 
        
        return true;
	}
	
	/**
	 * Get current date
	 * @return Date
	 */
	public static Date getDate() {
		Date date = new Date();
		return date;
	}
	
	/**
	 * Format date for users
	 * @param date
	 * @return
	 */
	public static String formatDate(Date date) {
		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm"); // Currently we will use GMT +1 Time (Europe/Paris)
		return dateFormat.format(date);
	}
	
	/**
	 * Converts to compliant with Dublin Core date format
	 * @return String
	 */
	public static String getDateForRDF(Date date) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'+01:00'"); // Currently we will use GMT +1 Time (Europe/Paris)
		return dateFormat.format(date);
	}
	
	/**
	 * Converts date from Dublin Core date format to normal date format
	 * @return String
	 */
	public static String getDateFromRDF(String date) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'+01:00'"); // Currently we will use GMT +1 Time (Europe/Paris)
		Date formattedDate = null;
		try {
			formattedDate = dateFormat.parse(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
		return dateFormat.format(formattedDate);
	}
	
	/**
	 * Converts date from Timestamp to Date
	 * @param timestamp
	 * @return
	 */
	public static Date convertTimestampToDate(long timestamp)
	{
		Timestamp stamp = new Timestamp(timestamp);
		Date date = new Date(stamp.getTime());
		return date;
	}
	
	/**
	 * This method handle strong sha1 password string
	 * @param content
	 * @return
	 */
	public static String ecryptToSha1(String content)
	{
		try 
		{
	        MessageDigest md = MessageDigest.getInstance("SHA1");
	        
	        md.update(content.getBytes());
	        
	        return new BigInteger( 1, md.digest() ).toString(16);
	    }
	    catch (NoSuchAlgorithmException e) { }
		
		return content;
	}	
}
