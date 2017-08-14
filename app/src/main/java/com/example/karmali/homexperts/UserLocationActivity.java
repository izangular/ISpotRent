package com.example.karmali.homexperts;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.ResultReceiver;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class UserLocationActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int REQUEST_PERMISSION_SETTING = 101;
    private boolean sentToSettings = false;
    private SharedPreferences permissionStatus;

    private AddressResultReceiver mResultReceiver;
    private FusedLocationProviderClient mFusedLocationClient;
    TextView txtCurrentAddress;

    //private LatLng mLocationLatLong;
    private GoogleMap mGoogleMap;
    private Location mCurrentLocation;
    private LocationCallback mLocationCallback;
    private LocationRequest mLocationRequest;
    private Marker mCurrLocationMarker;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_location);

        permissionStatus = getSharedPreferences("permissionStatus", MODE_PRIVATE);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mResultReceiver = new AddressResultReceiver(null);
        txtCurrentAddress = ((AppCompatActivity) this).findViewById(R.id.textViewCurrentAddress);
        //mLocationLatLong=new LatLng(0,0);

        ImageView imageView = ((AppCompatActivity) this).findViewById(R.id.imageViewClickPic);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImageView imgview = (ImageView)view;
                if(imgview.getDrawable()!=null) {
                    //capturePictureIntent();
                    if(checkCameraPermission()) {
                        Intent showImageActivity = new Intent(UserLocationActivity.this, CameraActivity.class);
                        startActivity(showImageActivity);
                    }else {
                        getCameraPermission();
                        if(checkCameraPermission()){
                            Intent showImageActivity = new Intent(UserLocationActivity.this, CameraActivity.class);
                            startActivity(showImageActivity);
                        }

                    }
                }
            }
        });

        mBackButtonFlagHandler =new Handler();

        getCurrentLocation();

        MapFragment mapFragment=(MapFragment)getFragmentManager().findFragmentById(R.id.mapViewCurrentLocation);
        mapFragment.getMapAsync(this);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                try {
                    for (Location location : locationResult.getLocations()) {
                        // Update UI with location data
                        mLocationCallback = new LocationCallback() {
                            @Override
                            public void onLocationResult(LocationResult locationResult) {
                                for (Location location : locationResult.getLocations()) {
                                    // Update UI with location data
                                    mCurrentLocation = location;
                                /*if (mCurrLocationMarker != null) {
                                    mCurrLocationMarker.remove();
                                }*/
                                    startGeocoderIntentService(location.getLatitude(), location.getLongitude());
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            setCurrentLocationOnMap();
                                        }
                                    });
                                }
                            }

                            ;
                        };
                    }
                }catch (Exception ex){
                    Toast.makeText(getBaseContext(), "Error getting location"+ex.toString(), Toast.LENGTH_LONG).show();
                }
            };
        };
    }


    private void getCurrentLocation(){
        try {
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                mCurrentLocation=location;
                                //mLocationLatLong=new LatLng(location.getLatitude(),location.getLongitude());
                                startGeocoderIntentService(location.getLatitude(),location.getLongitude());
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        setCurrentLocationOnMap();
                                    }
                                });
                                //startGeocoderIntentService(47.408129, 8.554985);
                                //txtCurrentAddress.setText();
                                //Toast.makeText(UserLocationActivity.this, "Hello Satvesh! Location- Lat: " + location.getLatitude() + " Long: " + location.getLongitude(), Toast.LENGTH_LONG).show();
                            }
                            else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(UserLocationActivity.this, "Error getting your current location", Toast.LENGTH_LONG).show();
                                    }
                                });

                            }
                        }
                    });
        }
        catch(SecurityException se){
            //rare case for exception since Permissions should be granted to reach this stage
            Toast.makeText(UserLocationActivity.this, "Security exception getting location: " + se.toString(), Toast.LENGTH_LONG).show();
        }
    }

    protected void startGeocoderIntentService(double lat, double lon) {
        Intent intent = new Intent(this, GeocoderIntentService.class);
        intent.putExtra(Constants.RECEIVER, mResultReceiver);
        intent.putExtra(Constants.LOCATION_LATITUDE_DATA_EXTRA, lat);
        intent.putExtra(Constants.LOCATION_LONGITUDE_DATA_EXTRA, lon);
        startService(intent);
    }

    static final int REQUEST_IMAGE_CAPTURE = 121;
    private void capturePictureIntent() {
        //checkStoragePermission();
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Toast.makeText(this, "Error occurred", Toast.LENGTH_SHORT).show();
            }
            // Continue only if the File was successfully created
            Uri photoURI=null;
            if (photoFile != null) {
                //Uri photoURI1= Uri.fromFile(photoFile);
                try {
                    photoURI = FileProvider.getUriForFile(this, "com.example.karmali.homexperts.fileprovider", photoFile);
                }
                catch(Exception e) {
                    Toast.makeText(this, "Error in file provider: " + e.toString(), Toast.LENGTH_LONG).show();
                }
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    String mCurrentPhotoPath;
    private File createImageFile() throws IOException {
        try {
            // Create an image file name
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String imageFileName = "JPEG_" + timeStamp + "_";
            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            File image = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".jpg",         /* suffix */
                    storageDir      /* directory */
            );

            // Save a file: path for use with ACTION_VIEW intents
            mCurrentPhotoPath = image.getAbsolutePath();
            return image;
        }
        catch(Exception e) {
            Toast.makeText(this, "Error occurred"+e.toString(), Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //final LocationSettingsStates states = LocationSettingsStates.fromIntent(data);
        switch (requestCode) {
            case REQUEST_IMAGE_CAPTURE:
                if(resultCode == RESULT_OK)
                {
                    try {

                        //Toast.makeText(this, "Image capture activityResult", Toast.LENGTH_LONG).show();
                        Uri tempUri=FileProvider.getUriForFile(this, "com.example.karmali.homexperts.fileprovider", new File(mCurrentPhotoPath));
                        //Toast.makeText(this, "Image Uri: "+tempUri.toString(), Toast.LENGTH_SHORT).show();
                        Bitmap imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), tempUri);

                        //ivProfilePic.setImageBitmap(Bitmap.createScaledBitmap(b, 120, 120, false));
                        //Bitmap imageBitmap = (Bitmap) extras.get("data");
                        if(imageBitmap ==null) Toast.makeText(this, "Null is image", Toast.LENGTH_SHORT).show();
                        else {
                            //Toast.makeText(this, "Image is good", Toast.LENGTH_SHORT).show();
                            //Image data proper, pass it to appraise activity
                            //ImageView imageView = (ImageView)findViewById(R.id.imageView);
                            //imageView.setImageBitmap(Bitmap.createScaledBitmap(imageBitmap, 300, 300, false));

                            /*
                            Intent appraiseActivity = new Intent(this, AppraisalActivity.class);
                            appraiseActivity.putExtra("PhotoUrl", mCurrentPhotoPath);
                            startActivity(appraiseActivity);
                            */
                            //show image activity
                            Intent showImageActivity = new Intent(this, AppraisalActivity.class);
                            showImageActivity.putExtra("PhotoUrl", mCurrentPhotoPath);
                            startActivity(showImageActivity);
                        }
                    }
                    catch (Exception e) {
                        Toast.makeText(this, "Error: "+e.toString(), Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }

    private void setCurrentAddress(Address address){
        try {
            //txtCurrentAddress.setText(address.getAddressLine(0) + ", " + address.getAddressLine(1));
            final AddressGlobal addressGlobal = (AddressGlobal)getApplicationContext();
            addressGlobal.setAddress(address.getAddressLine(0) + ", " + address.getAddressLine(1));
            addressGlobal.setAddressLatLng(new LatLng(mCurrentLocation.getLatitude(),mCurrentLocation.getLongitude()));
        }
        catch(Exception ex){
            Toast.makeText(UserLocationActivity.this, "Exception displaying address: " + ex.toString(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap=googleMap;
        /*
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(19.100290, 72.875016), 10);
        mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(19.100290, 72.875016)).title("Marker"));
        mGoogleMap.animateCamera(cameraUpdate);
        */
    }

    private void setCurrentLocationOnMap() {
        if (mGoogleMap!=null&&mCurrentLocation != null) {
            mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()), 14);
            if (mCurrLocationMarker != null)
                mCurrLocationMarker.remove();
            MarkerOptions markerOptions=new MarkerOptions().position(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude())).title("You're here");
            mCurrLocationMarker=mGoogleMap.addMarker(markerOptions);
            mGoogleMap.animateCamera(cameraUpdate);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        startLocationUpdates();
    }

    private void startLocationUpdates() {
        try {
            mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(60000); // One minute interval
            mLocationRequest.setFastestInterval(60000);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
            mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                    mLocationCallback,
                    null /* Looper */);
        }
        catch(SecurityException se){
            Toast.makeText(UserLocationActivity.this, "Security exception getting location updates: " + se.toString(), Toast.LENGTH_LONG).show();
        }
    }


    class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, final Bundle resultData) {
            if (resultCode == Constants.SUCCESS_RESULT) {
                final Address address = resultData.getParcelable(Constants.RESULT_ADDRESS);

                runOnUiThread(
                        new Runnable() {
                            @Override
                            public void run() {
                                //txtCurrentAddress.setText("Merces" + ", " + address.getPostalCode() + ", " + address.getLocality());
                                setCurrentAddress(address);
                            }
                        }
                );
            }
            else {
                //error getting address
            }
        }
    }

    boolean backPressedOnce=false;
    private Handler mBackButtonFlagHandler;
    private final Runnable mBackButtonFlagRunnable=new Runnable() {
        @Override
        public void run() {
            backPressedOnce=false;
        }
    };

    @Override
    public void onBackPressed(){
        if(!backPressedOnce) {
            backPressedOnce = true;
            Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show();

            mBackButtonFlagHandler.postDelayed(mBackButtonFlagRunnable,2000);
        }
        else {
            super.onBackPressed();
            return;
        }
    }


    private void getCameraPermission(){
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                //request the missing permissions
                if (ActivityCompat.shouldShowRequestPermissionRationale(UserLocationActivity.this, Manifest.permission.CAMERA)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(UserLocationActivity.this);
                    builder.setTitle("Need Camera Permission");
                    builder.setMessage("HomeXperts needs to access your Camera.");
                    builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            ActivityCompat.requestPermissions(UserLocationActivity.this, new String[]{Manifest.permission.CAMERA}, REQUEST_PERMISSION_SETTING);
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    builder.show();
                } else if (permissionStatus.getBoolean(Manifest.permission.CAMERA,false)) {
                    //Here, only 1 check is enough beacuse we have set flag based on location request response
                    //Previously Permission Request was cancelled with 'Don't Ask Again',
                    // Redirect to Settings after showing Information about why you need the permission

                    //Toast.makeText(this, "Check permission: Else if block", Toast.LENGTH_LONG).show();
                    AlertDialog.Builder builder = new AlertDialog.Builder(UserLocationActivity.this);
                    builder.setTitle("Need Camera permission");
                    builder.setMessage("HomeXperts needs to access your Camera.");
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
                    ActivityCompat.requestPermissions(UserLocationActivity.this, new String[]{Manifest.permission.CAMERA}, REQUEST_PERMISSION_SETTING);
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

    private boolean checkCameraPermission(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
            return true;
        else return false;
    }
}