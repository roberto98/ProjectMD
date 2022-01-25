package com.example.smartpark;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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

        if (id == R.id.action_opt_1) {
            startAutomaticSaving(); // Recall the function that start the activity
            Log.d("/tag", "ciao1");
            return true;
        }

        if (id == R.id.action_opt_2) {
            Log.d("/tag", "ciao2");
            return true;
        }

        if (id == R.id.action_opt_3) {
            Log.d("/tag", "ciao3");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    // ------------------------------------------------------
    // Work in progress
    // ------------------------------------------------------

    public void startAutomaticSaving(){
        Intent i=new Intent(this, AutomaticSaving.class);
        startActivity(i);
    }
    /*
    public void startPosDetails(View v){
        Intent i=new Intent(this, PositionDetails.class);
        startActivity(i);
    } */


}
