package com.example.mobile_semantic_image_search_frontend;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.HttpException;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
public class HttpTextTask {

    private final ApiService apiService;
    private final Context context;
    private final TextQueryTaskListener textQueryTaskListener;

    public HttpTextTask(Context context, TextQueryTaskListener textQueryTaskListener) {
        this.context = context;
        this.textQueryTaskListener = textQueryTaskListener;

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS) // Connection timeout
                .readTimeout(10, TimeUnit.SECONDS)    // Read timeout
                .writeTimeout(10, TimeUnit.SECONDS)   // Write timeout
                .build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://164.92.122.168:5000/")
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiService.class);
    }

    public void sendTextData(String userId, String textData) {
        // Perform the upload using Retrofit
        Call<ServerResponse> call = apiService.sendTextData(userId, textData);
        call.enqueue(new Callback<ServerResponse>() {

            @Override
            public void onResponse(Call<ServerResponse> call, retrofit2.Response<ServerResponse> response) {
                try {
                    if (response.isSuccessful()) {
                        ServerResponse serverResponse = response.body();
                        if (serverResponse != null) {
                            String status = serverResponse.getStatus();
                            Log.e("HTTP Text Query Response", "Server Response: " + status);
                            List<String> imageUriList = serverResponse.getImageUris();
                            notifyTextQueryResponseReceived(imageUriList);
                        }
                    } else {
                        changeButtonsVisibility();
                        Log.e("HTTP Text Query Server error", "Server Response Code: " + response.code());
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
                } else if (t instanceof HttpException) {
                    HttpException httpException = (HttpException) t;
                    int statusCode = httpException.code();
                    Log.e("HTTP Failure", "Error Code: " + statusCode);
                } else {
                    Log.e("HTTP Failure", "Error: " + t.getMessage());
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

    public interface TextQueryTaskListener {
        void onTextQueryResponseReceived(List<String> imageUriList);
    }

    private void notifyTextQueryResponseReceived(List<String> imageUriList) {
        if (textQueryTaskListener != null) {
            textQueryTaskListener.onTextQueryResponseReceived(imageUriList);
        }
    }
}
