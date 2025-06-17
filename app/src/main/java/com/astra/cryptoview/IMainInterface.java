package com.astra.cryptoview;

public interface IMainInterface {
    public void onNetworkError(final String sErr);                  //Ошибка сети (в параметре причина ошибки)
    public void onTestConnectionSuccess(final String successString);//Успешное выполнение сетевого запроса
    public void onAddTickerSuccess(String stockName, String tickerName, String timesetName);//Успешное добавление новой записи в список отслеживаемых позициий
    public void onAddTickerCancel();                                //Отказ от добавления новой записи
    public void onUpdateCandleList();                               //Обратный вызов для обновления списка свечей
    public void onUpdateCandle(int index, long dateTimeMs, double dO, double dH, double dL, double dC, double dV, boolean isUpdate);
    public void onBeginUpdateCandles();                             //Начало обновления списка свечей
    public void onEndUpdateCandles();                               //Конец обновления списка свечей
}
