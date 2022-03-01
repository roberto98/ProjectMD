package com.example.smartpark;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback{
    private static final String LOG_TAG = "/TAG/"+MainActivity.class.getSimpleName();
    double latitude;
    double longitude;

    TextView coord_txt, park_time;
    String time, input_blt_name, saving_type;
    LatLng Car_pos;
    GPSTracker gps;     // GPSTracker class

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getCoordinates();
        getParkDetails(); // Settings di coordinate gps e tempo e mappa già salvate

        Log.d(LOG_TAG, "OnCreate");
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
        // Handle action bar item clicks here.
        int id = item.getItemId();

        if (id == R.id.action_opt_1) { // automatic settings
            startAutomaticSaving(); // Recall the function that start the activity
            return true;
        }

        if (id == R.id.action_opt_2) { // clear
            park_time = findViewById(R.id.park_time);
            coord_txt = findViewById(R.id.coord_txt);
            park_time.setText("Park's time:\n");
            coord_txt.setText("Latitude: 0.0\nLongitude: 0.0");

            Clear_SharedPreferences();
            DeleteImage("lastPhoto.jpg");

            Car_pos = new LatLng(0, 0);
            initMap();

            Log.d(LOG_TAG, "Clear everything");
            return true;
        }

        if (id == R.id.action_opt_3) { // settings
            startSettings();
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
        Intent i = new Intent(this, AppSettings.class);
        startActivity(i);
    }

    public void startPosDetails(View v) {
        park_time = findViewById(R.id.park_time);
        coord_txt = findViewById(R.id.coord_txt);

        Intent i = new Intent(this, PositionDetails.class);
        i.putExtra("park_time", park_time.getText().toString());
        i.putExtra("coordinates", coord_txt.getText().toString());
        startActivity(i);
    }

    // ------------------------------------------------------
    // Salvataggio manuale + Position Saving
    // ------------------------------------------------------
    public void ManualSaving(View v) {
        getCoordinates();
        PositionSaving();

        Car_pos = new LatLng(latitude, longitude);
        initMap();
    }

    private void PositionSaving() {
        Log.d(LOG_TAG, "Executing position saving");

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        time = dateFormat.format(new Date());

        park_time = findViewById(R.id.park_time);
        park_time.setText(String.format("Park's time:\n%s", time));

        coord_txt = findViewById(R.id.coord_txt);
        coord_txt.setText(String.format("Latitude: %s\nLongitude: %s", latitude, longitude));
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

        String lat = mPreferences.getString("latitude", "");
        String lon = mPreferences.getString("longitude", "");

        if(!TextUtils.isEmpty(lat) && !TextUtils.isEmpty(lon)){
            latitude = Double.parseDouble(lat);
            longitude = Double.parseDouble(lon);
        } else {
            latitude = 0;
            longitude = 0;
        }

        park_time.setText(String.format("Park's time:\n%s", mPreferences.getString("park_time", "")));
        coord_txt.setText(String.format("Latitude: %s\nLongitude: %s", latitude, longitude));

        Car_pos = new LatLng(latitude, longitude);
        initMap();
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

    private void DeleteImage(String photo_name) {
        try {
            Context context = getApplicationContext();
            File path = new File(context.getFilesDir().getAbsolutePath());
            File fileToBeDeleted = new File(path, photo_name); // image to delete
            boolean WasDeleted = fileToBeDeleted.delete();

            SharedPreferences.Editor preferencesEditor = mPreferences.edit();
            preferencesEditor.remove("photo_time"); // delete time associated with photo
            preferencesEditor.apply();

        } catch (Exception e) {
            System.err.println(e.toString());
        }
    }

    private void manageModality(){
        String aux = mPreferences.getString("saving_type", "");
        if(!TextUtils.isEmpty(aux)) {
            saving_type = aux;
        } else {
            saving_type = "No_Auto";
        }

        Log.d(LOG_TAG, "modality type: "+saving_type);
        switch (saving_type){
            case "Bluetooth":
                BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                if(bluetoothAdapter == null){
                    Toast.makeText(getApplicationContext(), "Bluetooth not supported", Toast.LENGTH_LONG).show();
                } else {
                    IntentFilter filter = new IntentFilter();
                    filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
                    filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
                    filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
                    this.registerReceiver(mReceiver, filter);

                    if(!bluetoothAdapter.isEnabled()){
                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableBtIntent, 0); // Abilita il bluetooth
                    }
                }
                break;

            case "No_Bluetooth":
                // deve prendere sempre posizione, e controllare quanti passi o quanto tempo passa prima di salvare la posizione
                //Log.d(LOG_TAG, "Automatic Mode with NO bluetooth");
                break;
            case "No_Auto":
                // la posizione non si deve riaggiornare
                break;
            default:
                Toast.makeText(getApplicationContext(), "Something went wrong, contact the developer.\nCode #123", Toast.LENGTH_LONG).show();
        }
    }


    // ------------------------------------------------------
    // Display Google maps
    // ------------------------------------------------------
    private void getCoordinates(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    1);
        }

        gps = new GPSTracker(MainActivity.this); // Create class object

        if(gps.canGetLocation()) { // Check if GPS enabled
            latitude = gps.getLatitude();
            longitude = gps.getLongitude();
            //Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();
        } else {
            // Can't get location. // GPS or network is not enabled.
            gps.showSettingsAlert(); // Ask user to enable GPS/network in settings.
        }
    }

    private void initMap(){
        Log.d(LOG_TAG,"initMap: initializing map");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(MainActivity.this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        googleMap.clear(); // Per resettare i marker piazzati precedentemente
        googleMap.addMarker(new MarkerOptions().position(Car_pos).title("Car position"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(Car_pos, 16f));
    }

    // ------------------------------------------------------
    // BLUETOOTH
    // ------------------------------------------------------

    //The BroadcastReceiver that listens for bluetooth broadcasts
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        String BTdeviceName, BTdeviceHardwareAddress;

        @Override
        public void onReceive(Context context, @NonNull Intent intent) {
            String action = intent.getAction();
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                //Device found
            }
            else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                //Device is now connected
                BTdeviceName = device.getName();
                BTdeviceHardwareAddress = device.getAddress(); // MAC address
                Log.d(LOG_TAG, "BT connected: " + BTdeviceName + " - MAC: " + BTdeviceHardwareAddress);
            }
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                //Done searching
            }
            else if (BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equals(action)) {
                //Device is about to disconnect
            }
            else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                //Device has disconnected
                BTdeviceName = device.getName();
                //TODO: in futuro si potrebbe fare che ti faccio eseguire il primo collegamento e mi tengo salvato poi il mac
                BTdeviceHardwareAddress = device.getAddress(); // MAC address
                input_blt_name = mPreferences.getString("blt_name", "");

                if(BTdeviceName.equals(input_blt_name)) {    // Se il BT inserito dall'utente coincide allora è la macchina e posso salvare posizione
                    Toast.makeText(getApplicationContext(), "Bluetooth disconnected\nSaving Position", Toast.LENGTH_SHORT).show();
                    getCoordinates();
                    PositionSaving();
                    //TODO: non posso aggiornare la mappa al momento per via della callback di OnReadyMap eseguita dentro OnReceiver,
                    // se la sposto nella OnStart viene aggiornata prima che prenda le nuove coordinate
                    //Car_pos = new LatLng(latitude, longitude);
                    //initMap();

                    unregisterReceiver(mReceiver);
                }
                Log.d(LOG_TAG, "BT disconnected: " + BTdeviceName + " - MAC: " + BTdeviceHardwareAddress);
            }
        }
    };

    // ------------------------------------------------------
    // Activity States
    // -----------------------------------------------------

    @Override
    protected void onStart() {
        super.onStart();
        manageModality();
        Log.d(LOG_TAG, "onStart");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(LOG_TAG, "onRestart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(LOG_TAG, "onResume");
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
}
