package LearnerSpace;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.List;

public class MyScraper {
    public static void main(String[] args) throws IOException {
        //Connect to the URL (if that makes sense LOL!!)
        Document doc = Jsoup.connect("http://abetrack3.me:5500/Lecture5/Float_and_Align.html").get();

        //Get Title of the page. That's basically
        System.out.println(doc.title());

        Elements links = doc.select("div[id]"); // we select x[y] | where x is a tag and y is an attribute of the tag
        for(Element link : links){
            System.out.println("\nLink-id: " + link.attr("id"));
            System.out.println("\nLink-class: " + link.attr("class"));
            System.out.println("text: " + link.text());

            Elements childNodes = link.children();
            for(Element childNode : childNodes){
//                System.out.println("misaki start");
                System.out.print("--> " + childNode.nodeName());
//                System.out.println("misaki stop");
                System.out.println("--> " + childNode.text());
            }
        }


    }
}
