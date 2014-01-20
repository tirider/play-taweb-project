package models.service;

import java.util.ArrayList;
import java.util.List;
import models.beans.Weather;
import models.global.Core;
import play.Play;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class WeatherService 
{
	/**
	 * Retrieve weather information.
	 * @param Lat
	 * @param Long
	 * @return
	 */
	public static List<Weather> getWeatherByLatLongOnDate(double Lat, double Long) 
	{
		// HOLDS CUSTOM WEATHER OBJECTS 
		List<Weather> wd = new ArrayList<Weather>();
		
		// API URL HOLDER
		String jsonStr;
		
		try 
		{
			// http://openweathermap.org/API
			jsonStr = Core.readUrl("http://api.openweathermap.org/data/2.5/forecast/daily?lat=" + Lat + "&lon=" + Long + "&units=metric&cnt=14&mode=json&APPID="+Play.application().configuration().getString("OWMAPPID"));
			ObjectMapper mapper = new ObjectMapper();
			
			// ATTRS THE CUSTOM WEATHER OBJECTS
			JsonNode actualObj = mapper.readTree(jsonStr);
			JsonNode results = actualObj.get("list");
			String icon = null, description = null;
			int temperatureMin, temperatureMax = 0;
			long timestamp = 0;
			
			// BUILDS A SET OF CUSTOM WEATHER OBJECTS
			for (JsonNode element: results) 
			{
				timestamp = element.get("dt").asLong();
				temperatureMin = (int) Math.round(Double.parseDouble(element.get("temp").get("min").asText()));
				temperatureMax = (int) Math.round(Double.parseDouble(element.get("temp").get("max").asText()));
				JsonNode weatherNode = element.get("weather");
				
				for (JsonNode weatherNodeElement: weatherNode) 
				{
					icon = weatherNodeElement.get("icon").textValue();
					description = weatherNodeElement.get("description").textValue();
				}
				
				wd.add(new Weather(Core.convertTimestampToDate(timestamp), temperatureMin, temperatureMax, icon, description));
			}
			return wd;
		} 
		catch (Exception e) 
		{
			System.err.println("Sorry, we founds some problems with the weather service...");
			
			// Send list even empty
			return wd; 
		}
	}
}