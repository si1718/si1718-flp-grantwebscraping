package web.scraping.jsoup;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class TestJSoupScholar {
	
	private static final String URL_BASE = "https://scholar.google.com/";
	
	private static final String URL_SCHOLAR = URL_BASE + "citations?user=";
	
	public static void main(String[] args) throws  MalformedURLException, IOException{
		
		String idPablo = "y2qDY6IAAAAJ";
				
		Document doc = Jsoup.parse(new URL(URL_BASE + idPablo), 10000);
		
		System.out.println(doc.getElementsByAttributeValue("id", "gsc_prf_pup-img"));
	}

}
