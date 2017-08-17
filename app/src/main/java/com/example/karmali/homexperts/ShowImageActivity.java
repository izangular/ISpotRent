package com.example.karmali.homexperts;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;

public class ShowImageActivity extends AppCompatActivity {

    ImageView imageView;

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
                startActivity(new Intent(ShowImageActivity.this, CameraActivity.class));
            }
        });

        mActionBar.setCustomView(mCustomView);
        mActionBar.setDisplayShowCustomEnabled(true);
        ///////////Action Bar End//////////////
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

        //set it to imageview
        //fillImageView(imageUrl);



        Button buttonConfirmImage = findViewById(R.id.buttonConfirmImage);
        buttonConfirmImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent data = new Intent();
                String text = "OK";
                data.setData(Uri.parse(text));
                setResult(RESULT_OK, data);
                finish();
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

    private void fillImageView(String savedImageUrl) {
        try {
            Uri tempUri = FileProvider.getUriForFile(this, "com.example.karmali.homexperts.fileprovider", new File(savedImageUrl));
            //Toast.makeText(this, "Data: " + tempUri.toString(), Toast.LENGTH_SHORT).show();
            Bitmap imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), tempUri);
            //imageView
            imageView.setImageBitmap(Bitmap.createScaledBitmap(imageBitmap, imageView.getWidth(), imageView.getHeight(), false));
            //imageView.setImageBitmap(imageBitmap);
        }
        catch (Exception ex)
        {
            Toast.makeText(this, "Error: "+ex.toString(), Toast.LENGTH_SHORT).show();
        }
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
