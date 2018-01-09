package web.scraping.jsoup;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class TestJSoup {

	private static final String URL_BASE = "https://investigacion.us.es/sisius/sisius.php?struct=1&en=&text2search=&ct=&cs=&inside=";
	public static void main(String[] args) throws MalformedURLException, IOException{
		
		String anios = "2001 jksadnkjasdh 23022 93201. ldsa 2340";
		System.out.println("Mi cadena: " + anios);

		Pattern p = Pattern.compile("\\s[0-9]{4}");
		Matcher m = p.matcher(anios);
		m.find();
		System.out.println("Mi patron: " + m.group());
		
		/*Document doc = Jsoup.parse(new URL(URL_BASE), 10000);
		
		Elements elem = doc.getElementsByAttribute("valign");
		Element top = elem.get(0);
		int i = 0;
		for(Iterator<Element> itor = top.getElementsByTag("a").iterator(); itor.hasNext();) {
			Element e = itor.next();
			if(i%2 != 0) {
				System.out.println(e.attr("href"));
				System.out.println(e.text());
			}
			i++;
		}*/
	}

}
