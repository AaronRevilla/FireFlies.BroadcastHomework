package com.example.aaron.broadcasthomework;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.BatteryManager;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.example.aaron.greendao.db.DaoMaster;
import com.example.aaron.greendao.db.DaoSession;
import com.example.aaron.greendao.db.PhoneStatus;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by Oscar Aaron Revilla Escalona on 10/14/2016.
 */

public class BroadcastPlugEnergy extends BroadcastReceiver {

    public LocationManager locationManager = null;
    public static final boolean IS_POWER_ON = true;

    @Override
    public void onReceive(Context context, Intent intent) {

        Date time = new Date();
        PhoneStatus phoneStat = new PhoneStatus();
        phoneStat.setDate(time);
        phoneStat.setIsPowerOn(IS_POWER_ON);

        if(checkLocationPermission(context)){
            //Obtain Coords
            locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            Criteria crit = new Criteria();
            String provider = locationManager.getBestProvider(crit, false);
            Log.d("BroadcastUnplugEnergy", provider);
            Location location = locationManager.getLastKnownLocation(provider);

            if (location != null) {
                double lat = location.getLatitude();
                double longi = location.getLongitude();
                phoneStat.setLatitud(lat);
                phoneStat.setLongitud(longi);
                phoneStat.setLocationProvider(location.getProvider());
                phoneStat.setAddress(getAddress(lat, longi, context));
            } else {
                double lat = 0.0;
                double longi = 0.0;
                phoneStat.setLatitud(lat);
                phoneStat.setLongitud(longi);
                phoneStat.setLocationProvider("No provider");
                phoneStat.setAddress("Couldn't have the location at this moment");
            }

        }
        else{//No Location Permissions
            double lat = 0.0;
            double longi = 0.0;
            phoneStat.setLatitud(lat);
            phoneStat.setLongitud(longi);
            phoneStat.setLocationProvider("No provider");
            phoneStat.setAddress("No location");
        }


        //Obtain Battery Status
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, ifilter);
        int chargePlug = batteryStatus .getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
        boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        float batteryPct = level / (float)scale;

        phoneStat.setUsbCharge(usbCharge);
        phoneStat.setAcCharge(acCharge);
        phoneStat.setBatteryLevel(level);
        phoneStat.setBateryLevelScale(scale);
        phoneStat.setBatteryPtc(batteryPct);

        Log.d("BroadcastPlugEnergy", phoneStat.toString());

        //connetion to db
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(context, "power-v1-db", null);
        SQLiteDatabase db = helper.getWritableDatabase();
        DaoMaster daoMaster = new DaoMaster(db);
        DaoSession daoSession = daoMaster.newSession();
        daoSession.getPhoneStatusDao().insert(phoneStat);

        //close connection
        daoMaster.getDatabase().close();
        daoSession.getDatabase().close();
        db.close();
        helper.close();
        daoSession.clear();
        db=null;
        helper=null;
        daoSession=null;

    }

    public boolean isGPSOn() {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public boolean checkLocationPermission(Context context){

        boolean FineLocationP = ActivityCompat.checkSelfPermission( context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED;
        boolean CoarseLocation = ActivityCompat.checkSelfPermission( context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED;

        return (FineLocationP || CoarseLocation) && false;

    }

    public String getAddress(double lat, double lng, Context context) {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(context, Locale.getDefault());
        String response = "";
        try {
            addresses = geocoder.getFromLocation(lat, lng, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5

            if (addresses.size() > 0) {
                String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                String city = addresses.get(0).getLocality();
                String state = addresses.get(0).getAdminArea();
                String country = addresses.get(0).getCountryName();
                String postalCode = addresses.get(0).getPostalCode();
                String knownName = addresses.get(0).getFeatureName(); // Only if available else return NULL

                response = address + ", " + city + ", " + state + ", " + postalCode + ", " + country;
            } else {
                response = "No Adresses found";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }
}
