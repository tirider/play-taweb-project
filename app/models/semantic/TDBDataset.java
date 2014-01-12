package models.semantic;

import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.query.*;

import play.Play;

public class TDBDataset {
	
	private static final String directory = Play.application().path() + "/public/data/";
	private static TDBDataset instance = null;
	private Dataset dataset = null;
	
	public TDBDataset() {
		dataset = TDBFactory.createDataset(directory);
	}
	
	public static TDBDataset getInstance() {
		if(instance == null) {
			instance = new TDBDataset();
		}
		return instance;
	}
	
	public Dataset getDataset() {
		return dataset;
	}
}