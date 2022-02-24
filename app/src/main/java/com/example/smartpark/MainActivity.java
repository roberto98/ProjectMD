package com.example.smartpark;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements LocationListener {
    private static final String LOG_TAG = "//tag";
    private LocationManager locationManager;
    private double latitude=0;
    private double longitude=0;

    TextView coord_txt, park_time;
    String time;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ------------------------------------------------------
        // Gestione permessi GPS
        // ------------------------------------------------------

        // https://javapapers.com/android/get-current-location-in-android/
        //  https://www.youtube.com/watch?v=-dO23oDmAaE
        // https://developer.android.com/training/location/permissions

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //TODO: Se hai appena installato app, crasha ancor prima di chiedere i permessi.. se glieli dai poi va ma ogni volta che disinstalli e installi crasha la prima volta
        if(ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, 100);
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0 , this);

        // ------------------------------------------------------
        // Settings di coordinate gps e tempo già salvate
        // ------------------------------------------------------

        getParkDetails();


    }


    // ------------------------------------------------------
    // GET delle coordinate GPS
    // ------------------------------------------------------

    @Override
    public void onLocationChanged(Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        //locationManager.removeUpdates(this);
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d(LOG_TAG, "coordinates provider disable");
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d(LOG_TAG, "coordinates provider enabled");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d(LOG_TAG, " Coordinates status changed");
    }

    // ------------------------------------------------------
    // Gestione del Menù a 3 puntini nella barra in alto
    // ------------------------------------------------------

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_opt_1) { // automatic settings
            startAutomaticSaving(); // Recall the function that start the activity
            Log.d(LOG_TAG, "Automatic Settings");
            return true;
        }

        if (id == R.id.action_opt_2) { // clear
            Log.d(LOG_TAG, "Clear everything");

            park_time = findViewById(R.id.park_time);
            coord_txt = findViewById(R.id.coord_txt);

            park_time.setText("Park's time:\n");
            coord_txt.setText("Latitude: " + "\nLongitude: ");

            Clear_SharedPreferences();
            return true;
        }

        if (id == R.id.action_opt_3) { // settings
            startSettings();
            Log.d(LOG_TAG, "Settings page");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    // ------------------------------------------------------
    // Start delle activity
    // ------------------------------------------------------

    public void startAutomaticSaving() {
        Intent i = new Intent(this, AutomaticSaving.class);
        startActivity(i);
    }

    public void startSettings() {
        Intent i = new Intent(this, Settings.class);
        startActivity(i);
    }


    public void startPosDetails(View v) {
        Intent i = new Intent(this, PositionDetails.class);
        startActivity(i);
    }

    // ------------------------------------------------------
    // Salvataggio manuale
    // ------------------------------------------------------

    public void ManualSaving(View v) {
        Log.d(LOG_TAG, "Manual saving clicked");

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        time = dateFormat.format(new Date());

        park_time = findViewById(R.id.park_time);
        park_time.setText("Park's time:\n" + time);

        coord_txt = findViewById(R.id.coord_txt);
        coord_txt.setText("Latitude: "  + latitude + "\nLongitude: " + longitude);
        savePark(time, latitude, longitude);
    }


    // ------------------------------------------------------
    // Gestione di Shared Preference
    // ------------------------------------------------------
    private SharedPreferences mPreferences;

    private void savePark(String time, double latitude, double longitude){
        SharedPreferences.Editor preferencesEditor = mPreferences.edit();
        preferencesEditor.putString("park_time", time);
        preferencesEditor.putString("latitude", String.valueOf(latitude));
        preferencesEditor.putString("longitude", String.valueOf(longitude));
        preferencesEditor.apply();
    }

    private void getParkDetails(){
        park_time = findViewById(R.id.park_time);
        coord_txt = findViewById(R.id.coord_txt);

        String sharedPrefFile = "com.example.smartparkapp";
        mPreferences = getSharedPreferences(sharedPrefFile, MODE_PRIVATE); // Anche se disinstallo l'app e perdo i dati non mi importa, quindi uso SharedPreference

        park_time.setText("Park's time:\n" + mPreferences.getString("park_time", ""));
        coord_txt.setText("Latitude: "  + mPreferences.getString("latitude", "") +"\nLongitude: " + mPreferences.getString("longitude", ""));
    }

    private void Clear_SharedPreferences(){
        String sharedPrefFile = "com.example.smartparkapp";
        SharedPreferences.Editor preferencesEditor = getSharedPreferences(sharedPrefFile, MODE_PRIVATE).edit();
        //preferencesEditor.clear(); // cancella ogni cosa, pure automatic settings
        preferencesEditor.remove("park_time");
        preferencesEditor.remove("latitude");
        preferencesEditor.remove("longitude");
        preferencesEditor.apply();
    }

    // ------------------------------------------------------
    // Funzioni ausiliare
    // ------------------------------------------------------

    //https://stackoverflow.com/questions/42595585/how-to-turn-on-gps-sensor-and-get-location-from-a-service-in-android-m
    // https://stackoverflow.com/questions/1513485/how-do-i-get-the-current-gps-location-programmatically-in-android

    private void getGPS(){

    }
    //TODO: Bisogna usare i service per lavorare in background
}
