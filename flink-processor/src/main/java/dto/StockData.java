package dto;

import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class StockData implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String symbol;
    private String timestamp;
    private double open;
    private double high;
    private double low;
    private double close;
    private int volume;
    private String dataType;
    
    private double avgPrice;
    private int totalVolume;
    
    // Getters and setters
    public String getSymbol() {
        return symbol;
    }
    
    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }
    
    public String getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
    
    public double getOpen() {
        return open;
    }
    
    public void setOpen(double open) {
        this.open = open;
    }
    
    public double getHigh() {
        return high;
    }
    
    public void setHigh(double high) {
        this.high = high;
    }
    
    public double getLow() {
        return low;
    }
    
    public void setLow(double low) {
        this.low = low;
    }
    
    public double getClose() {
        return close;
    }
    
    public void setClose(double close) {
        this.close = close;
    }
    
    public int getVolume() {
        return volume;
    }
    
    public void setVolume(int volume) {
        this.volume = volume;
    }
    
    public String getDataType() {
        return dataType;
    }
    
    public void setDataType(String dataType) {
        this.dataType = dataType;
    }
    
    public double getAvgPrice() {
        return avgPrice;
    }
    
    public void setAvgPrice(double avgPrice) {
        this.avgPrice = avgPrice;
    }
    
    public int getTotalVolume() {
        return totalVolume;
    }
    
    public void setTotalVolume(int totalVolume) {
        this.totalVolume = totalVolume;
    }
}