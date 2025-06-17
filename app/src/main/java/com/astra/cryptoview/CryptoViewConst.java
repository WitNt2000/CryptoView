package com.astra.cryptoview;

//Класс определения констант
public class CryptoViewConst {
    public static String ALARM_TIME = "ARARM_TIME";
    public static String ALARM_SERVICE = "CRYPTO_VIEW_ARARM_SERVICE";

    //Ключии сетевых запросов
    public static final String NET_EVENT_TYPE = "NET_EVENTS_TYPE";      //Тип сетевого события
    public static final String NET_EVENT_DATA = "NET_EVENTS_DATA";      //Полученные данные сетевого события
    public static final String NET_EVENT_INDEX = "NET_EVENTS_INDEX";    //Номер обновляемой записи списка интересуемых позиций
    public static final String NET_EVENT_UPDATE = "NET_EVENTS_UPDATE";  //Необходимость обновления визуального списка

    //Типы событий
    public static final int NET_EVENT_NETWORK_ERROR         = 0x0001;   //Сетевая ошибка
    public static final int NET_EVENT_SUCCESS_CONNECTION    = 0x0002;   //Успешная проверка связи с сервисом
    public static final int NET_EVENT_BINANCE_TICKERS       = 0x0003;   //Получены тикеры с бииржи binance
    public static final int NET_EVENT_BITGET_TICKERS        = 0x0004;   //Получены тикеры с бииржи bitget
    public static final int NET_EVENT_BYBIT_TICKERS         = 0x0005;   //Получены тикеры с бииржи bybit

    public static final int NET_EVENT_BINANCE_CANDLE        = 0x0006;   //Получена свеча тикеры с биржи binance
    public static final int NET_EVENT_BITGET_CANDLE         = 0x0007;   //Получена свеча тикеры с биржи bitget
    public static final int NET_EVENT_BYBIT_CANDLE          = 0x0008;   //Получена свеча с биржи bybit

    //
    public static final int NET_EVENT_WIDGET_NETWORK_ERROR  = 0x0010;   //Сетевая ошибка для виджета
    public static final int NET_EVENT_WIDGET_BINANCE_CANDLE = 0x0020;   //Получена свеча тикеры с биржи binance для виджета
    public static final int NET_EVENT_WIDGET_BITGET_CANDLE  = 0x0030;   //Получена свеча тикеры с биржи bitget для виджета
    public static final int NET_EVENT_WIDGET_BYBIT_CANDLE   = 0x0040;   //Получена свеча тикеры с биржи bybit для виджета
    //
    //
    public static final String CRYPTO_VIEW_PREF   = "CryptoViewPref";           //Наимнование ключа настроек приложения
    //Ключи настроек приложения
    public static final String CRYPTO_VIEW_BINANCE_TICKER = "BinanceTicker";    //Ключ списка тикеров с биржи Binance
    public static final String CRYPTO_VIEW_BITGET_TICKER = "BitgetTicker";      //Ключ списка тикеров с биржи Bitget
    public static final String CRYPTO_VIEW_BYBIT_TICKER = "BybitTicker";        //Ключ списка тикеров с биржи Bybit
    public static final String CRYPTO_VIEW_CANDLE_LIST = "CandleList";          //Ключ текущего списка отслеживаемых свечей
    //
    //Ключи настроек виджета
    public static final String CRYPTO_VIEW_WIDGET_STOCK = "WidgetStock_";               //Имя биржb для виджета
    public static final String CRYPTO_VIEW_WIDGET_TICKER = "WidgetTicker_";             //Имя тикера для виджета
    public static final String CRYPTO_VIEW_WIDGET_TIMESET = "WidgetTimeSet_";           //Имя временной отсечки обновления информации для виджета
    public static final String CRYPTO_VIEW_WIDGET_PERIOD = "WidgetPeriod_";             //Величина временной отсечки обновления информации для виджета
    public static final String CRYPTO_VIEW_WIDGET_COLOR_THEME = "WidgetColorTheme_";    //Тема для букв виджета (тёмные или светлые)
    public static final String CRYPTO_VIEW_WIDGET_VOLUME = "WidgetVolume_";             //Объём сделок
    public static final String CRYPTO_VIEW_WIDGET_CLOSE = "WidgetClose_";               //Цена закрытия
    public static final String CRYPTO_VIEW_WIDGET_CHANGE = "WidgetChange_";             //Изменения цены по отношению к открытию
    public static final String CRYPTO_VIEW_WIDGET_PERCENT = "WidgetPercent_";           //Изменения в процентах


}
