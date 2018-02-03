package com.unitslink.acde;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.ResultReceiver;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.jesusm.holocircleseekbar.lib.HoloCircleSeekBar;
import com.kyleduo.switchbutton.SwitchButton;
import com.nex3z.togglebuttongroup.SingleSelectToggleGroup;

import org.reactivestreams.Subscriber;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private final String url = "http://39.104.114.111";
//    public static final String url = "http://192.168.200.112";

    private SwitchButton switchButton;
    AcService acService;
    Gson gson = new Gson();

    boolean flag;
    LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initAcService();
        initLayout();
        initPicker();
        initSwitch();
        initSelector();
        initLocationManager();
        dataSync();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initPermission();
    }

    public void initLocationManager() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
    }

    private Location locate() {
        Location location;
        List<String> prodiverlist = locationManager.getProviders(true);
        String provider = "";
        if (prodiverlist.contains(LocationManager.NETWORK_PROVIDER)) {
            provider = LocationManager.NETWORK_PROVIDER;
        } else if (prodiverlist.contains(LocationManager.GPS_PROVIDER)) {
            provider = LocationManager.GPS_PROVIDER;
        } else {
            Toast.makeText(this, "没有可用的位置提供器", Toast.LENGTH_SHORT).show();
        }
        if (!TextUtils.isEmpty(provider)) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                location = null;
            }
            location = locationManager.getLastKnownLocation(provider);
        } else {
            location = null;
        }
        return location;
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
    }

    private void initAcService() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
        acService = retrofit.create(AcService.class);
    }

    private void dataSync() {
        Observable.interval(0, 1, TimeUnit.SECONDS).subscribe(new Observer<Long>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(Long aLong) {
                Log.d("TAG", "Observable has begin.");
                acService.getAC()
                        .subscribeOn(Schedulers.single())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Observer<AirConditioning>() {
                            @Override
                            public void onSubscribe(Disposable d) {

                            }

                            @Override
                            public void onNext(AirConditioning airConditioning) {
                                refreshAll(airConditioning);
                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.e("TAG", e.getMessage());
                            }

                            @Override
                            public void onComplete() {
                                Log.d("TAG", "acService.getAC() onComplete");
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

    private void refreshAll(AirConditioning airConditioning) {
        SwitchButton switchButton = (SwitchButton) findViewById(R.id.switchbtn);
        switchButton.setChecked(airConditioning.getMode());
        if (switchButton.isChecked()) {
            switchButton.setBackColorRes(R.color.colorCold);
        } else {
            switchButton.setBackColorRes(R.color.colorHot);
        }
        HoloCircleSeekBar holoCircleSeekBar = (HoloCircleSeekBar) findViewById(R.id.picker);
        holoCircleSeekBar.setValue(airConditioning.getTemperature() - 15);
        TextView textView_temperature = findViewById(R.id.textView_temperature);
        textView_temperature.setText(airConditioning.getTemperature().toString());
        SingleSelectToggleGroup singleSelectToggleGroup = findViewById(R.id.group_choices);
        switch (airConditioning.getFanspeed() % 5) {
            case 1:
                singleSelectToggleGroup.check(R.id.fs1);
                break;
            case 2:
                singleSelectToggleGroup.check(R.id.fs2);
                break;
            case 3:
                singleSelectToggleGroup.check(R.id.fs3);
                break;
            case 4:
                singleSelectToggleGroup.check(R.id.fs4);
                break;
            case 5:
                singleSelectToggleGroup.check(R.id.fs5);
                break;
            default:
                singleSelectToggleGroup.check(R.id.fs1);
                break;
        }
    }

    private void initPicker() {
        HoloCircleSeekBar holoCircleSeekBar = (HoloCircleSeekBar) findViewById(R.id.picker);
        holoCircleSeekBar.setOnSeekBarChangeListener(new HoloCircleSeekBar.OnCircleSeekBarChangeListener() {
            @Override
            public void onProgressChanged(HoloCircleSeekBar holoCircleSeekBar, int i, boolean b) {
                TextView textView_temperature = findViewById(R.id.textView_temperature);
                Integer temperature = 15 + holoCircleSeekBar.getValue();
                String temp = temperature.toString();
                textView_temperature.setText(temp);
            }

            @Override
            public void onStartTrackingTouch(HoloCircleSeekBar holoCircleSeekBar) {

            }

            @Override
            public void onStopTrackingTouch(HoloCircleSeekBar holoCircleSeekBar) {
                Integer temperature = 15 + holoCircleSeekBar.getValue();
                Log.d("Tag", temperature.toString());
                freshTemp(temperature);
            }
        });
    }

    private void initSwitch() {
        switchButton = (SwitchButton) findViewById(R.id.switchbtn);
        switchButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Location location = locate();
                if (location != null) {
                    Log.d("TAG", "gps onSuccessLocation location:  lat==" + location.getLatitude() + "     lng==" + location.getLongitude());
                } else {
                    Log.d("TAG", "gps location is null");
                }
                if (b) {
                    switchButton.setBackColorRes(R.color.colorCold);
                } else {
                    switchButton.setBackColorRes(R.color.colorHot);
                }
                freshMode(b);
            }
        });
    }

    private void initSelector() {
        SingleSelectToggleGroup singleSelectToggleGroup = findViewById(R.id.group_choices);
        singleSelectToggleGroup.setOnCheckedChangeListener(new SingleSelectToggleGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SingleSelectToggleGroup group, int checkedId) {
                int value = 0;
                switch (checkedId) {
                    case R.id.fs1:
                        value = 1;
                        break;
                    case R.id.fs2:
                        value = 2;
                        break;
                    case R.id.fs3:
                        value = 3;
                        break;
                    case R.id.fs4:
                        value = 4;
                        break;
                    case R.id.fs5:
                        value = 5;
                        break;
                    default:
                        value = 1;
                        break;
                }
                freshFan(value);
            }
        });
    }

    private void initLayout() {
        LinearLayout layout = findViewById(R.id.layout_alarm);
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                //intent.setClass(getApplicationContext(), AlarmActivity.class);
                intent.setClass(getApplicationContext(), PanelActivity.class);
                startActivity(intent);
            }
        });
    }

    private void freshMode(final Boolean mode) {
        acService.getAC()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<AirConditioning>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(AirConditioning airConditioning) {
                        airConditioning.setMode(mode);
                        Call<Void> ac = acService.setAC(airConditioning);
                        ac.enqueue(new Callback<Void>() {
                            @Override
                            public void onResponse(Call<Void> call, Response<Void> response) {

                            }

                            @Override
                            public void onFailure(Call<Void> call, Throwable t) {
                                Log.d("TAG", "修改数据失败");
                            }
                        });
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("TAG", e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        Log.d("TAG", "acService.getAC() onComplete");
                    }
                });
    }

    private void freshTemp(final Integer temp) {
        acService.getAC()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<AirConditioning>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(AirConditioning airConditioning) {
                        airConditioning.setTemperature(temp);
                        Call<Void> ac = acService.setAC(airConditioning);
                        ac.enqueue(new Callback<Void>() {
                            @Override
                            public void onResponse(Call<Void> call, Response<Void> response) {

                            }

                            @Override
                            public void onFailure(Call<Void> call, Throwable t) {
                                Log.d("TAG", "修改数据失败");
                            }
                        });
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("TAG", e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        Log.d("TAG", "acService.getAC() onComplete");
                    }
                });
    }

    private void freshFan(final Integer fanspeed) {
        acService.getAC()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<AirConditioning>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(AirConditioning airConditioning) {
                        airConditioning.setFanspeed(fanspeed);
                        Call<Void> ac = acService.setAC(airConditioning);
                        ac.enqueue(new Callback<Void>() {
                            @Override
                            public void onResponse(Call<Void> call, Response<Void> response) {

                            }

                            @Override
                            public void onFailure(Call<Void> call, Throwable t) {
                                Log.d("TAG", "修改数据失败");
                            }
                        });
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("TAG", e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        Log.d("TAG", "acService.getAC() onComplete");
                    }
                });
    }
}
