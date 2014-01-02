package models.dao;

/** **/
public class DAOFactory 
{
	public MongoDB createMongodbConnection()
	{
		return MongoDB.getInstance();
	}
	
	public Object createMySqlConnection()
	{
		return null;
	}
}
