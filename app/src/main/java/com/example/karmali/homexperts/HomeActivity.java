package com.example.karmali.homexperts;

import android.*;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;

import java.util.UUID;

public class HomeActivity extends AppCompatActivity {


    // Typeface font1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // CalligraphyConfig.initDefault("fonts/your-font.ttf");
        setContentView(R.layout.activity_home);
        checkDeviceId();


        // font1 = Typeface.createFromAsset(this.getAssets(),"modernesans.ttf");

    }
    private void checkDeviceId()
    {
       String deviceId =  Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
       if(deviceId.equals("84d2789836089ebf"))
        {

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (ActivityCompat.checkSelfPermission(HomeActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                    {
                        final Intent mainIntent = new Intent(HomeActivity.this, UserLocationActivity.class);
                        startActivity(mainIntent);
                        HomeActivity.this.finish();
                    }
                    else
                    {
                        final Intent mainIntent = new Intent(HomeActivity.this, ShowLocationSettingActivity.class);
                        startActivity(mainIntent);
                        HomeActivity.this.finish();
                    }

                }
            }, 5000);

        }
        else
        {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    final Intent mainIntent = new Intent(HomeActivity.this, FormActivity.class);
                    HomeActivity.this.startActivity(mainIntent);
                    HomeActivity.this.finish();
                }
            }, 5000);
        }
    }
}