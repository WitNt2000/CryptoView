package com.astra.cryptoview;

import java.util.ArrayList;

public interface IServiceInterface {
    public void onBinanceTickerList(ArrayList<String> l);   //Получен список тикеров с биржы Binance
    public void onBitgetTickerList(ArrayList<String> l);    //Получен список тикеров с биржы Bitget
    public void onBybitTickerList(ArrayList<String> l);     //Получен список тикеров с биржы Bybit
    //
    //Обновление записи index c показателями свечи
    public void onUpdateCandle(int index, long dateTimeMs, double dO, double dH, double dL, double dC, double dV, boolean isUpdate);
}
