package stocker;

import stocker.stock.StockOperation;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
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
}