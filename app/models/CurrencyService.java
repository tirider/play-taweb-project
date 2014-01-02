package models;

import java.net.URL;
import java.util.List;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;

public class CurrencyService {
	public static String getCurrency(String currencyCode) {
		String rate = "";
		try {
            SAXBuilder sxb = new SAXBuilder();
            URL url = new URL("http://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml");
            Document document = sxb.build(url);
            Element racine = document.getRootElement();
            Namespace ns = Namespace.getNamespace("http://www.ecb.int/vocabulary/2002-08-01/eurofxref");
            Element cube = racine.getChild("Cube", ns);
            Element cube2 = cube.getChild("Cube", ns);
            List<Element> cube3 = cube2.getChildren("Cube", ns);
            for (Element element : cube3) {
                if(element.getAttributeValue("currency").equals(currencyCode)) {
                    rate = element.getAttributeValue("rate");
                }
            }
            return rate;
        } catch (Exception ex) {
        	return null;
        }
	}
}