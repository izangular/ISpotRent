package com.example.karmali.homexperts;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;

public class ShowImageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_image);

        Bundle bun = getIntent().getExtras();
        String imageUrl = bun.getString("PhotoUrl");
        //set it to imageview
        fillImageView(imageUrl);


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

        ImageView imageViewGoBackToLocation = findViewById(R.id.gotoloacationactivity);
        imageViewGoBackToLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent showImageActivity = new Intent(ShowImageActivity.this, CameraActivity.class);
                startActivity(showImageActivity);
                finish();
            }
        });

    }

    private void fillImageView(String savedImageUrl) {
        try {
            Uri tempUri = FileProvider.getUriForFile(this, "com.example.karmali.homexperts.fileprovider", new File(savedImageUrl));
            //Toast.makeText(this, "Data: " + tempUri.toString(), Toast.LENGTH_SHORT).show();
            Bitmap imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), tempUri);
            ImageView imageView = (ImageView)findViewById(R.id.imageViewCapturedImage);
            imageView.setImageBitmap(Bitmap.createScaledBitmap(imageBitmap, 500, 500, false));
            //imageView.setImageBitmap(imageBitmap);
        }
        catch (Exception ex)
        {
            Toast.makeText(this, "Error: "+ex.toString(), Toast.LENGTH_SHORT).show();
        }
    }
}
