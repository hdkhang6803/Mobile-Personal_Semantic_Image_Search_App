package com.example.mobile_semantic_image_search_frontend;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.SocketTimeoutException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
public class HttpImageTask {

    private final ApiService apiService;
    private final Context context;
    private final ImageQueryTaskListener imageQueryTaskListener;

    public HttpImageTask(Context context, ImageQueryTaskListener imageQueryTaskListener) {
        this.context = context;
        this.imageQueryTaskListener = imageQueryTaskListener;

        Gson gson = new GsonBuilder().setLenient().create();
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS) // Connection timeout
                .readTimeout(10, TimeUnit.SECONDS)    // Read timeout
                .writeTimeout(10, TimeUnit.SECONDS)   // Write timeout
                .build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://164.92.122.168:5000/")
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        apiService = retrofit.create(ApiService.class);
    }

    public void uploadImage(String userId, File imageFile) {

        // Create request body for userId
        RequestBody userIdRequestBody = RequestBody.create(MediaType.parse("text/plain"), userId);

        // Create request body for imageFile and MultipartBody for sending the whole file
        RequestBody imageFileRequestBody = RequestBody.create(MediaType.parse("image/*"), imageFile);
        MultipartBody.Part imageFilePart = MultipartBody.Part.createFormData("file", imageFile.getName(), imageFileRequestBody);

        // Create a call and call the upload image method
        Call<ServerResponse> call = apiService.uploadImage(userIdRequestBody, imageFilePart);

        call.enqueue(new Callback<ServerResponse>() {

            @Override
            public void onResponse(Call<ServerResponse> call, retrofit2.Response<ServerResponse> response) {
                try {
                    if (response.isSuccessful()) {
                        ServerResponse serverResponse = response.body();
                        if (serverResponse != null) {
                            String status = serverResponse.getStatus();
                            Log.e("HTTP Image Query Response", "Server Response: " + status);
                            List<String> imageUriList = serverResponse.getImageUris();
                            notifyImageQueryResponseReceived(imageUriList);
                        }
                    } else {
                        changeButtonsVisibility();
                        Log.e("HTTP Image Query Server error", "Server Response Code: " + response.code());
                    }
                } finally {
                    // Close the response body to release resources
                    if (response.errorBody() != null) {
                        response.errorBody().close();
                    }
                }
            }

            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) {
                changeButtonsVisibility();
                showTimeoutToast();
                if (t instanceof SocketTimeoutException) {
                    showTimeoutToast();
                } else {
                    Log.e("HTTP Image failure", "Error: " + t.getMessage());
                }
            }

            private void changeButtonsVisibility(){
                ImageButton sendButton = ((MainActivity) context).findViewById(R.id.sendButton);
                ImageButton cameraButton = ((MainActivity) context).findViewById(R.id.cameraButton);
                ProgressBar progressBarQuery = ((MainActivity) context).findViewById(R.id.progressBarQuery);
                sendButton.setVisibility(View.VISIBLE);
                cameraButton.setVisibility(View.VISIBLE);
                progressBarQuery.setVisibility(View.GONE);
            }

            private void showTimeoutToast() {
                Toast.makeText(context, "Request timed out. Please check your Internet connection.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private static String getBase64String(@NonNull File imageFile) {

        // Convert image file to Bitmap
        Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());

        // Convert Bitmap to byte array
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();

        // Convert the byte array to Base64
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    public interface ImageQueryTaskListener {
        void onImageQueryResponseReceived(List<String> imageUriList);
    }

    private void notifyImageQueryResponseReceived(List<String> imageUriList) {
        if (imageQueryTaskListener != null) {
            imageQueryTaskListener.onImageQueryResponseReceived(imageUriList);
        }
    }
}
