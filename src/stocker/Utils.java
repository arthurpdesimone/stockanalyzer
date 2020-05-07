package stocker;

import stocker.stock.Stock;
import stocker.stock.StockManager;
import stocker.stock.StockOperation;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Scanner;

import static java.nio.file.StandardCopyOption.*;

public class Utils {


    public static void reOrderListByDate(ArrayList<StockOperation> stocks){
        stocks.sort(Comparator.comparing(StockOperation::getDate));
    }

    public static void download(String url, String fileName) throws IOException {
        try (InputStream in = URI.create(url).toURL().openStream()) {
            Files.copy(in, Paths.get(fileName),REPLACE_EXISTING);
        }
    }

    /** Method to search for old stocks */
    public static void scanForOldStocks() throws FileNotFoundException {
        HashMap<String, ArrayList<Stock>> stocks = StockManager.getInstance().getStocks();
        FileInputStream fis = new FileInputStream("stocks.txt");
        Scanner sc = new Scanner(fis);
        while(sc.hasNextLine()) {
            String ticker = sc.nextLine();
            stocks.put(ticker, new ArrayList<>());
        }
    }
}
