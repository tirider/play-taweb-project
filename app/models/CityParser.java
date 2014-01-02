package models;

import models.beans.City;
import models.query.Queries;
import models.query.QueryRunner;

public class CityParser
{
	public static City parse(String cityName)
	{
		if(QueryRunner.exists(Queries.ASKCITYQUERY1, cityName))
		{
			return  QueryRunner.execute(Queries.CITYQUERY1, cityName);
		}
		else
		{
			return QueryRunner.execute(Queries.CITYQUERY2, cityName);
		}
	}
}