package GovDotUk;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class PrimeMinister implements Runnable{

    private String name;
    private Calendar servingSince;
    private Calendar servingTill;
    private ArrayList<String[]> speeches;
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
        this.speeches = new ArrayList<String[]>();
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

    public void dummyThreadTest(){
        int i = 10;
        while(i --> 0){
            System.out.println("Name = " + name);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
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
        ArrayList<String> outputFileNames = new ArrayList<String>();
        while (totalPageCount != 0){
            //create child threads
            String outputFileName = String.format("%s From %d To %d.csv", this.name, from, to);
            outputFileNames.add(outputFileName);
            Thread t = new Thread(new PrimeMinister(this.name, this.servingSince, this.servingTill, from, to));
            subTasks.add(t);
            t.start();

            //workLoad Parameter increment
            totalPageCount -= Math.min(load, totalPageCount);
            from += load;
            to += Math.min(load, totalPageCount);
        }
        //concurrent scraping has started at this point

        //waiting for all the threads to complete -- joining the thread
        for(Thread t : subTasks) {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        //combine all the output files
        String temp = String.format("Dataset/GovDotUK/%s/%s Combined.csv", this.name, this.name);
        try {
            CSVWriter csvWriter = new CSVWriter(new FileWriter(temp));

            //write columnNames
            String[] columnNames = {"Speaker Name", "Title", "Date of Speech Delivered", "URL to Speech"};
            csvWriter.writeNext(columnNames);

            //read all the csv file and write out on one csv file
            for(String filename : outputFileNames){
                temp = String.format("Dataset/GovDotUK/%s/%s", this.name, filename);
                CSVReader csvReader = new CSVReader(new FileReader(temp));
                csvWriter.writeAll(csvReader.readAll());
                csvWriter.flush();
            }
        } catch (IOException | CsvException e) {
            e.printStackTrace();
        }
    }

    public void executeTask() {
        for(int i = fromPage; i <= toPage; i++) {
            String url = generateLink(this.name, i);
//            System.out.println("url = " + url);
            Document searchPage = loadPage(url);
            scrapePage(searchPage);
            write();
        }
    }

    private void write(){
        String path = String.format("Dataset/GovDotUK/%s/%s From %d To %d.csv", this.name, this.name, this.fromPage, this.toPage);
        try {
            CSVWriter csvWriter = new CSVWriter(new FileWriter(path));
            csvWriter.writeAll(speeches);
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
            Calendar speechDate = extractSpeechDate(loadPage(speechUrl));
            String date = "" + speechDate.get(Calendar.DATE);
            date += " " + speechDate.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.US);
            date += " " + speechDate.get(Calendar.YEAR);
            String[] speech = {this.name, speechTitle, date, speechUrl};
            System.out.println(Arrays.toString(speech));
            speeches.add(speech);
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

    private Calendar extractSpeechDate(Document doc){
        Elements elements = doc.select("dd[class]");
        for(Element element : elements) {
            if(element.attr("class").equals("app-c-important-metadata__definition")){
                StringTokenizer st = new StringTokenizer(element.text());
                String shortenedStringDate = st.nextToken() + " " + st.nextToken() + " " + st.nextToken();
                SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy");
                try {
                    Date d = sdf.parse(shortenedStringDate);
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(d);
                    return cal;
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
        return null;
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

}
