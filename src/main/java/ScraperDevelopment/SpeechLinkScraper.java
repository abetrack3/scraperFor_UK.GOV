package ScraperDevelopment;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class SpeechLinkScraper {
    public static void main(String[] args) throws IOException {
        Document doc = Jsoup.connect("https://www.gov.uk/search/all?keywords=Theresa+May&order=relevance&page=1").get();

        Elements searchResults = doc.select("li[class]");
        for(Element searchResult : searchResults) {
            if(!searchResult.attr("class").equals("gem-c-document-list__item  "))
                continue;
//            System.out.println("Clickables --> " + searchResult.text());
            Element anchorTag =  searchResult.getElementsByTag("a").get(0);
            System.out.println("Search Result --> " + anchorTag.text());
            System.out.println("Click Here --> https://www.gov.uk/" + anchorTag.attr("href"));

        }
    }
}
