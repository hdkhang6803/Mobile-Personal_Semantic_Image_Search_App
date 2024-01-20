package com.example.mobile_semantic_image_search_frontend;


import android.content.Context;
import android.util.Log;
import java.io.File;
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

    public HttpImageTask(Context context) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://164.92.122.168:5000/")
                .addConverterFactory(GsonConverterFactory.create())
//                .client(new OkHttpClient.Builder().build())
                .build();

        apiService = retrofit.create(ApiService.class);
    }

    public void uploadImage(File imageFile) {
        // Create MultipartBody.Part from the image file
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), imageFile);
        MultipartBody.Part filePart = MultipartBody.Part.createFormData("file", imageFile.getName(), requestFile);

        // Perform the upload using Retrofit
        Call<ServerResponse> call = apiService.uploadImage(filePart);
        call.enqueue(new Callback<ServerResponse>() {

            @Override
            public void onResponse(Call<ServerResponse> call, retrofit2.Response<ServerResponse> response) {
                if (response.isSuccessful()) {
                    ServerResponse serverResponse = response.body();
                    if (serverResponse != null) {
                        String message = serverResponse.getMessage();
                        Log.d("HTTP Image Response", "Server Response: " + message);
                        // Handle the server response here
                    }
                } else {
                    Log.e("HTTP Image ERROR", "Server Response Code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) {
                Log.e("HTTP Image failure", "Error: " + t.getMessage());
            }
        });
    }
}