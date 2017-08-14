package com.example.karmali.homexperts;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.FusedLocationProviderClient;

import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.FileProvider;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONObject;
import android.app.ProgressDialog;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;


import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AppraisalActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, OnMapReadyCallback {


    private String UrlDefaultAppraise = "https://intservices.iazi.ch/api/apps/v1/defaultOfferedRentAppraisal";
    private String UrlAppraise ="https://intservices.iazi.ch/api/apps/v1/OfferedRentAppraisal";
    String deviceId;
    OkHttpClient okHttpClient;
    MediaType JSON;
    private Request request;
    private Handler mHandler;
    TextView txtAppraisePrice, txtAddress;
    TextView livserfacevalue, textViewRoomsVal;
    SeekBar  seekBarLivSurf, seekBarRooms,roomSeekBar,surfaceSeekBar;
    Spinner yearspin,objectTypeSpinner;
    CheckBox lift,liftCheckBox;
    Button buttonA3, buttonA2, estimate;
    ArrayAdapter<String> adapter, adapterObjectType;
    private ProgressBar bar;

    Bitmap imageBitmap;
    ProgressDialog dialog;
    private String requestZip,requestTown, requestStreet, requestCategory,requestlift="0";

    String userAddress;
    LatLng userAddressLatLng;

    private static final int REQUEST_CHECK_SETTINGS = 111;
    private FusedLocationProviderClient mFusedLocationClient;
    private GoogleApiClient mGoogleApiCLient;
    private static final int LOCATION_PERMISSION_CONSTANT = 100;
    private static final int REQUEST_PERMISSION_SETTING = 101;
    private boolean sentToSettings = false;
    private SharedPreferences permissionStatus;
    private GoogleMap mGoogleMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appraisal);

        dialog = new ProgressDialog(AppraisalActivity.this);
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        dialog.setMessage("Calculating rent with default parameters");
        dialog.show();

        Bundle bun = getIntent().getExtras();
        String savedImageUrl = bun.getString("PhotoUrl");
        fillImageView(savedImageUrl);

        yearspin = (Spinner) findViewById(R.id.yearspin);
        objectTypeSpinner = (Spinner) findViewById(R.id.objectTypeSpinner);
        livserfacevalue = (TextView) findViewById(R.id.textViewLivSurfVal);
        seekBarLivSurf= (SeekBar) findViewById(R.id.seekBarLivSurf);
        textViewRoomsVal = (TextView) findViewById(R.id.textViewRoomsVal);
        seekBarRooms = (SeekBar) findViewById(R.id.seekBarRooms);


        lift = (CheckBox) findViewById(R.id.lift);
        txtAppraisePrice = (TextView) findViewById(R.id.textViewAppraiseValue);

        defaultAppraisal(savedImageUrl);

        ImageView img = (ImageView) findViewById(R.id.gotoloacationactivity);

        ArrayList<String> years = new ArrayList<String>();

        //Spinner For Build Year
        int thisYear = Calendar.getInstance().get(Calendar.YEAR) + 2;
        for (int i = 1900; i <= thisYear; i++) {
            years.add(Integer.toString(i));
        }
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, years);

        Spinner spinYear = (Spinner)findViewById(R.id.yearspin);
        spinYear.setAdapter(adapter);


        /* Bitmap bitmap = (Bitmap) this.getIntent().getParcelableExtra("BitmapImage");
        ImageView imageview = (ImageView) findViewById(R.id.capturedImageView);
        imageview.setImageBitmap(bitmap);*/


        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        permissionStatus = getSharedPreferences("permissionStatus", MODE_PRIVATE);

        buttonA2 = (Button) findViewById(R.id.buttonA2);
        buttonA3 = (Button) findViewById(R.id.buttonA3);
        buttonA2.setTransformationMethod(null);
        buttonA3.setTransformationMethod(null);
        buttonA2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                buttonA2.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorAccent, null));
                buttonA3.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.roundedborder, null));
                buttonA2.setTextColor(Color.WHITE);
                buttonA3.setTextColor(ResourcesCompat.getColor(getResources(), R.color.colorAccent, null));
                requestCategory = "5";

            }
        });
        buttonA3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                buttonA3.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorAccent, null));
                buttonA2.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.roundedborder, null));
                buttonA3.setTextColor(Color.WHITE);
                buttonA2.setTextColor(ResourcesCompat.getColor(getResources(), R.color.colorAccent, null));
                requestCategory = "6";
            }
        });
        estimate = (Button) findViewById(R.id.buttonAppraise);
        estimate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                AppraisalService();
            }

        });

        //Bundle bun = getIntent().getExtras();
        // String savedImageUrl = bun.getString("PhotoUrl");
        img.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                goToLocation();
            }
        });

        liftCheckBox =(CheckBox) findViewById(R.id.lift);
        lift.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if(isChecked)
                    requestlift = "1";
                else
                    requestlift = "0";
            }
        });

        roomSeekBar = (SeekBar) findViewById(R.id.seekBarRooms);
        roomSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int roomNo, boolean fromUser) {

                textViewRoomsVal.setText(String.valueOf(roomNo));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        surfaceSeekBar = (SeekBar) findViewById(R.id.seekBarLivSurf);
        surfaceSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {

                livserfacevalue.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        txtAddress = findViewById(R.id.addressText);
        getAddress();
        txtAddress.setText(userAddress);

        MapFragment mapFragment=(MapFragment)getFragmentManager().findFragmentById(R.id.mapViewCurrentLocation);
        mapFragment.getMapAsync(this);
    }

    private void getAddress(){
        final AddressGlobal addressGlobal = (AddressGlobal)getApplicationContext();
        userAddress=addressGlobal.getAddress();
        userAddressLatLng=addressGlobal.getAddresslatLng();
    }

    public void goToLocation() {
        Intent intent = new Intent(AppraisalActivity.this,UserLocationActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        this.startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        finish();
    }

    //To display image
    private void fillImageView(String savedImageUrl) {
        try {
            Uri tempUri = FileProvider.getUriForFile(this, "com.example.karmali.homexperts.fileprovider", new File(savedImageUrl));
            //Toast.makeText(this, "Data: " + tempUri.toString(), Toast.LENGTH_SHORT).show();
            imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), tempUri);
            ImageView imageView = (ImageView)findViewById(R.id.capturedImageView);
            imageView.setImageBitmap(Bitmap.createScaledBitmap(imageBitmap, 450, 330, false));
            //imageView.setImageBitmap(imageBitmap);
            //GetLocation();
        }
        catch (Exception ex)
        {
            Toast.makeText(this, "Error: "+ex.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    static final int REQUEST_IMAGE_CAPTURE = 121;
    private void capturePictureIntent() {
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

    public void defaultAppraisal(String savedImageUrl) {
        okHttpClient = new OkHttpClient();

        if(!isNetworkAvailable())
        {
            Toast.makeText(this, "No network", Toast.LENGTH_SHORT).show();
            return;
        }
        Uri tempUri = FileProvider.getUriForFile(this, "com.example.karmali.homexperts.fileprovider", new File(savedImageUrl));


        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int nh = (int) ( imageBitmap.getHeight() * (512.0 / imageBitmap.getWidth()) );
        Bitmap small = Bitmap.createScaledBitmap(imageBitmap, 512, nh, true);
        small.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray  = byteArrayOutputStream .toByteArray();
        String imageBase64 = Base64.encodeToString(byteArray, Base64.DEFAULT);


        deviceId =  Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID); ;// "123XGH67";

        RequestBody formBody = new FormBody.Builder()
                .add("imageBase64",imageBase64)
                .add("lat", "47.4091209")
                .add("lng","8.5467016")
                .add("deviceId",deviceId.toString())
                .build();
        request = new Request.Builder().url(UrlDefaultAppraise).build();

        request = new Request.Builder()
                .url(UrlDefaultAppraise)
                .header("Accept","application/json")
                .header("Content-Type","application/x-www-form-urlencoded")
                .post(formBody)
                .build();
        try {
            //Toast.makeText(AppraisalActivity.this, "Before", Toast.LENGTH_SHORT).show();
            okHttpClient.newCall(request).enqueue(new Callback() {

                @Override
                public void onFailure(Call call, IOException e) {
                    Log.i("IN",e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {

                        final String myResponse = response.body().string();
                        Log.i("IN", myResponse);
                        final JSONObject json =  new JSONObject(myResponse);
                        final String category = json.getString("categoryCode");


/*                        if(category.trim().equals("0"))
                        {
                            AlertDialog.Builder builder = new AlertDialog.Builder(AppraisalActivity.this);
                            builder.setMessage("click another image.");
                            builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                            AlertDialog alert = builder.create();
                            alert.setCanceledOnTouchOutside(false);
                            alert.show();
                        }
                        else
                        {*/
                        final String appvalue = json.getString("appraisalValue");
                        final String surface = json.getString("surfaceContract");
                        final String liftValue = json.getString("lift");
                        final String year = json.getString("buildYear");
                        final String roomNo = json.getString("roomNb");
                        final String object = json.getString("objectType");
                        requestZip = json.getString("zip");
                        requestTown=json.getString("town");
                        requestStreet = json.getString("street");

                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {

                                //Spinner For Object Type
                                adapterObjectType = new ArrayAdapter<String>(AppraisalActivity.this, android.R.layout.simple_spinner_item,getResources().getStringArray(R.array.android_dropdown_objectType));
                                objectTypeSpinner.setAdapter(adapterObjectType);
                                if (!object.equals(null)) {
                                    int spinnerPosition = adapter.getPosition(object);
                                    objectTypeSpinner.setSelection(spinnerPosition);
                                }

                                livserfacevalue.setText(surface);
                                seekBarLivSurf.setProgress(Integer.parseInt(surface));
                                textViewRoomsVal.setText(roomNo);
                                int roomInt = (int)Float.parseFloat(roomNo);
                                seekBarRooms.setProgress(roomInt);

                                if (!year.equals(null)) {
                                    int spinnerPosition = adapter.getPosition(year);
                                    yearspin.setSelection(spinnerPosition);
                                }

                                if(liftValue.trim().equals("1"))
                                    lift.setChecked(true);
                                else
                                    lift.setChecked(false);

                                if(category.trim().equals("5"))
                                {
                                    buttonA2.performClick();
                                }

                                else
                                {
                                    buttonA3.performClick();
                                }


                                txtAppraisePrice.setText(appvalue);
                                dialog.dismiss();


                            }
                        });
                    }
                    catch(Exception e)
                    {
                        Toast.makeText(AppraisalActivity.this, "1.Error", Toast.LENGTH_SHORT).show();
                        Log.i("error",e.getMessage());
                    }
                }
            });
        }
        catch(Exception e)
        {
            Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void AppraisalService() {
        if(!isNetworkAvailable())
        {
            Toast.makeText(this, "No network", Toast.LENGTH_SHORT).show();
            return;
        }

        TextView roomNb = (TextView) findViewById(R.id.textViewRoomsVal);
        TextView surfaceLiving = (TextView) findViewById(R.id.textViewLivSurfVal);
        deviceId =  Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID); ;// "123XGH67";
        Spinner spinner = (Spinner)findViewById(R.id.yearspin);
        String buildYear = spinner.getSelectedItem().toString();


        Spinner objectspinner = (Spinner)findViewById(R.id.objectTypeSpinner);
        String objectType = objectspinner.getSelectedItem().toString();

        //Spinner For Object Type
        adapterObjectType = new ArrayAdapter<String>(AppraisalActivity.this, android.R.layout.simple_spinner_item,getResources().getStringArray(R.array.android_dropdown_objectType));
        objectTypeSpinner.setAdapter(adapterObjectType);

        int spinnerPosition = adapterObjectType.getPosition(objectType);
        objectTypeSpinner.setSelection(spinnerPosition);


        String values [] =  getResources().getStringArray(R.array.android_dropdown_objectValue);

        String value = values [spinnerPosition];

        RequestBody appraiseData = new FormBody.Builder()
                .add("ortId","35")
                .add("externalKey","35")
                .add("categoryCode",requestCategory)
                .add("objectTypeCode",value)
                .add("qualityMicro","3")
                .add("surfaceContract",surfaceLiving.getText().toString())
                .add("buildYear",buildYear)
                .add("roomNb",roomNb.getText().toString())
                .add("lift", requestlift)
                .add("deviceId",deviceId.toString())
                .add("address", "address")
                .add("address.lat","0")
                .add("address.lng","0")
                .add("address.country","Switzerland")
                .build();

        okHttpClient = new OkHttpClient();
        request = new Request.Builder().url(UrlAppraise).build();
        request = new Request.Builder().url(UrlAppraise)
                .header("Accept","application/json")
                .header("Content-Type","application/x-www-form-urlencoded")
                .post(appraiseData)
                .build();
        try {

            okHttpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    dialog.dismiss();
                    Log.i("IN",e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        final String myResponse=response.body().string();

                        final JSONObject json =  new JSONObject(myResponse);
                        final String appvalue = json.getString("appraisalValue");
                        final String surface = json.getString("surfaceContract");
                        final String liftValue = json.getString("lift");
                        final String category = json.getString("categoryCode");
                        final String object = json.getString("objectType");
                        final String year = json.getString("buildYear");
                        final String roomNo = json.getString("roomNb");
                        requestZip = json.getString("zip");
                        requestTown=json.getString("town");
                        requestStreet = json.getString("street");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {


                                livserfacevalue.setText(surface);
                                seekBarLivSurf.setProgress(Integer.parseInt(surface));
                                textViewRoomsVal.setText(roomNo);
                                int roomInt = (int)Float.parseFloat(roomNo);
                                seekBarRooms.setProgress(roomInt);

                                if (!object.equals(null)) {
                                    int spinnerPosition = adapter.getPosition(object);
                                    objectTypeSpinner.setSelection(spinnerPosition);
                                }


                                if (!year.equals(null)) {
                                    int spinnerPosition = adapter.getPosition(year);
                                    yearspin.setSelection(spinnerPosition);
                                }
                                if(liftValue.trim().equals("1"))
                                    lift.setChecked(true);
                                else
                                    lift.setChecked(false);

                                if(category.trim().equals("5"))
                                    buttonA2.performClick();
                                else
                                    buttonA3.performClick();

                                txtAppraisePrice.setText(appvalue);
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
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void getLocation() {
        /*
        LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean enabled = service.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!enabled) {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }
        */


        //check whether app is given permission to access location
        checkLocationPermission();
        try {
            if (permissionStatus.getBoolean(Manifest.permission.ACCESS_FINE_LOCATION, true)) {
                //check if device location is turned on


                mGoogleApiCLient=new GoogleApiClient.Builder(getBaseContext()).addApi(LocationServices.API).addConnectionCallbacks(this).addOnConnectionFailedListener(this).build();
                mGoogleApiCLient.connect();

                LocationRequest locationRequest=LocationRequest.create();
                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY).setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

                LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                        .addLocationRequest(locationRequest);


                PendingResult<LocationSettingsResult> result =
                        LocationServices.SettingsApi.checkLocationSettings(mGoogleApiCLient, builder.build());

                result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                    @Override
                    public void onResult(LocationSettingsResult result) {
                        final Status status = result.getStatus();
                        final LocationSettingsStates lss = result.getLocationSettingsStates();
                        switch (status.getStatusCode()) {
                            case LocationSettingsStatusCodes.SUCCESS:
                                // All location settings are satisfied. The client can initialize location
                                // requests here.
                                break;
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                // Location settings are not satisfied. But could be fixed by showing the user
                                // a dialog.
                                try {
                                    // Show the dialog by calling startResolutionForResult(),
                                    // and check the result in onActivityResult().
                                    status.startResolutionForResult(
                                            AppraisalActivity.this,
                                            REQUEST_CHECK_SETTINGS);
                                } catch (IntentSender.SendIntentException e) {
                                    // Ignore the error.
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                // Location settings are not satisfied. However, we have no way to fix the
                                // settings so we won't show the dialog.
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
        }
        catch (Exception ex) {
            Toast.makeText(this, "get location after permission check"+ex.toString(), Toast.LENGTH_LONG).show();
        }

    }

    private void checkLocationPermission() {
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                if (ActivityCompat.shouldShowRequestPermissionRationale(AppraisalActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    //Show Information about why you need the permission
                    //Toast.makeText(this, "if block", Toast.LENGTH_LONG).show();
                    AlertDialog.Builder builder = new AlertDialog.Builder(AppraisalActivity.this);
                    builder.setTitle("Need Location Permission");
                    builder.setMessage("This app needs to access your location.");
                    builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            ActivityCompat.requestPermissions(AppraisalActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_CONSTANT);
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
                    //Previously Permission Request was cancelled with 'Dont Ask Again',
                    // Redirect to Settings after showing Information about why you need the permission

                    Toast.makeText(this, "Check permission: Else if block", Toast.LENGTH_LONG).show();
                    AlertDialog.Builder builder = new AlertDialog.Builder(AppraisalActivity.this);
                    builder.setTitle("Need Location Permission");
                    builder.setMessage("This app needs to access your location.");
                    builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            sentToSettings = true;
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", getPackageName(), null);
                            intent.setData(uri);
                            startActivityForResult(intent, REQUEST_PERMISSION_SETTING);
                            Toast.makeText(getBaseContext(), "Go to Permissions to Grant Location access", Toast.LENGTH_LONG).show();
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
                    Toast.makeText(this, "Check permission: Else block", Toast.LENGTH_LONG).show();
                    ActivityCompat.requestPermissions(AppraisalActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_CONSTANT);
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

    private void getDeviceLocation() {
        try {
            Toast.makeText(getBaseContext(), "All set up. Now getting location", Toast.LENGTH_LONG).show();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //final LocationSettingsStates states = LocationSettingsStates.fromIntent(data);
        switch (requestCode) {
            case REQUEST_PERMISSION_SETTING :
                if (ActivityCompat.checkSelfPermission(AppraisalActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    //Got Permission
                    //Toast.makeText(getBaseContext(), "Permission granted", Toast.LENGTH_LONG).show();
                }
                break;
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
                            ImageView imageView = (ImageView)findViewById(R.id.imageView);
                            imageView.setImageBitmap(Bitmap.createScaledBitmap(imageBitmap, 300, 300, false));

                            //Intent appraiseActivity = new Intent(this, AppraisalActivity.class);
                            //Bundle bun = new Bundle();
                            //bun.putString("PhotoUrl");
                            //appraiseActivity.putExtra("PhotoUrl", mCurrentPhotoPath);
                            //startActivity(appraiseActivity);
                        }
                    }
                    catch (Exception e) {
                        Toast.makeText(this, "Error: "+e.toString(), Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }

/*    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        final LocationSettingsStates states = LocationSettingsStates.fromIntent(data);
        switch (requestCode) {
            case REQUEST_PERMISSION_SETTING :
                if (ActivityCompat.checkSelfPermission(AppraisalActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    //Got Permission
                    //Toast.makeText(getBaseContext(), "Permission granted", Toast.LENGTH_LONG).show();
                }
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
        }
    }*/

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (sentToSettings) {
            if (ActivityCompat.checkSelfPermission(AppraisalActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                //Got Permission
                //Toast.makeText(getBaseContext(), "Returned from settings. Permission granted", Toast.LENGTH_LONG).show();
            }
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


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap=googleMap;
        setCurrentLocationOnMap();
    }

    private Marker mCurrLocationMarker;
    private void setCurrentLocationOnMap() {
        if (mGoogleMap!=null&&userAddressLatLng != null) {
            mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(userAddressLatLng, 14);
            if (mCurrLocationMarker != null)
                mCurrLocationMarker.remove();
            MarkerOptions markerOptions=new MarkerOptions().position(userAddressLatLng).title("You're here");
            mCurrLocationMarker=mGoogleMap.addMarker(markerOptions);
            mGoogleMap.moveCamera(cameraUpdate);
        }
    }
}