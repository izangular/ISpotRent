package com.example.karmali.homexperts;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;


public class LocationActivity extends AppCompatActivity implements View.OnClickListener {

    //private OkHttpClient okHttpClient;
   // private Request request;
    static Uri capturedImageUri = null;
   // private String Url = "https://intweb.iazi.ch/api/auth/v1/login";
    Button btncamera;
   // TextView txtToken;

   private Handler mHandler;
    private static final int CAMERA_REQUEST = 1888;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);


      mHandler = new Handler(Looper.getMainLooper());
        btncamera = (Button) findViewById(R.id.btncamera);
        btncamera.setOnClickListener(LocationActivity.this);

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btncamera:
                try {
                    Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cameraIntent, CAMERA_REQUEST);
                    Calendar cal = Calendar.getInstance();
                    File file = new File(Environment.getExternalStorageDirectory(), (cal.getTimeInMillis() + ".jpg"));
                    if (!file.exists()) {
                        try {
                            file.createNewFile();
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    } else {
                        file.delete();
                        try {
                            file.createNewFile();
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                    capturedImageUri = Uri.fromFile(file);
                    Intent i = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                   // Toast.makeText(this, capturedImageUri.toString(), Toast.LENGTH_LONG).show();
                   i.putExtra(MediaStore.EXTRA_OUTPUT, capturedImageUri);
                    startActivityForResult(i, CAMERA_REQUEST);
                } catch (Exception e) {
                    Toast.makeText(this, "Error:" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
          }
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST) {
            try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), capturedImageUri);
                    ImageView imageView = (ImageView) findViewById(R.id.capturedImageView);
                    imageView.setImageBitmap(bitmap);

               // Bitmap image = (Bitmap) data.getExtras().get("data");
               // ImageView imageview = (ImageView) findViewById(R.id.capturedImageView);
                //imageview.setImageBitmap(image);

                Intent intent = new Intent(LocationActivity.this,AppraisalActivity.class);
                intent.putExtra("BitmapImage", bitmap);
                LocationActivity.this.startActivity(intent);

            } catch (Exception e) {
                Toast.makeText(this, "ff" + e.getMessage(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }



  /*  private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    private void Authenticate() {
       if(!isNetworkAvailable())
        {
            Toast.makeText(this, "No network", Toast.LENGTH_SHORT).show();
            return;
        }
        *//*txtToken = (TextView) findViewById(R.id.txtResult);*//*
        ////EditText txtUsrName = (EditText) findViewById(R.id.txtUserName);
       // EditText txtPwd = (EditText) findViewById(R.id.txtPwd);
        Toast.makeText(this, "Button clicked", Toast.LENGTH_SHORT).show();

        RequestBody formBody = new FormBody.Builder()
                .add("username","devmodelrinternal@iazi.ch")
                .add("password", "LetsT3st")
                .add("app", "service")
                .build();

        okHttpClient = new OkHttpClient();
        request = new Request.Builder().url(Url).build();
        request = new Request.Builder().url(Url)
                .header("Accept","application/json")
                .header("Content-Type","application/x-www-form-urlencoded")
                .post(formBody)
                .build();
        try {
            int i=0;
            okHttpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.i("IN",e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {

                        final String token=response.body().string();
                        Log.i("IN", token);
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    JSONObject jsonResponse = new JSONObject(token);

                                    try
                                    {
                                        txtToken.setText(jsonResponse.getString("message"));
                                    }
                                    catch(Exception exp){}
                                    try
                                    {
                                        txtToken.setText(jsonResponse.getString("token"));
                                    }
                                    catch(Exception exp){}
                                }
                                catch(Exception e)
                                {
                                    Log.i("error",e.getMessage());
                                }

                            }
                        });
                    }
                    catch(Exception e)
                    {
                        Log.i("error",e.getMessage());
                    }
                }
            });


        }
        catch(Exception e)
        {
            Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();}
    }*/

}