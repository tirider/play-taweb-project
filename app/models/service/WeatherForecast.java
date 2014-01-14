package models.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import models.beans.Weather;
import models.global.Core;
import play.Play;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class WeatherForecast {

	public static List<Weather> getWeatherByLatLongOnDate(double Lat, double Long, Date ArrivalDate) {
		List<Weather> wd = new ArrayList<Weather>();
		String jsonStr;
		try {
			jsonStr = Core.readUrl("http://api.openweathermap.org/data/2.5/forecast?lat=" + Lat + "&lon=" + Long + "&units=metric&cnt=99&APPID="+Play.application().configuration().getString("OWMAPPID"));
			ObjectMapper mapper = new ObjectMapper();
			JsonNode actualObj = mapper.readTree(jsonStr);
			JsonNode results = actualObj.get("list");
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String icon = null, description = null;
			int temperature = 0;
			Date weatherDate = null;
			for (JsonNode element: results) {
				weatherDate = formatter.parse(element.get("dt_txt").textValue());
				if(ArrivalDate.before(weatherDate)) {
					JsonNode weatherNode = element.get("weather");
					for (JsonNode weatherNodeElement: weatherNode) {
						icon = weatherNodeElement.get("icon").textValue();
						description = weatherNodeElement.get("description").textValue();
					}
					temperature = (int) Math.round(Double.parseDouble(element.get("main").get("temp").toString()));
					wd.add(new Weather(weatherDate, temperature, icon, description));
				}
			}
			return wd;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}