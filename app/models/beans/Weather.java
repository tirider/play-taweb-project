package models.beans;

import java.util.Date;

public class Weather {

	public Date date;
	public int temperatureMin;
	public int temperatureMax;
	public String icon;
	public String description;
	
	public Weather(Date date, int temperatureMin, int temperatureMax, String icon, String description) {
		this.date = date;
		this.temperatureMin = temperatureMin;
		this.temperatureMax = temperatureMax;
		this.icon = icon;
		this.description = description;
	}
}
