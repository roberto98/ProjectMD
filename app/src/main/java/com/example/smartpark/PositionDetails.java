package com.example.smartpark;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PositionDetails extends AppCompatActivity {
    private static final String LOG_TAG = "/TAG/"+PositionDetails.class.getSimpleName();
    private SharedPreferences mPreferences;
    ImageView imageView;
    String time, coordinates, park_time;
    TextView park_time_pos, coord_pos;
    EditText textbox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_position_details);
        Log.d(LOG_TAG, "onCreate");

        park_time_pos = findViewById(R.id.park_time_pos);
        coord_pos = findViewById(R.id.coord_pos);
        textbox = findViewById(R.id.textbox);

        imageView = (ImageView) findViewById(R.id.imageView);
        loadImageFromStorage(); // load the last picture saved otherwise load the default image


        // ------------------------------------------------------
        // Setting coordinates and park time from intent
        // ------------------------------------------------------
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras != null) {
                park_time = extras.getString("park_time");
                coordinates = extras.getString("coordinates");
            }
        }

        park_time_pos.setText(park_time);
        coord_pos.setText(coordinates);

        // ------------------------------------------------------
        // Setting the textbox from Shared Pref
        // ------------------------------------------------------
        String sharedPrefFile = "com.example.smartparkapp";
        mPreferences = getSharedPreferences(sharedPrefFile, MODE_PRIVATE);
        textbox.setText(mPreferences.getString("text_box", ""));
        time = mPreferences.getString("photo_time", "");


        // ------------------------------------------------------
        // CLICK  on the photos ->
        // short click = display date || long click = delete photo
        // ------------------------------------------------------
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), time,  Toast.LENGTH_SHORT).show();
                Log.d(LOG_TAG, "Show date and time of image");
            }
        });

        // Hold down to delete the image
        imageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Log.d(LOG_TAG, "Image deleted");
                //imageView.setImageResource(R.drawable.img_missing);
                DeleteImage("lastPhoto.jpg");
                return true;
                //return false; // It trigger also Short Click
            }
        });

    } // Close OnCreate

    // ------------------------------------------------------
    // LOAD the photo taken
    // ------------------------------------------------------
    private void loadImageFromStorage() {
        Context context = getApplicationContext();
        File path = new File(context.getFilesDir().getAbsolutePath());
        File f = new File(path, "lastPhoto.jpg");

        if (f.exists()) {
            try {
                Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
                ImageView imageView = findViewById(R.id.imageView);
                imageView.setImageBitmap(b);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            imageView = (ImageView)findViewById(R.id.imageView);
            imageView.setImageResource(R.drawable.img_missing);
        }
    }

    // ------------------------------------------------------
    // DELETE the photo taken
    // ------------------------------------------------------
    private void DeleteImage(String photo_name) {
        Context context = getApplicationContext();
        File path = new File(context.getFilesDir().getAbsolutePath());
        File fileToBeDeleted = new File(path, photo_name); // image to delete

        if (fileToBeDeleted.exists()) {
            boolean is_deleted = fileToBeDeleted.delete(); // delete image from the app path
            if(is_deleted) {
                imageView = (ImageView)findViewById(R.id.imageView);
                imageView.setImageResource(R.drawable.img_missing);

                SharedPreferences.Editor preferencesEditor = mPreferences.edit();
                preferencesEditor.remove("photo_time"); // delete time associated with photo
                preferencesEditor.apply();
            }
        }
    }


    // ------------------------------------------------------
    // SAVE the photo taken
    // ------------------------------------------------------
    ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        Bitmap imageBitmap = null;
                        if (data != null) {
                            imageBitmap = (Bitmap) data.getExtras().get("data");
                        }

                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // To display in the Toast
                        time = dateFormat.format(new Date());

                        SharedPreferences.Editor preferencesEditor = mPreferences.edit();
                        preferencesEditor.putString("photo_time", time);
                        preferencesEditor.apply();

                        saveImageBitmap(imageBitmap);
                        imageView = (ImageView) findViewById(R.id.imageView);
                        imageView.setImageBitmap(imageBitmap);

                        Toast.makeText(getBaseContext(), "Photo saved\nTo delete, hold it", Toast.LENGTH_LONG).show();
                        //imageView.setRotation(90);
                    }

                }

            });

    public void TakePictureIntent(View view) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            someActivityResultLauncher.launch(takePictureIntent);
        } catch (ActivityNotFoundException e) {
            // display error state to the user
            ActivityCompat.requestPermissions(
                    PositionDetails.this,
                    new String[]{Manifest.permission.CAMERA},
                    1);
        }
    }

    private void saveImageBitmap(Bitmap bitmapImage){
        Context context = getApplicationContext();
        File directory = context.getFilesDir();
        File mypath = new File(directory, "lastPhoto.jpg");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // return directory.getAbsolutePath();
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
    protected void onPause() { // Quitting the page, it allows to save anyway the information
        super.onPause();
        textbox = findViewById(R.id.textbox);
        SharedPreferences.Editor preferencesEditor = mPreferences.edit();
        preferencesEditor.putString("text_box", textbox.getText().toString());
        preferencesEditor.apply();
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