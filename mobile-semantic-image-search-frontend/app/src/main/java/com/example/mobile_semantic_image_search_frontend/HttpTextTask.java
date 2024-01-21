package com.example.mobile_semantic_image_search_frontend;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
public class HttpTextTask  {
//    private final String inputData;
//    private final Context activity;
//
//    public HttpTextTask(@NotNull String inputData, @NotNull Context activity) {
//        this.inputData = inputData;
//        this.activity = activity;
//    }

    private final ApiService apiService;

    public HttpTextTask(Context context) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://164.92.122.168:5000/")
                .addConverterFactory(GsonConverterFactory.create())
//                .client(new OkHttpClient.Builder().build())
                .build();

        apiService = retrofit.create(ApiService.class);
    }
    public void sendTextData(String textData) {
        // Create RequestBody for the text data
        RequestBody textRequestBody = RequestBody.create(MediaType.parse("text/plain"), textData);

        // Perform the upload using Retrofit
        Call<ServerResponse> call = apiService.sendTextData(textRequestBody.toString());
        call.enqueue(new Callback<ServerResponse>() {

            @Override
            public void onResponse(Call<ServerResponse> call, retrofit2.Response<ServerResponse> response) {
                try {
                    if (response.isSuccessful()) {
                        ServerResponse serverResponse = response.body();
                        if (serverResponse != null) {
                            String message = serverResponse.getMessage();
                            Log.e("HTTP Text Response", "Server Response: " + message);
                            // Handle the server response here
                        }
                    } else {
                        Log.e("HTTP Text Server error", "Server Response Code: " + response.code());
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
                Log.e("HTTP text failure", "Error: " + t.getMessage());
            }
        });
    }

}