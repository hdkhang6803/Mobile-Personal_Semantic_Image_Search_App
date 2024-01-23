package com.example.mobile_semantic_image_search_frontend;


import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
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
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://164.92.122.168:5000/")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        apiService = retrofit.create(ApiService.class);
    }

    public void uploadImage(String userId, File imageFile) {
//        // Create MultipartBody.Part from the image file
//        RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), imageFile);
//        MultipartBody.Part filePart = MultipartBody.Part.createFormData("file", imageFile.getName(), requestFile);
//
//        // Create RequestBody for userId
//        RequestBody userIdRequestBody = RequestBody.create(MediaType.parse("text/plain"), userId);





//        // pass it like this
//        RequestBody requestFile =
//                RequestBody.create(MediaType.parse("image/%"), imageFile);
//
//        // MultipartBody.Part is used to send also the actual file name
//        MultipartBody.Part body =
//                MultipartBody.Part.createFormData("image", imageFile.getName(), requestFile);
//
//        // add another part within the multipart request
//        RequestBody userIdRequestBody =
//                RequestBody.create(MultipartBody.FORM, userId);
//
//        // Perform the upload using Retrofit
//        Call<ServerResponse> call = apiService.uploadImage(userIdRequestBody, body);


        //creating request body for userId
        RequestBody userIdRequestBody = RequestBody.create(MediaType.parse("text/plain"), userId);
        //creating request body for file
        RequestBody imageFileRequestBody = RequestBody.create(MediaType.parse("image/jpeg"), imageFile);
        //MultipartBody.Part body = MultipartBody.Part.createFormData("file", imageFile.getName(), requestFile);
        Log.e("userId", userId);
        Log.e("imageFile.getPath()", imageFile.getPath());
        //Log.e("requestFile", requestFile.toString());



        //creating a call and calling the upload image method
        Call<ServerResponse> call = apiService.uploadImage(userIdRequestBody, imageFileRequestBody);


        call.enqueue(new Callback<ServerResponse>() {

            @Override
            public void onResponse(Call<ServerResponse> call, retrofit2.Response<ServerResponse> response) {
                try {
                    if (response.isSuccessful()) {
                        ServerResponse serverResponse = response.body();
                        if (serverResponse != null) {

                            // Handle response
                            Log.d("Retrofit", "Response Code: " + response.code());
                            Log.d("Retrofit", "Response Body: " + response.body());

                            String status = serverResponse.getStatus();
                            String error = serverResponse.getError();
                            List<String> imageUriList = serverResponse.getImageUris();

                            if (status == null){
                                Log.e("status", "null");
                            }
                            else Log.e("status", status);

                            if (error == null){
                                Log.e("error", "null");
                            }
                            else Log.e("error", error);

                            if (imageUriList == null){
                                Log.e("uri list", "null list");
                                imageUriList = new ArrayList<>();
                                imageUriList.add(imageFile.getPath());
                            }
                            else if (imageUriList.size() == 0){
                                Log.e("uri list", "empty list");
                            }
                            else for (String uri : imageUriList){
                                Log.e("uri list", uri);
                                }
//                            Log.e("HTTP Image Query Response", "Server Response: " + status);
//                            List<String> imageUriList = serverResponse.getImageUris();
                            notifyImageQueryResponseReceived(imageUriList);
                        }
                    } else {
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
                Log.e("HTTP Image failure", "Error: " + t.getMessage());
            }
        });
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
