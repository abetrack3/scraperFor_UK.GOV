package GovDotUk;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import com.opencsv.exceptions.CsvValidationException;

import java.io.*;
import java.util.*;


public class GovDotUKController {
    public static void main(String[] args) {
        List<String[]> ministers = getMinisterList();
        List<Thread> ministerScraper = new ArrayList<Thread>();
        for(String[] minister: ministers){
            Thread t = new Thread( new PrimeMinister(minister[0], minister[1], minister[2]));
            ministerScraper.add(t);
            t.start();
        }
        for(Thread t : ministerScraper) {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static List<String[]> getMinisterList(){
        try {
            FileReader fr = new FileReader("Dataset/GovDotUK/UKPrimeMinisterList.csv");
            CSVReader csvReader = new CSVReader(fr);
            csvReader.readNext();
            List<String[]> uKPrimeMinisterList = csvReader.readAll();
            return  uKPrimeMinisterList;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (CsvValidationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CsvException e) {
            e.printStackTrace();
        }
        return null;
    }

}
