package com.unitslink.acde.global;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.unitslink.acde.service.AcService;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * 全局变量
 * Created by gugia on 2018/5/26.
 */
public class Constants {

    public final static String URL = "http://39.104.114.111";
//    public static final String url = "http://13.125.133.35";

    public final static String EXIT_EXCLUSIVE_PASSWORD = "4321";

    public static boolean isAdminMode = false;

    public static boolean canUpdate = true;

    public static boolean isPowerOn = false;

    public static int currentMode = 1;
}
