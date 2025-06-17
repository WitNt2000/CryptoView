package com.astra.cryptoview;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import java.util.List;

public class AddTickerFragment extends Fragment {
    public static final String TAG = "AddTckerFragment";

    protected List stockList = null;
    protected List frequencyList = null;
    protected List tickerList = null;
    protected Spinner spinnerStock = null;
    protected Spinner spinnerTicker = null;
    protected Spinner spinnerFrequency = null;

    protected ArrayAdapter<String> stockAdapter = null;
    protected ArrayAdapter<String> tickerAdapter = null;
    protected ArrayAdapter<String> frequencyAdapter = null ;

    //Конструктор
    public AddTickerFragment() {
        // Пустой конструктор
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //
        stockList = CryptoViewService.getRef().getStockList();
        frequencyList = CryptoViewService.getRef().getFrequencyList();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_add_ticker, container, false);
        //
        //Получаем управляющие элементы диалога
        spinnerStock = (Spinner)v.findViewById(R.id.spinner_stock);
        spinnerTicker = (Spinner)v.findViewById(R.id.spinner_ticker);
        spinnerFrequency = (Spinner)v.findViewById(R.id.spinner_frequency);
        //
        //Теперь адаптеры
        stockAdapter = new ArrayAdapter<>(this.getContext(), R.layout.adapter_record, R.id.idRecord, stockList);
        spinnerStock.setAdapter(stockAdapter);
        //
        frequencyAdapter = new ArrayAdapter<>(this.getContext(), R.layout.adapter_record, R.id.idRecord, frequencyList);
        spinnerFrequency.setAdapter(frequencyAdapter);
        //
        spinnerTicker.post(new Runnable() {
            @Override
            public void run() {
                createTickerSpinner();
                refreshSpinnerTickerView();
            }
        });

        spinnerStock.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            //@Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String stockName = stockList.get(position).toString();
                //
                if ( stockName.equals("Binance") ) tickerList = CryptoViewService.getRef().tickerBinanceList;
                else if ( stockName.equals("Bitget") )  tickerList = CryptoViewService.getRef().tickerBitgetList;
                else tickerList = CryptoViewService.getRef().tickerBybitList;
                //
                spinnerTicker.post(new Runnable() {
                    @Override
                    public void run() {
                        createTickerSpinner();
                        refreshSpinnerTickerView();
                    }
                });
            }

            //@Override
            public void onNothingSelected(AdapterView<?> parent) {
                //И голо, и пусто, и ветер гуляет...
            }
        });

        //
        //А теперь кнопки
        ((Button)v.findViewById(R.id.id_ok)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String stockName = spinnerStock.getSelectedItem().toString();
                String tickerName = spinnerTicker.getSelectedItem().toString();
                String freqName = spinnerFrequency.getSelectedItem().toString();
                //
                //Записываем новую запись в список отслеживаемых позиций
                MainActivity.getRef().onAddTickerSuccess(stockName, tickerName, freqName);
            }
        });
        ((Button)v.findViewById(R.id.id_cancel)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.getRef().onBackPressed();
            }
        });
        return v;
    }

    public void onDestroy(){
        stockList = null;
        tickerList = null;
        frequencyList = null;
        //
        spinnerStock = null;
        spinnerTicker = null;
        spinnerFrequency = null;
        //
        stockAdapter = null;
        tickerAdapter = null;
        frequencyAdapter = null ;
        //
        super.onDestroy();
    }

    //Дополнительные функции
    public void refreshSpinnerTickerView(){
        spinnerTicker.invalidate();
    }

    protected void createTickerSpinner(){
        tickerAdapter = null;
        //
        if ( tickerList==null ) tickerList = CryptoViewService.getRef().tickerBinanceList;
        tickerAdapter = new ArrayAdapter<>(this.getContext(), R.layout.adapter_record, R.id.idRecord, tickerList);
        spinnerTicker.setAdapter(tickerAdapter);
    }

}