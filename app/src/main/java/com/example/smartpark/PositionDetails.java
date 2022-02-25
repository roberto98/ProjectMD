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
    String time;
    TextView park_time_pos, coord_pos;
    EditText textbox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_position_details);
        Log.d(LOG_TAG, "onCreate");

        imageView = (ImageView) findViewById(R.id.imageView);
        loadImageFromStorage();
        getParkDetails();

        // ------------------------------------------------------
        // Click della foto
        // ------------------------------------------------------
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), time,  Toast.LENGTH_SHORT).show();
                Log.d(LOG_TAG, "Show date and time of image");
            }
        });

        // hold down to delete the image
        imageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Log.d(LOG_TAG, "Image deleted");
                imageView.setImageDrawable(null);
                DeleteImage("lastPhoto.jpg");
                return true;
                //return false; // It trigger also Short Click
            }

        });
    }

    private void DeleteImage(String photo_name)
    {
        try
        {
            Context context = getApplicationContext();
            File path = new File(context.getFilesDir().getAbsolutePath());
            File fileToBeDeleted = new File(path, photo_name); // image to delete
            boolean WasDeleted = fileToBeDeleted.delete();

        } catch (Exception e) {
            System.err.println(e.toString());
        }
    }

    // ------------------------------------------------------
    // Salvataggio della foto
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

                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        time = dateFormat.format(new Date());

                        saveImageBitmap(imageBitmap);
                        imageView = (ImageView) findViewById(R.id.imageView);
                        imageView.setImageBitmap(imageBitmap);

                        Toast.makeText(getBaseContext(), "Photo saved\nYou can delete it holding it", Toast.LENGTH_LONG).show();
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

    private void loadImageFromStorage()
    {
        Context context = getApplicationContext();
        File path = new File(context.getFilesDir().getAbsolutePath());
        try {
            File f=new File(path, "lastPhoto.jpg");
            Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
            imageView=(ImageView)findViewById(R.id.imageView);
            imageView.setImageBitmap(b);
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }

    }

    // ------------------------------------------------------
    // Gestione di Shared Preference
    // ------------------------------------------------------

    private void getParkDetails(){
        park_time_pos = findViewById(R.id.park_time_pos);
        coord_pos = findViewById(R.id.coord_pos);
        textbox = findViewById(R.id.textbox);

        String sharedPrefFile = "com.example.smartparkapp";
        mPreferences = getSharedPreferences(sharedPrefFile, MODE_PRIVATE);

        park_time_pos.setText(String.format("Park's time:\n%s", mPreferences.getString("park_time", "")));
        coord_pos.setText(String.format("Latitude: %s\nLongitude: %s", mPreferences.getString("latitude", ""), mPreferences.getString("longitude", "")));
        textbox.setText(String.format(mPreferences.getString("text_box", "")));
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
    protected void onPause() { // Salva in automatico uscendo dalla pagina
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