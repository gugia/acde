package com.unitslink.acde.listener;

import android.content.Context;
import android.location.*;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.unitslink.acde.service.AcService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by gugia on 2018/2/7.
 */

public class CarLocationListener implements LocationListener {

    AcService acService;
    Context context;

    public CarLocationListener(Context context, AcService acService) {
        this.context = context;
        this.acService = acService;
    }

    @Override
    public void onLocationChanged(Location loc) {
        Log.d("TAG", "onLocationChanged()");
        if (null != loc) {
            Log.d("TAG", "经度2".concat(String.valueOf(loc.getLongitude())));
            Log.d("TAG", "纬度2".concat(String.valueOf(loc.getLatitude())));
            com.unitslink.acde.bean.Location location = new com.unitslink.acde.bean.Location();
            Double[] coordinates = {loc.getLongitude(), loc.getLatitude()};
            location.setCoordinates(coordinates);
            Call<Void> voidCall = acService.setLocation(location);
            voidCall.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    Log.d("TAG", "发送定位成功");
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Log.d("TAG", "发送定位失败");
                    Toast.makeText(context, "Failed to send location data.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
