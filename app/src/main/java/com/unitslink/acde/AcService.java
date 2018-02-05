package com.unitslink.acde;

import io.reactivex.Observable;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
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

    @POST("location/set")
    Call<Void> setLocation(@Body Location location);
}
