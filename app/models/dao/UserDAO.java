package models.dao;

import java.util.ArrayList;
import java.util.List;

import models.beans.User;
import models.crypto.Encryptor;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.WriteResult;

/**  **/
public class UserDAO implements IUserDAO
{
	// REPRESENTE LA COLLECTION
	private static final String TABLE_NAME    = "user";
	
	// CHAMPS DE LA COLLETION DANS LA BASE DE DONNEES
	private static final String TABLE_FIELD_1 = "_id";
	private static final String TABLE_FIELD_2 = "email";	
	private static final String TABLE_FIELD_3 = "password";
	private static final String TABLE_FIELD_4 = "inscriptiondate";
	private static final String TABLE_FIELD_5 = "name";		

	// DATA ACCESS OBJECT
	private IDAO dao;
	
	// CONSTRUCTOR
	public UserDAO(IDAO dao)
	{
		this.dao = dao;
	}

	@Override
	public User find(String email) 
	{
		// USE A CONNECTION FROM THE POOL
		DB db = (DB) this.dao.getConnection();
		db.requestStart();
		
		// MAPPING THE RELATED COLLECTION
		DBCollection collection = db.getCollection(TABLE_NAME);
		
		// BUILD THE QUERY
		BasicDBObject searchquery = new BasicDBObject();
		searchquery.put(TABLE_FIELD_2, email.toLowerCase());

		// FETCH THE REQUEST
		DBObject row = collection.findOne(searchquery);
		
		// INIT USER BEANS
		User user = new User();
	
		try
		{
			if(row != null)
			{
				user.setId(row.get(TABLE_FIELD_1).toString());
				user.setName(row.get(TABLE_FIELD_5).toString());
				user.setEmail(row.get(TABLE_FIELD_2).toString());
				user.setPassword(row.get(TABLE_FIELD_3).toString());
			}
		}
		finally
		{
			// RELEASE THE CONNECTION BACK TO THE POOL 
			db.requestDone();
		}
		
		return user;
	}

	@Override
	public boolean save(User user) 
	{
		// USE A CONNECTION FROM THE POOL
		DB db = (DB) this.dao.getConnection();
		db.requestStart();
		
		// MAPPING THE RELATED COLLECTION
		DBCollection collection = db.getCollection(TABLE_NAME);
		
		// BUILD THE QUERY
		BasicDBObject query = new BasicDBObject();
		
		query.put(TABLE_FIELD_5, user.getName().toLowerCase());	
		query.put(TABLE_FIELD_2, user.getEmail().toLowerCase());		
		query.put(TABLE_FIELD_3, Encryptor.ecryptToSha1(user.getPassword()));
		query.put(TABLE_FIELD_4, user.getInscriptiondate());
		
		// COMMITING OPERATION 
		WriteResult  insertion = collection.insert(query);
		
		// RELEASE THE CONNECTION BACK TO THE POOL 
		db.requestDone();
		
		// CHECKING OPERATION
		if (insertion.getLastError() != null)
			return true;
		return false;
	}

	@Override
	public boolean remove(String email) 
	{
		// USE A CONNECTION FROM THE POOL
		DB db = (DB) this.dao.getConnection();
		db.requestStart();
		
		// MAPPING THE RELATED COLLECTION
		DBCollection collection = db.getCollection(TABLE_NAME);
		
		// BUILD THE QUERY		
		BasicDBObject wherequery = new BasicDBObject();
		wherequery.put(TABLE_FIELD_2, email.toLowerCase());
		
		// COMMITING OPERATION
		WriteResult  delete = collection.remove(wherequery);
		
		// RELEASE THE CONNECTION BACK TO THE POOL 
		db.requestDone();
		
		// CHECKING THE OPERATION RESULT
		if (delete.getLastError() != null)
			return true;
		return false;
	}

	@Override
	public boolean update(String email, String newpassword) 
	{
		// USE A CONNECTION FROM THE POOL
		DB db = (DB) this.dao.getConnection();
		db.requestStart();
		
		// MAPPING THE RELATED COLLECTION
		DBCollection collection = db.getCollection(TABLE_NAME);
		
		// BUILD THE QUERY
		BasicDBObject wherequery = new BasicDBObject();
		wherequery.append(TABLE_FIELD_2, email.toLowerCase());
		
		BasicDBObject updatequery = new BasicDBObject();
		BasicDBObject setquery    = new BasicDBObject();
		setquery.append(TABLE_FIELD_3, Encryptor.ecryptToSha1(newpassword));
		updatequery.put("$set", setquery);
		
		// COMMITING OPERATION
		WriteResult update = collection.update(wherequery, updatequery);
		
		// RELEASE THE CONNECTION BACK TO THE POOL 
		db.requestDone();
		
		// CHECKING THE OPERATION RESULT
		if (update.getLastError() != null)
			return true;
		return false;
	}

	@Override
	public List<User> findAll() 
	{
		// USE A CONNECTION FROM THE POOL
		DB db = (DB) this.dao.getConnection();
		db.requestStart();
		
		// MAPPING THE RELATED COLLECTION
		DBCollection collection = db.getCollection(TABLE_NAME);
		
		// BUILD THE QUERY
		BasicDBObject searchquery = new BasicDBObject();
		
		// RESULTAT DE LA REQUETE
		DBCursor rows = collection.find(searchquery);
		
		// OBJECT RECEVEUR DE BEANS UTILISATEUR
		List<User> userSet = new ArrayList<User>();
		
		try
		{
			while (rows.hasNext())
			{
				// REPRESENTE UNE LIGNE DE LA COLLECTION STOCKE DANS LE RESULTSET
				DBObject row = rows.next();
				
				// BEANS UTILISATEUR
				User user = new User();
				
				user.setId(row.get(TABLE_FIELD_1).toString());
				user.setName(row.get(TABLE_FIELD_5).toString());
				user.setEmail(row.get(TABLE_FIELD_2).toString());
				//user.setPassword(row.get(TABLE_FIELD_3).toString());
				
				// SET DANS LA LISTE
				userSet.add(user);
			}
		}
		finally
		{
			// CLOSE THE CURSOR
			rows.close();
			
			// RELEASE THE CONNECTION BACK TO THE POOL 
			db.requestDone();
		}
		
		return userSet;
	}

	@Override
	public boolean exists(String username) 
	{
		// GET A THE CONNECTION FROM THE POOL
		DB db = (DB) this.dao.getConnection();
		db.requestStart();
		
		// ON CHERCHE LA COLLECTION CORRESPONDANTE SUR MONGODB
		DBCollection collection = db.getCollection(TABLE_NAME);
		
		// CREATION DU QUERY
		BasicDBObject searchquery = new BasicDBObject();
		searchquery.put(TABLE_FIELD_5, username.toLowerCase());

		// RESULTAT DE LA REQUETE
		DBObject row = collection.findOne(searchquery);
		
		boolean exists = false;
		
		if(row != null) exists = true;
			
		// RELEASE THE CONNECTION BACK TO THE POOL 
		db.requestDone();		
		
		return exists;
	}
	
	@Override
	public boolean existsEmail(String email) 
	{
		// GET A THE CONNECTION FROM THE POOL
		DB db = (DB) this.dao.getConnection();
		db.requestStart();
		
		// ON CHERCHE LA COLLECTION CORRESPONDANTE SUR MONGODB
		DBCollection collection = db.getCollection(TABLE_NAME);
		
		// CREATION DU QUERY
		BasicDBObject searchquery = new BasicDBObject();
		searchquery.put(TABLE_FIELD_2, email.toLowerCase());

		// RESULTAT DE LA REQUETE
		DBObject row = collection.findOne(searchquery);
		
		boolean exists = false;
		
		if(row != null) exists = true;
			
		// RELEASE THE CONNECTION BACK TO THE POOL 
		db.requestDone();		
		
		return exists;
	}
		
	@Override
	public boolean exists(String email, String password)
	{
		// GET A THE CONNECTION FROM THE POOL
		DB db = (DB) this.dao.getConnection();
		db.requestStart();
		
		// ON CHERCHE LA COLLECTION CORRESPONDANTE SUR MONGODB
		DBCollection collection = db.getCollection(TABLE_NAME);
		
		// CREATION DU QUERY
		BasicDBObject searchquery = new BasicDBObject();
		searchquery.put(TABLE_FIELD_2, email.toLowerCase());
		searchquery.put(TABLE_FIELD_3, Encryptor.ecryptToSha1(password));

		// RESULTAT DE LA REQUETE
		DBObject row = collection.findOne(searchquery);
		
		boolean exists = false;
		
		if(row != null) exists = true;
			
		// RELEASE THE CONNECTION BACK TO THE POOL 
		db.requestDone();		
		
		return exists;
	}
}