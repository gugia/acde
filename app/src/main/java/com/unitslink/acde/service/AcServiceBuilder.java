package com.unitslink.acde.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.unitslink.acde.global.Constants;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by gugia on 2018/5/26.
 */

public class AcServiceBuilder {

    static Gson gson = new GsonBuilder()
            .setLenient()
            .create();

    static Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(Constants.URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build();


    private static AcService acService;

    public static AcService getAcService() {
        if (acService == null) {
            return retrofit.create(AcService.class);
        }
        return acService;
    }
}
