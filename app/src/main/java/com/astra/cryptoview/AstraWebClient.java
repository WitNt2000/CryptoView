/******************************************************************************************/
/* Класс реализует работу HTTP-клиента, использую внутренние вызовы Андроиа               */
/* Copyright (c) 2020 Nechitailo Vitali E-mail: WitNt2000@mail.ru                         */
/******************************************************************************************/
package com.astra.cryptoview;

import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.MimeTypeMap;

//import com.google.zxing.common.StringUtils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AstraWebClient {
    // Константы
    static final String COOKIES_HEADER = "Set-Cookie";
    //
    public static final int CONNECTION_RESET = 0x00;    //Исходной состояние флагов соединения
    public static final int CONNECTION_INPUT = 0x01;    //Флаг необходимости включения ввода соединения (считать полученные данные)
    public static final int CONNECTION_OUTPUT = 0x02;   //Флаг необходимости включения вывода соединения (записать данные-параметры в выходной поток соедиения)
    public static final int CONNECTION_CACHE = 0x04;    //Включить кэширование соединения
    //
    public static final String CRLF = "\r\n";           //Перевод строки, возврат каретки
    public static final String TWOHYPHENS = "--";       //Два дефиса
    public static final int BUFFER_SIZE = 4096;         //Размер буфера файла
    //Данные
    private String stringUrl;   //Адрес ресурса для запроса
    //
    HttpURLConnection httpConnection = null;
    //
    private Map<String, Object> parList = null;		//Список параметров запроса
    private Map<String, String> headerList = null;  //Список дополнительных заголовков в запрос
    private Map<String, String> fileList = null;    //Список загружаемых файлов
    //
    private Map<String, List<String>> responseHeaders = null;   //Заголовки ответа
    //
    private Map<String, String> cookiesList = null;             //Массив куки

    private int connectionFlag = 0;

    private int readTimeout = 0;
    private int connectionTimeout = 0;

    //Конструктор
    public AstraWebClient(String stringUrl, int readTimeout, int connectionTimeout){
        this.stringUrl = stringUrl;
        this.readTimeout = readTimeout;
        this.connectionTimeout = connectionTimeout;
    }
    /*****************************************************************************************/
    // Методы для работы с флагами соедиения
    public void vResetConnectionFlag(){ connectionFlag = CONNECTION_RESET; }
    public int iGetConnectionFlag(){ return connectionFlag; }
    //Флаг открытия входного потока (чтение результата)
    public void vSetConnectionFlagInput(){ connectionFlag |= CONNECTION_INPUT; }
    public void vResetConnectionFlagInput(){ connectionFlag &= ~CONNECTION_INPUT; }
    public boolean bGetConnectionFlagInput(){ return (connectionFlag & CONNECTION_INPUT) > 0; }
    //Флаг открытия выходного потока (передача параметров)
    public void vSetConnectionFlagOutput(){ connectionFlag |= CONNECTION_OUTPUT; }
    public void vResetConnectionFlagOutput(){ connectionFlag &= ~CONNECTION_OUTPUT; }
    public boolean bGetConnectionFlagOutput(){ return (connectionFlag & CONNECTION_OUTPUT) > 0 ; }
    //Флаг кэширования
    public void vSetConnectionFlagCache(){ connectionFlag |= CONNECTION_CACHE; }
    public void vResetConnectionFlagCache(){ connectionFlag &= ~CONNECTION_CACHE; }
    public boolean bGetConnectionFlagCache(){ return (connectionFlag & CONNECTION_CACHE) > 0 ; }

    /*****************************************************************************************/
    public void DestroyData(){
        DeleteParamList();
        DeleteHeaderList();
        DeleteFileList();
        DeleteCookiesList();
    }

    /*****************************************************************************************/
    //Создать список параметров
    public void CreateParamList(){
        DeleteParamList();
        parList = new HashMap<>();
    }
    //Очистить список параметров
    public void DeleteParamList(){
        if ( parList!=null ) { parList.clear(); parList = null; }
    }
    //Добавить параметр к списку
    public void AddParam(String name, Object val){
        if ( parList==null ) CreateParamList();
        parList.put(name, val);
    }

    /*****************************************************************************************/
    //Создать список дополнительных заголовков запроса
    public void CreateHeaderList(){
        DeleteHeaderList();
        headerList = new HashMap<>();
    }
    //Удалить список дополнительных заголовков запроса
    public void DeleteHeaderList(){
        if ( headerList!=null ) { headerList.clear(); headerList = null; }
    }
    //Добавить параметр к списку дополнительных заголовков запроса
    public void AddHeader(String name, String val){
        if ( headerList==null ) CreateHeaderList();
        headerList.put(name, val);
    }
    //Получить значение заголовка по ключу
    public String GetItemFromHeaderList(String headerKey){
        if ( headerList==null || headerKey==null || headerKey.length()==0 ) return null;
        for(Map.Entry<String, String> entry : headerList.entrySet() ){
            if ( entry.getKey().equals(headerKey) ) return entry.getValue();
        }
        return null;
    }

    /*****************************************************************************************/
    //Создать список имён файлов
    public void CreateFileList() {
        DeleteFileList();
        fileList = new HashMap<>();
    }
    //Очистить список имён файлов
    public void DeleteFileList() {
        if ( fileList!=null ) { fileList.clear(); fileList = null; }
    }
    //Добавить файл в список. Параметры:
    //fieldName - имя поля в форме
    //fileName - полное имя файла
    public void AddFile(String fieldName, String fileName){
        if ( fileList==null ) CreateFileList();
        fileList.put(fieldName, fileName);
    }

    /*****************************************************************************************/
    //Создать список Cookies
    public void CreateCookieList(){
        DeleteCookiesList();
        cookiesList = new HashMap<>();
    }
    //Очистить спаисок Cookies
    public void DeleteCookiesList(){
        if ( cookiesList!=null ) { cookiesList.clear(); cookiesList = null; }
    }
    //Добавить cookie в список
    public void AddCookie(String cookieKey, String cookieValue){
        if ( cookiesList==null ) CreateCookieList();
        if ( cookieKey==null || cookieKey.length()==0 ) return;
        if ( getCookie(cookieKey)==null ) cookiesList.put(cookieKey, cookieValue);
        else setCookie(cookieKey, cookieValue);
    }
    //Удалить Cookie из списка
    public void RemoveCookie(String cookieKey){
        if ( cookiesList==null ) return;
        for(Map.Entry<String, String> entry : cookiesList.entrySet() ){
            if ( entry.getKey().equals(cookieKey) ) cookiesList.remove(entry);
        }
    }
    //Установить Cookie в новое значение
    public void setCookie(String cookieKey, String cookieValue){
        if ( cookiesList==null ) return;
        for(Map.Entry<String, String> entry : cookiesList.entrySet() ){
            if ( entry.getKey().equals(cookieKey) ) entry.setValue(cookieValue);
        }
    }
    //Получить значение cookie с ключом
    public String getCookie(String cookieKey){
        if ( cookiesList==null || cookieKey==null || cookieKey.length()==0 ) return null;
        for(Map.Entry<String, String> entry : cookiesList.entrySet() ){
            if ( entry.getKey().equals(cookieKey) ) return entry.getValue();
        }
        return null;
    }

    /*****************************************************************************************/

    public String doGet(){
        String result = null;
        if ( stringUrl==null ) return result;
        try {
            String query = null;
            if ( parList!=null && parList.size()>0 ) query = getDataString();
            query = query!=null ? stringUrl + "?" + query : stringUrl;
            URL url = new URL(query);
            //
            httpConnection = (HttpURLConnection)url.openConnection();
            httpConnection.setRequestMethod("GET");
            //
            //Выставляем таймауты
            if ( this.readTimeout>0 ) httpConnection.setReadTimeout(this.readTimeout);
            if ( this.connectionTimeout>0 ) httpConnection.setConnectTimeout(this.connectionTimeout);
            //
            //Проверка, есть ли дополнительные заголовки запроса
            if ( headerList!=null && headerList.size()>0 ) {
                for(Map.Entry<String, String> entry : headerList.entrySet())
                    httpConnection.setRequestProperty(entry.getKey(), entry.getValue());
            }
            //Добавить Cookie в заголовок
            if ( cookiesList!=null && cookiesList.size()>0 ){
                String cookieTxt = "";
                for(Map.Entry<String, String> entry : cookiesList.entrySet()){
                    cookieTxt += entry.getKey() + "=" + entry.getValue() + ";";
                }
                if ( cookieTxt.length()>0 ) cookieTxt = cookieTxt.substring(0, cookieTxt.length()-1);
                httpConnection.setRequestProperty("Cookie", cookieTxt);
            }
            //
            //Устанавливаем свойство кэшиования соединения
            httpConnection.setUseCaches(bGetConnectionFlagCache());
            if ( bGetConnectionFlagInput() ) httpConnection.setDoInput(true);
            httpConnection.connect();
            //
            //Считываем результат запроса
            result = readResult();
        }
        catch(Exception e){
            Log.e(getClass().getName().toString(), e.getStackTrace().toString());
            Log.e(getClass().getName().toString(), e.getMessage().toString());
        }
        finally {
            if ( httpConnection!=null ) httpConnection.disconnect();
        }
        return result;
    }

    //Выполнитьь POST запрос
    public String doPost(){
        String result = null;
        if ( stringUrl==null ) return result;
        try {
            URL url = new URL(stringUrl);
            httpConnection = (HttpURLConnection) url.openConnection();
            httpConnection.setRequestMethod("POST");
            //
            //Выставляем таймауты
            if ( this.readTimeout>0 ) httpConnection.setReadTimeout(this.readTimeout);
            if ( this.connectionTimeout>0 ) httpConnection.setConnectTimeout(this.connectionTimeout);
            //
            //Проверка, есть ли дополнительные заголовки запроса
            if ( headerList!=null && headerList.size()>0 ) {
                for(Map.Entry<String, String> entry : headerList.entrySet())
                    httpConnection.setRequestProperty(entry.getKey(), entry.getValue());
            }
            //Добавить Cookie в заголовок
            if ( cookiesList!=null && cookiesList.size()>0 ){
                String cookieTxt = "";
                for(Map.Entry<String, String> entry : cookiesList.entrySet()){
                    cookieTxt += entry.getKey() + "=" + entry.getValue() + ";";
                }
                if ( cookieTxt.length()>0 ) cookieTxt = cookieTxt.substring(0, cookieTxt.length()-1);
                httpConnection.setRequestProperty("Cookie", cookieTxt);
            }
            //Проверка, есть ли параметры
            if ( !bGetConnectionFlagOutput() || parList.size()==0 ) throw new IOException("Parameters are missing");
            httpConnection.setDoOutput(true);
            httpConnection.connect();
            //
            String query = getDataString();
            OutputStream os = httpConnection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(query);
            writer.flush();
            os.close();
            //
            //Считываем результат запроса
            result = readResult();
        }
        catch (Exception e) {
            Log.e(getClass().getName().toString(), e.getStackTrace().toString());
            Log.e(getClass().getName().toString(), e.getMessage().toString());
        }
        finally {
            if ( httpConnection!=null ) httpConnection.disconnect();
        }
        return result;
    }

    //Выполнить POST-запрос с атрибутом multipart/form-data
    public String doPostMultipart() {
        String result = null;
        if ( this.stringUrl==null ) return result;
        //
        //Граница передачи файла
        String boundary = "***" + System.currentTimeMillis() + "***";
        //
        try {
            URL url = new URL(stringUrl);
            httpConnection = (HttpURLConnection) url.openConnection();
            httpConnection.setRequestMethod("POST");
            //
            //Выставляем таймауты
            if ( this.readTimeout>0 ) httpConnection.setReadTimeout(this.readTimeout);
            if ( this.connectionTimeout>0 ) httpConnection.setConnectTimeout(this.connectionTimeout);
            //
            //Проверка, есть ли дополнительные заголовки запроса
            if ( headerList!=null && headerList.size()>0 ) {
                for(Map.Entry<String, String> entry : headerList.entrySet())
                    httpConnection.setRequestProperty(entry.getKey(), entry.getValue());
            }
            //Добавить Cookie в заголовок
            if ( cookiesList!=null && cookiesList.size()>0 ){
                String cookieTxt = "";
                for(Map.Entry<String, String> entry : cookiesList.entrySet()){
                    cookieTxt += entry.getKey() + "=" + entry.getValue() + ";";
                }
                if ( cookieTxt.length()>0 ) cookieTxt = cookieTxt.substring(0, cookieTxt.length()-1);
                httpConnection.setRequestProperty("Cookie", cookieTxt);
            }
            //Принудительно добавляем заголовок о границе файла
            httpConnection.setRequestProperty("Content-type", "multipart/form-data; boundary=" + boundary);
            //
            //Проверка, есть ли параметры
            if ( !bGetConnectionFlagOutput() || parList.size()==0 ) throw new IOException("Parameters are missing");
            httpConnection.setDoOutput(true);
            httpConnection.connect();
            //
            //Передаём обычные параметры
            String query = getDataMultipartFormString(boundary);
            //Log.e(this.getClass().getName().toString(), query);
            OutputStream os = httpConnection.getOutputStream();
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(os, "UTF-8"), true);
            writer.write(query);
            writer.flush();
            //
            //Передаём файлы
            if ( fileList!=null && !fileList.isEmpty() ) {
                for(Map.Entry<String, String> entry : fileList.entrySet()) {
                    if ( entry.getKey()==null || entry.getValue()==null ) continue;
                    File file = new File(entry.getValue());
                    if ( !file.exists() || !file.isFile() ) continue;
                    //
                    FileInputStream fis = new FileInputStream(file);
                    String mimeType = URLConnection.guessContentTypeFromName(entry.getValue());
                    //
                    query = TWOHYPHENS + boundary + CRLF +
                            "Content-Disposition: form-data; name=\"" + entry.getKey() + "\"; filename=\"" + file.getName() + "\"" + CRLF +
                            ( mimeType!=null ? "Content-Type: " + mimeType + CRLF: "" ) +
                            "Content-Transfer-Encoding: binary" + CRLF + CRLF;
                    writer.write(query);
                    writer.flush();
                    //
                    //Считываем файл
                    byte[] fileBuffer = new byte[BUFFER_SIZE];
                    for(int bytesRead = -1; (bytesRead = fis.read(fileBuffer, 0, BUFFER_SIZE)) > 0; os.write(fileBuffer, 0, bytesRead)) ;
                    os.flush();
                    fis.close();
                    writer.write(CRLF + CRLF);
                    writer.flush();
                }
            }
            writer.write(TWOHYPHENS + boundary + CRLF);
            writer.flush();
            os.close();
            //
            //Считываем результат запроса
            result = readResult();
        }
        catch (Exception e) {
            Log.e(getClass().getName().toString(), e.getStackTrace().toString());
            Log.e(getClass().getName().toString(), e.getMessage().toString());
        }
        finally {
            if ( httpConnection!=null ) httpConnection.disconnect();
        }
        return result;
    }

    //Прочитать результат запоса
    private String readResult() throws Exception {
        String result = null;
        //
        //Чтение заголовков ответа
        responseHeaders = httpConnection.getHeaderFields();
        //Опеделение списка Cookie
        List<String> cookiesHeader = responseHeaders.get(COOKIES_HEADER);
        if ( cookiesHeader!=null && cookiesHeader.size()>0 ) {
            CreateCookieList();
            for(String cookie : cookiesHeader){
                HttpCookie httpCookie = HttpCookie.parse(cookie).get(0);
                AddCookie(
                    URLDecoder.decode(httpCookie.getName(), StandardCharsets.UTF_8.name()),
                    URLDecoder.decode(httpCookie.getValue(), StandardCharsets.UTF_8.name())
                );
            }
        }
        //
        if (httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            //Считываем результат запроса
            if ( bGetConnectionFlagInput() ) {
                InputStream inputStream = httpConnection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"), 8);
                //
                StringBuffer stringBuffer = new StringBuffer();
                String line = null;
                while ((line = reader.readLine()) != null) stringBuffer.append(line).append(CRLF);
                stringBuffer.trimToSize();
                result = stringBuffer.toString();
                reader.close();
                inputStream.close();
            }
        }
        return result;
    }

    //Формирует строку параметров запроса
    public String getDataString() throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for(Map.Entry<String, Object> entry : parList.entrySet()){
            if ( first ) first = false;
            else result.append("&");
            result.append(URLEncoder.encode(entry.getKey(), "UTF-8")).append("=").
                    append(URLEncoder.encode(entry.getValue().toString(), "UTF-8"));
        }
        return result.toString();
    }

    public String getDataMultipartFormString(String boundary) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        for(Map.Entry<String, Object> entry : parList.entrySet()){
            result.append(TWOHYPHENS + boundary + CRLF).
                    append("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"" + CRLF).
                    append("Content-Type: text/plain; charset=UTF-8" + CRLF).
                    append(CRLF).
                    append(URLEncoder.encode(entry.getValue().toString(), "UTF-8")+CRLF);
        }
        return result.toString();
    }

}
