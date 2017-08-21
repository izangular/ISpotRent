package com.example.karmali.homexperts;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.provider.Settings.Secure;

import org.json.JSONObject;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static java.security.AccessController.getContext;


/**
 * Created by sail on 03.08.2017.
 */

public class FormActivity extends AppCompatActivity {
    RelativeLayout relView;
    Button register;

    EditText firstName;
    EditText lastName;
    EditText email;
    EditText phone;
    String deviceId;
    OkHttpClient okHttpClient;
    MediaType JSON;
    private Request request;
    Dialog dialog;
    private Handler mHandler;

    private String UrlRegister = "https://intservices.iazi.ch/api/apps/v1/register";
    private String Token = "Bearer ew0KICAiYWxnIjogIkhTMjU2IiwNCiAgInR5cCI6ICJKV1QiDQp9.ew0KICAiZW1haWwiOiAiYXBwc2VydmljZUBpYXppLmNoIiwNCiAgImN1c3RvbWVyTmFtZSI6ICJJSVRBcHBTZXJ2aWNlIiwNCiAgInVzZXJJZCI6ICIxOTYwIiwNCiAgImN1c3RvbWVySWQiOiAiMTE2IiwNCiAgImFwcERhdGEiOiB7DQogICAgImFwcFNlcnZpY2UiOiB7DQogICAgICAibW9kZWJhc2UiOiB0cnVlDQogICAgfSwNCiAgICAiYWRkcmVzcyI6IHsNCiAgICAgICJuYnZhbGlkYXRpb25zIjogMTAwMDAwLA0KICAgICAgIm5ibGl2ZXZhbGlkYXRpb25zIjogMTAwMDAwLA0KICAgICAgIm1vZGVnZW9jb2RlIjogdHJ1ZSwNCiAgICAgICJtb2RlaW50ZXJuYWwiOiB0cnVlLA0KICAgICAgIm1vZGViYXNlIjogdHJ1ZSwNCiAgICAgICJzdXBlclVzZXIiOiB0cnVlDQogICAgfSwNCiAgICAibWFjcm8iOiB7DQogICAgICAibW9kZWJhc2UiOiB0cnVlDQogICAgfSwNCiAgICAibWljcm8iOiB7DQogICAgICAibmJ2YWxpZGF0aW9ucyI6IDEwMDAwLA0KICAgICAgIm5ibGl2ZXZhbGlkYXRpb25zIjogMTAwMCwNCiAgICAgICJtb2RlZ2VvY29kZSI6IHRydWUsDQogICAgICAibW9kZWludGVybmFsIjogdHJ1ZSwNCiAgICAgICJtb2RlYmFzZSI6IHRydWUNCiAgICB9LA0KICAgICJtb2RlbHIiOiB7DQogICAgICAiYWdpbmciOiB0cnVlLA0KICAgICAgIm1vZGVpbnRlcm5hbCI6IHRydWUsDQogICAgICAiUkFDSCI6IHsNCiAgICAgICAgIkZDRiI6IHRydWUsDQogICAgICAgICJHSVIiOiB0cnVlDQogICAgICB9LA0KICAgICAgIlBSQ0giOiB7DQogICAgICAgICJSNSI6IHRydWUsDQogICAgICAgICJSNCI6IHRydWUsDQogICAgICAgICJSMyI6IHRydWUsDQogICAgICAgICJSMSI6IHRydWUsDQogICAgICAgICJtaXNzaW5nIjogdHJ1ZQ0KICAgICAgfSwNCiAgICAgICJPUkNIIjogew0KICAgICAgICAiUmVzaWRlbnRpYWwiOiB0cnVlLA0KICAgICAgICAiQ29tbWVyY2lhbCI6IHRydWUsDQogICAgICAgICJtaXNzaW5nIjogdHJ1ZQ0KICAgICAgfSwNCiAgICAgICJPUENIIjogew0KICAgICAgICAiQTMiOiB0cnVlLA0KICAgICAgICAiQTIiOiB0cnVlLA0KICAgICAgICAibWlzc2luZyI6IHRydWUNCiAgICAgIH0sDQogICAgICAiUFBBVCI6IHsNCiAgICAgICAgIkEzIjogdHJ1ZSwNCiAgICAgICAgIkEyIjogdHJ1ZSwNCiAgICAgICAgIm1pc3NpbmciOiB0cnVlDQogICAgICB9LA0KICAgICAgIlBQQ0giOiB7DQogICAgICAgICJBMSI6IHRydWUsDQogICAgICAgICJBMiI6IHRydWUsDQogICAgICAgICJBMyI6IHRydWUsDQogICAgICAgICJtaXNzaW5nIjogdHJ1ZSwNCiAgICAgICAgIkExSGV1cmlzdGljIjogZmFsc2UsDQogICAgICAgICJIUCI6IHsNCiAgICAgICAgICAiSFJfV0NNIjogMSwNCiAgICAgICAgICAiSFJfTFIiOiAtMC4yLA0KICAgICAgICAgICJIUl9MTyI6IC0wLjEsDQogICAgICAgICAgIkhSX0hPIjogMC4yLA0KICAgICAgICAgICJIUl9IUiI6IDAuNCwNCiAgICAgICAgICAiSFJfSFAiOiAiZmxhdCIsDQogICAgICAgICAgIkhSX1JDIjogMjAxNjAzMzEsDQogICAgICAgICAgIkhSX0FWIjogMzUzLA0KICAgICAgICAgICJIUl9PRCI6IDEsDQogICAgICAgICAgIkhSX0NSIjogMC4wNSwNCiAgICAgICAgICAiSFJfQ1QiOiAwLjE5OSwNCiAgICAgICAgICAiSFJfQksiOiAwLA0KICAgICAgICAgICJIUl9ISyI6IDAsDQogICAgICAgICAgIkhSX1RSIjogMy41LA0KICAgICAgICAgICJIUl9UViI6IDYuNQ0KICAgICAgICB9LA0KICAgICAgICAiQTFjYXByYXRlIjogZmFsc2UsDQogICAgICAgICJDUCI6IHsNCiAgICAgICAgICAiQ0FQX1dDTSI6IDEsDQogICAgICAgICAgIkNBUF9UViI6IDEsDQogICAgICAgICAgIkNBUF9UUiI6IDFFLTA1LA0KICAgICAgICAgICJDQVBfUkMiOiAyMDE2MDMzMSwNCiAgICAgICAgICAiQ0FQX09EIjogMiwNCiAgICAgICAgICAiQ0FQX0xSIjogLTAuMiwNCiAgICAgICAgICAiQ0FQX0xPIjogLTAuMSwNCiAgICAgICAgICAiQ0FQX0hSIjogMC40LA0KICAgICAgICAgICJDQVBfSFAiOiAicmVudCIsDQogICAgICAgICAgIkNBUF9ITyI6IDAuMiwNCiAgICAgICAgICAiQ0FQX0hLIjogMCwNCiAgICAgICAgICAiQ0FQX0NUIjogMC4yLA0KICAgICAgICAgICJDQVBfQ1IiOiAwLjA1LA0KICAgICAgICAgICJDQVBfQksiOiAwLA0KICAgICAgICAgICJDQVBfQVYiOiAzNTMNCiAgICAgICAgfQ0KICAgICAgfSwNCiAgICAgICJMUENIIjogew0KICAgICAgICAiQTIiOiB0cnVlLA0KICAgICAgICAibWlzc2luZyI6IHRydWUNCiAgICAgIH0NCiAgICB9DQogIH0sDQogICJpYXQiOiAxNTAyMDk3Mjk1LA0KICAiZXhwIjogMTUwMjE4MzY5NSwNCiAgImF1ZCI6ICJpSmJxWFRqOW9FVE1aWldXN3E2Y1JDeWdOUTRWQzBvVSIsDQogICJpc3MiOiAiaHR0cHM6Ly93d3cuaWF6aS5jaC9pbnQiDQp9.5DGNS74irueVupMpNDQic2jxCyutsDvdwlvJzdFGkrM";

    //Typeface font1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form);

        relView = findViewById(R.id.progressLayout);
        register = findViewById(R.id.register);

        register.setOnClickListener( new View.OnClickListener(){
             @Override
             public void onClick(View view) {

                 firstName = (EditText) findViewById(R.id.firstName);
                 lastName = (EditText) findViewById(R.id.lastName);
                 email = (EditText) findViewById(R.id.email);
                 phone = (EditText) findViewById(R.id.phone);
                 TextInputLayout firstNameinputLayout = (TextInputLayout) findViewById(R.id.firstNameMessage);
                 TextInputLayout lastNameinputLayout = (TextInputLayout) findViewById(R.id.lastNameMessage);
                 TextInputLayout emailinputLayout = (TextInputLayout) findViewById(R.id.emailMessage);
                 TextInputLayout phoneinputLayout = (TextInputLayout) findViewById(R.id.phoneMessage);

                 if( firstName.getText().toString().trim().equals("")){

                     firstNameinputLayout.setError( "First name is required!" );
                 }
                 else if (lastName.getText().toString().trim().equals(""))
                 {
                     lastNameinputLayout.setError("Last name is required!");
                 }
                 else if (email.getText().toString().trim().equals(""))
                 {
                     emailinputLayout.setError("Email  is required!");
                 }
                 else if (phone.getText().toString().trim().equals(""))
                 {
                     phoneinputLayout.setError("Phone number is required!");
                 }
                 else if(!isValidEmail(email.getText().toString()))
                 {
                     emailinputLayout.setError("Invalid Email Id!");
                 }
                 else if(!isValidMobile(phone.getText().toString()))
                 {
                     phoneinputLayout.setError("Invalid Phone Number!");
                 }

                 else{
                     StartLocationActivity();
                 }

             }

        });

    }

    // validating email id
   /* private boolean isValidEmail(String email) {
        String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

        Pattern pattern = Pattern.compile(EMAIL_PATTERN);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }*/

    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
    private boolean isValidMobile(String phone) {
        return android.util.Patterns.PHONE.matcher(phone).matches();
    }

    public void StartLocationActivity()
    {
        okHttpClient = new OkHttpClient();

        if(!isNetworkAvailable())
        {
            Toast.makeText(this, "No network", Toast.LENGTH_SHORT).show();
            return;
        }

        register.setVisibility(View.GONE);
        relView.setVisibility(View.VISIBLE);

        firstName = (EditText) findViewById(R.id.firstName);
        lastName = (EditText) findViewById(R.id.lastName);
        email = (EditText) findViewById(R.id.email);
        phone = (EditText) findViewById(R.id.phone);
        deviceId =  Secure.getString(this.getContentResolver(), Secure.ANDROID_ID); ;// "123XGH67";

        RequestBody formBody = new FormBody.Builder()
                .add("firstName",firstName.getText().toString())
                .add("lastName", lastName.getText().toString())
                .add("email",email.getText().toString())
                .add("phone",phone.getText().toString())
                .add("deviceId",deviceId.toString())
                .build();
        request = new Request.Builder().url(UrlRegister).build();

        request = new Request.Builder()
                .url(UrlRegister)
                .header("Accept","application/json")
                .header("Content-Type","application/x-www-form-urlencoded")
                .post(formBody)
                .build();

       try {

           // Toast.makeText(FormActivity.this, "Before", Toast.LENGTH_SHORT).show();
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
                        if(response.isSuccessful())
                        {
                            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(FormActivity.this);

                                // mark first time has runned.
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putBoolean("firstTime", true);
                            editor.commit();
                            Intent intent = new Intent(FormActivity.this,ShowLocationSettingActivity.class);
                            startActivity(intent);
                            finish();
                        }
                        else
                        {
                            Toast.makeText(FormActivity.this,response.body().toString(),Toast.LENGTH_SHORT).show();

                            register.setVisibility(View.VISIBLE);
                            relView.setVisibility(View.INVISIBLE);
                        }


    /*                    mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    JSONObject jsonResponse = new JSONObject(myResponse);

                                    Toast.makeText(FormActivity.this,jsonResponse.toString(),Toast.LENGTH_SHORT).show();
                                }
                                catch(Exception e)
                                {
                                    Toast.makeText(FormActivity.this, "2. Error", Toast.LENGTH_SHORT).show();
                                    Log.i("error",e.getMessage());
                                }

                            }
                        });*/
                    }
                    catch(Exception e)
                    {
                        Toast.makeText(FormActivity.this, "1.Error", Toast.LENGTH_SHORT).show();
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

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
