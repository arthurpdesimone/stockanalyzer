package stocker.stock;

import java.util.Date;

public class Stock {
    private String ticker;
    private Double close;
    private Double MACD;
    private Double MACDHist;
    private Double MACDSignal;
    private Double RSI;
    private Date date;

    public Stock(String ticker, Double close, Date date) {
        this.ticker = ticker;
        this.close = close;
        this.date = date;
    }

    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public Double getClose() {
        return close;
    }

    public void setClose(Double close) {
        this.close = close;
    }

    public Date getDate() {
        return date;
    }

    public Double getMACD() {
        return MACD;
    }

    public void setMACD(Double MACD) {
        this.MACD = MACD;
    }

    public Double getMACDHist() {
        return MACDHist;
    }

    public void setMACDHist(Double MACDHist) {
        this.MACDHist = MACDHist;
    }

    public Double getMACDSignal() {
        return MACDSignal;
    }

    public void setMACDSignal(Double MACDSignal) {
        this.MACDSignal = MACDSignal;
    }

    public Double getRSI() {
        return RSI;
    }

    public void setRSI(Double RSI) {
        this.RSI = RSI;
    }

}
