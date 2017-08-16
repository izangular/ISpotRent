package com.example.karmali.homexperts;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

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

public class ShowLocationSettingActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{
    private GoogleApiClient mGoogleApiClient;
    private SharedPreferences permissionStatus;
    private static final int REQUEST_CHECK_SETTINGS = 111;
    private static final int REQUEST_PERMISSION_SETTING = 101;
    private static boolean LOCATION_ENABLED=false;
    private boolean sentToSettings = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        permissionStatus = getSharedPreferences("permissionStatus", MODE_PRIVATE);

        checkLocationStatus();

        setContentView(R.layout.activity_show_location_setting);

        final Button buttonEnableLocation = (Button) findViewById(R.id.buttonEnableLocation);
        buttonEnableLocation.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                enableLocation();
            }
        });
    }

    private void checkLocationStatus() {
        if (checkPermission()) {
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
                            Intent userLocationActivity = new Intent(ShowLocationSettingActivity.this, UserLocationActivity.class);
                            startActivity(userLocationActivity);
                            finish();

                            break;
                    }
                }
            });
        }
    }

    private void enableLocation(){
        try {

            //permission check needed?
            if (checkPermission()) {
                //check if device location is turned on

                mGoogleApiClient =new GoogleApiClient.Builder(getBaseContext()).addApi(LocationServices.API).addConnectionCallbacks(this).addOnConnectionFailedListener(this).build();
                mGoogleApiClient.connect();

                LocationRequest locationRequest=LocationRequest.create();
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
                                LOCATION_ENABLED=true;
                                Intent userLocationActivity = new Intent(ShowLocationSettingActivity.this, UserLocationActivity.class);
                                startActivity(userLocationActivity);
                                finish();

                                break;
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                // Location settings are not satisfied. But could be fixed by showing the user
                                // a dialog.
                                try {
                                    // Show the dialog by calling startResolutionForResult(),
                                    // and check the result in onActivityResult().
                                    status.startResolutionForResult(
                                            ShowLocationSettingActivity.this,
                                            REQUEST_CHECK_SETTINGS);
                                } catch (IntentSender.SendIntentException e) {
                                    // Ignore the error.
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                // Location settings are not satisfied. However, we have no way to fix the settings so we won't show the dialog.
                                break;
                        }
                    }
                });

                //make it wait till permissions are set else its gonna crash
                /*
                mFusedLocationClient.getLastLocation()
                        .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                // Got last known location. In some rare situations this can be null.
                                if (location != null) {
                                    Toast.makeText(AppraisalActivity.this, "Location- Lat: " + location.getLatitude() + " Long: " + location.getLongitude(), Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                */
            }
            else {
                setPermissions();
            }
        }
        catch (Exception ex) {
            Toast.makeText(this, "get location after permission check - "+ex.toString(), Toast.LENGTH_LONG).show();
        }
    }

    private void getDeviceLocation() {
        try {
            //Toast.makeText(getBaseContext(), "All set up. Now getting location", Toast.LENGTH_LONG).show();

            //check if location is still off
            if(!LOCATION_ENABLED) {
                enableLocation();
            }
            else {
                Intent userLocationActivity = new Intent(this, UserLocationActivity.class);
                startActivity(userLocationActivity);
                finish();
            }
            /*
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                Toast.makeText(AppraisalActivity.this, "Hello Satvesh! Location- Lat: " + location.getLatitude() + " Long: " + location.getLongitude(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                    */
        } catch (SecurityException ex) {

        }
    }

    private boolean checkPermission(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            return true;
        else return false;
    }

    private void setPermissions() {
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                //request the missing permissions
                //public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) for user response
                if (ActivityCompat.shouldShowRequestPermissionRationale(ShowLocationSettingActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    //Show Information about why you need the permission
                    //Toast.makeText(this, "if block", Toast.LENGTH_LONG).show();
                    AlertDialog.Builder builder = new AlertDialog.Builder(ShowLocationSettingActivity.this);
                    builder.setTitle("Need Location Permission");
                    builder.setMessage("XpertRent needs to access your location.");
                    builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            ActivityCompat.requestPermissions(ShowLocationSettingActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSION_SETTING);
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    builder.show();
                } else if (permissionStatus.getBoolean(Manifest.permission.ACCESS_FINE_LOCATION,false)) {
                    //Here, only 1 check is enough beacuse we have set flag based on location request response
                    //Previously Permission Request was cancelled with 'Don't Ask Again',
                    // Redirect to Settings after showing Information about why you need the permission

                    Toast.makeText(this, "Check permission: Else if block", Toast.LENGTH_LONG).show();
                    AlertDialog.Builder builder = new AlertDialog.Builder(ShowLocationSettingActivity.this);
                    builder.setTitle("Need Location permission");
                    builder.setMessage("XpertRent needs to access your location.");
                    builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            sentToSettings = true;
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", getPackageName(), null);
                            intent.setData(uri);
                            startActivityForResult(intent, REQUEST_PERMISSION_SETTING);
                            //Toast.makeText(getBaseContext(), "Go to Permissions to Grant Location access", Toast.LENGTH_LONG).show();
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    builder.show();
                } else {
                    //just request the permission
                    //Toast.makeText(this, "Check permission: Else block", Toast.LENGTH_LONG).show();
                    ActivityCompat.requestPermissions(ShowLocationSettingActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSION_SETTING);
                }

                SharedPreferences.Editor editor = permissionStatus.edit();
                editor.putBoolean(Manifest.permission.ACCESS_FINE_LOCATION,true);
                editor.commit();
            }
        }
        catch  (Exception ex) {
            Toast.makeText(this, "Exception: "+ex.toString(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (sentToSettings) {
            if (ActivityCompat.checkSelfPermission(ShowLocationSettingActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                //Got Permission, go to next activity
                //Toast.makeText(getBaseContext(), "Returned from settings. Permission granted", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        // All required changes were successfully made
                        getDeviceLocation();//FINALLY YOUR OWN METHOD TO GET YOUR USER LOCATION HERE
                        break;
                    case Activity.RESULT_CANCELED:
                        // The user was asked to change settings, but chose not to

                        break;
                    default:
                        break;
                }
                break;
            case REQUEST_PERMISSION_SETTING :
                if (ActivityCompat.checkSelfPermission(ShowLocationSettingActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    //Got Permission
                    enableLocation();
                }
                break;
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
