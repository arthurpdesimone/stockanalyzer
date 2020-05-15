package stocker.stock;

import java.util.Date;

public class Stock {
    private String ticker;
    private Double close;
    private Double SMA1;
    private Double SMA2;
    private Double MACD;
    private Double MACDHist;
    private Double MACDSignal;
    private Double RSI;
    private Double RSIStochasticFastD;
    private Double RSIStochasticFastK;
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

    public void setDate(Date date) {
        this.date = date;
    }

    public Double getSMA1() {
        return SMA1;
    }

    public void setSMA1(Double SMA1) {
        this.SMA1 = SMA1;
    }

    public Double getSMA2() {
        return SMA2;
    }

    public void setSMA2(Double SMA2) {
        this.SMA2 = SMA2;
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

    public Double getRSIStochasticFastD() {
        return RSIStochasticFastD;
    }

    public void setRSIStochasticFastD(Double RSIStochasticFastD) {
        this.RSIStochasticFastD = RSIStochasticFastD;
    }

    public Double getRSIStochasticFastK() {
        return RSIStochasticFastK;
    }

    public void setRSIStochasticFastK(Double RSIStochasticFastK) {
        this.RSIStochasticFastK = RSIStochasticFastK;
    }
}
