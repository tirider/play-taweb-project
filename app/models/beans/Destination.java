package models.beans;

import java.util.List;

import models.beans.Review;

public class Destination {
	
	public String cityName;
	public String countryName;
	public String cityLatLong;
	public int timesInterested;
	public int timesTraveled;
	public int rate;
	public List<Review> reviews;
	
	public Destination(String cityName, String countryName, int timesInterested, int timesTraveled, int rate, List<Review> reviews, String cityLatLong) {
		this.cityName = cityName;
		this.countryName = countryName;
		this.timesInterested = timesInterested;
		this.timesTraveled = timesTraveled;
		this.rate = rate;
		this.reviews = reviews;
		this.cityLatLong = cityLatLong;
	}

	public String getCityLatLong() {
		return cityLatLong;
	}

	public void setCityLatLong(String cityLatLong) {
		this.cityLatLong = cityLatLong;
	}

	public List<Review> getReviews() {
		return reviews;
	}

	public void setReviews(List<Review> reviews) {
		this.reviews = reviews;
	}

	public String getCityName() {
		return cityName;
	}

	public void setCityName(String cityName) {
		this.cityName = cityName;
	}

	public String getCountryName() {
		return countryName;
	}

	public void setCountryName(String countryName) {
		this.countryName = countryName;
	}

	public int getTimesInterested() {
		return timesInterested;
	}

	public void setTimesInterested(int timesInterested) {
		this.timesInterested = timesInterested;
	}

	public int getTimesTraveled() {
		return timesTraveled;
	}

	public void setTimesTraveled(int timesTraveled) {
		this.timesTraveled = timesTraveled;
	}

	public int getRate() {
		return rate;
	}

	public void setRate(int rate) {
		this.rate = rate;
	}

}
