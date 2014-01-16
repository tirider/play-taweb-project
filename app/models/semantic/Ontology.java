package models.semantic;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.vocabulary.XSD;

public class Ontology 
{
	// ENCAPSULATE PREFIXES
	private static final String owl 		= OWL.getURI();
	private static final String rdf 		= RDF.getURI();
	private static final String rdfs 		= RDFS.getURI();
	private static final String foaf 		= FOAF.getURI();
	private static final String dc 			= DC.getURI();
	private static final String dcterms 	= DCTerms.getURI();
	private static final String xsd 		= XSD.getURI();
	private static final String voaf 		= "http://purl.org/vocommons/voaf#";
	private static final String cc 			= "http://creativecommons.org/ns#";
	private static final String vann 		= "http://purl.org/vocab/vann/";
	private static final String trvl 		= "http://4travelers.org/";
	private static final String trvlowl		= "http://4travelers.org/ontology#";
	private static final String rev			= "http://purl.org/stuff/rev#";

	/**
	 * This method create the whole ontology hierarchy.
	 * @param type
	 * @return
	 */
	public static File generateOntology(String type) {
		
		// DEFINE LOCAL MODEL
		Model m = ModelFactory.createOntologyModel();
		
		// ONTOLOGY PREFIXES
		m.setNsPrefix("owl", owl);
		m.setNsPrefix("rdf", rdf);
		m.setNsPrefix("rdfs", rdfs);
		m.setNsPrefix("foaf", foaf);
		m.setNsPrefix("dc", dc);
		m.setNsPrefix("dcterms", dcterms);
		m.setNsPrefix("cc", cc);
		m.setNsPrefix("vann", vann);
		m.setNsPrefix("voaf", voaf);
		m.setNsPrefix("xsd", xsd);
        m.setNsPrefix("trvl", trvl);
        m.setNsPrefix("trvl-owl", trvlowl);
        m.setNsPrefix("rev", rev);

        // CREATE ONTOLOGY
        Resource travelersOntology = m.createResource("http://4travelers.org/ontology/");
        Resource voafVocabulary = m.createResource(voaf + "Vocabulary");
        travelersOntology.addProperty(RDF.type, OWL.Ontology);
        travelersOntology.addProperty(RDF.type, voafVocabulary);
        
        // ABOUT LICENCE
        Resource licenseR = m.createResource("http://creativecommons.org/licenses/by/4.0/");
        Property licenseProp = m.createProperty(cc + "license");
        travelersOntology.addProperty(licenseProp, licenseR);
        
        // ABOUT AUTHORS
        travelersOntology.addProperty(DCTerms.creator, "Matthieu KAPETANOS, Rider CARRION-CLEGER");
        
        // ABOUT THIS ONTOLOGY
        travelersOntology.addProperty(DCTerms.description, "Ontology describing travel and destinations.");
        travelersOntology.addProperty(DCTerms.title, "Travel ontology");
        
        // CREATION DATE
        Literal dateIssued = m.createTypedLiteral("2013-12-22", XSDDatatype.XSDdate);
        travelersOntology.addProperty(DCTerms.issued, dateIssued);
        
        // ABOUT PREFIXES
        Property PreferredNamespacePrefix = m.createProperty(vann + "preferredNamespacePrefix");
        Property PreferredNamespaceUri = m.createProperty(vann + "PreferredNamespaceUri");
        travelersOntology.addProperty(PreferredNamespacePrefix, "trvl");
        travelersOntology.addProperty(PreferredNamespaceUri, travelersOntology);
        
        // ABOUT HIERARCHY
        Property classNumber = m.createProperty(voaf + "classNumber");
        Property propertyNumber = m.createProperty(voaf + "propertyNumber");
        travelersOntology.addProperty(classNumber, "2");
        travelersOntology.addProperty(propertyNumber, "5");
        
        // ABOUT VERSIONING
        travelersOntology.addProperty(OWL.versionInfo, "Version 1.0");
        
        // FURTHER RESOURCES
        Resource ReviewR = m.createResource(rev + "Review");

        // CLASS Destination
        Resource destinationR = m.createResource(trvl + "Destination");
        m.add(destinationR, RDF.type, OWL.Class);
        destinationR.addProperty(RDFS.label, "Destination", "en");
        destinationR.addProperty(RDFS.comment, "This class describes a destination", "en");
        destinationR.addProperty(RDFS.isDefinedBy, travelersOntology);
        
        // CLASS PersonDestination
        Resource userDestinationR = m.createResource(trvl + "PersonDestination");
        m.add(userDestinationR, RDF.type, OWL.Class);
        userDestinationR.addProperty(RDFS.label, "PersonDestination", "en");
        userDestinationR.addProperty(RDFS.comment, "This class describes how users are interested by destinations", "en");
        userDestinationR.addProperty(RDFS.isDefinedBy, travelersOntology);
        
        // PROPERTY timesInterested
        Resource timesInterestedInR = m.createResource(trvl + "timesInterested");
        m.add(timesInterestedInR, RDF.type, OWL.DatatypeProperty);
        timesInterestedInR.addProperty(RDFS.isDefinedBy, travelersOntology);
        timesInterestedInR.addProperty(RDFS.domain, userDestinationR);
        timesInterestedInR.addProperty(RDFS.range, XSD.nonNegativeInteger);
        
        // PROPERTY timesTraveled
        Resource timesTraveledToR = m.createResource(trvl + "timesTraveled");
        m.add(timesTraveledToR, RDF.type, OWL.DatatypeProperty);
        timesTraveledToR.addProperty(RDFS.isDefinedBy, travelersOntology);
        timesTraveledToR.addProperty(RDFS.domain, userDestinationR);
        timesTraveledToR.addProperty(RDFS.range, XSD.nonNegativeInteger);
        
        // PROPERTY to
        Resource toR = m.createResource(trvl + "to");
        m.add(toR, RDF.type, OWL.ObjectProperty);
        toR.addProperty(RDFS.isDefinedBy, travelersOntology);
        toR.addProperty(RDFS.domain, FOAF.Person);
        toR.addProperty(RDFS.range, userDestinationR);
        
        // PROPERTY destination
        Resource destinationProp = m.createResource(trvl + "destination");
        m.add(destinationProp, RDF.type, OWL.ObjectProperty);
        destinationProp.addProperty(RDFS.isDefinedBy, travelersOntology);
        destinationProp.addProperty(RDFS.domain, ReviewR);
        destinationProp.addProperty(RDFS.domain, destinationR);
        destinationProp.addProperty(RDFS.range, destinationR);
        
        // PROPERTY review
        Resource reviewProp = m.createResource(trvl + "review");
        m.add(reviewProp, RDF.type, OWL.ObjectProperty);
        reviewProp.addProperty(RDFS.isDefinedBy, travelersOntology);
        reviewProp.addProperty(RDFS.domain, destinationR);
        reviewProp.addProperty(RDFS.range, rev + "Review");
        
        // ONTOLOGY OUTPUT FILE
        File file = null;
        try 
		{
            if(type.toUpperCase().equals("N3"))
            {
            	file = new File("output/4travelersOntology.n3");
            	FileOutputStream ost = new FileOutputStream(file);
            	m.write(ost,"N3");
            }
            else
            {
            	file = new File("output/4travelersOntology.rdf");
				FileOutputStream ost = new FileOutputStream(file);
				m.write(ost,"RDF/XML-ABBREV");
            }
		}
		catch (FileNotFoundException e) 
		{
			System.err.println("Some exception(s) is up when writing a ontology file:\n"+e.getMessage());
		}      
		return file;
	}
}
