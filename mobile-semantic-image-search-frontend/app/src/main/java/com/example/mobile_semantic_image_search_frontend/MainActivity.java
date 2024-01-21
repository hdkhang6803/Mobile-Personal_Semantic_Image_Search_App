package com.example.mobile_semantic_image_search_frontend;

import static com.example.mobile_semantic_image_search_frontend.CameraUtil.REQUEST_IMAGE_CAPTURE;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

import java.io.File;

import kotlinx.coroutines.CoroutineScope;
import kotlinx.coroutines.Dispatchers;



public class MainActivity extends AppCompatActivity {
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 124;
    private HttpImageTask httpImageTask = new HttpImageTask(this);
    private HttpTextTask httpTextTask = new HttpTextTask(this);
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
        }
        mAuth = FirebaseAuth.getInstance();

//        Log.e("USER", mAuth.getCurrentUser().getUid());

        TextInputLayout textInputLayout = findViewById(R.id.textInputLayout);
        EditText editText = findViewById(R.id.editText);
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                // Adjust maxLines based on focus state
                editText.setMaxLines(hasFocus ? Integer.MAX_VALUE : 1);
            }
        });


        ImageButton sendButton = findViewById(R.id.sendButton);
        ImageButton cameraButton = findViewById(R.id.cameraButton);

        Activity context = this;

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String textQuery = editText.getText().toString();
                editText.clearFocus();
                imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);

                Toast.makeText(getApplicationContext(), "The query is sent, please wait", Toast.LENGTH_SHORT).show();
                // Handle text query submission here
                new Handler(getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        httpTextTask.sendTextData(textQuery);
                    }
                });
            }
        });

        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editText.clearFocus();
                imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                CameraUtil.dispatchTakePictureIntent(context);
            }
        });

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        File photoFile = CameraUtil.getPhotoFile();

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            if (photoFile != null) {
                Toast.makeText(getApplicationContext(), "The image query is sent, please wait", Toast.LENGTH_SHORT).show();
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