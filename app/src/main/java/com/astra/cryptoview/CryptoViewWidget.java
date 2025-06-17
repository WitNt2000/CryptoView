package com.astra.cryptoview;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.RemoteViews;

public class CryptoViewWidget extends AppWidgetProvider {
    public final static String TAG = "CryptoViewWidget";
    public static final String formatValue = "%12.2f";
    public static final String formatChange = "%6.2f%%";

    @Override
    public void onEnabled(Context context){
        super.onEnabled(context);
        Log.e(TAG, "CryptoViewWidget.onEnabled");
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds){
        //super.onUpdate(context, appWidgetManager, appWidgetIds);
        Log.e(TAG, "CryptoViewWidget.onUpdate");
        //
        for(int appWidgetId : appWidgetIds){
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent){
        super.onReceive(context, intent);
        Log.e(TAG, "CryptoViewWidget.onReceive");
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds){
        super.onDeleted(context, appWidgetIds);
        Log.e(TAG, "CryptoViewWidget.onDeleted");
        //
        //Удаляем настройки
        SharedPreferences.Editor editor = context.getSharedPreferences(CryptoViewConst.CRYPTO_VIEW_PREF, Context.MODE_PRIVATE).edit();
        for(int appWidgetId : appWidgetIds){
            editor.remove(CryptoViewConst.CRYPTO_VIEW_WIDGET_STOCK + appWidgetId);
            editor.remove(CryptoViewConst.CRYPTO_VIEW_WIDGET_TICKER + appWidgetId);
            editor.remove(CryptoViewConst.CRYPTO_VIEW_WIDGET_TIMESET + appWidgetId);
            editor.remove(CryptoViewConst.CRYPTO_VIEW_WIDGET_PERIOD + appWidgetId);
            editor.remove(CryptoViewConst.CRYPTO_VIEW_WIDGET_COLOR_THEME + appWidgetId);
            //
            editor.remove(CryptoViewConst.CRYPTO_VIEW_WIDGET_VOLUME + appWidgetId);
            editor.remove(CryptoViewConst.CRYPTO_VIEW_WIDGET_CLOSE + appWidgetId);
            editor.remove(CryptoViewConst.CRYPTO_VIEW_WIDGET_CHANGE + appWidgetId);
            editor.remove(CryptoViewConst.CRYPTO_VIEW_WIDGET_PERCENT + appWidgetId);
            //
            //Здесь ещё отменяем планировщик задания
            WidgetUpdateTaskScheduler.cancelWidgetUpdateWorker(context, appWidgetId);
        }
        editor.commit();
    }

    @Override
    public void onDisabled(Context context){
        super.onDisabled(context);
        Log.e(TAG, "CryptoViewWidget.onDisabled");
    }

    //==================================================================================
    //Функции форматирования чисел
    public static String getFormatValue(double value){
        return String.format(formatValue, value).trim();
    }

    public static String getFormatChange(double value){
        return String.format(formatChange, value).trim();
    }


    public static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        // Создаем RemoteViews и настраиваем его содержимое
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.adapter_widget);
        //
        //Формируем ключи получения данных из настроек
        SharedPreferences sp = context.getSharedPreferences(CryptoViewConst.CRYPTO_VIEW_PREF, Context.MODE_PRIVATE);
        String keyStock     = CryptoViewConst.CRYPTO_VIEW_WIDGET_STOCK + appWidgetId;
        String keyTicker    = CryptoViewConst.CRYPTO_VIEW_WIDGET_TICKER + appWidgetId;
        String keyFreq      = CryptoViewConst.CRYPTO_VIEW_WIDGET_TIMESET + appWidgetId;
        String keyDarkText  = CryptoViewConst.CRYPTO_VIEW_WIDGET_COLOR_THEME + appWidgetId;
        //
        String keyVolume    = CryptoViewConst.CRYPTO_VIEW_WIDGET_VOLUME + appWidgetId;
        String keyClose     = CryptoViewConst.CRYPTO_VIEW_WIDGET_CLOSE + appWidgetId;
        String keyChange    = CryptoViewConst.CRYPTO_VIEW_WIDGET_CHANGE + appWidgetId;
        String keyPercent   = CryptoViewConst.CRYPTO_VIEW_WIDGET_PERCENT + appWidgetId;
        //
        //Теперь получаем данные
        boolean bDark = sp.getBoolean(keyDarkText, false);
        int colorBase = context.getResources().getColor(R.color.gray);
        int colorRed = context.getResources().getColor(bDark ? R.color.dark_red : R.color.light_red);
        int colorGreen = context.getResources().getColor(bDark ? R.color.dark_green : R.color.light_green);
        //
        views.setTextViewText(R.id.id_stock_name, sp.getString(keyStock, ""));
        views.setTextColor(R.id.id_stock_name, colorBase);
        views.setTextViewText(R.id.id_ticker_name, sp.getString(keyTicker, ""));
        views.setTextColor(R.id.id_ticker_name, colorBase);
        //
        views.setTextViewText(R.id.id_volume, getFormatValue(sp.getFloat(keyVolume, (float)0.0)));
        views.setTextColor(R.id.id_volume, colorBase);
        views.setTextViewText(R.id.id_close_value, getFormatValue(sp.getFloat(keyClose, (float)0.0)));
        views.setTextColor(R.id.id_close_value, colorBase);
        //
        float dV = sp.getFloat(keyChange, (float)0.0);
        views.setTextViewText(R.id.id_change_value, getFormatValue(dV));
        views.setTextColor(R.id.id_change_value, dV>0.0 ? colorGreen : ( dV<0.0 ? colorRed : colorBase));
        //
        dV = sp.getFloat(keyPercent, (float)0.0);
        views.setTextViewText(R.id.id_percent_value, getFormatChange(dV));
        views.setTextColor(R.id.id_percent_value, dV>0.0 ? colorGreen : ( dV<0.0 ? colorRed : colorBase));
        //
        //Настраиваем реакцию на нажатие виджета для его настроек
        Intent configIntent = new Intent(context, CryptoViewWidgetConfigureActivity.class);
        configIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        //PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, configIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, appWidgetId, configIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.settings_button, pendingIntent);
        //
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

}
