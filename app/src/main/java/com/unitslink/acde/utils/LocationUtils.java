package com.unitslink.acde.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.unitslink.acde.PanelActivity;

import java.util.List;

/**
 * Created by gugia on 2018/1/31.
 */

public class LocationUtils {

    LocationManager locationManager;
    Context context;

    public LocationUtils(Context context, LocationManager locationManager) {
        this.context = context;
        this.locationManager = locationManager;
    }


    public Location getLocation() {
        Location location = null;
        List<String> providers = locationManager.getProviders(true);
        String provider = "";
        if (providers.contains(LocationManager.NETWORK_PROVIDER)) {
            provider = LocationManager.NETWORK_PROVIDER;
        } else if (providers.contains(LocationManager.PASSIVE_PROVIDER)) {
            provider = LocationManager.PASSIVE_PROVIDER;
        } else if (provider.contains(LocationManager.GPS_PROVIDER)) {
            provider = LocationManager.GPS_PROVIDER;
        }
        if (!TextUtils.isEmpty(provider)) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return null;
            }
            location = locationManager.getLastKnownLocation(provider);
            if (null != location) {
                Log.d("LocationUtils", "经度".concat(String.valueOf(location.getLongitude())));
                Log.d("LocationUtils", "纬度".concat(String.valueOf(location.getLatitude())));
            }
        }
        return location;
    }
}
