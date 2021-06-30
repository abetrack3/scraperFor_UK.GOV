package LearnerSpace;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class MyScraper {
    public static void main(String[] args) throws IOException {
        //Connect to the URL (if that makes sense LOL!!)
        Document doc = Jsoup.connect("https://www.google.com").get();

        //Get Title of the page. That's basically
        System.out.println(doc.title());

        Elements links = doc.select("a[href]");
        for(Element link : links){
            System.out.println("\nLink: " + link.attr("href"));
            System.out.println("text: " + link.text());
        }
    }
}
