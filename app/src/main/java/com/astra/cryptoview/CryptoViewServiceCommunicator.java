package com.astra.cryptoview;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.RequiresApi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CryptoViewServiceCommunicator{
    //Константы
    public final static String TAG = "CryptoViewServiceCommunicator";

    private Handler netHandler = null;    //Вспомогательный обработчик сетевых событий приложения

    CryptoViewServiceCommunicator(){
        SetNetworkHandler();              //Устанавливаем вспомогательный обработчик сетевых запросов приложения
    }

    //Устанавливаем вспомогательный обработчик сетевых запросов приложения
    private void SetNetworkHandler(){
        netHandler = new Handler(){
            //@Override
            public void handleMessage(Message msg){
                super.handleMessage(msg);
                //
                int event_type = msg.getData().getInt(CryptoViewConst.NET_EVENT_TYPE);
                switch(event_type) {
                    //Общая сетеевая обработка
                    case CryptoViewConst.NET_EVENT_NETWORK_ERROR        :
                        networkHandlerNetworkError(msg.getData().getString(CryptoViewConst.NET_EVENT_DATA));
                        break;
                    case CryptoViewConst.NET_EVENT_SUCCESS_CONNECTION   :
                        networkHandlerSuccessConnection(msg.getData().getString(CryptoViewConst.NET_EVENT_DATA));
                        break;
                    //Тикеры
                    case CryptoViewConst.NET_EVENT_BINANCE_TICKERS  :
                        networkHandlerBinanceTickers(msg.getData().getString(CryptoViewConst.NET_EVENT_DATA));
                        break;
                    case CryptoViewConst.NET_EVENT_BITGET_TICKERS   :
                        networkHandlerBitgetTickers(msg.getData().getString(CryptoViewConst.NET_EVENT_DATA));
                        break;
                    case CryptoViewConst.NET_EVENT_BYBIT_TICKERS    :
                        networkHandlerBybitTickers(msg.getData().getString(CryptoViewConst.NET_EVENT_DATA));
                        break;
                    //Свечи
                    case CryptoViewConst.NET_EVENT_BINANCE_CANDLE   :
                        networkHandlerBinanceCandle(
                                msg.getData().getString(CryptoViewConst.NET_EVENT_DATA),
                                msg.getData().getInt(CryptoViewConst.NET_EVENT_INDEX),
                                msg.getData().getBoolean(CryptoViewConst.NET_EVENT_UPDATE));
                        break;
                    case CryptoViewConst.NET_EVENT_BITGET_CANDLE    :
                        networkHandlerBitgetCandle(
                                msg.getData().getString(CryptoViewConst.NET_EVENT_DATA),
                                msg.getData().getInt(CryptoViewConst.NET_EVENT_INDEX),
                                msg.getData().getBoolean(CryptoViewConst.NET_EVENT_UPDATE));
                        break;
                    case CryptoViewConst.NET_EVENT_BYBIT_CANDLE     :
                        networkHandlerBybitCandle(
                                msg.getData().getString(CryptoViewConst.NET_EVENT_DATA),
                                msg.getData().getInt(CryptoViewConst.NET_EVENT_INDEX),
                                msg.getData().getBoolean(CryptoViewConst.NET_EVENT_UPDATE));
                        break;
                    case CryptoViewConst.NET_EVENT_WIDGET_NETWORK_ERROR:
                        networkHandlerWidgetNetworkError(msg.getData().getString(CryptoViewConst.NET_EVENT_DATA));
                        break;
                    //Свечи для виджета
                    case CryptoViewConst.NET_EVENT_WIDGET_BINANCE_CANDLE:
                        networkHandlerBinanceCandleForWidget(msg.getData().getString(CryptoViewConst.NET_EVENT_DATA));
                        break;
                    case CryptoViewConst.NET_EVENT_WIDGET_BITGET_CANDLE:
                        networkHandlerBitgetCandleForWidget(msg.getData().getString(CryptoViewConst.NET_EVENT_DATA));
                        break;
                    case CryptoViewConst.NET_EVENT_WIDGET_BYBIT_CANDLE:
                        networkHandlerBybitCandleForWidget(msg.getData().getString(CryptoViewConst.NET_EVENT_DATA));
                        break;
                    //
                }
            }
        };
    }

    //Обработчик ошибки
    @SuppressLint("LongLogTag")
    private void networkHandlerNetworkError(String networkError){
        MainActivity.getRef().onNetworkError(networkError);
    }

    //Обработчик отправки проверки связи с сервисом
    @SuppressLint("LongLogTag")
    private void networkHandlerSuccessConnection(String jsonString){
        MainActivity.getRef().onTestConnectionSuccess(null);
    }

    @SuppressLint("LongLogTag")
    private void networkHandlerBinanceTickers(String jsonString){
        try {
            ArrayList<String> tL = new ArrayList<String>();
            JSONArray rows = new JSONArray(jsonString);
            for(int i=0; i<rows.length(); i++) tL.add(rows.getJSONObject(i).getString("symbol"));
            CryptoViewService.getRef().onBinanceTickerList(tL);
        }
        catch(JSONException e){
            Log.e(TAG, e.getMessage().toString());
        }
    }

    @SuppressLint("LongLogTag")
    private void networkHandlerBitgetTickers(String jsonString){
        try {
            ArrayList<String> tL = new ArrayList<String>();
            JSONArray rows = new JSONObject(jsonString).getJSONArray("data");
            for(int i=0; i<rows.length(); i++) tL.add(rows.getJSONObject(i).getString("symbol"));
            CryptoViewService.getRef().onBitgetTickerList(tL);
        }
        catch(JSONException e){
            Log.e(TAG, e.getMessage().toString());
        }
    }

    @SuppressLint("LongLogTag")
    private void networkHandlerBybitTickers(String jsonString){
        try {
            ArrayList<String> tL = new ArrayList<String>();
            JSONArray rows = new JSONObject(jsonString).getJSONObject("result").getJSONArray("list");
            for(int i=0; i<rows.length(); i++) tL.add(rows.getJSONObject(i).getString("symbol"));
            CryptoViewService.getRef().onBybitTickerList(tL);
        }
        catch(JSONException e){
            Log.e(TAG, e.getMessage().toString());
        }
    }

    //Свеча с Binance
    @SuppressLint("LongLogTag")
    private void networkHandlerBinanceCandle(String jsonString, int index, boolean isUpdate){
        try {
            //[[1745193600000,"85179.24000000","87795.26000000","85144.76000000","87300.51000000","14355.09669000",1745279999999,"1249876818.68357080",1162843,"8007.15061000","696953207.04160190","0"]]
            JSONArray jsonCandle = new JSONArray(jsonString).getJSONArray(0);
            //MainActivity.getRef().onUpdateCandle(index, jsonCandle.getLong(6),
            MainActivity.getRef().onUpdateCandle(index, new Date().getTime(),
                    jsonCandle.getDouble(1), jsonCandle.getDouble(2),
                    jsonCandle.getDouble(3), jsonCandle.getDouble(4),
                    jsonCandle.getDouble(5), isUpdate);
        }
        catch(JSONException e){
            Log.e(TAG, e.getMessage().toString());
        }
    }

    //Свеча с Bitget
    @SuppressLint("LongLogTag")
    private void networkHandlerBitgetCandle(String jsonString, int index, boolean isUpdate){
        //[[1745193600000,"85179.24000000","87795.26000000","85144.76000000","87300.51000000","14355.09669000",1745279999999,"1249876818.68357080",1162843,"8007.15061000","696953207.04160190","0"]]
        try {
            JSONArray jsonCandle = new JSONObject(jsonString).getJSONArray("data").getJSONArray(0);
            //MainActivity.getRef().onUpdateCandle(index, jsonCandle.getLong(6),
            MainActivity.getRef().onUpdateCandle(index, new Date().getTime(),
                    jsonCandle.getDouble(1), jsonCandle.getDouble(2),
                    jsonCandle.getDouble(3), jsonCandle.getDouble(4),
                    jsonCandle.getDouble(5), isUpdate);
        }
        catch(JSONException e){
            Log.e(TAG, e.getMessage().toString());
        }
    }

    //Свеча с Bybit
    @SuppressLint("LongLogTag")
    private void networkHandlerBybitCandle(String jsonString, int index, boolean isUpdate){
        //{"retCode":0,"retMsg":"OK","result":{"category":"spot","symbol":"BTCUSDT","list":[["1745884800000","95009.9","95461.5","94206.9","94851.4","3434.4692","325599405.76492207"]]},"retExtInfo":{},"time":1745919999317}
        try {
            JSONObject jsonResult = new JSONObject(jsonString);
            long timeMS = jsonResult.getLong("time");
            //
            JSONArray jsonCandle = jsonResult.getJSONObject("result").getJSONArray("list").getJSONArray(0);
            //MainActivity.getRef().onUpdateCandle(index, timeMS,
            MainActivity.getRef().onUpdateCandle(index, new Date().getTime(),
                    jsonCandle.getDouble(1), jsonCandle.getDouble(2),
                    jsonCandle.getDouble(3), jsonCandle.getDouble(4),
                    jsonCandle.getDouble(5), isUpdate);
        }
        catch(JSONException e){
            Log.e(TAG, e.getMessage().toString());
        }
    }

    //Обработчик ошибки для виджета
    @SuppressLint("LongLogTag")
    private void networkHandlerWidgetNetworkError(String networkError){
        CryptoViewWidgetConfigureActivity.getRef().onNetworkError(networkError);
    }

    //Обработчик свечи binance для виджета
    @SuppressLint("LongLogTag")
    private void networkHandlerBinanceCandleForWidget(String jsonString){
        try {
            //[[1745193600000,"85179.24000000","87795.26000000","85144.76000000","87300.51000000","14355.09669000",1745279999999,"1249876818.68357080",1162843,"8007.15061000","696953207.04160190","0"]]
            JSONArray jsonCandle = new JSONArray(jsonString).getJSONArray(0);
            CryptoViewWidgetConfigureActivity.getRef().onUpdateCandle(
                    jsonCandle.getDouble(1), jsonCandle.getDouble(4), jsonCandle.getDouble(5));
        }
        catch(JSONException e){
            Log.e(TAG, e.getMessage().toString());
        }
    }

    //Обработчик свечи bitget для виджета
    @SuppressLint("LongLogTag")
    private void networkHandlerBitgetCandleForWidget(String jsonString){
        //[[1745193600000,"85179.24000000","87795.26000000","85144.76000000","87300.51000000","14355.09669000",1745279999999,"1249876818.68357080",1162843,"8007.15061000","696953207.04160190","0"]]
        try {
            JSONArray jsonCandle = new JSONObject(jsonString).getJSONArray("data").getJSONArray(0);
            //MainActivity.getRef().onUpdateCandle(index, jsonCandle.getLong(6),
            CryptoViewWidgetConfigureActivity.getRef().onUpdateCandle(
                    jsonCandle.getDouble(1), jsonCandle.getDouble(4), jsonCandle.getDouble(5));
        }
        catch(JSONException e){
            Log.e(TAG, e.getMessage().toString());
        }
    }

    //Обработчик свечи bybit для виджета
    @SuppressLint("LongLogTag")
    private void networkHandlerBybitCandleForWidget(String jsonString){
        //{"retCode":0,"retMsg":"OK","result":{"category":"spot","symbol":"BTCUSDT","list":[["1745884800000","95009.9","95461.5","94206.9","94851.4","3434.4692","325599405.76492207"]]},"retExtInfo":{},"time":1745919999317}
        try {
            JSONObject jsonResult = new JSONObject(jsonString);
            JSONArray jsonCandle = jsonResult.getJSONObject("result").getJSONArray("list").getJSONArray(0);
            //MainActivity.getRef().onUpdateCandle(index, timeMS,
            CryptoViewWidgetConfigureActivity.getRef().onUpdateCandle(
                    jsonCandle.getDouble(1), jsonCandle.getDouble(4), jsonCandle.getDouble(5));
        }
        catch(JSONException e){
            Log.e(TAG, e.getMessage().toString());
        }
    }

    /****************************************************************************************/
    // Вспомогатиельные функции

    //Отправка сообщения в обработчик из потока отправки запроса
    protected void sendNetworkResultMessage(int networkType, String networkData){
        if ( netHandler==null ) return;
        Bundle b = new Bundle();
        b.putInt(CryptoViewConst.NET_EVENT_TYPE, networkType);
        b.putString(CryptoViewConst.NET_EVENT_DATA, networkData);
        Message msg = netHandler.obtainMessage();
        msg.setData(b);
        netHandler.sendMessage(msg);
    }

    //Отправка сообщения в обработчик из потока отправки запроса c дополнительными данными
    protected void sendNetworkResultMessageWithExtendData(int networkType, String networkData, int index, boolean isUpdate){
        if ( netHandler==null ) return;
        Bundle b = new Bundle();
        b.putInt(CryptoViewConst.NET_EVENT_TYPE, networkType);
        b.putString(CryptoViewConst.NET_EVENT_DATA, networkData);
        b.putInt(CryptoViewConst.NET_EVENT_INDEX, index);
        b.putBoolean(CryptoViewConst.NET_EVENT_UPDATE, isUpdate);   //Ножно ли визульное обновление после сетевого запроса
        Message msg = netHandler.obtainMessage();
        msg.setData(b);
        netHandler.sendMessage(msg);
    }

    //Дополнительная обработка возвращаемой строки посл выполнения сетевого запроса
    //В случае ошибки возвращает null, иначе обработанная строка
    protected String extendedProcessNetworkResult(String resultString){
        if ( resultString==null ) return null;
        resultString = resultString.trim();
        if ( resultString==null ) return null;
        //Предварительный разбор JSON-строки
        int iB = resultString.indexOf("{");
        int iE = resultString.lastIndexOf("}");
        if ( iB==-1 || iE==-1 || iE<iB ) return null;
        return  resultString.substring(iB, iE+1);
    }

    /****************************************************************************************/
    //Выполнение сетевых запросов

    public void getRequestBinanceTicker(String stockUrl, String extendedPath){
        if ( !CryptoViewService.getRef().isOnline() ) {
            MainActivity.getRef().onNetworkError(CryptoViewService.getRef().getResources().getString(R.string.network_missing));
            return;
        }
        //
        Thread thread = new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void run() {
                //AstraWebClient awc = new AstraWebClient(MainActivity.getRef().getResources().getString(R.string.http_mobile_path), 10000, 15000);
                AstraWebClient awc = new AstraWebClient(stockUrl + extendedPath, 10000, 15000);
                if ( awc==null ) return;
                //
                awc.vSetConnectionFlagInput();
                //awc.vSetConnectionFlagOutput();
                //
                //String resultString = extendedProcessNetworkResult(awc.doGet());
                String resultString = awc.doGet();
                if ( resultString==null ) {
                    sendNetworkResultMessage(CryptoViewConst.NET_EVENT_NETWORK_ERROR, CryptoViewService.getRef().getResources().getString(R.string.invalid_params));
                    return;
                }
                sendNetworkResultMessage(CryptoViewConst.NET_EVENT_BINANCE_TICKERS, resultString);
            }
        });
        thread.start();
    }

    public void getRequestBitGetTicker(String stockUrl, String extendedPath) {
        if (!CryptoViewService.getRef().isOnline()) {
            MainActivity.getRef().onNetworkError(CryptoViewService.getRef().getResources().getString(R.string.network_missing));
            return;
        }
        Thread thread = new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void run() {
                AstraWebClient awc = new AstraWebClient(stockUrl + extendedPath, 10000, 15000);
                if ( awc==null ) return;
                //
                awc.vSetConnectionFlagInput();
                //
                //String resultString = extendedProcessNetworkResult(awc.doGet());
                String resultString = awc.doGet();
                if ( resultString==null ) {
                    sendNetworkResultMessage(CryptoViewConst.NET_EVENT_NETWORK_ERROR, CryptoViewService.getRef().getResources().getString(R.string.invalid_params));
                    return;
                }
                sendNetworkResultMessage(CryptoViewConst.NET_EVENT_BITGET_TICKERS, resultString);
            }
        });
        thread.start();
    }

    public void getRequestByBitTicker(String stockUrl, String extendedPath) {
        if (!CryptoViewService.getRef().isOnline()) {
            MainActivity.getRef().onNetworkError(CryptoViewService.getRef().getResources().getString(R.string.network_missing));
            return;
        }
        Thread thread = new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void run() {
                AstraWebClient awc = new AstraWebClient(stockUrl + extendedPath, 10000, 15000);
                if ( awc==null ) return;
                //
                awc.vSetConnectionFlagInput();
                //
                //String resultString = extendedProcessNetworkResult(awc.doGet());
                String resultString = awc.doGet();
                if ( resultString==null ) {
                    sendNetworkResultMessage(CryptoViewConst.NET_EVENT_NETWORK_ERROR, CryptoViewService.getRef().getResources().getString(R.string.invalid_params));
                    return;
                }
                sendNetworkResultMessage(CryptoViewConst.NET_EVENT_BYBIT_TICKERS, resultString);
            }
        });
        thread.start();
    }

    //Получит данные по дневной свече
    public void getRequestCandleBinance(String stockUrl, String extendedPath, String tickerName, int index, boolean isUpdate){
        if (!CryptoViewService.getRef().isOnline()) {
            MainActivity.getRef().onNetworkError(CryptoViewService.getRef().getResources().getString(R.string.network_missing));
            return;
        }
        Thread thread = new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void run() {
                AstraWebClient awc = new AstraWebClient(stockUrl + extendedPath, 10000, 15000);
                if ( awc==null ) return;
                //
                awc.CreateParamList();
                awc.AddParam("symbol", tickerName);
                awc.AddParam("interval", "1d");
                awc.AddParam("startTime", CryptoViewService.getRef().getBeginDayTimeMs());
                //
                awc.vSetConnectionFlagInput();
                awc.vSetConnectionFlagOutput();
                //
                //String resultString = extendedProcessNetworkResult(awc.doGet());
                String resultString = awc.doGet();
                if ( resultString==null ) {
                    sendNetworkResultMessage(CryptoViewConst.NET_EVENT_NETWORK_ERROR, CryptoViewService.getRef().getResources().getString(R.string.invalid_params));
                    return;
                }
                sendNetworkResultMessageWithExtendData(CryptoViewConst.NET_EVENT_BINANCE_CANDLE, resultString, index, isUpdate);
            }
        });
        thread.start();
    }

    public void getRequestCandleBitget(String stockUrl, String extendedPath, String tickerName, int index, boolean isUpdate){
        if (!CryptoViewService.getRef().isOnline()) {
            MainActivity.getRef().onNetworkError(CryptoViewService.getRef().getResources().getString(R.string.network_missing));
            return;
        }
        Thread thread = new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void run() {
                AstraWebClient awc = new AstraWebClient(stockUrl + extendedPath, 10000, 15000);
                if ( awc==null ) return;
                //
                awc.CreateParamList();
                awc.AddParam("symbol", tickerName);
                awc.AddParam("granularity", "1D");
                awc.AddParam("startTime", CryptoViewService.getRef().getBeginDayTimeMs());
                awc.AddParam("productType", "usdt-futures");
                //
                awc.vSetConnectionFlagInput();
                awc.vSetConnectionFlagOutput();
                //
                //String resultString = extendedProcessNetworkResult(awc.doGet());
                String resultString = awc.doGet();
                if ( resultString==null ) {
                    sendNetworkResultMessage(CryptoViewConst.NET_EVENT_NETWORK_ERROR, CryptoViewService.getRef().getResources().getString(R.string.invalid_params));
                    return;
                }
                sendNetworkResultMessageWithExtendData(CryptoViewConst.NET_EVENT_BITGET_CANDLE, resultString, index, isUpdate);
            }
        });
        thread.start();
    }

    public void getRequestCandleBybit(String stockUrl, String extendedPath, String tickerName, int index, boolean isUpdate){
        if (!CryptoViewService.getRef().isOnline()) {
            MainActivity.getRef().onNetworkError(CryptoViewService.getRef().getResources().getString(R.string.network_missing));
            return;
        }
        Thread thread = new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void run() {
                AstraWebClient awc = new AstraWebClient(stockUrl + extendedPath, 10000, 15000);
                if ( awc==null ) return;
                //
                awc.CreateParamList();
                awc.AddParam("symbol", tickerName);
                awc.AddParam("category", "spot");
                awc.AddParam("interval", "D");
                awc.AddParam("limit", 1);
                awc.AddParam("startTime", CryptoViewService.getRef().getBeginDayTimeMs());
                //
                awc.vSetConnectionFlagInput();
                awc.vSetConnectionFlagOutput();
                //
                //String resultString = extendedProcessNetworkResult(awc.doGet());
                String resultString = awc.doGet();
                if ( resultString==null ) {
                    sendNetworkResultMessage(CryptoViewConst.NET_EVENT_NETWORK_ERROR, CryptoViewService.getRef().getResources().getString(R.string.invalid_params));
                    return;
                }
                sendNetworkResultMessageWithExtendData(CryptoViewConst.NET_EVENT_BYBIT_CANDLE, resultString, index, isUpdate);
            }
        });
        thread.start();
    }

    //================================================================================================
    public void getCandleFromBinanceForWidget(String stockUrl, String extendedPath, String tickerName){
        if (!CryptoViewService.getRef().isOnline()) {
            CryptoViewWidgetConfigureActivity.getRef().onNetworkError(CryptoViewService.getRef().getResources().getString(R.string.network_missing));
            return;
        }
        Thread thread = new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void run() {
                AstraWebClient awc = new AstraWebClient(stockUrl + extendedPath, 10000, 15000);
                if ( awc==null ) return;
                //
                awc.CreateParamList();
                awc.AddParam("symbol", tickerName);
                awc.AddParam("interval", "1d");
                awc.AddParam("startTime", CryptoViewService.getRef().getBeginDayTimeMs());
                //
                awc.vSetConnectionFlagInput();
                awc.vSetConnectionFlagOutput();
                //
                //String resultString = extendedProcessNetworkResult(awc.doGet());
                String resultString = awc.doGet();
                if ( resultString==null ) {
                    sendNetworkResultMessage(CryptoViewConst.NET_EVENT_WIDGET_NETWORK_ERROR, CryptoViewService.getRef().getResources().getString(R.string.invalid_params));
                    return;
                }
                sendNetworkResultMessage(CryptoViewConst.NET_EVENT_WIDGET_BINANCE_CANDLE, resultString);
            }
        });
        thread.start();
    }

    public void getCandleFromBitgetForWidget(String stockUrl, String extendedPath, String tickerName){
        if (!CryptoViewService.getRef().isOnline()) {
            CryptoViewWidgetConfigureActivity.getRef().onNetworkError(CryptoViewService.getRef().getResources().getString(R.string.network_missing));
            return;
        }
        Thread thread = new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void run() {
                AstraWebClient awc = new AstraWebClient(stockUrl + extendedPath, 10000, 15000);
                if ( awc==null ) return;
                //
                awc.CreateParamList();
                awc.AddParam("symbol", tickerName);
                awc.AddParam("granularity", "1D");
                awc.AddParam("startTime", CryptoViewService.getRef().getBeginDayTimeMs());
                awc.AddParam("productType", "usdt-futures");
                //
                awc.vSetConnectionFlagInput();
                awc.vSetConnectionFlagOutput();
                //
                //String resultString = extendedProcessNetworkResult(awc.doGet());
                String resultString = awc.doGet();
                if ( resultString==null ) {
                    sendNetworkResultMessage(CryptoViewConst.NET_EVENT_WIDGET_NETWORK_ERROR, CryptoViewService.getRef().getResources().getString(R.string.invalid_params));
                    return;
                }
                sendNetworkResultMessage(CryptoViewConst.NET_EVENT_WIDGET_BITGET_CANDLE, resultString);
            }
        });
        thread.start();
    }

    public void getCandleFromBybitForWidget(String stockUrl, String extendedPath, String tickerName){
        if (!CryptoViewService.getRef().isOnline()) {
            CryptoViewWidgetConfigureActivity.getRef().onNetworkError(CryptoViewService.getRef().getResources().getString(R.string.network_missing));
            return;
        }
        Thread thread = new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void run() {
                AstraWebClient awc = new AstraWebClient(stockUrl + extendedPath, 10000, 15000);
                if ( awc==null ) return;
                //
                awc.CreateParamList();
                awc.AddParam("symbol", tickerName);
                awc.AddParam("category", "spot");
                awc.AddParam("interval", "D");
                awc.AddParam("limit", 1);
                awc.AddParam("startTime", CryptoViewService.getRef().getBeginDayTimeMs());
                //
                awc.vSetConnectionFlagInput();
                awc.vSetConnectionFlagOutput();
                //
                //String resultString = extendedProcessNetworkResult(awc.doGet());
                String resultString = awc.doGet();
                if ( resultString==null ) {
                    sendNetworkResultMessage(CryptoViewConst.NET_EVENT_WIDGET_NETWORK_ERROR, CryptoViewService.getRef().getResources().getString(R.string.invalid_params));
                    return;
                }
                sendNetworkResultMessage(CryptoViewConst.NET_EVENT_WIDGET_BYBIT_CANDLE, resultString);
            }
        });
        thread.start();
    }

}