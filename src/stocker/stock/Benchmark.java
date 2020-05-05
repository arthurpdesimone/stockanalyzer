package stocker.stock;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;

import static stocker.stock.StockOperation.BUY;

public class Benchmark {

    private LinkedList<StockOperation> operations;
    private ArrayList<Stock> stocks;

    private static DecimalFormat df = new DecimalFormat("#.##");

    public Benchmark(){
    }

    public LinkedList<StockOperation> getOperations() {
        return operations;
    }

    public void setOperations(LinkedList<StockOperation> operations) {
        this.operations = operations;
    }

    public ArrayList<Stock> getStocks() {
        return stocks;
    }

    public void setStocks(ArrayList<Stock> stocks) {
        this.stocks = stocks;
    }

    public int getOperationsBuyAndSell() {
        return operations.size();
    }

    public String getProfit() {
        final double[] profit = {1};
        operations.forEach(stockOperation -> {
            if(stockOperation.getOperationType().equals(BUY)){
                profit[0] = profit[0] /stockOperation.getStock().getClose();
            }else {
                profit[0] = profit[0] * stockOperation.getStock().getClose();
            }
        });
        return df.format((profit[0]-1)*100);
    }


    public String getBuyAndHold() {
        double firstClose = stocks.get(0).getClose();
        double lastClose = stocks.get(stocks.size()-1).getClose();
        return df.format((lastClose/firstClose-1)*100);
    }

    public String getStock(){
        return getStocks().get(0).getTicker();
    }


}
