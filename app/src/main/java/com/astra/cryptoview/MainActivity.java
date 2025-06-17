package com.astra.cryptoview;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.View;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.astra.cryptoview.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.List;

//Главная точка входа в приложение
public class MainActivity extends AppCompatActivity implements IMainInterface {
    //Константы
    public static final String TAG = "CRYPTOVIEW_MAIN";
    private AppBarConfiguration appBarConfiguration;
    public ActivityMainBinding binding;

    //Данные
    //Ссылка на себя
    private static MainActivity selfActivity = null;
    public static MainActivity getRef() {
        return selfActivity;
    }

    /****************************************************************************************/
    //Данные запуска сервиса
    public volatile boolean isServiceBind = false;  //Флаг соединения с сервисом
    public CryptoViewService cryptoViewService;
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            cryptoViewService = ((CryptoViewService.LocalBinder)iBinder).getService();
            MainActivity.this.isServiceBind = true;
            //
            if ( getCurrentFragmentId()==R.id.CandleListFragment ) {
                ((CandleListFragment)getCurrentFragment()).loadCandleListFromSettings();
                ((CandleListFragment)getCurrentFragment()).refreshCandleList();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            MainActivity.this.isServiceBind = false;
        }
    };

    protected ProgressDialog pD = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.selfActivity = this;   //Запоминаем себя

        //Подключаем сервис
        bindService(new Intent(this, CryptoViewService.class), mConnection, Context.BIND_AUTO_CREATE);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

        //Меняем стандартную иконку на нашу
        //binding.fab.setImageIcon(Icon.createWithResource(this, R.drawable.baseline_add_24));

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAnchorView(R.id.fab)
                        .setAction("Action", null).show();

                */
                menuUpdateCandlesHandler();
            }
        });
    }

    @Override
    public void onDestroy(){
        Log.w(TAG, "onDestroy");
        unbindService(mConnection); //Отсоединяемся от сервиса
        //
        System.gc();
        super.onDestroy();
    }

    //Обработчик кнопки Back
    @Override
    public void onBackPressed() {
        //DrawerLayout drawer = findViewById(R.id.drawer_layout);
        //if (drawer.isDrawerOpen(GravityCompat.START)) drawer.closeDrawer(GravityCompat.START);
        //else super.onBackPressed();
        //
        super.onBackPressed();
        //
        if ( getCurrentFragmentId()==R.id.CandleListFragment ){
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ((CandleListFragment)getCurrentFragment()).loadCandleListFromSettings();
                    ((CandleListFragment)getCurrentFragment()).refreshCandleList();
                }
            });
            //
            binding.fab.show();     //Показываем кнопку обновления
        }
    }

    //Обработчик для кнопки стрелка влево на toolbar
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();  //или this.finish или что то свое
        return true;
    }

    /****************************************************************************************/
    //Работа главного меню с обработчиками

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu;(CandleListFragment)getCurrentFragment() this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu){
        //Предварительно прячем некоторые пункты меню
        menu.findItem(R.id.action_add_record).setVisible(false);
        menu.findItem(R.id.action_update_tickers).setVisible(false);
        menu.findItem(R.id.action_about).setVisible(false);
        //
        binding.fab.hide(); //Предварительно прячем кнопку обновления
        //
        if ( this.getCurrentFragmentId()==R.id.CandleListFragment ){
            //Спрятанное показываем
            menu.findItem(R.id.action_add_record).setVisible(true);
            menu.findItem(R.id.action_update_tickers).setVisible(true);
            menu.findItem(R.id.action_about).setVisible(true);
            binding.fab.show();
        }
        return super.onPrepareOptionsMenu(menu);
    }

    //Обработчик целей в меню
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if ( id==R.id.action_add_record ) {
            menuAddRecordHandler();
        } else if ( id==R.id.action_update_tickers ) {
            menuUpdateTickersHandler();
        } else if ( id==R.id.action_about ){
            menuAbout();
        } else if ( id==R.id.action_share ) {
            menuShareApp();
        } else if ( id==R.id.action_github ) {
            menuGithubShare();
        } else if ( id==R.id.action_exit ){
            finish();
        }/* else if ( id==R.id.action_test_1) {
            CandleListFragment candleListFragment = (CandleListFragment)getCurrentFragment();
            MsgBox("Test1");
        } else if ( id==R.id.action_test_2 ) {
            CandleListFragment candleListFragment = (CandleListFragment)getCurrentFragment();
            MsgBox("Test2");
        }*/
        return super.onOptionsItemSelected(item);
    }

    //Обработчик меню добавления новой записи в список для отслеживания
    protected void menuAddRecordHandler(){
        setCurrentFragment(R.id.AddTickerFragment, null);
    }

    //Обновить тикеры по всем отслеживаемым биржам
    protected void menuUpdateTickersHandler(){
        cryptoViewService.updateAllTickers();
    }

    //Обработчик пункта "О программе"
    protected void menuAbout(){
        setCurrentFragment(R.id.AboutFragment, null);
    }

    //Поделится ссылкой на приложение
    protected void menuShareApp(){
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.play_market_ref));
        try{ startActivity(Intent.createChooser(intent, getString(R.string.play_market_ref))); }
        catch(ActivityNotFoundException e){}
    }

    protected void menuGithubShare(){
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.git_hub_ref));
        try{ startActivity(Intent.createChooser(intent, getString(R.string.git_hub_ref))); }
        catch(ActivityNotFoundException e){}
    }

    //Обновить записи отслеживаемых инструментов
    protected void menuUpdateCandlesHandler(){
        if ( !isServiceBind ) return;
        new Thread(new Runnable() {
            @Override
            public void run() {
                cryptoViewService.updateAllCandles(((CandleListFragment)getCurrentFragment()).getAdapter().getCandleList());
            }
        }).start();
    }

    /****************************************************************************************/
    //Методы реализации интерфейса IMainInterface

    //Ошибка сети (в параметре причина ошибки)
    public void onNetworkError(final String errorString){
        if ( errorString!=null && errorString.length()>0 ) {
            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    MsgBox(errorString);
                }
            });
        }
    }

    public void onTestConnectionSuccess(final String successString){
        if ( successString!=null && successString.length()>0 ) {
            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    MsgBox(successString);
                }
            });
        }
    }

    public void onAddTickerSuccess(String stockName, String tickerName, String timesetName){
        onBackPressed();
        //Теперь берём текущий фрагмент со списком интересных, целевых пар
        ((CandleListFragment)getCurrentFragment()).addNewRecord(stockName, tickerName, timesetName);
    }
    public void onAddTickerCancel(){
        onBackPressed();
    }

    //Обратный вызов для обновления списка свечей
    public void onUpdateCandleList(){
        //В случае если меняется фрагмент и текущим становится список свечей, то осуществляем визуальное обновление адаптера
        /*
        if ( getCurrentFragmentId()!=R.id.CandleListFragment ) return;
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((CandleListFragment)getCurrentFragment()).refreshCandleList();
            }
        });
        */

        if ( getCurrentFragmentId()==R.id.CandleListFragment ){
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ((CandleListFragment)getCurrentFragment()).loadCandleListFromSettings();
                    ((CandleListFragment)getCurrentFragment()).refreshCandleList();
                }
            });
            //
            binding.fab.show();     //Показываем кнопку обновления
        }

    }

    public void onUpdateCandle(int index, long dateTimeMs, double dO, double dH, double dL, double dC, double dV, boolean isUpdate){
        if ( getCurrentFragmentId()!=R.id.CandleListFragment ) return;
        //
        ((CandleListFragment)getCurrentFragment()).candleListAdapter.getCandleList().getItem(index).setValue(dateTimeMs, dO, dH, dL, dC, dV);
        ((CandleListFragment)getCurrentFragment()).candleListAdapter.getCandleList().saveItemArray();
        if ( isUpdate ) ((CandleListFragment)getCurrentFragment()).candleListAdapter.refreshAdapter();
    }

    //Начало обновления списка свечей
    public void onBeginUpdateCandles(){
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if ( pD!=null ) {
                    pD.dismiss();
                    pD = null;
                }
                pD = new ProgressDialog(MainActivity.this);
                pD.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                pD.setMessage(getString(R.string.wait));
                pD.setCancelable(false);
                pD.show();
            }
        });
    }

    //Конец обновления списка свечей
    public void onEndUpdateCandles(){
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if ( pD==null ) return;
                pD.dismiss();
                pD = null;
            }
        });
    }

    //=============================================================================================
    //Функция возвращает текущий фрагмент
    protected Fragment getCurrentFragment(){
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return navController.getCurrentDestination()==null ? null : getSupportFragmentManager().getPrimaryNavigationFragment().getChildFragmentManager().getFragments().get(0);
    }

    //Функция возвращает идентификатор текущего фрагмента
    protected int getCurrentFragmentId(){
        return Navigation.findNavController(this, R.id.nav_host_fragment_content_main).getCurrentDestination().getId();
    }

    //Установить текущим фрагмент с идентификатором idFragment
    public void setCurrentFragment(int idFragment, Bundle b){
        //Получаем контроллер
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        navController.navigate(idFragment, b); //Осуществляем навигацию фрагментов
        NavigationUI.setupActionBarWithNavController(this, navController);  //Меняем гамбургер на стрелку
        //getSupportActionBar().setHomeButtonEnabled(false);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //DrawerLayout drawer = findViewById(R.id.drawer_layout);
        //drawer.setDrawerLockMode(idFragment==R.id.fragmentCustomList ?
        //        DrawerLayout.LOCK_MODE_UNLOCKED : DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        //invalidateNavMenuItems(idFragment); //Вот здесь нужно разрешать пункты выдвигаемого меню
        invalidateOptionsMenu();    //Обновляем меню
        //
        //В случае если меняется фрагмент и текущим становится список свечей, то осуществляем визуальное обновление адаптера
        if ( idFragment==R.id.CandleListFragment ) ((CandleListFragment)getCurrentFragment()).refreshCandleList();
    }


    /****************************************************************************************/
    //Показ всплывающего сообщения
    public void MsgBox(String msg){
        Context context = getApplicationContext();
        Toast toast = Toast.makeText(context, msg, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    //Перегруженная функция показа всплывающего окна. В параметрах - идентификатор ресурса
    public void MsgBox(int idResString){ MsgBox(getResources().getString(idResString)); }

}