package com.example.karmali.homexperts;

import android.*;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import java.util.UUID;

public class HomeActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {


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
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (!prefs.getBoolean("firstTime", false)) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    final Intent mainIntent = new Intent(HomeActivity.this, FormActivity.class);
                    HomeActivity.this.startActivity(mainIntent);
                    HomeActivity.this.finish();
                }
            }, 3000);

            // mark first time has runned.
            //SharedPreferences.Editor editor = prefs.edit();
           // editor.putBoolean("firstTime", true);
           // editor.commit();
        }

        else{
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (ActivityCompat.checkSelfPermission(HomeActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                    {

                        checkLocationStatus();
                        /*final Intent mainIntent = new Intent(HomeActivity.this, UserLocationActivity.class);
                        startActivity(mainIntent);
                        HomeActivity.this.finish();*/
                    }
                    else
                    {
                        final Intent mainIntent = new Intent(HomeActivity.this, ShowLocationSettingActivity.class);
                        startActivity(mainIntent);
                        HomeActivity.this.finish();
                    }
                }
            }, 3000);
        }
     /*
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
        }*/
    }

    private boolean checkPermission(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            return true;
        else return false;
    }

    private void checkLocationStatus() {
        if (checkPermission()) {
            GoogleApiClient mGoogleApiClient;
            mGoogleApiClient = new GoogleApiClient.Builder(getBaseContext()).addApi(LocationServices.API).addConnectionCallbacks(this).addOnConnectionFailedListener(this).build();
            mGoogleApiClient.connect();

            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY).setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(locationRequest);

            PendingResult<LocationSettingsResult> result =
                    LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());

            result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                @Override
                public void onResult(LocationSettingsResult result) {
                    final Status status = result.getStatus();
                    final LocationSettingsStates lss = result.getLocationSettingsStates();
                    switch (status.getStatusCode()) {
                        case LocationSettingsStatusCodes.SUCCESS:
                            // All location settings are satisfied. The client can initialize location
                            // requests here.
                            Intent userLocationActivity = new Intent(HomeActivity.this, UserLocationActivity.class);
                            startActivity(userLocationActivity);
                            finish();

                            break;
                        default:
                            Intent locationSettingActivity = new Intent(HomeActivity.this, ShowLocationSettingActivity.class);
                            startActivity(locationSettingActivity);
                            finish();
                            break;
                    }
                }
            });
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}