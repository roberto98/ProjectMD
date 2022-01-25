package com.example.smartpark;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;

public class AutomaticSaving extends AppCompatActivity {

    //GPS: https://stackoverflow.com/questions/42595585/how-to-turn-on-gps-sensor-and-get-location-from-a-service-in-android-m
    // https://stackoverflow.com/questions/1513485/how-do-i-get-the-current-gps-location-programmatically-in-android
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_automatic_saving);

    }
}
