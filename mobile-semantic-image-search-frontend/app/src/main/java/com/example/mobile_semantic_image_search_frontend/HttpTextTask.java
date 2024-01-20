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
                if (response.isSuccessful()) {
                    ServerResponse serverResponse = response.body();
                    if (serverResponse != null) {
                        String message = serverResponse.getMessage();
                        Log.d("HTTP Text Response", "Server Response: " + message);
                        // Handle the server response here
                    }
                } else {
                    Log.e("HTTP Text Server error", "Server Response Code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) {
                Log.e("HTTP text failure", "Error: " + t.getMessage());
            }
        });
    }

//
//    @Nullable
//    @Override
//    protected String doInBackground(Void... params) {
//        try {
//            // Create Retrofit instance
//            Retrofit retrofit = new Retrofit.Builder()
//                    .baseUrl("http://164.92.122.168:5000/")
//                    .addConverterFactory(GsonConverterFactory.create())
//                    .build();
//
//            // Create ApiService using Retrofit
//            ApiService apiService = retrofit.create(ApiService.class);
//
//            // Make the network request using Retrofit
//            Call<ServerResponse> call = apiService.sendTextData(inputData);
//            Response<ServerResponse> response = call.execute();
//
//            if (response.isSuccessful()) {
//                ServerResponse serverResponse = response.body();
//                if (serverResponse != null) {
//                    return serverResponse.getMessage();
//                } else {
//                    return "ERROR: Server response is null";
//                }
//            } else {
//                return "ERROR: Server Response Code - " + response.code();
//            }
//        } catch (IOException e) {
//            Log.e("HTTP", "Error: " + e.getMessage());
//            return "ERROR: " + e.getMessage();
//        }
//    }
//
//    @Override
//    protected void onPostExecute(String result) {
//        super.onPostExecute(result);
//        Log.d("AAAA", "AsyncTask finished: " + result);
//    }
}