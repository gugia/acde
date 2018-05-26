package com.unitslink.acde.service;

import com.unitslink.acde.bean.AirConditioning;
import com.unitslink.acde.bean.Location;

import io.reactivex.Observable;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

/**
 * Created by gugia on 2018/1/28.
 */

public interface AcService {

    @GET("ac/getrand")
    Observable<AirConditioning> getrand();

    @GET("ac/get")
    Observable<AirConditioning> getAC();

    @POST("ac/set")
    Call<Void> setAC(@Body AirConditioning airConditioning);

    @POST("ac/power")
    Observable<String> updateACPower(@Body Boolean power);

    @POST("ac/mode")
    Observable<String> updateACMode(@Body int mode);

    @POST("ac/temperature")
    Observable<String> updateACTemperatureSet(@Body int temperatureSet);

    @POST("ac/fanspeed")
    Observable<String> updateACFanspeed(@Body int fanspeed);

    @POST("location/set")
    Call<Void> setLocation(@Body Location location);

    @POST("admin/set")
    Call<String> setAdmin(@Body String pwd);

    @GET("admin/get")
    Call<String> getAdmin();
}
