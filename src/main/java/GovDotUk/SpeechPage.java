package GovDotUk;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.StringTokenizer;

public class SpeechPage {

    private String name;
    private String title;
    private Calendar dateOfSpeechDelivery;
    private String urlToSpeech;
    private String speechText;

    SpeechPage (String name, String title, String url, Document speechPage) {
        this.name = name;
        this.title = title;
        this.urlToSpeech = url;
        this.dateOfSpeechDelivery = extractSpeechDate(speechPage);
        this.speechText = extractSpeech(speechPage);
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

    private String extractSpeech(Document doc) {
        return doc.select("div[class=\"govspeak\"]").get(0).text();
    }

    public Calendar getDateOfSpeechDelivery() {
        return dateOfSpeechDelivery;
    }

    public int compareTo(SpeechPage sp) {
        return dateOfSpeechDelivery.compareTo(sp.getDateOfSpeechDelivery());
    }

    public String[] toStringArray(){
        String date = "" + this.dateOfSpeechDelivery.get(Calendar.DATE);
        date += " " + this.dateOfSpeechDelivery.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.US);
        date += " " + this.dateOfSpeechDelivery.get(Calendar.YEAR);
        String[] res = {name, title, date, urlToSpeech, speechText};
        return res;
    }

    public String toString() {
        return java.util.Arrays.toString(toStringArray());
    }

}
