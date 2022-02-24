package com.example.smartpark;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class AutomaticSaving extends AppCompatActivity {
    private static final String LOG_TAG = "/TAG/"+AutomaticSaving.class.getSimpleName();
    private SharedPreferences autoPreferences;
    EditText bluetooth_text;
    Switch auto_switch, blt_switch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_automatic_saving);

        // ------------------------------------------------------
        // GET delle informazioni
        // ------------------------------------------------------

        auto_switch = findViewById(R.id.automatic_switch);
        blt_switch = findViewById(R.id.bluetooth_switch);
        bluetooth_text = findViewById(R.id.bluetoothtext);

        String sharedPrefFile = "com.example.smartparkapp";
        autoPreferences = getSharedPreferences(sharedPrefFile, MODE_PRIVATE);

        auto_switch.setChecked(autoPreferences.getBoolean("auto_switch", false));
        blt_switch.setChecked(autoPreferences.getBoolean("blt_switch", false));
        bluetooth_text.setText(autoPreferences.getString("blt_name", ""));

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
    // Salvataggio delle informazioni
    // ------------------------------------------------------

    public void saveButton(View v){
        saveFunction();
    }

    private void saveFunction(){
        SharedPreferences.Editor preferencesEditor = autoPreferences.edit();
        preferencesEditor.putBoolean("auto_switch", auto_switch.isChecked());
        preferencesEditor.putBoolean("blt_switch", blt_switch.isChecked());
        preferencesEditor.putString("blt_name", bluetooth_text.getText().toString());
        preferencesEditor.apply();

        Toast.makeText(getApplicationContext(), "Settings Saved",  Toast.LENGTH_LONG).show();
    }

    public void Clear(View v){
        String sharedPrefFile = "com.example.smartparkapp";
        SharedPreferences.Editor preferencesEditor = getSharedPreferences(sharedPrefFile, MODE_PRIVATE).edit();
        //you did not set filename and file privacy like private mode or public mode..
        preferencesEditor.remove("auto_switch");
        preferencesEditor.remove("blt_switch");
        preferencesEditor.remove("blt_name");
        preferencesEditor.apply();

        Toast.makeText(getApplicationContext(), "Settings Cleared\n\nRefresh the page too see the changes",  Toast.LENGTH_LONG).show();
    }



}
