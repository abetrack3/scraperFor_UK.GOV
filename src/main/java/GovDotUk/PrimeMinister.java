package GovDotUk;

import com.opencsv.CSVWriter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class PrimeMinister implements Runnable{

    private String name;
    private Calendar servingSince;
    private Calendar servingTill;
    private ArrayList<SpeechPage> speeches;
    private int fromPage;
    private int toPage;

    PrimeMinister(String name, String servingSince, String servingTill){
        this.name = name;
        this.servingSince = parseDateString(servingSince);
        this.servingTill = parseDateString(servingTill);
        this.fromPage = -1;
        this.toPage = -1;
    }

    private PrimeMinister(String name, Calendar servingSince, Calendar servingTill, int fromPage, int toPage){
        this.name = name;
        this.servingSince = servingSince;
        this.servingTill = servingTill;
        this.fromPage = fromPage;
        this.toPage = toPage;
        this.speeches = new ArrayList<SpeechPage>();
    }

    public Calendar parseDateString(String date){
        StringTokenizer st = new StringTokenizer(date);
        String shortenedStringDate = st.nextToken() + " " + st.nextToken() + " " + st.nextToken();
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy");
        Date d = null;
        try {
            d = sdf.parse(shortenedStringDate);
        } catch (ParseException e) {
            e.printStackTrace();
            System.out.println("date = " + date);
            return null;
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        return cal;
    }

    @Override
    public void run() {
        if(this.fromPage == -1 && this.toPage == -1) splitTask();
        else executeTask();
    }

    public void splitTask() {
        //make Prime Minister exclusive directory
        new File("Dataset/GovDotUK/"+this.name).mkdirs();

        //divide main task into smaller subTasks
        int load = 200; //each child thread will scrape upto 200 search result pages
        int totalPageCount = getTotalPageCount(loadPage(generateLink(this.name, 1)));
        int from = 1;
        int to = Math.min(load, totalPageCount);
        ArrayList<Thread> subTasks = new ArrayList<Thread>();
        ArrayList<PrimeMinister> pmList = new ArrayList<PrimeMinister>();
        while (totalPageCount != 0){
            //create child threads
            PrimeMinister pm = new PrimeMinister(this.name, this.servingSince, this.servingTill, from, to);
            Thread t = new Thread(pm);
            subTasks.add(t);
            pmList.add(pm);
            t.start();

            //workLoad Parameter increment
            totalPageCount -= Math.min(load, totalPageCount);
            from += load;
            to += Math.min(load, totalPageCount);
        }
        //concurrent scraping has started at this point

        //waiting for all the threads to complete -- joining the thread
        speeches = new ArrayList<SpeechPage>();
        for(int i = 0; i < subTasks.size(); i++) {
            try {
                subTasks.get(i).join();
                speeches.addAll(pmList.get(i).getSpeeches());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        write();
    }

    public void executeTask() {
        for(int i = fromPage; i <= toPage; i++) {
            String url = generateLink(this.name, i);
            Document searchPage = loadPage(url);
            scrapePage(searchPage);
        }
    }

    private void write() {
        try {
            //sorting the speeches based on date of speech delivery
            speeches.sort((x, y) -> x.compareTo(y)); //using lambda expression for comparator

            //combine all the output files
            String temp = String.format("Dataset/GovDotUK/%s/%s.csv", this.name, this.name);
            CSVWriter csvWriter = new CSVWriter(new FileWriter(temp));

            //write columnNames
            String[] columnNames = {"Speaker Name", "Title", "Date of Speech Delivered", "URL to Speech", "Text of Speech"};
            csvWriter.writeNext(columnNames);
            for(SpeechPage t : speeches)
                csvWriter.writeNext(t.toStringArray());
            csvWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
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
            String speechTitle = anchorTag.text();
            String speechUrl = "https://www.gov.uk" + anchorTag.attr("href");
            Document speech = loadPage(speechUrl);
            SpeechPage speechPage = new SpeechPage(this.name, speechTitle, speechUrl, speech);
            System.out.println(speechPage);
            speeches.add(speechPage);
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
        return 1;
    }

    private Document loadPage(String url) {
        try {
            return Jsoup.connect(url).get();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String generateLink(String name, int pageNo){
        //24+July+2019
        if(name.equals("")) return "";
        StringTokenizer tokenizer = new StringTokenizer(name);
        String res = "https://www.gov.uk/search/all?keywords=";
        res += tokenizer.nextToken();
        while(tokenizer.hasMoreTokens()) {
            res += "+" + tokenizer.nextToken();
        }
        res += "&order=relevance&page=" + pageNo;
        res += "&public_timestamp%5Bfrom%5D=";
        res += "" + this.servingSince.get(Calendar.DATE);
        res += "+" + this.servingSince.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.US);
        res += "+" + this.servingSince.get(Calendar.YEAR);
        res += "&public_timestamp%5Bto%5D=";
        res += "" + this.servingTill.get(Calendar.DATE);
        res += "+" + this.servingTill.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.US);
        res += "+" + this.servingTill.get(Calendar.YEAR);
        return res;
    }

    public ArrayList<SpeechPage> getSpeeches() {
        return speeches;
    }

}
