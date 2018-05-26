package com.unitslink.acde;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.timqi.sectorprogressview.ColorfulRingProgressView;
import com.unitslink.acde.bean.AirConditioning;
import com.unitslink.acde.global.Constants;
import com.unitslink.acde.listener.AcOnCircularSeekBarChangeListener;
import com.unitslink.acde.listener.AcOnLongClickListener;
import com.unitslink.acde.service.AcService;
import com.unitslink.acde.service.AcServiceBuilder;
import com.unitslink.acde.utils.LocationUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import dmax.dialog.SpotsDialog;
import io.nlopez.smartlocation.SmartLocation;
import io.nlopez.smartlocation.rx.ObservableFactory;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;
import me.tankery.lib.circularseekbar.CircularSeekBar;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PanelActivity extends AppCompatActivity {

    AcService acService;
    AirConditioning ac = new AirConditioning();

    float delta = 0F;

    LocationUtils locationUtils;
    Location currentLocation;

    TextView tv_appname;
    RatingBar rb_vent;
    CircularSeekBar csb;
    Button btn_power;
    Button btn_cool;
    Button btn_heat;
    Button btn_vent;
    Button btn_send;
    ImageView iv_cool;
    ImageView iv_heat;
    ImageView iv_vent;
    TextView tv_temp;
    TextView tv_tempunit;
    TextView tv_cooldesc;
    TextView tv_heatdesc;
    TextView tv_ventdesc;
    ColorfulRingProgressView crpv;

    SpotsDialog dialog;
    SpotsDialog dialogLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_panel);
        initUI();
        initPermission();
        startDataSync();
    }

    private void initUI() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        TextView tvDate = findViewById(R.id.tv_date);
        tvDate.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/lgdr.ttf"));
        tvDate.setText(new SimpleDateFormat("yyyy/MM/dd").format(new Date()));
        //tvDate.setTextSize();
        tv_appname = findViewById(R.id.tv_appname);
        rb_vent = findViewById(R.id.rb_vent);
        csb = findViewById(R.id.csb);
        btn_power = findViewById(R.id.btn_power);
        btn_cool = findViewById(R.id.btn_cool);
        btn_heat = findViewById(R.id.btn_heat);
        btn_vent = findViewById(R.id.btn_vent);
        iv_cool = findViewById(R.id.iv_cool);
        iv_heat = findViewById(R.id.iv_heat);
        iv_vent = findViewById(R.id.iv_vent);
        btn_send = findViewById(R.id.btn_send);
        tv_temp = findViewById(R.id.tv_temp);
        tv_tempunit = findViewById(R.id.tv_tempunit);
        tv_cooldesc = findViewById(R.id.tv_cooldesc);
        tv_heatdesc = findViewById(R.id.tv_heatdesc);
        tv_ventdesc = findViewById(R.id.tv_ventdesc);
        crpv = findViewById(R.id.crpv);
        csb.setEnabled(false);
        csb.setOnSeekBarChangeListener(new AcOnCircularSeekBarChangeListener(rb_vent, tv_temp));
        tv_appname.setOnLongClickListener(new AcOnLongClickListener(this));
    }

    private void initPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //检查权限
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                //请求权限
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationUtils = new LocationUtils(this, locationManager);
    }

    public void btn_power_onClick(View view) {
        if (null == view) {
            return; // 如果不是界面人为操作，不作响应
        }
        Constants.canUpdate = false;
        acService.updateACPower(!Constants.isPowerOn)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .timeout(6, TimeUnit.SECONDS)
                .subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        dialog = new SpotsDialog(PanelActivity.this, !Constants.isPowerOn ? R.style.diagStart : R.style.diagStop);
                        dialog.show();
                    }

                    @Override
                    public void onNext(String msg) {
                        Constants.isPowerOn = !Constants.isPowerOn;
                        if (Constants.isPowerOn) {
                            animatePowerOn();
                        } else {
                            animatePowerOff();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("btn_power_onClick", "启停操作失败", e);
                        dialog.dismiss();
                        Constants.canUpdate = true;
                    }

                    @Override
                    public void onComplete() {
                        dialog.dismiss();
                        Constants.canUpdate = true;
                    }
                });
    }

    public void animatePowerOn() {
        Observable.interval(10, TimeUnit.MILLISECONDS)
                .takeUntil(new Predicate<Long>() {
                    @Override
                    public boolean test(Long n) throws Exception {
                        return n > 99;
                    }
                })
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Long>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        delta = 0;
                    }

                    @Override
                    public void onNext(Long n) {
                        float x = 0.2F - delta;
                        if (csb.getProgress() >= 13.8) {
                            return;
                        }
                        csb.setProgress(csb.getProgress() + x);
                        if (n >= 50) {
                            delta += 0.004F;
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onComplete() {
                        onPowerAnimationComplete(true);
                    }
                });
    }

    public void animatePowerOff() {
        Observable.interval(10, TimeUnit.MILLISECONDS)
                .takeUntil(new Predicate<Long>() {
                    @Override
                    public boolean test(Long n) throws Exception {
                        return n > 99;
                    }
                })
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Long>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        delta = 0;
                        csb.setEnabled(false);
                    }

                    @Override
                    public void onNext(Long n) {
                        float x = 0.2F - delta;
                        float progress = csb.getProgress();
                        if (progress <= 0.2) {
                            return;
                        }
                        csb.setProgress(progress - x);
                        if (n >= 50) {
                            delta += 0.004F;
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onComplete() {
                        onPowerAnimationComplete(false);
                    }
                });
    }

    private void onPowerAnimationComplete(boolean power) {
        csb.setEnabled(power);
        btn_cool.setEnabled(power);
        btn_heat.setEnabled(power);
        btn_vent.setEnabled(power);
        btn_send.setEnabled(power);
        if (power) {
            tv_temp.setTextColor(getResources().getColor(R.color.colorSky));
            tv_tempunit.setTextColor(getResources().getColor(R.color.colorSky));
        } else {
            iv_cool.setImageResource(R.drawable.icon_c1);
            iv_heat.setImageResource(R.drawable.icon_h1);
            iv_vent.setImageResource(R.drawable.icon_v1);
            rb_vent.setRating(0);
            csb.setProgress(0);
            tv_temp.setTextColor(getResources().getColor(R.color.colorLightGray));
            tv_tempunit.setTextColor(getResources().getColor(R.color.colorLightGray));
            tv_cooldesc.setTextColor(getResources().getColor(R.color.colorDarkGray));
            tv_heatdesc.setTextColor(getResources().getColor(R.color.colorDarkGray));
            tv_ventdesc.setTextColor(getResources().getColor(R.color.colorDarkGray));
            crpv.stopAnimateIndeterminate();
            crpv.setVisibility(View.INVISIBLE);
        }
    }

    public void btn_cool_onClick(View view) {
        Constants.currentMode = 1;
        btn_cool.setEnabled(false);
        btn_heat.setEnabled(true);
        btn_vent.setEnabled(true);
        tv_cooldesc.setTextColor(getResources().getColor(R.color.colorPrimary));
        tv_heatdesc.setTextColor(getResources().getColor(R.color.colorDarkGray));
        tv_ventdesc.setTextColor(getResources().getColor(R.color.colorDarkGray));
        iv_cool.setImageResource(R.drawable.icon_c2);
        iv_heat.setImageResource(R.drawable.icon_h1);
        iv_vent.setImageResource(R.drawable.icon_v1);
        if (view != null) {
            acService.updateACMode(1)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .timeout(4, TimeUnit.SECONDS)
                    .subscribe(new Observer<String>() {
                        @Override
                        public void onSubscribe(Disposable d) {
                            Constants.canUpdate = false;
                        }

                        @Override
                        public void onNext(String msg) {
                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.e("btn_cool_onClick", "切换制冷模式失败", e);
                            Toast.makeText(PanelActivity.this, "Network error.", Toast.LENGTH_SHORT).show();
                            Constants.canUpdate = true;
                        }

                        @Override
                        public void onComplete() {
                            Constants.canUpdate = true;
                        }
                    });
        }
    }

    public void btn_heat_onClick(View view) {
        Constants.currentMode = 2;
        btn_cool.setEnabled(true);
        btn_heat.setEnabled(false);
        btn_vent.setEnabled(true);
        tv_cooldesc.setTextColor(getResources().getColor(R.color.colorDarkGray));
        tv_heatdesc.setTextColor(getResources().getColor(R.color.colorPrimary));
        tv_ventdesc.setTextColor(getResources().getColor(R.color.colorDarkGray));
        iv_cool.setImageResource(R.drawable.icon_c1);
        iv_heat.setImageResource(R.drawable.icon_h2);
        iv_vent.setImageResource(R.drawable.icon_v1);
        if (view != null) {
            acService.updateACMode(2)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .timeout(4, TimeUnit.SECONDS)
                    .subscribe(new Observer<String>() {
                        @Override
                        public void onSubscribe(Disposable d) {
                            Constants.canUpdate = false;
                        }

                        @Override
                        public void onNext(String msg) {
                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.e("btn_cool_onClick", "切换制热模式失败", e);
                            Toast.makeText(PanelActivity.this, "Network error.", Toast.LENGTH_SHORT).show();
                            Constants.canUpdate = true;
                        }

                        @Override
                        public void onComplete() {
                            Constants.canUpdate = true;
                        }
                    });
        }
    }

    public void btn_vent_onClick(View view) {
        Constants.currentMode = 3;
        btn_cool.setEnabled(true);
        btn_heat.setEnabled(true);
        btn_vent.setEnabled(false);
        tv_cooldesc.setTextColor(getResources().getColor(R.color.colorDarkGray));
        tv_heatdesc.setTextColor(getResources().getColor(R.color.colorDarkGray));
        tv_ventdesc.setTextColor(getResources().getColor(R.color.colorPrimary));
        iv_cool.setImageResource(R.drawable.icon_c1);
        iv_heat.setImageResource(R.drawable.icon_h1);
        iv_vent.setImageResource(R.drawable.icon_v2);
        if (view != null) {
            acService.updateACMode(3)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .timeout(4, TimeUnit.SECONDS)
                    .subscribe(new Observer<String>() {
                        @Override
                        public void onSubscribe(Disposable d) {
                            Constants.canUpdate = false;
                        }

                        @Override
                        public void onNext(String msg) {
                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.e("btn_cool_onClick", "切换通风模式失败", e);
                            Toast.makeText(PanelActivity.this, "Network error.", Toast.LENGTH_SHORT).show();
                            Constants.canUpdate = true;
                        }

                        @Override
                        public void onComplete() {
                            Constants.canUpdate = true;
                        }
                    });
        }
    }

    public void btn_send_onClick(View view) {
        if (view != null) {
            view.setEnabled(false);
        }
        crpv.setVisibility(View.VISIBLE);
        crpv.animateIndeterminate();
        onDataSendBegin();
    }

    private void startDataSync() {
        acService = AcServiceBuilder.getAcService();
        Observable.interval(0, 2, TimeUnit.SECONDS)
                .subscribe(new Observer<Long>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        dialogLoading = new SpotsDialog(PanelActivity.this);
                        dialogLoading.show();
                    }

                    @Override
                    public void onNext(Long n) {
                        acService.getAC()
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .timeout(6, TimeUnit.SECONDS)
                                .subscribe(new Observer<AirConditioning>() {
                                    @Override
                                    public void onSubscribe(Disposable d) {

                                    }

                                    @Override
                                    public void onNext(AirConditioning airConditioning) {
                                        ac = airConditioning;
                                        onDataSync(airConditioning);
                                    }

                                    @Override
                                    public void onError(Throwable e) {
                                        Log.e("startDataSync", e.getMessage());
                                        Toast.makeText(PanelActivity.this,
                                                "Network error.", Toast.LENGTH_SHORT).show();
                                        dialogLoading.dismiss();
                                    }

                                    @Override
                                    public void onComplete() {
                                        dialogLoading.dismiss();
                                    }
                                });
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }

    private void onDataSync(AirConditioning airConditioning) {
        Constants.currentMode = airConditioning.getMode();
        if (Constants.isPowerOn != airConditioning.getPower()) {
            Constants.isPowerOn = airConditioning.getPower();
            if (Constants.isPowerOn) {
                animatePowerOn();
            } else {
                animatePowerOff();
            }
        }
        if (!Constants.isPowerOn || !Constants.canUpdate) {
            return;
        }
        int temp = airConditioning.getTemperatureSet();
        if (temp > 30) {
            temp = 30;
        }
        if (temp < 16) {
            temp = 16;
        }
        tv_temp.setText(String.valueOf(temp));
        int fanspeed = airConditioning.getFanspeed();
        if (fanspeed > 5) {
            fanspeed = 5;
        }
        if (fanspeed < 1) {
            fanspeed = 1;
        }
        rb_vent.setRating(fanspeed);
        switch (airConditioning.getMode()) {
            case 1:
                btn_cool_onClick(null);
                csb_animate(true, temp);
                break;
            case 2:
                btn_heat_onClick(null);
                csb_animate(true, temp);
                break;
            case 3:
                btn_vent_onClick(null);
                csb_animate(false, fanspeed);
                break;
            default:
                break;
        }
    }

    private void csb_animate(final boolean isTemperature, final int num) {
        final float progress = csb.getProgress();
        float distance = 0;
        boolean isNumBigger = false;
        if (isTemperature) {
            int currentTemp = (int) (progress + 16);
            if (currentTemp == num) {
                return;
            } else {
                distance = Math.abs(currentTemp - num);
                isNumBigger = num > currentTemp;
            }
        } else {
            int currentFanspeed = (int) Math.ceil((progress + 1) / 3);
            if (currentFanspeed == num || currentFanspeed > 5) {
                return;
            } else {
                float targetProcess;
                switch (num) {
                    case 1:
                        targetProcess = 0F;
                        break;
                    case 2:
                        targetProcess = 3.5F;
                        break;
                    case 3:
                        targetProcess = 7F;
                        break;
                    case 4:
                        targetProcess = 10.5F;
                        break;
                    case 5:
                        targetProcess = 14F;
                        break;
                    default:
                        targetProcess = 0F;
                        break;
                }
                if (targetProcess == progress) {
                    return;
                }
                distance = targetProcess - progress;
                isNumBigger = distance > 0;
                distance = Math.abs(distance);
            }
        }
        final int alpha = (int) (distance / 0.4F);
        if (alpha == 0) {
            return;
        }
        Log.i("csb_animate", "要移动的距离等于：" + distance);
        final boolean flag = isNumBigger;
        Observable.interval(20, TimeUnit.MILLISECONDS)
                .takeUntil(new Predicate<Long>() {
                    @Override
                    public boolean test(Long n) throws Exception {
                        return n >= alpha;
                    }
                })
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Long>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        Constants.canUpdate = false;
                    }

                    @Override
                    public void onNext(Long n) {
                        float d = 0.4F;
                        if (n < alpha / 3) {
                            d = 0.7F;
                        } else if (n > alpha * 2 / 3) {
                            d = 0.1F;
                        }
                        if (flag) {
                            if (csb.getProgress() >= 13.9) {
                                return;
                            }
                            csb.setProgress(csb.getProgress() + d);
                        } else {
                            if (csb.getProgress() <= 0.1) {
                                return;
                            }
                            csb.setProgress(csb.getProgress() - d);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Constants.canUpdate = true;
                    }

                    @Override
                    public void onComplete() {
                        Constants.canUpdate = true;
                    }
                });
    }

    private void onDataSendBegin() {
//        LocationParams top = (new LocationParams.Builder()).setAccuracy(LocationAccuracy.HIGH).setDistance(0.0F).setInterval(100L).build();
//        Observable<Location> locationObservable = ObservableFactory.from(SmartLocation.with(this).location().config(top).oneFix());
        Observable<Location> observable = ObservableFactory.from(SmartLocation.with(this).location().oneFix());
        observable.timeout(8, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Location>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        currentLocation = null;
                    }

                    @Override
                    public void onNext(Location loc) {
                        currentLocation = loc;
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("onDataSendBegin", "onError: yes", e);
                        Toast.makeText(PanelActivity.this, "Unable to fetch precise location, try in another way.", Toast.LENGTH_SHORT).show();
                        currentLocation = locationUtils.getLocation();
                        if (currentLocation == null) {
                            Log.e("TAG", "无法获取设备定位信息（包括最近一次成功定位）！");
                        }
                        sendLocation();
                    }

                    @Override
                    public void onComplete() {
                        sendLocation();
                    }
                });
    }

    private void onDataSendEnd(boolean isSuccess) {
        if (isSuccess) {
            Toast.makeText(PanelActivity.this, "Data send successfully.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(PanelActivity.this, "Both satellites and network signal is too weak to fetch your location.", Toast.LENGTH_LONG).show();
        }
        crpv.stopAnimateIndeterminate();
        crpv.setVisibility(View.INVISIBLE);
        btn_send.setEnabled(true);
    }

    private void sendLocation() {
        if (currentLocation == null) {
            onDataSendEnd(false);
            return;
        }
        com.unitslink.acde.bean.Location location = new com.unitslink.acde.bean.Location();
        Double[] coordinates = {currentLocation.getLongitude(), currentLocation.getLatitude()};
        location.setCoordinates(coordinates);
        Call<Void> voidCall = acService.setLocation(location);
        voidCall.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                Log.d("sendLocation", "发送定位成功");
                onDataSendEnd(true);
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.d("sendLocation", "发送定位失败");
                onDataSendEnd(false);
            }
        });
    }
}
