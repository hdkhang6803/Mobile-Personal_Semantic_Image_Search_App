package com.example.mobile_semantic_image_search_frontend;

import static com.example.mobile_semantic_image_search_frontend.CameraUtil.REQUEST_IMAGE_CAPTURE;

import android.Manifest;
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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexboxLayoutManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements HttpTextTask.TextQueryTaskListener{
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 124;
    private static final int READ_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE = 124;
    private HttpImageTask httpImageTask = new HttpImageTask(this);
    private HttpTextTask httpTextTask = new HttpTextTask(this, this);
    private EditText editText;
    private ImageButton sendButton;
    private ImageButton cameraButton;
    private RecyclerView imageRegion;
    private ImageAdapter imageAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Activity context = this;

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, READ_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE);
        }

        editText = findViewById(R.id.editText);
        sendButton = findViewById(R.id.sendButton);
        cameraButton = findViewById(R.id.cameraButton);
        imageRegion = findViewById(R.id.imageRegion);
        imageAdapter = new ImageAdapter(this, new ArrayList<>());

        setOnClickListenerSendButton(editText, sendButton);
        setOnClickListenerCameraButton(context, cameraButton);
        setupImageRegion(imageRegion);
    }

    private void setupImageRegion(RecyclerView imageRegion){
        imageRegion.setAdapter(imageAdapter);
        imageRegion.setLayoutManager(new GridLayoutManager(this, 3));
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

    @Override
    public void onTextQueryResponseReceived(List<String> imageUriList) {

        // đoạn này dùng để test
        List<String> imageUriListTest = new ArrayList<>();
        imageUriListTest.add("/storage/emulated/0/DCIM/Facebook/FB_IMG_1705760019332.jpg");
        imageUriListTest.add("/storage/emulated/0/DCIM/Facebook/FB_IMG_1705754144977.jpg");
        imageUriListTest.add("/storage/emulated/0/DCIM/Facebook/FB_IMG_170576001933.jpg");
        imageUriListTest.add("/storage/emulated/0/DCIM/Facebook/FB_IMG_1705760019332.jpg");
        for (String uri : imageUriListTest){
            Log.d("uri list test", uri);
        }

//        for (String uri : imageUriList){
//            Log.d("uri list", uri);
//        }
        imageAdapter.setImageUriList(imageUriListTest);
        imageAdapter.notifyDataSetChanged();
    }
}