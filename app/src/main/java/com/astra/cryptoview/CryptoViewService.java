package com.astra.cryptoview;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.XmlResourceParser;
import android.net.ConnectivityManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class CryptoViewService extends Service implements IServiceInterface{
    public static final String TAG = "Shalash Service";

    /*
    public static final String CRYPTO_VIEW_CURRENT_STOCK = "CurrentStock";      //Ключ текущей биржи
    public static final String CRYPTO_VIEW_CURRENT_TICKER = "CurrentTicker";    //Ключ текущего тикера
    public static final String CRYPTO_VIEW_CURRENT_TIMESET = "CurrentTimeSet";  //Ключ текущего временного отрезка
    */

    //Класс для локального связывания сервиса
    private final IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        public CryptoViewService getService() {
            return CryptoViewService.this;
        }
    }

    //@Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
    /****************************************************************************************/
    //Ссылка на самого себя
    private static CryptoViewService selfRef = null;
    public static CryptoViewService getRef() { return selfRef; }

    /****************************************************************************************/
    //Локальные данные
    HashMap<String, String> stockList;      //Хэш-таблица с именами (ключами) и URL-адресами (значениями) бирж
    ArrayList<FrequencyRecord> freqList;   //Список обхектов с настройками отметок времени

    //Тикеры с бирж
    public ArrayList<String> tickerBinanceList;
    public ArrayList<String> tickerBitgetList;
    public ArrayList<String> tickerBybitList;

    //Объект для взаимодействия с сервисом "Шалаш"
    private CryptoViewServiceCommunicator cryptoViewCom = null;

    /****************************************************************************************/
    @Override
    public void onCreate(){
        super.onCreate();
        synchronized(this){ selfRef = this; }
        //
        //Порождаем коммуникатор сетевого взаимодействия
        cryptoViewCom = new CryptoViewServiceCommunicator();
        //
        initData();         //Инициализируем данные
    }

    @Override
    public void onDestroy(){
        synchronized(this){
            cryptoViewCom = null;
            selfRef = null;
        }
        super.onDestroy();
    }

    //Инициализация данных
    private void initData() {
        this.stockList = getStockArrayFromResource();
        this.freqList = getFrequencyArrayFromResource();
        //
        //Считываем тикеры из настироек приложения и при необходимости выполняем сетевой запрос на получение
        this.tickerBinanceList = this.getListFromSharedPref(CryptoViewConst.CRYPTO_VIEW_BINANCE_TICKER);
        if ( this.tickerBinanceList==null || this.tickerBinanceList.size()==0 ) this.updateBinanceTickers();
        //
        this.tickerBitgetList = this.getListFromSharedPref(CryptoViewConst.CRYPTO_VIEW_BITGET_TICKER);
        if ( this.tickerBitgetList==null || this.tickerBitgetList.size()==0 ) this.updateBitGetTickers();
        //
        this.tickerBybitList = this.getListFromSharedPref(CryptoViewConst.CRYPTO_VIEW_BYBIT_TICKER);
        if ( this.tickerBybitList==null || this.tickerBybitList.size()==0 ) this.updateByBitTickers();
    }

    /****************************************************************************************/
    // Работа с биржами

    //Функция возвращает словарь с названиями бирж и их URL-адресами. Ключом является название биржи
    protected HashMap<String, String> getStockArrayFromResource(){
        HashMap<String, String> sL = new HashMap<String, String>();
        XmlResourceParser xmlParser = getResources().getXml(R.xml.stock);
        try {
            //Переменные для формирования записи в массив
            String stockName = null;
            String stockUrl = null;
            //
            for(int eventType = xmlParser.getEventType(); eventType!= XmlPullParser.END_DOCUMENT; eventType=xmlParser.next()){
                if ( eventType==XmlPullParser.START_TAG ) {
                    if (xmlParser.getName().equals("item")) {
                        stockName = xmlParser.getAttributeValue(null, "key");
                        if (stockName == null) {
                            break;
                        }
                    }
                } else if ( eventType==XmlPullParser.END_TAG ){
                    if (xmlParser.getName().equals("item")) {
                        sL.put(stockName, stockUrl);
                        stockName = null;
                        stockUrl = null;
                    }
                } else if ( eventType==XmlPullParser.TEXT ) {
                    stockUrl = xmlParser.getText();
                }
            }
            xmlParser.close();
        }
        catch(Exception e){
            Log.e(this.getClass().getName().toString(), e.getMessage().toString());
            stockList = null;
        }
        return sL;
    }

    //Возвращает список c названиями бирж
    public List getStockList(){
        List<String> stockListName = new ArrayList<String>();
        if ( this.stockList==null ) return stockListName;
        //
        Set<String> set = stockList.keySet();
        for(String s : set) stockListName.add(s);
        Collections.sort(stockListName);
        return stockListName;
    }

    //Возвращает URL-биржи по ключу (по её имени)
    public String getStockUrlByName(String stockName) {
        if ( this.stockList==null || stockName==null ) return null;
        return stockList.get(stockName);
    }

    /****************************************************************************************/
    //Отметки времени

    //Возвращает список объектов (отметок времени)
    protected ArrayList<FrequencyRecord> getFrequencyArrayFromResource(){
        ArrayList<FrequencyRecord> fl = new ArrayList<FrequencyRecord>();
        XmlResourceParser xmlParser = getResources().getXml(R.xml.frequency);
        //
        try {
            //Переменные для формирования записи в массив
            String key = null;
            int value = 0;
            String freqName = null;
            for(int eventType = xmlParser.getEventType(); eventType!= XmlPullParser.END_DOCUMENT; eventType=xmlParser.next()){
                if ( eventType==XmlPullParser.START_TAG ) {
                    if (xmlParser.getName().equals("item")) {
                        //Получаем ключ
                        key = xmlParser.getAttributeValue(null, "key");
                        if (key == null) break;
                        //Получаем значение
                        value = xmlParser.getAttributeIntValue(1, 100000);
                    }
                } else if ( eventType==XmlPullParser.END_TAG ){
                    if (xmlParser.getName().equals("item")) {
                        FrequencyRecord fr = new FrequencyRecord(key, value, freqName);
                        fl.add(fr);
                        //
                        key = null;
                        value = 0;
                        freqName = null;
                    }
                } else if ( eventType==XmlPullParser.TEXT ) {
                    //получаем текст названия частоты обновления
                    freqName = xmlParser.getText();
                }
            }
            xmlParser.close();
        }
        catch(Exception e){
            Log.e(this.getClass().getName().toString(), e.getMessage().toString());
        }
        return fl;
    }

    //Возвращает список отметок времени
    public List getFrequencyList(){
        List<String> frequencyName = new ArrayList<String>();
        if ( this.freqList==null ) return frequencyName;
        for(FrequencyRecord fr : this.freqList) frequencyName.add(fr.freqName);
        return frequencyName;
    }

    //Внутренний класс для работы с отметками частоты обновления
    protected class FrequencyRecord {
        public String key;      //Ассоциированный ключ
        public int value;       //Величина отметки времени в секундах
        public String freqName; //Наименгование частоты обновления
        FrequencyRecord(String key, int value, String freqName){
            this.key = key;
            this.value = value;
            this.freqName = freqName;
        }
    }

    public ArrayList<FrequencyRecord> getFreqList(){
        return this.freqList;
    }

    /****************************************************************************************/
    //Функция возвращает начало текущих суток в милисекундах
    public static long getBeginDayTimeMs(){
        Calendar c = Calendar.getInstance();
        return new Date(
                c.get(Calendar.YEAR)-1900,
                c.get(Calendar.MONTH),
                c.get(Calendar.DATE),
                0, 0, 0).getTime();
    }

    //Конвертирует милисекунды в объект Дата-время
    public Date getDateFromMs(long ms){
        return new Date(ms);
    }

    //Конвертация объекта даты-время в строку
    public String getDateToString(Date d) {
        DateFormat dateFormat = new SimpleDateFormat("yyy MM dd HH:mm:ss");
        return dateFormat.format(d);
    }

    /****************************************************************************************/
    //Обновить списки тикеры по всем биржам
    public void updateAllTickers(){
        updateBinanceTickers();
        updateBitGetTickers();
        updateByBitTickers();
    }

    //Обновить дневные свечи по всек выбранным тикерам
    public void updateAllCandles(CandleList candleList){
        if ( !this.isOnline() ) {
            MainActivity.getRef().onNetworkError(getResources().getString(R.string.network_missing));
            return;
        }
        //Создаём таймер и через каждые три секунды отправляем сетевой запрос на обновление данных
        MainActivity.getRef().onBeginUpdateCandles();       //Включаем ProgressDialog
        for(int i = 0; i<candleList.getList().size(); i++){
            CandleItem candleItem = candleList.getList().get(i);
            this.getCandle(i, candleItem.stockName, candleItem.tickerName, i==candleList.getList().size()-1);
            //Делаем искуственную задержку в 3 секунды
            try {
                TimeUnit.SECONDS.sleep(1L);
            }
            catch(InterruptedException e){
                Log.e(TAG, e.getMessage().toString());
            }
        }
        MainActivity.getRef().onEndUpdateCandles();         //Выключаем ProgressDialog
    }

    //Сделать сетевой запрос и получить новую свечу. Параметры:
    //index - номер записи в списке отслеживаемых позиций
    public void getCandle(int index, @NonNull String stockName, String tickerName, boolean isUpdate){
        //Выполняем мультиплексирование вызова
        if ( stockName.equals("Binance") ) getDayCandleFromBinance(index, tickerName, isUpdate);
        else if ( stockName.equals("Bitget") )  getDayCandleFromBitget(index, tickerName, isUpdate);
        else if ( stockName.equals("Bybit") ) getDayCandleFromBybit(index, tickerName, isUpdate);
    }

    /****************************************************************************************/
    //Выполнение HTTP-запросов по тикерам к биржам

    //Обновить тикеры с Binance
    protected void updateBinanceTickers(){
        String extendedPath = "/api/v3/ticker/price";
        if ( this.cryptoViewCom!=null ) cryptoViewCom.getRequestBinanceTicker(getStockUrlByName("Binance"), extendedPath);
    }

    //Обновить тикеры с BitGet
    protected void updateBitGetTickers(){
        String extendedPath = "/api/spot/v1/market/tickers";
        if ( this.cryptoViewCom!=null ) cryptoViewCom.getRequestBitGetTicker(getStockUrlByName("Bitget"), extendedPath);
    }

    //Обновить тикеры с Bybit
    protected void updateByBitTickers(){
        String extendedPath = "/v5/market/tickers?category=spot";
        if ( this.cryptoViewCom!=null ) cryptoViewCom.getRequestByBitTicker(getStockUrlByName("Bybit"), extendedPath);
    }

    //Получить дневную свечу с Binance
    protected void getDayCandleFromBinance(int index, String tickerName, boolean isUpdate){
        if ( this.cryptoViewCom==null ) return;
        String extendedPath = "/api/v3/klines";
        cryptoViewCom.getRequestCandleBinance(getStockUrlByName("Binance"), extendedPath, tickerName, index, isUpdate);
    }

    //Получить дневную свечу с BitGet
    protected void getDayCandleFromBitget(int index, String tickerName, boolean isUpdate){
        if ( this.cryptoViewCom==null ) return;
        String extendedPath = "/api/v2/mix/market/candles";
        cryptoViewCom.getRequestCandleBitget(getStockUrlByName("Bitget"), extendedPath, tickerName, index, isUpdate);
    }

    //Получить дневную свечу с Bybit
    protected void getDayCandleFromBybit(int index, String tickerName, boolean isUpdate){
        if ( this.cryptoViewCom==null ) return;
        String extendedPath = "/v5/market/kline";
        cryptoViewCom.getRequestCandleBybit(getStockUrlByName("Bybit"), extendedPath, tickerName, index, isUpdate);
    }

    //А это вызов из виджета
    public void getDayCandleFromBinanceWidget(String tickerName){
        if ( this.cryptoViewCom==null ) return;
        String extendedPath = "/api/v3/klines";
        cryptoViewCom.getCandleFromBinanceForWidget(getStockUrlByName("Binance"), extendedPath, tickerName);
    }

    public void getDayCandleFromBitgetWidget(String tickerName){
        if ( this.cryptoViewCom==null ) return;
        String extendedPath = "/api/v2/mix/market/candles";
        cryptoViewCom.getCandleFromBitgetForWidget(getStockUrlByName("Bitget"), extendedPath, tickerName);
    }

    public void getDayCandleFromBybitWidget(String tickerName){
        if ( this.cryptoViewCom==null ) return;
        String extendedPath = "/v5/market/kline";
        cryptoViewCom.getCandleFromBybitForWidget(getStockUrlByName("Bybit"), extendedPath, tickerName);
    }


    /****************************************************************************************/
    // Функции интерфейсас IServiceInterface

    public void onBinanceTickerList(ArrayList<String> l){
        Collections.sort(l);    //Сортируем список
        this.tickerBinanceList = l;  //Запоминаем в переменную
        putListInSharedPref(l, CryptoViewConst.CRYPTO_VIEW_BINANCE_TICKER);
    }
    public void onBitgetTickerList(ArrayList<String> l){
        Collections.sort(l);
        this.tickerBitgetList = l;
        putListInSharedPref(l, CryptoViewConst.CRYPTO_VIEW_BITGET_TICKER);
    }
    public void onBybitTickerList(ArrayList<String> l){
        Collections.sort(l);
        this.tickerBybitList = l;
        putListInSharedPref(l, CryptoViewConst.CRYPTO_VIEW_BYBIT_TICKER);
    }

    //Обновление записи index c показателями свечи
    public void onUpdateCandle(int index, long dateTimeMs, double dO, double dH, double dL, double dC, double dV, boolean isUpdate){
        CandleList candleList = loadCandleList();
        if ( index<0 || index>=candleList.getList().size() ) return;
        candleList.getItem(index).setValue(dateTimeMs, dO, dH, dL, dC, dV); //Сохраняем новые показатели свечи
        saveCandleList(candleList);                             //Сохраняем массив в настройках
        //
        //А вот здесь нужно уведомить список об изменении показателей свечи
        if ( isUpdate ) MainActivity.getRef().onUpdateCandleList();
    }


    /****************************************************************************************/
    // Работа со свечами интересуемых криптопар

    //Загрузить список интересных целей из настроек
    public CandleList loadCandleList(){
        CandleList candleList = new CandleList();
        String jsonArrayString = getStringFromSharedPref(CryptoViewConst.CRYPTO_VIEW_CANDLE_LIST);
        try {
            JSONArray jsonArray = new JSONArray(jsonArrayString);
            for(int i=0; i<jsonArray.length(); i++){
                JSONObject jsonObject = (JSONObject)jsonArray.get(i);
                CandleItem candleItem = new CandleItem(
                        jsonObject.getString("STOCK"),
                        jsonObject.getString("TICKER"),
                        jsonObject.getString("FREQUENCY")
                    );
                candleItem.setValue(
                        jsonObject.getLong("DATETIME"),
                        jsonObject.getDouble("OPEN"),
                        jsonObject.getDouble("HIGH"),
                        jsonObject.getDouble("LOW"),
                        jsonObject.getDouble("CLOSE"),
                        jsonObject.getDouble("VOLUME"));
                candleList.addItem(candleItem);
            }
        }
        catch(JSONException e){
            Log.e(TAG, e.getMessage().toString());
        }
        return candleList;
    }

    //Сохраняем спсиок интересных целей в JSON-массив и далее в настройки
    public void saveCandleList(CandleList candleList){
        if ( candleList==null || candleList.getList().size()==0 ) return;
        JSONArray candleItemArray = new JSONArray();
        for(int i=0; i<candleList.getList().size(); i++){
            JSONObject candleItem = new JSONObject();
            try {
                candleItem.put("STOCK", candleList.getItem(i).stockName);
                candleItem.put("TICKER", candleList.getItem(i).tickerName);
                candleItem.put("FREQUENCY", candleList.getItem(i).freqName);
                candleItem.put("DATETIME", candleList.getItem(i).dateTimeMs);
                candleItem.put("OPEN", candleList.getItem(i).dOpen);
                candleItem.put("HIGH", candleList.getItem(i).dHigh);
                candleItem.put("LOW", candleList.getItem(i).dLow);
                candleItem.put("CLOSE", candleList.getItem(i).dClose);
                candleItem.put("VOLUME", candleList.getItem(i).dVolume);
                candleItemArray.put(candleItem);
            }
            catch (JSONException e){}
        }
        putStringToSharedPref(CryptoViewConst.CRYPTO_VIEW_CANDLE_LIST, candleItemArray.toString());
    }

    /****************************************************************************************/
    //Вспомогательные функции

    protected String serializeArrayListToString(ArrayList<String> l){
        return String.join("|", l);
    }

    protected ArrayList<String> deserializeStringToArraylist(String str){
        return new ArrayList<String>(Arrays.asList(str.split("\\|")));
    }

    public String getStringFromSharedPref(String keyStr){
        SharedPreferences sp = getSharedPreferences(CryptoViewConst.CRYPTO_VIEW_PREF, MODE_PRIVATE);
        if ( sp==null ) return "";
        return sp.getString(keyStr, "");
    }

    public void putStringToSharedPref(String keyStr, String strData){
        SharedPreferences sp = getSharedPreferences(CryptoViewConst.CRYPTO_VIEW_PREF, MODE_PRIVATE);
        if ( sp==null ) return;
        sp.edit().putString(keyStr, strData).commit();
    }

    //Сохранить объект (список) в
    protected void putListInSharedPref(ArrayList<String> l, String keyList){
        putStringToSharedPref(keyList, serializeArrayListToString(l));
    }

    //Получить список из
    protected ArrayList<String> getListFromSharedPref(String keyList){
        return deserializeStringToArraylist(getStringFromSharedPref(keyList));
    }

    /***********************************************************************************************************/
    //Проверка доступности сети
    public boolean isOnline(){
        boolean bc = false;
        try{
            ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
            bc = cm!=null && cm.getActiveNetworkInfo().isConnected();
        }
        catch(Exception e){
            Log.e(getClass().getName().toString(), e.getStackTrace().toString());
            Log.e(getClass().getName().toString(), e.getMessage().toString());
        }
        return bc;
    }

}