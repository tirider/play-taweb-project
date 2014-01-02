package models.query;

public class Queries 
{
	//======================================================================================================================
	
	public static String PREFIX = "" +
			"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
			"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +	
			"PREFIX foaf: <http://xmlns.com/foaf/0.1/> " +
			"PREFIX dbpedia-owl: <http://dbpedia.org/ontology/> " +
			"PREFIX dbpprop: <http://dbpedia.org/property/>  " +
			"PREFIX geo:  <http://www.w3.org/2003/01/geo/wgs84_pos#> ";
	
	//======================================================================================================================
	
	public static String ASKCITYQUERY1  = PREFIX +
			"ASK "+
			"WHERE "+
			"{ "+
			"   ?cityResource rdf:type dbpedia-owl:City. "+			
			"   ?cityResource rdfs:label \"%s\"@en."+
			"   ?cityResource geo:lat ?cityLat."+
			"   ?cityResource geo:long ?cityLong."+
			"   ?cityResource dbpedia-owl:abstract ?cityAbstract."+
			"   ?cityResource dbpedia-owl:populationTotal ?cityPopulationTotal."+
			
			"   ?cityResource  dbpedia-owl:country ?countryResource."+
			"   ?countryResource  rdfs:label ?countryName."+
			"   ?countryResource dbpprop:currencyCode ?currencyCode."+
			   
			"   FILTER (lang(?countryName)= 'en' && lang(?cityAbstract)= 'en')"+
			"}";
	public static String CITYQUERY1  = PREFIX +
			"SELECT DISTINCT ?cityAbstract ?cityLat ?cityLong ?cityPopulationTotal ?countryName ?currencyCode "+
			"WHERE "+
			"{ "+
			"   ?cityResource rdf:type dbpedia-owl:City. "+			
			"   ?cityResource rdfs:label \"%s\"@en."+
			"   ?cityResource geo:lat ?cityLat."+
			"   ?cityResource geo:long ?cityLong."+
			"   ?cityResource dbpedia-owl:abstract ?cityAbstract."+
			"   ?cityResource dbpedia-owl:populationTotal ?cityPopulationTotal."+
			
			"   ?cityResource  dbpedia-owl:country ?countryResource."+
			"   ?countryResource  rdfs:label ?countryName."+
			"   ?countryResource dbpprop:currencyCode ?currencyCode."+
			   
			"   FILTER (lang(?countryName)= 'en' && lang(?cityAbstract)= 'en')"+
			"}" +
			"LIMIT 1";	
	//======================================================================================================================
		
	public static String ASKCITYQUERY2    = PREFIX +
			"ASK "+
			"WHERE "+
			"{ "+
			"   ?cityResource rdf:type dbpedia-owl:Settlement. "+			
			"   ?cityResource rdfs:label \"%s\"@en."+
			"   ?cityResource geo:lat ?cityLat."+
			"   ?cityResource geo:long ?cityLong."+
			"   ?cityResource dbpedia-owl:abstract ?cityAbstract."+
			"   ?cityResource dbpedia-owl:populationTotal ?cityPopulationTotal."+
			
			"   ?cityResource  dbpedia-owl:country ?countryResource."+
			"   ?countryResource  rdfs:label ?countryName."+
			"   ?countryResource dbpprop:currencyCode ?currencyCode."+
			   
			"   FILTER (lang(?countryName)= 'en' && lang(?cityAbstract)= 'en')"+
			"}" ;
	public static String CITYQUERY2    = PREFIX +
			"SELECT DISTINCT ?cityAbstract ?cityLat ?cityLong ?cityPopulationTotal ?countryName ?currencyCode "+
			"WHERE "+
			"{ "+
			"   ?cityResource rdf:type dbpedia-owl:Settlement. "+			
			"   ?cityResource rdfs:label \"%s\"@en."+
			"   ?cityResource geo:lat ?cityLat."+
			"   ?cityResource geo:long ?cityLong."+
			"   ?cityResource dbpedia-owl:abstract ?cityAbstract."+
			"   ?cityResource dbpedia-owl:populationTotal ?cityPopulationTotal."+
			
			"   ?cityResource  dbpedia-owl:country ?countryResource."+
			"   ?countryResource  rdfs:label ?countryName."+
			"   ?countryResource dbpprop:currencyCode ?currencyCode."+
			   
			"   FILTER (lang(?countryName)= 'en' && lang(?cityAbstract)= 'en')"+
			"}" +
			"LIMIT 1";	

	//======================================================================================================================
}