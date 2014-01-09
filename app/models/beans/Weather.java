package models.beans;

import java.util.Date;

public class Weather {

	public Date date;
	public int temperature;
	public String icon;
	public String description;
	
	public Weather(Date date, int temperature, String icon, String description) {
		this.date = date;
		this.temperature = temperature;
		this.icon = icon;
		this.description = description;
	}
}
