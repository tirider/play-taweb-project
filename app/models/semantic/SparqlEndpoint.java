package models.semantic;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.List;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class SparqlEndpoint {

	public static ResultSet queryData(String q)
	{
		Model taweb = Semantic.getTDBModel();

        // GET NAMESPACES FROM MODELS
        String dbpediaowlNS = taweb.getNsPrefixURI("dbpedia-owl");
    	String dbpediaNS = taweb.getNsPrefixURI("dbpedia");
    	String dbppropNS = taweb.getNsPrefixURI("dbpprop");
    	String revNS = taweb.getNsPrefixURI("rev");
    	String geoNS = taweb.getNsPrefixURI("geo");
    	String trvlNS = taweb.getNsPrefixURI("trvl");
    	String trvlowlNS = taweb.getNsPrefixURI("trvl-owl");

    	// PREFIXES
    	String prefixes = 	"PREFIX rdfs: <"+RDFS.getURI()+"> "+
    						"PREFIX rdf: <"+RDF.getURI()+"> " +
    						"PREFIX owl: <"+OWL.getURI()+"> " +
    						"PREFIX dc: <"+DC.getURI()+"> " +
    						"PREFIX foaf: <"+FOAF.getURI()+"> "+
    						"PREFIX dbpedia-owl: <"+dbpediaowlNS+"> "+
    						"PREFIX dbpedia: <"+dbpediaNS+"> " +
    						"PREFIX dbpprop: <"+dbppropNS+"> " +
    						"PREFIX rev: <"+revNS+"> " +
    						"PREFIX geo: <"+geoNS+"> " +
    						"PREFIX trvl: <"+trvlNS+"> " +
    						"PREFIX trvl-owl: <"+trvlowlNS+"> " + 
    						"PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>";

        // SPARQL
    	String q1 = prefixes + q;

    	ResultSet results = null; 
    			
    	try {
	        Query query = QueryFactory.create(q1);
	        QueryExecution qexec = QueryExecutionFactory.create(query, taweb);
	        results = qexec.execSelect();
    	}
    	catch(Exception e) {
    		results = null;
    	}
    	finally {
    		try {
        		taweb.close();
        		Dataset dataset = Semantic.getDataset();
        		Semantic.closeDataset(dataset);
			} 
    		catch (Exception e2) {
				System.out.println(e2.getMessage());
			}
    	}
    	
    	return results;
	}
	
	public static String outputHtml(ResultSet results)
	{
		/*// RETRIEVE ALL VARIABLE COMMING FROM THE QUERY
		List<String> varsets = results.getResultVars();
		
		// HOLD THE FINAL RESULT
		String resultHtml = new String();
		
		while(results.hasNext())
		{
		    QuerySolution bind = results.nextSolution();
		    String row = new String();
		    String res = new String();
		    
		    for(int i =0; i< varsets.size();i++)
		    {
		    	res = bind.get(varsets.get(i)).toString();
		    	
		    	if(res.matches("^http://"))
		    		row += "<a href=\"http://localhost:9000/Montpellier\">"+res+"</a>"+" ### ";
		    	else
		    		row += "\""+res+"\""+ " ### ";
		    }
		    
		    resultHtml += "<br/>"+row + "\n";
		}*/
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ResultSetFormatter.out(baos, results);
	    String resultStr = baos.toString();
	    //System.out.println(resultStr);
	    resultStr = resultStr.replace("<", "&lt;");
	    resultStr = resultStr.replace(">", "&gt;");
	    resultStr = resultStr.replace("-", " ");
	    resultStr = resultStr.replace("=", " ");
	    resultStr = resultStr.replace("\r", "</td></tr>");
	    resultStr = resultStr.replace("\n", "<tr><td>");
	    resultStr = resultStr.replace("|</td></tr><tr><td>|", "</td></tr><tr><td>");
	    resultStr = resultStr.replace("|", "</td><td>");
	    resultStr = resultStr.trim();
	    resultStr = "<table>" + resultStr;
	    resultStr += "</table>";
	    resultStr = resultStr.replace("<table></td></tr><tr><td></td>", "<table border=\"1\"><tr>");
	    resultStr = resultStr.replace("</td></tr><tr><td></table>", "</tr></td></table>");
	    resultStr = resultStr.replace("</td><td></td></tr><tr><td>", " ");
	    resultStr = resultStr.replace("</td></tr><tr><td></td><td>", "</td></tr><tr><td>");
	    
	    return resultStr;
	}
}
