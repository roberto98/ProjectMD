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
import java.util.Locale;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback{
    private static final String LOG_TAG = "/TAG/"+MainActivity.class.getSimpleName();

    private SharedPreferences mPreferences;
    double latitude, longitude;
    TextView coord_txt, park_time;
    String time, input_blt_name, saving_type;
    LatLng Car_pos;
    GPSTracker gps;     // GPSTracker class

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //getCoordinates(); // Get current GPS coordinates - To save manual position you have to enter the app, so the app get ready
        getParkDetails(); // Get the park details (coordinates and time) recently saved

        Log.d(LOG_TAG, "OnCreate");
    }

    // ------------------------------------------------------
    // DROPDOWN MENU in the Navbar
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

            clearParkDetails(); // Delete information and picture of the park

            Car_pos = new LatLng(0, 0); // Refresh the map
            initMap();

            Log.d(LOG_TAG, "Clear everything");
            return true;
        }

        if (id == R.id.action_opt_3) { // help
            startSettings();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    // ------------------------------------------------------
    // Start activity pages
    // ------------------------------------------------------
    public void startAutomaticSaving() {
        Intent i = new Intent(this, AutomaticSaving.class);
        startActivity(i);
    }

    public void startSettings() { // Help page
        Intent i = new Intent(this, AppSettings.class);
        startActivity(i);
    }

    public void startPosDetails(View v) {
        park_time = findViewById(R.id.park_time);
        coord_txt = findViewById(R.id.coord_txt);

        Intent i = new Intent(this, PositionDetails.class); // Send information to the page
        i.putExtra("park_time", park_time.getText().toString());
        i.putExtra("coordinates", coord_txt.getText().toString());
        startActivity(i);
    }

    // ------------------------------------------------------
    // MANUAL SAVING BUTTON
    // ------------------------------------------------------
    public void ManualSaving(View v) {
        getCoordinates(); // Get current GPS coordinates
        PositionSaving(); // Save the coordinates and update information displayed

        Car_pos = new LatLng(latitude, longitude);
        initMap(); // Refresh the map
    }

    // ------------------------------------------------------
    // REFRESH and SAVE the information about the GPS position
    // ------------------------------------------------------
    private void PositionSaving() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        time = dateFormat.format(new Date()); // Current date and time

        park_time = findViewById(R.id.park_time);
        park_time.setText(String.format("Park's time:\n%s", time));

        coord_txt = findViewById(R.id.coord_txt);
        coord_txt.setText(String.format("Latitude: %s\nLongitude: %s", latitude, longitude));

        // Update the last position saved
        SharedPreferences.Editor preferencesEditor = mPreferences.edit();
        preferencesEditor.putString("park_time", time);
        preferencesEditor.putString("latitude", String.valueOf(latitude));
        preferencesEditor.putString("longitude", String.valueOf(longitude));
        preferencesEditor.apply();
    }

    // ------------------------------------------------------
    // GET information to set/refresh the Home
    // ------------------------------------------------------
    private void getParkDetails(){
        park_time = findViewById(R.id.park_time);
        coord_txt = findViewById(R.id.coord_txt);

        // If i uninstall and lose the data i don't care, so i use the SharedPreferences
        String sharedPrefFile = "com.example.smartparkapp";
        mPreferences = getSharedPreferences(sharedPrefFile, MODE_PRIVATE);

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

    // ------------------------------------------------------
    // CLEAR every park details
    // ------------------------------------------------------
    private void clearParkDetails(){
        // ---------- RESET INFORMATION DISPLAYED ------
        park_time = findViewById(R.id.park_time);
        coord_txt = findViewById(R.id.coord_txt);
        park_time.setText("Park's time:\n");
        coord_txt.setText("Latitude: 0.0\nLongitude: 0.0");

        // -------- CLEAR PREFERENCES --------
        String sharedPrefFile = "com.example.smartparkapp";
        SharedPreferences.Editor mPreferences = getSharedPreferences(sharedPrefFile, MODE_PRIVATE).edit();
        //preferencesEditor.clear(); // Delete everything, also the Automatic settings
        mPreferences.remove("park_time");
        mPreferences.remove("latitude");
        mPreferences.remove("longitude");
        mPreferences.remove("text_box");

        // --------- CLEAR PHOTO TAKEN -----------
        Context context = getApplicationContext();
        File path = new File(context.getFilesDir().getAbsolutePath());
        File fileToBeDeleted = new File(path, "lastPhoto.jpg"); // image to delete

        if (fileToBeDeleted.exists()) {
            boolean is_deleted = fileToBeDeleted.delete(); // delete image from the app path
            if(is_deleted) {
                mPreferences.remove("photo_time"); // delete time associated with photo
            }
        }

        mPreferences.apply();
    }

    // ------------------------------------------------------
    // HANDLE OF AUTOMATIC SAVING
    // ------------------------------------------------------
    private void manageModality(){
        String aux = mPreferences.getString("saving_type", "");
        if(!TextUtils.isEmpty(aux)) {
            saving_type = aux;
        } else {
            saving_type = "No_Auto";
        }

        Log.d(LOG_TAG, "modality type: "+saving_type);
        switch (saving_type){
            case "Bluetooth":   // --------------------------------------------------------------------------- BLUETOOTH -----------------
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.BLUETOOTH},
                            1);
                } else { // Bluetooth permissions already granted
                    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                    if (bluetoothAdapter == null) {
                        Toast.makeText(getApplicationContext(), "Bluetooth not supported", Toast.LENGTH_LONG).show();
                    } else {
                        IntentFilter filter = new IntentFilter();
                        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
                        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
                        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
                        this.registerReceiver(mReceiver, filter);

                        if (!bluetoothAdapter.isEnabled()) {
                            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                            startActivityForResult(enableBtIntent, 0);
                        }
                    }
                }
                break;

            case "No_Bluetooth":    // --------------------------------------------------------------- REGULAR AUTOMATIC SAVING ---------------
                /* IDEA TO BE DONE:
                count_step = 0
                Repeat it every minutes:
                      speed = accelerometer value in this instant // to understand if i'm driving or i'm on foot.
                      If my speed is very low:   // i'm on foot or i simply stopped with the car
                            temp_pos = GPS position // I save temporary the gps position
                      count_step = step counter value in this instant //It tells me how far i'm from the parked car.
                      If the steps are higher than a threshold:
                            gps_pos = temp_pos //I save permanently the GPS position
                            count_step = 0 // reset the steps counter
                */
                break;
            case "No_Auto":     // -------------------------------------------------------------------------- MANUAL SAVING ---------------
                // In this case only manual saving is enabled, so i do nothing. I don't have to update the position.
                break;
            default:
                Toast.makeText(getApplicationContext(), "Something went wrong, contact the developer.\nCode #123", Toast.LENGTH_LONG).show();
        }
    }


    // ------------------------------------------------------
    // DISPLAY Google maps
    // ------------------------------------------------------
    private void getCoordinates(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    1);
        } else { // GPS permission already granted
            gps = new GPSTracker(MainActivity.this); // Create class object

            if (gps.canGetLocation()) { // Check if GPS enabled
                latitude = gps.getLatitude();
                longitude = gps.getLongitude();
                //Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();
            } else {
                // Can't get location. // GPS or network is not enabled.
                gps.showSettingsAlert(); // Ask user to enable GPS/network in settings.
            }
        }
    }

    private void initMap(){
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(MainActivity.this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        googleMap.clear(); // To reset the previous marker spots saved
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
                //BTdeviceName = device.getName();
                //BTdeviceHardwareAddress = device.getAddress(); // MAC address
                //Log.d(LOG_TAG, "BT connected: " + BTdeviceName + " - MAC: " + BTdeviceHardwareAddress);

                /* FUTURE IDEA TO DO:
                        When the first connection is done, i can save the MAC address, so i don't need to ask to the users the bluetooth name.
                        And everytime that the smartphone disconnect from BT i can know if i disconnected from the car and so save position.
                */
            }
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                //Done searching
            }
            else if (BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equals(action)) {
                //Device is about to disconnect
            }
            else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {  //Device has disconnected

                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH)
                        == PackageManager.PERMISSION_GRANTED) { // Permission granted

                    BTdeviceName = device.getName();
                    BTdeviceHardwareAddress = device.getAddress(); // MAC address
                    input_blt_name = mPreferences.getString("blt_name", "");

                    // If the BT's name disconnected is equal to the BT inserted in the app by the user, i can save the position
                    if (BTdeviceName.equals(input_blt_name)) {
                        Toast.makeText(getApplicationContext(), "Bluetooth disconnected...\nSaving Position", Toast.LENGTH_SHORT).show();
                        getCoordinates();
                        PositionSaving();

                    /* I can't refresh the google map because of the callback of OnReadyMap executed inside the OnReceiver.
                    //Car_pos = new LatLng(latitude, longitude);
                    //initMap(); */

                        unregisterReceiver(mReceiver);
                    }
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
