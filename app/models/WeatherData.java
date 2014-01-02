package models;

import java.util.Date;

public class WeatherData {

	public Date date;
	public int temperature;
	public String icon;
	public String description;
	
	public WeatherData(Date date, int temperature, String icon, String description) {
		this.date = date;
		this.temperature = temperature;
		this.icon = icon;
		this.description = description;
	}
}
