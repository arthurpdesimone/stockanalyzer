package stocker.stock;

import java.util.ArrayList;
import java.util.HashMap;

public class StockManager {
    /** static variable single_instance of type Singleton */
    private static StockManager singleInstance = null;
    private HashMap<String, ArrayList<Stock>> stocks = new HashMap<>();
    private ArrayList<StockOperation> cashFlow = new ArrayList<>();
    /** private constructor restricted to this class itself */
    private StockManager() {
    }
    public static StockManager getInstance() {
        if (singleInstance == null)
            singleInstance = new StockManager();
        return singleInstance;
    }

    /** Stocks Hashmap */
    public HashMap<String, ArrayList<Stock>> getStocks() {
        return stocks;
    }
    /** Get operations on this market */
    public ArrayList<StockOperation> getCashFlow() {
        return cashFlow;
    }

}