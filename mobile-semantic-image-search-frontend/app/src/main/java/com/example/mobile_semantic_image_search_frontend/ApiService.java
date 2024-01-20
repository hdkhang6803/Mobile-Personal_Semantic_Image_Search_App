package com.example.mobile_semantic_image_search_frontend;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ApiService {
    @Multipart
    @POST("txt_query")
    Call<ServerResponse> uploadImage(@Part MultipartBody.Part filePart);

    @POST("txt_query")
    Call<ServerResponse> sendTextData(@Body String data);
}
