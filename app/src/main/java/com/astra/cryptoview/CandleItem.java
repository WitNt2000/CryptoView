package com.astra.cryptoview;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class CandleItem {
    public static final String formatValue = "%12.2f";
    public static final String formatChange = "%6.2f";
    String stockName = null;
    String tickerName = null;
    String freqName = null;
    long dateTimeMs = 0L;       //Это время ответа запроса
    double dOpen = 0.0;
    double dHigh = 0.0;
    double dLow = 0.0;
    double dClose = 0.0;
    double dVolume = 0.0;
    public CandleItem(String stockName, String tickerName, String freqName){
        this.stockName = stockName;
        this.tickerName = tickerName;
        this.freqName = freqName;
        //
        resetValue();
    }

    //String title = purpose + " (" + String.format("%18.2f", dSum).trim() + ")";
    //
    public void resetValue(){
        this.dateTimeMs = 0L;
        this.dOpen = 0.0;
        this.dHigh = 0.0;
        this.dLow = 0.0;
        this.dClose = 0.0;
        this.dVolume = 0.0;
    }

    //Установить новые показатели записи
    public void setValue(long dateTimeMs, double dOpen, double dHigh, double dLow, double dClose, double dValue){
        this.dateTimeMs = dateTimeMs;
        this.dOpen = dOpen;
        this.dHigh = dHigh;
        this.dLow = dLow;
        this.dClose = dClose;
        this.dVolume = dValue;
    }

    //Возвращает true, если идёт рост
    protected boolean isIncrease(){ return this.dClose > this.dOpen; }
    //Возращает true? если закрытие и открытие равны
    protected boolean isEquals(){ return this.dClose == this.dOpen; }

    /****************************************************************************************/
    //Форматировать значение
    protected String getFormatValue(double value){
        return String.format(formatValue, value).trim();
    }

    public String getOpen(){ return getFormatValue(this.dOpen); }
    public String getHigh(){ return getFormatValue(this.dHigh); }
    public String getLow(){ return getFormatValue(this.dLow); }
    public String getClose(){ return getFormatValue(this.dClose); }
    public String getVolume(){ return getFormatValue(this.dVolume); }

    public String getChange(){
        return String.format(this.formatValue, this.dClose - this.dOpen).trim();
    }

    public String getPercent(){
        double dResult = this.dOpen==0.0 ? 0.0 : (this.dClose - this.dOpen) / this.dOpen * 100.0;
        return String.format(this.formatChange, dResult).trim();
    }

    public int getColor(){
        return this.isIncrease() ? 0xFF2E7D32 : ( this.isEquals() ? 0xFF757575: 0xFFD50000);
    }

    public String getDateTime(){
        DateFormat dateFormat = new SimpleDateFormat("yyyy MM dd HH:mm:ss");
        //dateFormat.setTimeZone(TimeZone.getTimeZone("Europe/Moscow"));
        return dateFormat.format(new Date(this.dateTimeMs));
    }

}
