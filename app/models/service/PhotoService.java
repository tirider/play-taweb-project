package models.service;

import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import models.beans.Photo;
import models.global.Core;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;

public class PhotoService {

	public static List<Photo> getPhotosByLatLong(double Lat, double Long) {
		List<Photo> ph = new ArrayList<Photo>();
		String imgThumbnailUrl = "";
		String imgLargeUrl = "";
		try {
            SAXBuilder sxb = new SAXBuilder();
            URL url = new URL("http://api.flickr.com/services/rest/?method=flickr.photos.search&api_key=7cf1e6f78c839a1951ae3e6ad00802b8&&accuracy=11&has_geo=1&media=photos&lat=" + Lat + "&lon=" + Long + "&sort=relevance&content_type=1geo_context=2");
            if(!Core.checkUrl(url)) {
            	return null;
            }
            Document document = sxb.build(url);
            Element racine = document.getRootElement();
            Element photos = racine.getChild("photos");
            List<Element> photoList = photos.getChildren("photo");
            if(photoList.size() == 0) {
            	return null;
            }
            int photosCount = 18;
            for(Element p : photoList) {
        		imgThumbnailUrl = "http://farm" + p.getAttributeValue("farm") + ".staticflickr.com/" + p.getAttributeValue("server") + "/" + p.getAttributeValue("id") + "_" + p.getAttributeValue("secret") + "_q.jpg";
        		imgLargeUrl = "http://farm" + p.getAttributeValue("farm") + ".staticflickr.com/" + p.getAttributeValue("server") + "/" + p.getAttributeValue("id") + "_" + p.getAttributeValue("secret") + "_z.jpg";
        		ph.add(new Photo(imgThumbnailUrl, imgLargeUrl));
            	photosCount--;
            	if(photosCount == 0) {
            		break;
            	}
            }
            
            return ph;
        } catch (Exception ex) {
        	return null;
        }
		
	}
}
