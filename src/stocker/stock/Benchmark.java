package stocker.stock;

import java.util.ArrayList;

public class Benchmark {

    private ArrayList<StockOperation> operations;
    private ArrayList<Stock> stocks;
    private int operationsBuyAndSell;
    private double profit;
    private double buyAndHold;
    public Benchmark(){
    }

    public ArrayList<StockOperation> getOperations() {
        return operations;
    }

    public void setOperations(ArrayList<StockOperation> operations) {
        this.operations = operations;
    }

    public ArrayList<Stock> getStocks() {
        return stocks;
    }

    public void setStocks(ArrayList<Stock> stocks) {
        this.stocks = stocks;
    }

    public int getOperationsBuyAndSell() {
        return operationsBuyAndSell;
    }

    public double getProfit() {
        return profit;
    }


    public double getBuyAndHold() {
        return buyAndHold;
    }

}
