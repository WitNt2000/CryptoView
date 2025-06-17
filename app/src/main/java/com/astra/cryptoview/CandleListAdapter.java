package com.astra.cryptoview;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class CandleListAdapter extends BaseAdapter {
    private Context context;
    protected CandleList candleList;
    private LayoutInflater layoutInflater;

    //Конструктор
    CandleListAdapter(Context context){
        this.context = context;
        this.layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        //
        this.candleList = new CandleList();
    }

    @Override
    public int getCount() {
        return this.candleList==null ? 0 : this.candleList.getList().size();
    }

    @Override
    public Object getItem(int iPosition){
        return this.candleList==null ? null : candleList.getItem(iPosition);
    }

    @Override
    public long getItemId(int iPosition) {
        return iPosition;
    }

    @Override
    public View getView(int iPosition, View convertView, ViewGroup parent){
        View view = convertView;
        if ( view==null ) view = (View)layoutInflater.inflate(R.layout.adapter_candle_item, parent, false);
        view.setBackgroundColor(iPosition%2>0 ? 0xFFEEEEEE : 0xFFE0E0E0);
        //
        CandleItem candleItem = candleList.getItem(iPosition);
        ((TextView)view.findViewById(R.id.id_stock_name)).setText(candleItem.stockName);
        ((TextView)view.findViewById(R.id.id_ticker_name)).setText(candleItem.tickerName);
        //
        ((TextView)view.findViewById(R.id.id_frequency_name)).setText(candleItem.freqName);
        //
        ((TextView)view.findViewById(R.id.id_open)).setText(candleItem.getOpen());
        ((TextView)view.findViewById(R.id.id_high)).setText(candleItem.getHigh());
        ((TextView)view.findViewById(R.id.id_low)).setText(candleItem.getLow());
        ((TextView)view.findViewById(R.id.id_close)).setText(candleItem.getClose());
        ((TextView)view.findViewById(R.id.id_volume)).setText(candleItem.getVolume());
        //
        //Абсолютные изменения
        TextView changeText = (TextView)view.findViewById(R.id.id_change);
        changeText.setText(candleItem.getChange());
        changeText.setTextColor(candleItem.getColor());
        //
        //Относительные изменения
        TextView percentText = (TextView)view.findViewById(R.id.id_percent);
        percentText.setText(candleItem.getPercent());
        percentText.setTextColor(candleItem.getColor());
        //
        ((TextView)view.findViewById(R.id.id_date)).setText(candleItem.getDateTime());
        //
        return view;
    }

    /***********************************************************************************************/
    //Методы расширения функциональности адаптера

    //Получить список данных
    public CandleList getCandleList(){
        return this.candleList;
    }

    //Обновить свечу из списка с номером index
    public void updatePosition(int index){
        if ( CryptoViewService.getRef()==null ) return;
        CandleItem candleItem = this.candleList.getList().get(index);
        CryptoViewService.getRef().getCandle(index, candleItem.stockName, candleItem.tickerName, true);
    }

    //Загрузить данные - список свечей
    public void loadData(){
        if ( CryptoViewService.getRef()==null ) return;
        this.candleList.getList().clear();
        this.candleList.getList().addAll(CryptoViewService.getRef().loadCandleList().getList());
    }

    //Обновить адаптер
    public void refreshAdapter(){
        this.notifyDataSetInvalidated();
        this.notifyDataSetChanged();
    }

}
