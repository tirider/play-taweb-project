package models.service;

import models.beans.City;
import models.query.Queries;
import models.query.QueryRunner;

public class DBPediaService
{
	/**
	 * 
	 * @param cityName
	 * @return
	 */
	public static City parse(String cityName)
	{
		// CHECKS WHETHER THE DESTINATION IS A CITY
		if(QueryRunner.exists(Queries.ASKCITYQUERY1, cityName))
		{
			return  QueryRunner.execute(Queries.CITYQUERY1, cityName);
		}
		// CHECKS WHETHER THE DESTINATION IS A SETTLEMENT OTHERWISE
		else
		{
			return QueryRunner.execute(Queries.CITYQUERY2, cityName);
		}
	}
}