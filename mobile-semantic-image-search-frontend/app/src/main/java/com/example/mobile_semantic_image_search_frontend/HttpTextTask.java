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
//    private final String inputData;
//    private final Context activity;
//
//    public HttpTextTask(@NotNull String inputData, @NotNull Context activity) {
//        this.inputData = inputData;
//        this.activity = activity;
//    }

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
                if (response.isSuccessful()) {
                    ServerResponse serverResponse = response.body();
                    if (serverResponse != null) {
                        String status = serverResponse.getStatus();
                        Log.d("HTTP Text Response", "Server Response: " + status);
                        List<String> imageUriList = serverResponse.getImageUris();
                        notifyTextQueryResponseReceived(imageUriList);
                    }
                } else {
                    Log.e("HTTP Text Server error", "Server Response Code: " + response.code());
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



    public interface TextQueryTaskListener {
        void onTextQueryResponseReceived(List<String> imageUriList);
    }

    private void notifyTextQueryResponseReceived(List<String> imageUriList) {
        if (textQueryTaskListener != null) {
            textQueryTaskListener.onTextQueryResponseReceived(imageUriList);
        }
    }

}