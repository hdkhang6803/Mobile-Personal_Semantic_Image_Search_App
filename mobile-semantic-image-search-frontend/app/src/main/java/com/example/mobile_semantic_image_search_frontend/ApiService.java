package com.example.mobile_semantic_image_search_frontend;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;
import okhttp3.RequestBody;

public interface ApiService {
    @Multipart
    @POST("img_query")
    Call<ServerResponse> uploadImage(@Part("userId") RequestBody userIdRequestBody,
                                     @Part("file") RequestBody imageFileRequestBody);
    @POST("txt_query")
    Call<ServerResponse> sendTextData(@Query("userId") String userId, @Body String textData);
}
