package com.astra.cryptoview;

import android.app.Activity;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.List;

public class CandleListFragment extends Fragment {
    public static final String TAG = "CandleListFragment";
    protected ListView candleListView = null;
    protected CandleListAdapter candleListAdapter = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_candles_list, container, false);
        //Порождаем адаптер
        if ( this.candleListAdapter==null ) this.candleListAdapter = new CandleListAdapter(MainActivity.getRef().getBaseContext());
        //
        if ( this.candleListView==null ) {
            this.candleListView = (ListView)v.findViewById(R.id.candleList);
            this.candleListView.setAdapter(candleListAdapter);
            //Подключаем контекстное меню для списка
            registerForContextMenu(candleListView);
        }
        return v;
    }

    public void onDestroyView(){
        super.onDestroyView();
        //Отключаем контекстное меню
        unregisterForContextMenu(candleListView);
        //Обнуляем данные
        candleListView = null;
        candleListAdapter = null;
    }

    public CandleListAdapter getAdapter(){
        return this.candleListAdapter;
    }

    /*****************************************************************************************************/
    // Контекстное меню списка

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo){
        super.onCreateContextMenu(menu, v, menuInfo);
        getActivity().getMenuInflater().inflate(R.menu.menu_context_ticket_item, menu);
    }

    //Обработчики меню и контектного меню
    @Override
    public boolean onContextItemSelected(MenuItem item){
        //Определяем номер позиции в списке
        int position = ((AdapterView.AdapterContextMenuInfo)item.getMenuInfo()).position;
        //
        int id = item.getItemId();
        if ( id==R.id.action_up ) {
            candleListAdapter.candleList.upItem(position);
            refreshCandleList();
        } else if ( id==R.id.action_delete ) {
            candleListAdapter.candleList.removeItem(position);
            refreshCandleList();
        } else if ( id==R.id.action_update ){
            candleListAdapter.updatePosition(position);
        } else if ( id==R.id.action_down ){
            candleListAdapter.candleList.downItem(position);
            refreshCandleList();
        }
        return super.onContextItemSelected(item);
    }

    /*****************************************************************************************************/
    //Вспомогательные функции фрагмента

    public void loadCandleListFromSettings(){
        candleListAdapter.loadData();
    }

    public void addNewRecord(String stockName, String tickerName, String freqName){
        if ( candleListAdapter.getCandleList()==null ) candleListAdapter.loadData();
        candleListAdapter.getCandleList().addItem(new CandleItem(stockName, tickerName, freqName));
        //Здесь нужно сделать сетевой запрос на получение свечи для новой записи интересных целей
        CryptoViewService.getRef().getCandle(candleListAdapter.getCandleList().getList().size()-1, stockName, tickerName, true);
    }

    //Обновить список
    public void refreshCandleList(){
        candleListAdapter.refreshAdapter();
        candleListView.invalidate();
    }

}