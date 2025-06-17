package com.astra.cryptoview;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CandleList {
    protected List<CandleItem> candleItemList = null;

    public CandleList(){
        candleItemList = new ArrayList<CandleItem>();
    }

    ///Возвращает список интересных целей
    public List<CandleItem> getList(){ return this.candleItemList; }

    //Взять элемент с индексом index
    public CandleItem getItem(int index){
        return index<0 || index>candleItemList.size() ? null : candleItemList.get(index);
    }

    //Добавить запись
    public void addItem(CandleItem candleItem){
        if ( candleItem==null ) return;
        candleItemList.add(candleItem);
        saveItemArray();
    }

    //Удалить запись
    public void removeItem(int index){
        if ( index < 0 || index > candleItemList.size() ) return;
        candleItemList.remove(index);
        saveItemArray();
    }

    //Передвинуть запиь вперёд
    public void upItem(int index){
        if ( index < 1 || index > candleItemList.size() ) return;
        Collections.swap(candleItemList, index, index-1);
        saveItemArray();
    }

    //Передвинуть запиьь назад
    public void downItem(int index){
        if ( index < 0 || index >= candleItemList.size()-1 ) return;
        Collections.swap(candleItemList, index, index+1);
        saveItemArray();
    }

    //Вспомогательные функции, использующие функции сервиса
    protected void saveItemArray(){
        CryptoViewService.getRef().saveCandleList(this);
    }

}
