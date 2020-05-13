package stocker;

import stocker.stock.Stock;
import stocker.stock.StockManager;
import stocker.stock.StockOperation;

import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Scanner;

import static java.nio.file.StandardCopyOption.*;

public class Utils {
    public static void reOrderListByDate(ArrayList<StockOperation> stocks) {
        stocks.sort(Comparator.comparing(StockOperation::getDate));
    }

    public static String downloadToString(String url) throws IOException {
        InputStream in = URI.create(url).toURL().openStream();
        String text = null;
        try (Scanner scanner = new Scanner(in, StandardCharsets.UTF_8.name())) {
            text = scanner.useDelimiter("\\A").next();
        }
        return text;
    }

    public static String convertExceptionToString(Exception e){
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String sStackTrace = sw.toString(); // stack trace as a string
        return sStackTrace;
    }
}
