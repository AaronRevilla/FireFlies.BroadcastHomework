package com.example.aaron.broadcasthomework;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.example.aaron.greendao.db.DaoMaster;
import com.example.aaron.greendao.db.DaoSession;
import com.example.aaron.greendao.db.PhoneStatus;
import com.example.aaron.greendao.db.PhoneStatusDao;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    //DBConnection
    public DaoMaster.DevOpenHelper helper;
    public SQLiteDatabase db;
    public DaoMaster daoMaster;
    public DaoSession daoSession;
    public List<PhoneStatus> listData;
    public RecyclerViewAdpater adapter;

    //Elements
    public RecyclerView recyclerView;
    public TextView totalTimesConnected;
    public TextView totalTime;
    public TextView averageTime;

    //add another br
    public BroadcastReceiver mBRConnected;
    public IntentFilter intentFilterConnected;
    public BroadcastReceiver mBRDisconnected;
    public IntentFilter intentFilterDisconnected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(!isConnectedToDB()){
            openDBConnection();
        }

        recyclerView = ((RecyclerView) findViewById(R.id.recyclerViewList));
        //RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST);
        //recyclerView.addItemDecoration(itemDecoration);
        adapter =  new RecyclerViewAdpater(getPhoneStatus());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager( new LinearLayoutManager(this));

        totalTimesConnected = ((TextView) findViewById(R.id.totalTimesConnected));
        totalTime = ((TextView) findViewById(R.id.totalTime));
        averageTime = ((TextView) findViewById(R.id.avgTime));
    }

    public List<PhoneStatus> getPhoneStatus(){

        listData = daoSession
                            .getPhoneStatusDao()
                            .queryBuilder()
                            .where(PhoneStatusDao.Properties.Date.isNotNull())
                            .orderDesc(PhoneStatusDao.Properties.Id)
                            .list();

        /*listData = daoSession.getPhoneStatusDao().loadAll();
        Comparator<PhoneStatus> comparator = new Comparator<PhoneStatus>() {
            @Override
            public int compare(PhoneStatus o1, PhoneStatus o2) {
                return o2.getId().compareTo(o1.getId());
            }
        };
        Collections.sort(listData, comparator);*/
        return listData;
    }

    @Override
    protected void onResume() {
        if(!isConnectedToDB()){
            openDBConnection();
        }

        if(mBRConnected == null){
            mBRConnected = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    adapter.updateList(getPhoneStatus());
                    //initView();
                }
            };
        }
        intentFilterConnected = new IntentFilter();
        intentFilterConnected.addAction("android.intent.action.ACTION_POWER_CONNECTED");

        if(mBRDisconnected == null){
            mBRDisconnected = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    adapter.updateList(getPhoneStatus());
                    initView();
                }
            };
        }
        intentFilterDisconnected = new IntentFilter();
        intentFilterDisconnected.addAction("android.intent.action.ACTION_POWER_DISCONNECTED");

        registerReceiver(mBRConnected, intentFilterConnected);
        registerReceiver(mBRDisconnected, intentFilterDisconnected);
        initView();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        if(isConnectedToDB()){
            closeDBConnection();
        }
        if(mBRConnected != null){
            unregisterReceiver(mBRConnected);
        }
        if(mBRDisconnected != null){
            unregisterReceiver(mBRDisconnected);
        }
        super.onDestroy();
    }

    public void openDBConnection(){
        //connetion to db
        helper = new DaoMaster.DevOpenHelper(this, "power-v1-db", null);
        db = helper.getWritableDatabase();
        daoMaster = new DaoMaster(db);
        daoSession = daoMaster.newSession();
    }

    public void closeDBConnection(){
        daoMaster.getDatabase().close();
        daoSession.getDatabase().close();
        db.close();
        helper.close();
        daoSession.clear();
        db=null;
        helper=null;
        daoSession=null;
    }

    public boolean isConnectedToDB(){
        if(daoMaster != null && daoSession != null){
            return daoSession.getDatabase().isOpen();
        }
        else{
            return false;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if(isConnectedToDB()){
            closeDBConnection();
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if(!isConnectedToDB()){
            openDBConnection();
        }
        super.onRestoreInstanceState(savedInstanceState);
    }

    public void initView(){
        //create querys
        boolean wasOnPowerFirstTime = false;
        PhoneStatusDao dao = daoSession.getPhoneStatusDao();
        long dataNumber = dao.queryBuilder().count();
        List<Date> connectedTime = new ArrayList<Date>();
        List<Date> disconnectedTime = new ArrayList<Date>();
        List<Long> timeConneted = new ArrayList<Long>();
        long averageTimeLong = 0;
        long allTime = 0;
        long diffSecondstotal = 0;
        long diffMinutestotal = 0;
        long diffHourstotal = 0;
        long diffDaystotal = 0;
        long diffSecondsAvg = 0;
        long diffMinutesAvg = 0;
        long diffHoursAvg = 0;
        long diffDaysAvg = 0;
        if(!listData.isEmpty()){
            wasOnPowerFirstTime = listData.get(listData.size()-1).getIsPowerOn();
            for (PhoneStatus stat: listData){
                if(stat.getIsPowerOn()){//connected
                    connectedTime.add(stat.getDate());
                }
                else{
                    disconnectedTime.add(stat.getDate());
                }
            }
            //get the in the pairs of dates
            if(connectedTime.size() <= disconnectedTime.size()){
                for(Date date: connectedTime){
                    int position = connectedTime.indexOf(date);
                    if(wasOnPowerFirstTime){//connected date < disconnected date
                        timeConneted.add(disconnectedTime.get(position).getTime() - date.getTime() );
                    }
                    else{//disconnected date < connected date
                        timeConneted.add(date.getTime() - disconnectedTime.get(position).getTime());
                    }
                }
            }
            else{
                for(Date date: disconnectedTime){
                    int position = disconnectedTime.indexOf(date);
                    if(wasOnPowerFirstTime){//connected date < disconnected date
                        timeConneted.add(connectedTime.get(position).getTime() - date.getTime());
                    }
                    else{//disconnected date < connected date
                        timeConneted.add(date.getTime() - connectedTime.get(position).getTime());
                    }
                }
            }

            for (Long timeMili: timeConneted){
                allTime += timeMili.longValue();
            }

            if(!timeConneted.isEmpty()){
                averageTimeLong = allTime / timeConneted.size();
            }
            else{
                averageTimeLong = allTime;
            }

            diffSecondstotal = allTime / 1000 % 60;
            diffMinutestotal = allTime / (60 * 1000) % 60;
            diffHourstotal = allTime / (60 * 60 * 1000) % 24;
            diffDaystotal = allTime / (24 * 60 * 60 * 1000);

            diffSecondsAvg = averageTimeLong / 1000 % 60;
            diffMinutesAvg = averageTimeLong / (60 * 1000) % 60;
            diffHoursAvg = averageTimeLong / (60 * 60 * 1000) % 24;
            diffDaysAvg = averageTimeLong / (24 * 60 * 60 * 1000);
        }



        totalTimesConnected.setText(String.valueOf(timeConneted.size()));
        totalTime.setText(diffDaystotal + " days " + diffHourstotal + ":" + diffMinutestotal + ":" + diffSecondstotal);
        averageTime.setText(diffDaysAvg + " days " + diffHoursAvg + ":" + diffMinutesAvg + ":" + diffSecondsAvg);
    }

    public void resetDB(View view) {
        daoSession.getPhoneStatusDao().deleteAll();
        adapter.updateList(getPhoneStatus());
        initView();
    }
}
