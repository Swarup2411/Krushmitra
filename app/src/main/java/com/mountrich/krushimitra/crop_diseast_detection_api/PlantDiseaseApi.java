package com.mountrich.krushimitra.crop_diseast_detection_api;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface PlantDiseaseApi {

    @Headers({
            "Content-Type: application/octet-stream"
    })
    @POST("models/Daksh159/plant-disease-mobilenetv2")
    Call<Object> detectDisease(
            @Header("Authorization") String token,
            @Body RequestBody image
    );
}