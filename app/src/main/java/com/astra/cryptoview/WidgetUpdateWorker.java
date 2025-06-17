package com.astra.cryptoview;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

//
//Класс для организации фоновых процессов. В данном случае выполняется сетвеой запрос с параметрами
public class WidgetWorker extends Worker {

    public WidgetWorker(@NonNull Context context, @NonNull WorkerParameters params){
        super(context, params);
        //
        //

    }

    @NonNull
    @Override
    public Result doWork(){
        //Осуществляем мультиплексирование сетевого запроса

        //Выполняем сетевой запрос к соотвествующему ресурсу
        //После удачного применения осуществляем обновление виджета

        return Result.success();
    }

    //Сетевые запросы для получения данных

    //Запрос к Binance
    protected void getCandleBinance(){

    }

    //Запрос к Bitget
    protected void getCandleBitget(){

    }

    //Запрос к Bybit
    protected void getCandleBybit(){

    }


}
