package com.mountrich.krushimitra.crop_diseast_detection_api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HuggingFaceClient {

    private static Retrofit retrofit;

    public static Retrofit getClient() {

        if (retrofit == null) {

            retrofit = new Retrofit.Builder()
                    .baseUrl("https://api-inference.huggingface.co/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }

        return retrofit;
    }
}