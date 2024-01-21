package com.example.mobile_semantic_image_search_frontend;

import static com.example.mobile_semantic_image_search_frontend.CameraUtil.REQUEST_IMAGE_CAPTURE;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexboxLayoutManager;

import java.io.File;

import kotlinx.coroutines.CoroutineScope;
import kotlinx.coroutines.Dispatchers;



public class MainActivity extends AppCompatActivity {
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 124;
    private HttpImageTask httpImageTask = new HttpImageTask(this);
    private HttpTextTask httpTextTask = new HttpTextTask(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Activity context = this;

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
        }

        EditText editText = findViewById(R.id.editText);
        ImageButton sendButton = findViewById(R.id.sendButton);
        ImageButton cameraButton = findViewById(R.id.cameraButton);
        RecyclerView imageRegion = findViewById(R.id.imageRegion);

        setOnClickListenerSendButton(editText, sendButton);
        setOnClickListenerCameraButton(context, cameraButton);
        setupImageRegion(imageRegion);
    }

    private void setupImageRegion(RecyclerView imageRegion){
        imageRegion.setLayoutManager(new FlexboxLayoutManager(this, FlexDirection.ROW));
    }

    private static void setOnClickListenerCameraButton(Activity context, ImageButton cameraButton) {
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CameraUtil.dispatchTakePictureIntent(context);
            }
        });
    }

    private void setOnClickListenerSendButton(EditText editText, ImageButton sendButton) {
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String textQuery = editText.getText().toString();
                // Handle text query submission here
                new Handler(getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        httpTextTask.sendTextData(textQuery);
                    }
                });
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        File photoFile = CameraUtil.getPhotoFile();

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            if (photoFile != null) {
                new Handler(getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        httpImageTask.uploadImage(photoFile);
                    }
                });
            }
        }
    }

}