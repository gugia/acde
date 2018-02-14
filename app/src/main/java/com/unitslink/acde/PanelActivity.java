package com.unitslink.acde;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ethanco.lib.PasswordDialog;
import com.ethanco.lib.abs.OnPositiveButtonListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.timqi.sectorprogressview.ColorfulRingProgressView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import dmax.dialog.SpotsDialog;
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
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class PanelActivity extends AppCompatActivity {

    public static boolean admin = false;
    private final String url = "http://39.104.114.111";
//    public static final String url = "http://13.125.133.35";

    private boolean power = false;
    private boolean powerWill = false;
    private boolean modified = false;

    AcService acService;
    AirConditioning ac = new AirConditioning();

    int mode = 1;
    int temperature = 16;
    int fanspeed = 1;
    float delta = 0F;

    Gson gson = new Gson();

    LocationManager locationManager;

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
        initAcService();
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
        tv_appname.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (admin) {
                    Call<String> call = acService.setAdmin("4321");
                    call.enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response) {
                            Toast.makeText(PanelActivity.this, "Exit exclusive mode.", Toast.LENGTH_SHORT).show();
                            admin = false;
                            tv_appname.setTextColor(getResources().getColor(R.color.colorDarkGray));
                        }

                        @Override
                        public void onFailure(Call<String> call, Throwable t) {
                            Toast.makeText(PanelActivity.this,
                                    "Failed to exit exclusive mode, please check your network.", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    PasswordDialog.Builder builder = new PasswordDialog.Builder(PanelActivity.this)
                            .setTitle("Please input_password")
                            .setBoxCount(4) //设置密码位数
                            .setBorderNotFocusedColor(R.color.colorSecondaryText)
                            .setDotNotFocusedColor(R.color.colorSecondaryText)//密码圆点颜色
                            .setPositiveListener(new OnPositiveButtonListener() {
                                @Override //确定
                                public void onPositiveClick(DialogInterface dialog, int which, String text) {
                                    if (TextUtils.isEmpty(text)) {
                                        return;
                                    }
                                    Call<String> call = acService.setAdmin(text.trim());
                                    call.enqueue(new Callback<String>() {
                                        @Override
                                        public void onResponse(Call<String> call, Response<String> response) {
                                            String result = response.body();
                                            Log.d("TAG", "setAdmin() return ".concat(result));
                                            if ("success".equals(result)) {
                                                Toast.makeText(PanelActivity.this, "Enter exclusive mode.", Toast.LENGTH_SHORT).show();
                                                admin = true;
                                                tv_appname.setTextColor(getResources().getColor(R.color.colorPrimary));
                                            } else {
                                                Toast.makeText(PanelActivity.this, "Wrong password.", Toast.LENGTH_SHORT).show();
                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<String> call, Throwable t) {
                                            Log.d("ERROR", t.toString());
                                            Toast.makeText(PanelActivity.this,
                                                    "Failed to enter exclusive mode, please check your network.", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            });
                    builder.create().show();
                }
                return true;
            }
        });
        csb.setEnabled(false);
        csb.setOnSeekBarChangeListener(new CircularSeekBar.OnCircularSeekBarChangeListener() {
            @Override
            public void onProgressChanged(CircularSeekBar circularSeekBar, float progress, boolean fromUser) {
                if (power) {
                    if (mode != 3) {
                        temperature = (int) progress + 16;
                        tv_temp.setText(String.valueOf(temperature));
                    } else {
                        fanspeed = (int) ((progress + 1) / 3);
                        rb_vent.setRating(fanspeed);
                    }
                } else {
                    rb_vent.setRating(fanspeed);
                }
            }

            @Override
            public void onStopTrackingTouch(CircularSeekBar seekBar) {
                ac.setTemperatureSet(temperature);
                ac.setFanspeed(fanspeed);
                modified = false;
                setAcDataImmediately();
            }

            @Override
            public void onStartTrackingTouch(CircularSeekBar seekBar) {
                modified = true;
            }
        });
    }

    private void initAcService() {
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
        acService = retrofit.create(AcService.class);
    }

    private void initPermission() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //检查权限
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                //请求权限
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
    }

    public void btn_power_onClick(View view) {
        powerWill = !power;
        if (null != view) {
            ac.setPower(powerWill);
            Call<Void> call = acService.setAC(ac);
            call.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    Log.d("TAG", "修改数据成功");
                    modified = false;
                    dialog = new SpotsDialog(PanelActivity.this, powerWill ? R.style.diagStart : R.style.diagStop);
                    dialog.show();
                    Observable.interval(500, TimeUnit.MILLISECONDS)
                            .takeUntil(new Predicate<Long>() {
                                @Override
                                public boolean test(Long aLong) throws Exception {
                                    return aLong == 2;
                                }
                            })
                            .subscribeOn(Schedulers.single())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Observer<Long>() {
                                @Override
                                public void onSubscribe(Disposable d) {

                                }

                                @Override
                                public void onNext(Long aLong) {
                                }

                                @Override
                                public void onError(Throwable e) {

                                }

                                @Override
                                public void onComplete() {
                                    dialog.dismiss();
                                }
                            });
                    Observable.interval(20, TimeUnit.MILLISECONDS)
                            .takeUntil(new Predicate<Long>() {
                                @Override
                                public boolean test(Long aLong) throws Exception {
                                    return aLong > 48;
                                }
                            })
                            .subscribeOn(Schedulers.single())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Observer<Long>() {
                                @Override
                                public void onSubscribe(Disposable d) {
                                    delta = 0;
                                    if (powerWill) {
                                        csb.setProgress(0);
                                    }
                                }

                                @Override
                                public void onNext(Long aLong) {
                                    Log.d("TAG", "aLong=".concat(aLong.toString()));
                                    float progress = csb.getProgress();
                                    float x = 0.5F - delta;
                                    if (x < 0.2F) {
                                        x = 0.2F;
                                    }
                                    if (powerWill) {
                                        if (progress >= 14.5F) {
                                            return;
                                        }
                                        csb.setProgress(progress + x);
                                    } else {
                                        if (progress <= 0.5F) {
                                            return;
                                        }
                                        csb.setProgress(progress - x);
                                    }
                                    delta += 0.01F;
                                }

                                @Override
                                public void onError(Throwable e) {

                                }

                                @Override
                                public void onComplete() {
                                    power = !power;
                                    powerWill = power;
//                        modified = false;
                                    csb.setEnabled(power);
                                    btn_cool.setEnabled(power);
                                    btn_heat.setEnabled(power);
                                    btn_vent.setEnabled(power);
                                    btn_send.setEnabled(power);
                                    //csb.setProgress(0);
                                    if (power) {
                                        tv_temp.setTextColor(getResources().getColor(R.color.colorSky));
                                        tv_tempunit.setTextColor(getResources().getColor(R.color.colorSky));
                                        //onDataSync(ac);
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
                                    }
                                }
                            });
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Log.d("TAG", "修改数据失败");
                    modified = false;
                }
            });
        }
    }

    private void csb_animate(final float n) {
        Log.d("TAG", "n=".concat(String.valueOf(n)));
        final float progress = csb.getProgress();
        Log.d("TAG", "progress=".concat(String.valueOf(progress)));
        Float del = Math.abs((progress - n) / 0.4F);
        final int delta = del.intValue();
        Log.d("TAG", "delta=".concat(String.valueOf(delta)));
        Observable.interval(20, TimeUnit.MILLISECONDS)
                .takeUntil(new Predicate<Long>() {
                    @Override
                    public boolean test(Long aLong) throws Exception {
                        return aLong > delta;
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Long>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Long aLong) {
                        Log.d("TAG", "aLong=".concat(aLong.toString()));
                        if (aLong >= delta) {
                            return;
                        }
                        float i = csb.getProgress();
                        if (progress > n) {
                            if ((i - n) < (progress - n) / 3) {
                                csb.setProgress(i - 0.2F);
                            } else if ((i - n) > (progress - n) * 2 / 3) {
                                csb.setProgress(i - 0.6F);
                            } else {
                                csb.setProgress(i - 0.4F);
                            }
                        } else {
                            if ((n - i) < (n - progress) / 3) {
                                csb.setProgress(i + 0.2F);
                            } else if ((n - i) > (n - progress) * 2 / 3) {
                                csb.setProgress(i + 0.6F);
                            } else {
                                csb.setProgress(i + 0.4F);
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }

    public void btn_cool_onClick(View view) {
//        modified = true;
        mode = 1;
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
            ac.setMode(mode);
            setAcDataImmediately();
        }
    }

    public void btn_heat_onClick(View view) {
//        modified = true;
        mode = 2;
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
            ac.setMode(mode);
            setAcDataImmediately();
        }
    }

    public void btn_vent_onClick(View view) {
//        modified = true;
        mode = 3;
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
            ac.setMode(mode);
            setAcDataImmediately();
        }
    }

    public void btn_send_onClick(View view) {
        crpv.setVisibility(View.VISIBLE);
        crpv.animateIndeterminate();
        setAcData();
        sendLocation();
    }

    private void startDataSync() {
        dialogLoading = new SpotsDialog(PanelActivity.this);
        dialogLoading.show();
        Observable.interval(0, 2, TimeUnit.SECONDS).subscribe(new Observer<Long>() {
            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onNext(Long aLong) {
                Log.d("TAG", "Observable has begin.");
                acService.getAC()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
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
                                Log.e("TAG", e.getMessage());
                            }

                            @Override
                            public void onComplete() {
                                Log.d("TAG", "acService.getAC() onComplete");
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
        if (power != powerWill) {
            return;
        }
        if (!power || modified) {
            return;
        }
        if (power != airConditioning.getPower()) {
            btn_power_onClick(null);
        }
        int temp = airConditioning.getTemperatureSet();
        if (temp > 30) {
            temp = 30;
        }
        if (temp < 16) {
            temp = 16;
        }
        tv_temp.setText(String.valueOf(temp));
        int rating = airConditioning.getFanspeed();
        if (rating > 5) {
            rating = 5;
        }
        if (rating < 1) {
            rating = 1;
        }
        switch (airConditioning.getMode()) {
            case 1:
                btn_cool_onClick(null);
                csb_animate(temp - 16);
                //csb.setProgress(temp - 16);
                break;
            case 2:
                btn_heat_onClick(null);
                csb_animate(temp - 16);
                break;
            case 3:
                btn_vent_onClick(null);
                csb_animate(16 * rating / 5);
                break;
            default:
                btn_cool_onClick(null);
                csb_animate(temp - 16);
        }
        rb_vent.setRating(rating);
    }

    private void setAcData() {
        AirConditioning airConditioning = ac;
        airConditioning.setPower(power);
        airConditioning.setMode(mode);
        airConditioning.setTemperatureSet(temperature);
        airConditioning.setFanspeed(fanspeed);
        airConditioning.setCurrentAlarm(RandomUtils.randomBool());
        airConditioning.setPressureAlarm(RandomUtils.randomBool());
        airConditioning.setVoltAlarm(RandomUtils.randomBool());
        airConditioning.setTemperatureAlarm(RandomUtils.randomBool());
        Call<Void> call = acService.setAC(airConditioning);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                Log.d("TAG", "修改数据成功");
                Toast.makeText(PanelActivity.this, "Data send successfully.", Toast.LENGTH_SHORT).show();
//                modified = false;
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        crpv.stopAnimateIndeterminate();
                        crpv.setVisibility(View.INVISIBLE);
                    }
                }, 1200);
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.d("TAG", "修改数据失败");
                Toast.makeText(PanelActivity.this, "Data sending failed, please check your network.", Toast.LENGTH_SHORT).show();
//                modified = false;
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        crpv.stopAnimateIndeterminate();
                        crpv.setVisibility(View.INVISIBLE);
                    }
                }, 1200);
            }
        });
    }

    private void setAcDataImmediately() {
        modified = true;
        Call<Void> call = acService.setAC(ac);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                Log.d("TAG", "修改数据成功");
                modified = false;
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.d("TAG", "修改数据失败");
                modified = false;
            }
        });
    }

    private Location locate() {
        Location location;
        List<String> prodiverlist = locationManager.getProviders(true);
        String provider = "";
        if (prodiverlist.contains(LocationManager.GPS_PROVIDER)) {
            provider = LocationManager.GPS_PROVIDER;
        } else if (prodiverlist.contains(LocationManager.NETWORK_PROVIDER)) {
            provider = LocationManager.NETWORK_PROVIDER;
        } else {
            Toast.makeText(this, "No available location provider.", Toast.LENGTH_SHORT).show();
        }
        if (!TextUtils.isEmpty(provider)) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                location = null;
            }
            location = locationManager.getLastKnownLocation(provider);
//            CarLocationListener listener = new CarLocationListener(PanelActivity.this, acService);
//            locationManager.requestSingleUpdate(provider, listener, Looper.getMainLooper());
        } else {
            location = null;
        }
        if (null != location) {
            Log.d("TAG", "经度".concat(String.valueOf(location.getLongitude())));
            Log.d("TAG", "纬度".concat(String.valueOf(location.getLatitude())));
        }
        return location;
    }

    private void sendLocation() {
        Location loc = locate();
        if (null != loc) {
            com.unitslink.acde.Location location = new com.unitslink.acde.Location();
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
                    Toast.makeText(PanelActivity.this, "Failed to send location data.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
