package GovDotUk;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.StringTokenizer;

public class SpeechLinkScraper implements Runnable {

    private String name;
    private int speechCount;
    private int fromPage;
    private int toPage;

    public SpeechLinkScraper(String name, int fromPage, int toPage){
        this.name = name;
        this.fromPage = fromPage;
        this.toPage = toPage;
        Thread t = new Thread(this);
        t.start();
    }

    @Override
    public void run() {
        scrapePersonSpeeches(this.name);
    }

    private void scrapePersonSpeeches(String name) {
        String link = generateLink(name, 1);
        Document searchResult = loadPage(link);
        if(searchResult == null) return;
        int totalPageCount = getTotalPageCount(searchResult);
        scrapePage(searchResult);
        System.out.println("Successful scraping Page No: 1 out of " + totalPageCount);
        System.out.println("speechCount = " + speechCount);
        for (int i = 2; i <= totalPageCount; i++) {
            link = generateLink(name, i);
            searchResult = loadPage(link);
            scrapePage(searchResult);
            System.out.println("Successful scraping Page No: " + i + " out of " + totalPageCount);
            System.out.println("speechCount = " + speechCount);
        }
    }

    private int getTotalPageCount(Document doc){
        Elements links = doc.select("span[class]");
        for(Element link : links) {
            if (!link.attr("class").equals("gem-c-pagination__link-label"))
                continue;
            int totalPageCount = Integer.parseInt(link.text().split(" ")[2]);
            return totalPageCount;
        }
        return 0;
    }

    private String generateLink(String name, int pageNo){
        if(name.equals("")) return "";
        StringTokenizer tokenizer = new StringTokenizer(name);
        String res = "https://www.gov.uk/search/all?keywords=";
        res += tokenizer.nextToken();
        while(tokenizer.hasMoreTokens()) {
            res += "+" + tokenizer.nextToken();
        }
        res += "&order=relevance&page=" + pageNo;
        return res;
    }

    private Document loadPage(String url) {
        try {
            return Jsoup.connect(url).get();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void scrapePage(Document doc) {
        Elements searchResults = doc.select("li[class]"); // we select x[y] | where x is a tag and y is an attribute of the tag
        for (Element searchResult : searchResults) {
            if (!searchResult.attr("class").equals("gem-c-document-list__item  "))
                continue;
            Element anchorTag = searchResult.getElementsByTag("a").get(0);
            if(!anchorTag.attr("href").startsWith("/government/speeches"))
                continue;
            System.out.println("Search Result --> " + anchorTag.text());
            System.out.println("Click Here --> https://www.gov.uk" + anchorTag.attr("href"));
            speechCount++;
            parseDateString(loadPage("https://www.gov.uk" + anchorTag.attr("href")));
        }
    }

    private void parseDateString(Document doc) {
        Elements elements = doc.select("dd[class]");
        for(Element element : elements) {
            if(element.attr("class").equals("app-c-important-metadata__definition")){
                System.out.println("Date(unparsed) --> " + element.text());
                StringTokenizer st = new StringTokenizer(element.text());
                String shortenedStringDate = st.nextToken() + " " + st.nextToken() + " " + st.nextToken();
                SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy");
                try {
                    Date d = sdf.parse(shortenedStringDate);
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(d);
                    System.out.println("DATE = " + cal.get(Calendar.DATE));
                    System.out.println("MONTH = " + cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.US));
                    System.out.println("cal.get(Calendar.YEAR) = " + cal.get(Calendar.YEAR));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                break;
            }
        }

    }
}
