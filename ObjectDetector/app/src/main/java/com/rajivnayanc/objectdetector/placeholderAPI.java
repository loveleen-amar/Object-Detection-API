package com.rajivnayanc.objectdetector;


import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface placeholderAPI {

    @Multipart
    @POST(".")
    Call<ResponseBody> objectDetect(
            @Part MultipartBody.Part image
            );

}
