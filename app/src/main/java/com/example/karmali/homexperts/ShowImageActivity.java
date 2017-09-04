package com.example.karmali.homexperts;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.design.widget.AppBarLayout;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ShowImageActivity extends AppCompatActivity {

    ImageView imageView;


    private String UrlDefaultAppraise = "https://intservices.iazi.ch/api/apps/v1/defaultOfferedRentAppraisal";
    private String UrlAppraise ="https://intservices.iazi.ch/api/apps/v1/OfferedRentAppraisal";
    String deviceId;
    OkHttpClient okHttpClient;
    MediaType JSON;
    private Request request;
    private Handler mHandler;
    TextView txtAppraisePrice, txtAddress;
    TextView livserfacevalue, textViewRoomsVal;
    SeekBar seekBarLivSurf, seekBarRooms,roomSeekBar,surfaceSeekBar;
    Spinner yearspin,objectTypeSpinner;
    CheckBox lift,liftCheckBox;
    Button buttonA3, buttonA2;
    Button estimate;
    ArrayAdapter<String> adapter, adapterObjectType;
    private ImageView imageViewCapturedImage;

    Bitmap imageBitmap;
    private String requestZip,requestTown, requestStreet, requestCategory="6",requestlift="0",requestaddres,requestqualityMicro,requestortId;

    String userAddress;
    LatLng userAddressLatLng;

    private static final int REQUEST_CHECK_SETTINGS = 111;
    private FusedLocationProviderClient mFusedLocationClient;
    private GoogleApiClient mGoogleApiCLient;
    private static final int LOCATION_PERMISSION_CONSTANT = 100;
    private static final int REQUEST_PERMISSION_SETTING = 101;
    private boolean sentToSettings = false;
    private SharedPreferences permissionStatus;

    private ProgressBar progressBarAppraisal;
    private ProgressBar progressBarAppraisalHeader;
    private TextView appraisalMessage,wait;

    Button buttonConfirmImage;


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_image);
        ///////////Action Bar//////////////
        ActionBar mActionBar = getSupportActionBar();
        mActionBar.setDisplayShowHomeEnabled(false);
        mActionBar.setDisplayShowTitleEnabled(false);
        LayoutInflater mInflater = LayoutInflater.from(this);

        View mCustomView = mInflater.inflate(R.layout.customactionbar, null);

        ImageView backButton = (ImageView) mCustomView.findViewById(R.id.backbutton);
        ImageButton imageButton = (ImageButton) mCustomView.findViewById(R.id.action_settings);
        imageButton.setVisibility(View.INVISIBLE);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ShowImageActivity.this, CameraActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                finish();
            }
        });

        mActionBar.setCustomView(mCustomView);
        mActionBar.setDisplayShowCustomEnabled(true);
        ///////////Action Bar End//////////////
        getAddress();
        Bundle bun = getIntent().getExtras();
        final String imageUrl = bun.getString("PhotoUrl");

        imageView = (ImageView)findViewById(R.id.imageViewCapturedImage);
        imageView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

                fillImageView(imageUrl);

                imageView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
        progressBarAppraisal = (ProgressBar) findViewById(R.id.progressBarAppraisal);
        appraisalMessage = (TextView) findViewById(R.id.appraisalMessage);
        wait = findViewById(R.id.wait);
        //set it to imageview
        //fillImageView(imageUrl);



        buttonConfirmImage = findViewById(R.id.buttonConfirmImage);
        buttonConfirmImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{

                    imageView.setVisibility(View.INVISIBLE);
                    progressBarAppraisal.setVisibility(View.VISIBLE);
                    wait.setVisibility(View.VISIBLE);
                    defaultAppraisal(imageUrl);
                    buttonConfirmImage.setVisibility(View.GONE);
                }
                catch (Exception e)
                {
                    Toast.makeText(ShowImageActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                }
               // Intent data = new Intent();
                //String text = "OK";
               // data.setData(Uri.parse(text));
               // setResult(RESULT_OK, data);
              //  finish();
            }
        });

       // getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        /*
        ImageView imageViewGoBackToLocation = findViewById(R.id.gotoloacationactivity);
        imageViewGoBackToLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent backToCamActivity = new Intent(ShowImageActivity.this, CameraActivity.class);
                backToCamActivity.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(backToCamActivity);
                finish();
            }
        });
        */

    }
    public void defaultAppraisal(final String savedImageUrl) {
        try {

            okHttpClient = new OkHttpClient();

            if (!isNetworkAvailable()) {
                Toast.makeText(this, "No network", Toast.LENGTH_SHORT).show();
                return;
            }


            Uri tempUri = FileProvider.getUriForFile(ShowImageActivity.this, "com.example.karmali.homexperts.fileprovider", new File(savedImageUrl));
            imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), tempUri);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            int nh = (int) (imageBitmap.getHeight() * (512.0 / imageBitmap.getWidth()));
            Bitmap small = Bitmap.createScaledBitmap(imageBitmap, 512, nh, true);
            small.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            String imageBase64 = Base64.encodeToString(byteArray, Base64.DEFAULT);


            deviceId = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
            ;// "123XGH67";

            RequestBody formBody = new FormBody.Builder()
                    .add("imageBase64", imageBase64)
                    .add("lat",String.valueOf(userAddressLatLng.latitude))
                    .add("lng",String.valueOf(userAddressLatLng.longitude))
                    .add("deviceId", deviceId.toString())
                    .build();
            request = new Request.Builder().url(UrlDefaultAppraise).build();

            request = new Request.Builder()
                    .url(UrlDefaultAppraise)
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .post(formBody)
                    .build();

            //Toast.makeText(AppraisalActivity.this, "Before", Toast.LENGTH_SHORT).show();
            okHttpClient.newCall(request).enqueue(new Callback() {

                @Override
                public void onFailure(Call call, IOException e) {
                    Log.i("IN", e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {

                        final String myResponse = response.body().string();
                        Log.i("IN", myResponse);
                        final JSONObject json = new JSONObject(myResponse);
                        final String category = json.getString("categoryCode");
                        final String categoryText = json.getString("category");

                        if (category.equals("-1")) {

                            runOnUiThread(new Runnable() {

                                @Override
                                public void run() {

                                    Resources res = getResources();
                                    String message_text = String.format(res.getString(R.string.appraisal_message), "\"" + categoryText + "\"");
                                    progressBarAppraisal.setVisibility(View.GONE);
                                    wait.setVisibility(View.GONE);
                                    appraisalMessage.setText(message_text);
                                    appraisalMessage.setVisibility(View.VISIBLE);


                                }
                            });

                        }
                        else {

                            Intent showImageActivity = new Intent(ShowImageActivity.this, AppraisalActivity.class);
                            showImageActivity.putExtra("PhotoUrl", savedImageUrl);
                            showImageActivity.putExtra("defValues",json.toString());
                            startActivity(showImageActivity);
                            finish();

                           /* final String appvalue = json.getString("appraisalValue");
                            final String surface = json.getString("surfaceContract");
                            final String liftValue = json.getString("lift");
                            final String year = json.getString("buildYear");
                            final String roomNo = json.getString("roomNb");
                            final String object = json.getString("objectType");
                            requestZip = json.getString("zip");
                            requestTown = json.getString("town");
                            requestStreet = json.getString("street");
                            requestaddres = requestStreet + ", " + requestZip + " " + requestTown;
                            requestqualityMicro = json.getString("qualityMicro");
                            requestortId = json.getString("ortId");


                            DecimalFormatSymbols symbols = new DecimalFormatSymbols();
                            symbols.setGroupingSeparator('\'');
                            DecimalFormat decimalFormat = new DecimalFormat("#,### CHF", symbols);
                            final  String localeFormattedNumber = decimalFormat.format(Integer.parseInt(appvalue));

                            runOnUiThread(new Runnable() {

                                @Override
                                public void run() {


                                    txtAddress.setText(requestaddres);

                                    //Spinner For Object Type
                                    adapterObjectType = new ArrayAdapter<String>(ShowImageActivity.this, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.android_dropdown_objectType));
                                    objectTypeSpinner.setAdapter(adapterObjectType);
                                    if (!object.equals(null)) {
                                        int spinnerPosition = adapter.getPosition(object);
                                        objectTypeSpinner.setSelection(spinnerPosition);
                                    }

                                    livserfacevalue.setText(surface);
                                    seekBarLivSurf.setProgress(Integer.parseInt(surface));


                                    int roomInt = (int) (Float.parseFloat(roomNo) * 2);
                                    seekBarRooms.setProgress(roomInt);

                                    if (!year.equals(null)) {
                                        int spinnerPosition = adapter.getPosition(year);
                                        yearspin.setSelection(spinnerPosition);
                                    }

                                    if (liftValue.trim().equals("1"))
                                        lift.setChecked(true);
                                    else
                                        lift.setChecked(false);

                                    if (category.trim().equals("5")) {
                                        buttonA2.performClick();
                                    } else {
                                        buttonA3.performClick();
                                    }


                                    // txtAppraisePrice.setText(localeFormattedNumber);
                                    //   textViewAppraiseValueThumb.setText(localeFormattedNumber);

                                    progressBarAppraisal.setVisibility(View.INVISIBLE);
                                    progressBarAppraisalHeader.setVisibility(View.INVISIBLE);
                                }
                            });
                        }*/


                        }
                    }
                    catch (Exception e) {
                        Toast.makeText(ShowImageActivity.this, "1.Error", Toast.LENGTH_SHORT).show();
                        Log.i("error", e.getMessage());
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
    private void fillImageView(String savedImageUrl) {
        try {
            /*
            Uri tempUri = FileProvider.getUriForFile(this, "com.example.karmali.homexperts.fileprovider", new File(savedImageUrl));
            Bitmap imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), tempUri);
            imageView.setImageBitmap(Bitmap.createScaledBitmap(imageBitmap, imageView.getWidth(), imageView.getHeight(), false));
            */
            RequestOptions requestOptions = new RequestOptions();
            requestOptions.centerCrop();
            Glide.with(this).load(savedImageUrl).apply(requestOptions).into(imageView);
        }
        catch (Exception ex)
        {
            Toast.makeText(this, "Error: "+ex.toString(), Toast.LENGTH_SHORT).show();
        }
    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void getAddress(){
        final AddressGlobal addressGlobal = (AddressGlobal)getApplicationContext();
        userAddress=addressGlobal.getAddress();
        userAddressLatLng=addressGlobal.getAddresslatLng();
    }
   /* @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:
                finish();
        }
        return super.onOptionsItemSelected(item);
    }*/
}
