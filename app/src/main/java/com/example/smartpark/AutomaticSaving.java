package com.example.smartpark;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

public class AutomaticSaving extends AppCompatActivity {
    private static final String LOG_TAG = "/TAG/"+AutomaticSaving.class.getSimpleName();
    private SharedPreferences mPreferences;
    EditText bluetooth_text;
    Switch auto_switch, blt_switch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_automatic_saving);

        auto_switch = findViewById(R.id.automatic_switch);
        auto_switch.setClickable(false); // TO DO: Setted to false because this functionality is not implemented yet (17/01/2023)

        blt_switch = findViewById(R.id.bluetooth_switch);
        bluetooth_text = findViewById(R.id.bluetoothtext);

        // ------------------------------------------------------
        // Synchronized switch buttons
        // ------------------------------------------------------
        blt_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                auto_switch.setChecked(isChecked);
            }
        });

        auto_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked) {
                    blt_switch.setChecked(false);
                }
            }
        });


        // ------------------------------------------------------
        // GET the last saved information
        // ------------------------------------------------------
        String sharedPrefFile = "com.example.smartparkapp";
        mPreferences = getSharedPreferences(sharedPrefFile, MODE_PRIVATE);

        auto_switch.setChecked(mPreferences.getBoolean("auto_switch", false));
        blt_switch.setChecked(mPreferences.getBoolean("blt_switch", false));
        bluetooth_text.setText(mPreferences.getString("blt_name", ""));

    }

    @Override
    protected void onStart() {
        super.onStart();
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

    // ------------------------------------------------------
    // SAVE the information
    // ------------------------------------------------------
    public void saveButton(View v){
        saveFunction();
        finish(); // Finish the current activity
    }

    private void saveFunction(){
        SharedPreferences.Editor preferencesEditor = mPreferences.edit();
        preferencesEditor.putBoolean("auto_switch", auto_switch.isChecked());
        preferencesEditor.putBoolean("blt_switch", blt_switch.isChecked());
        preferencesEditor.putString("blt_name", bluetooth_text.getText().toString());

        if(auto_switch.isChecked()){ // Check which type of saving is enabled and store it in the preferences
            if(blt_switch.isChecked()){
                preferencesEditor.putString("saving_type", "Bluetooth");
            } else {
                preferencesEditor.putString("saving_type", "No_Bluetooth");
            }
        } else {
            preferencesEditor.putString("saving_type", "No_Auto");
        }
        preferencesEditor.apply();
        Toast.makeText(getApplicationContext(), "Settings Saved",  Toast.LENGTH_LONG).show();
    }

    // ------------------------------------------------------
    // CLEAR all the saved information
    // ------------------------------------------------------
    public void Clear(View v){
        String sharedPrefFile = "com.example.smartparkapp";
        SharedPreferences.Editor preferencesEditor = getSharedPreferences(sharedPrefFile, MODE_PRIVATE).edit();
        //you did not set filename and file privacy like private mode or public mode..
        preferencesEditor.remove("auto_switch");
        preferencesEditor.remove("blt_switch");
        preferencesEditor.remove("blt_name");
        preferencesEditor.putString("saving_type", "No_Auto"); // the default value is No_Auto

        preferencesEditor.apply();
        Toast.makeText(getApplicationContext(), "Settings Cleared",  Toast.LENGTH_LONG).show();
        finish(); // Finish the current activity
    }

}
