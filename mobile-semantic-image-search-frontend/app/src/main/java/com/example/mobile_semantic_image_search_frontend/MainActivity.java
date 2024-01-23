package com.example.mobile_semantic_image_search_frontend;

import static com.example.mobile_semantic_image_search_frontend.CameraUtil.REQUEST_IMAGE_CAPTURE;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

import java.io.File;
import java.util.ArrayList;
import java.util.List;



public class MainActivity extends AppCompatActivity
        implements HttpTextTask.TextQueryTaskListener, HttpImageTask.ImageQueryTaskListener{
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 122;
    private static final int READ_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE = 124;
    private static final int WRITE_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE = 125;
    private HttpImageTask httpImageTask = new HttpImageTask(this, this);
    private HttpTextTask httpTextTask = new HttpTextTask(this, this);
    private EditText editText;
    private ImageButton sendButton;
    private ImageButton cameraButton;
    private RecyclerView imageRegion;
    private Button uploadButton;
    private ProgressBar progressBar;
    private RelativeLayout progressRelativeLayout;

    private ImageAdapter imageAdapter;

    private FirebaseAuth mAuth;
    static InputMethodManager imm;

    private BroadcastReceiver progressReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int progress = intent.getIntExtra("progress", 0);
            progressBar.setProgress(progress);
        }
    };

    private BroadcastReceiver serviceDoneReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Make the ProgressBar invisible
            progressBar.setVisibility(View.INVISIBLE);
            progressRelativeLayout.setVisibility(View.INVISIBLE);
        }
    };

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
        //Check write external storage permission and request if not granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE);
        }

        mAuth = FirebaseAuth.getInstance();

//        Log.e("USER", mAuth.getCurrentUser().getUid());

        TextInputLayout textInputLayout = findViewById(R.id.textInputLayout);


        editText = findViewById(R.id.editText);
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                // Adjust maxLines based on focus state
                editText.setMaxLines(hasFocus ? Integer.MAX_VALUE : 1);
            }
        });


        sendButton = findViewById(R.id.sendButton);
        cameraButton = findViewById(R.id.cameraButton);
        imageRegion = findViewById(R.id.imageRegion);
        imageAdapter = new ImageAdapter(this, new ArrayList<>());

        setOnClickListenerSendButton(editText, sendButton);
        setOnClickListenerCameraButton(context, editText, cameraButton);
        setupImageRegion(imageRegion);

        uploadButton = findViewById(R.id.upLoadButton);
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                startActivity(intent);
                startBackgroundService();
            }
        });

        // Initialize your ProgressBar
        progressBar = findViewById(R.id.progressBar);
        progressRelativeLayout = findViewById(R.id.progressBarRelativeLayout);

        // Register the receiver
        IntentFilter filter = new IntentFilter("your.package.name.ACTION_UPDATE_PROGRESS");
        registerReceiver(progressReceiver, filter, Context.RECEIVER_NOT_EXPORTED);

        // Register the receiver
        IntentFilter filterService = new IntentFilter("your.package.name.ACTION_SERVICE_DONE");
        registerReceiver(serviceDoneReceiver, filterService, Context.RECEIVER_NOT_EXPORTED);
    }



    private void setupImageRegion(RecyclerView imageRegion){
        imageRegion.setAdapter(imageAdapter);
        imageRegion.setLayoutManager(new GridLayoutManager(this, 3));
    }

    private static void setOnClickListenerCameraButton(Activity context, EditText editText, ImageButton cameraButton) {
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editText.clearFocus();
                imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                CameraUtil.dispatchTakePictureIntent(context);
            }
        });
    }

    private void setOnClickListenerSendButton(EditText editText, ImageButton sendButton) {
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String textQuery = editText.getText().toString();
                editText.clearFocus();
                imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);

                Toast.makeText(getApplicationContext(), "The query is sent, please wait.", Toast.LENGTH_SHORT).show();
                // Handle text query submission here
                new Handler(getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        httpTextTask.sendTextData(mAuth.getCurrentUser().getUid(), textQuery);
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
                Toast.makeText(getApplicationContext(), "The image query is sent, please wait.", Toast.LENGTH_SHORT).show();
                new Handler(getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        httpImageTask.uploadImage(mAuth.getCurrentUser().getUid(), photoFile);
                    }
                });
            }
        }
    }

    @Override
    public void onTextQueryResponseReceived(List<String> imageUriList) {

        // đoạn này dùng để test
        List<String> imageUriListTest = new ArrayList<>();
        imageUriListTest.add("/storage/emulated/0/DCIM/Screenshots/Screenshot_20240122_115710_Zalo.jpg");
        imageUriListTest.add("/storage/emulated/0/DCIM/Screenshots/Screenshot_20240122_115555_Zalo.jpg");
//        imageUriListTest.add("/storage/emulated/0/DCIM/Facebook/FB_IMG_170576001933.jpg");

        // for (String uri : imageUriListTest){
        //     Log.d("uri list test", uri);
        // }

        if (imageUriList == null){
            imageAdapter.setImageUriList(new ArrayList<>());
            imageAdapter.notifyDataSetChanged();
            Toast.makeText(this, "No images found.", Toast.LENGTH_SHORT).show();
        }
        else{
            for (String uri : imageUriList)
                Log.d("uri list", uri);
            imageAdapter.setImageUriList(imageUriList);
            imageAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onImageQueryResponseReceived(List<String> imageUriList) {
        if (imageUriList == null){
            imageAdapter.setImageUriList(new ArrayList<>());
            imageAdapter.notifyDataSetChanged();
            Toast.makeText(this, "No images found.", Toast.LENGTH_SHORT).show();
        }
        else{
            for (String uri : imageUriList)
                Log.d("uri list", uri);
            imageAdapter.setImageUriList(imageUriList);
            imageAdapter.notifyDataSetChanged();
        }
    }

    private void startBackgroundService() {
        Intent serviceIntent = new Intent(this, BackgroundService.class);
        progressBar.setVisibility(View.VISIBLE);
        progressRelativeLayout.setVisibility(View.VISIBLE);
        startService(serviceIntent);

//        progressBar = findViewById(R.id.progressBar);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(progressReceiver);
        unregisterReceiver(serviceDoneReceiver);
    }
}