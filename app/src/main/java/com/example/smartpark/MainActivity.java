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
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.text.SimpleDateFormat;
import java.util.Date;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.RuntimePermissions;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

@RuntimePermissions
public class MainActivity extends AppCompatActivity implements LocationListener, OnMapReadyCallback{
    private static final String LOG_TAG = "//tag";
    private LocationManager locationManager;
    private double latitude = 0;
    private double longitude = 0;

    TextView coord_txt, park_time;
    String time, blt_name, saving_type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(LOG_TAG, "OnCreate");
        Log.d(LOG_TAG, "1- "+latitude);

        // https://javapapers.com/android/get-current-location-in-android/
        //  https://www.youtube.com/watch?v=-dO23oDmAaE
        // https://developer.android.com/training/location/permissions

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        MainActivityPermissionsDispatcher.getCoordinatesWithPermissionCheck(this);

        saving_type = CheckAutomaticSavings(); // Verifico quale tipo di salvataggio automatico è attivo

        getParkDetails(); // Settings di coordinate gps e tempo già salvate
        Log.d(LOG_TAG, "2- "+latitude);
        // Google map - Get a handle to the fragment and register the callback.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        Log.d(LOG_TAG, "3- "+latitude);

    }


    // ------------------------------------------------------
    // GESTIONE PERMESSI GPS
    //
    // https://github.com/permissions-dispatcher/PermissionsDispatcher -> runtime location permission with PermissionsDispatcher
    // If u don't have MainActivityPermissionDispatcher Class -> Just do Build->Clean Project and then Build->Rebuild Project
    // Se non lo usavo c'era il problema che l'app crashava nella oncreate senza neanche darti il tempo di cedere i permessi
    // ------------------------------------------------------

    @NeedsPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    void getCoordinates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            showDenied_getCoordinates();
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        }
    }

    // Annotate a method which is invoked if the user doesn't grant the permissions
    @OnPermissionDenied(Manifest.permission.ACCESS_FINE_LOCATION)
    void showDenied_getCoordinates() {
        Toast.makeText(getApplicationContext(), "Permission denied", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // NOTE: delegate the permission handling to generated method
        MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }


    // ------------------------------------------------------
    // GET delle coordinate GPS
    // ------------------------------------------------------
    // https://stackoverflow.com/questions/1513485/how-do-i-get-the-current-gps-location-programmatically-in-android

    @Override
    public void onLocationChanged(Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();

        switch (saving_type){
            case "Bluetooth":
                // prende sempre la posizione, appena vede una disconnessione dal nome salvato salva posizione
                break;
            case "No_Bluetooth":
                // deve prendere sempre posizione, e controllare quanti passi o quanto tempo passa prima di salvare la posizione
                break;
            case "No_Auto":
                // la posizione non si deve riaggiornare
                break;
            default:
                Toast.makeText(getApplicationContext(), "Something seems to not working, contact the developer.\nCode #123", Toast.LENGTH_LONG).show();

        }

           // coord_txt = findViewById(R.id.coord_txt);
           // coord_txt.setText(String.format("Latitude: %s\nLongitude: %s", latitude, longitude));

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

            park_time = findViewById(R.id.park_time);
            coord_txt = findViewById(R.id.coord_txt);
            park_time.setText("Park's time:\n");
            coord_txt.setText("Latitude: \nLongitude: ");

            Clear_SharedPreferences();

            latitude = 0;
            longitude = 0;
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);

            Log.d(LOG_TAG, "Clear everything");
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
        park_time.setText(String.format("Park's time:\n%s", time));

        coord_txt = findViewById(R.id.coord_txt);
        coord_txt.setText(String.format("Latitude: %s\nLongitude: %s", latitude, longitude));
        savePark(time, latitude, longitude);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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

        park_time.setText(String.format("Park's time:\n%s", mPreferences.getString("park_time", "")));
        coord_txt.setText(String.format("Latitude: %s\nLongitude: %s", mPreferences.getString("latitude", ""), mPreferences.getString("longitude", "")));

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private String CheckAutomaticSavings(){

        String sharedPrefFile = "com.example.smartparkapp";
        mPreferences = getSharedPreferences(sharedPrefFile, MODE_PRIVATE);

        boolean auto_switch = mPreferences.getBoolean("auto_switch", false);
        boolean blt_switch = mPreferences.getBoolean("blt_switch", false);
        String bluetooth_text = mPreferences.getString("blt_name", "");

        if(auto_switch){
            if(blt_switch){
                blt_name = bluetooth_text;
                saving_type =  "Bluetooth";
            } else {
                saving_type =  "No_Bluetooth";
            }
        } else {
            saving_type =  "No_Auto";
        }
        return saving_type;
    }

    private void Clear_SharedPreferences(){
        String sharedPrefFile = "com.example.smartparkapp";
        SharedPreferences.Editor mPreferences = getSharedPreferences(sharedPrefFile, MODE_PRIVATE).edit();
        //preferencesEditor.clear(); // cancella ogni cosa, pure automatic settings
        mPreferences.remove("park_time");
        mPreferences.remove("latitude");
        mPreferences.remove("longitude");
        mPreferences.apply();
    }

    // ------------------------------------------------------
    // Funzioni ausiliare
    // ------------------------------------------------------





    // ------------------------------------------------------
    // Display Google maps
    //
    // https://developers.google.com/maps/documentation/android-sdk/map#view_the_code
    // https://www.youtube.com/watch?v=eiexkzCI8m8
    // ------------------------------------------------------

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        LatLng Car_pos = new LatLng(latitude, longitude);

        googleMap.addMarker(new MarkerOptions().position(Car_pos).title("Car position"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(Car_pos, 18.5f));
    }


    // ------------------------------------------------------
    // Activity States
    // ------------------------------------------------------

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(LOG_TAG, "onResume"+latitude+"--"+longitude);
        getParkDetails();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(LOG_TAG, "OnPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(LOG_TAG, "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(LOG_TAG, "onDestroy");
    }
    //TODO: Bisogna usare i service per lavorare in background
}
