import GovDotUk.SpeechLinkScraper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class CodeRunner {
    public static void main(String[] args) {
        String name = "Theresa May";
        new SpeechLinkScraper("Theresa May", 1, 100);
    }

}
