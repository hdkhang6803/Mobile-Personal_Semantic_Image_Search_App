package com.example.mobile_semantic_image_search_frontend;

import android.content.Context;
import android.util.Log;

import java.util.List;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.HttpException;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
public class HttpTextTask  {

    private final ApiService apiService;
    private final Context context;
    private TextQueryTaskListener textQueryTaskListener;

    public HttpTextTask(Context context, TextQueryTaskListener textQueryTaskListener) {
        this.context = context;
        this.textQueryTaskListener = textQueryTaskListener;
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
                            String status = serverResponse.getStatus();
                            Log.e("HTTP Text Response", "Server Response: " + status);
                            List<String> imageUriList = serverResponse.getImageUris();
                        notifyTextQueryResponseReceived(imageUriList);
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
                if (t instanceof HttpException) {
                    HttpException httpException = (HttpException) t;
                    int statusCode = httpException.code();
                    Log.e("HTTP Failure", "Error Code: " + statusCode);
                } else {
                    Log.e("HTTP Failure", "Error: " + t.getMessage());
                }
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