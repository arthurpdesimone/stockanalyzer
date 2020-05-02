package stocker.stock;

import java.util.Date;

public class StockOperation {
    public static String BUY = "BUY";
    public static String SELL = "SELL";
    private Stock stock;
    private String operationType;

    public StockOperation(Stock stock) {
        this.stock = stock;
    }

    public Stock getStock() {
        return stock;
    }

    public Date getDate() { return stock.getDate();}

    public void setStock(Stock stock) {
        this.stock = stock;
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }
}
