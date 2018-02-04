package com.unitslink.acde;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.kyleduo.switchbutton.SwitchButton;
import com.timqi.sectorprogressview.ColorfulRingProgressView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.Observer;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
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

    private final String url = "http://39.104.114.111";
//    public static final String url = "http://192.168.200.112";

    private boolean power = false;
    private boolean powerWill = false;
    private boolean modified = false;

    AcService acService;
    AirConditioning ac = new AirConditioning();
    int mode = 1;
    int temperature = 16;
    int fanspeed = 1;

    Gson gson = new Gson();

    LocationManager locationManager;

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
    ColorfulRingProgressView crpv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_panel);
        initUI();
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
        crpv = findViewById(R.id.crpv);
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

            }

            @Override
            public void onStartTrackingTouch(CircularSeekBar seekBar) {
                modified = true;
            }
        });
    }

    private void initAcService() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
        acService = retrofit.create(AcService.class);
    }

    public void btn_power_onClick(View view) {
        powerWill = !power;
        csb.setProgress(0);
        Observable.interval(20, TimeUnit.MILLISECONDS)
                .takeUntil(new Predicate<Long>() {
                    @Override
                    public boolean test(Long aLong) throws Exception {
                        if (!powerWill) {
                            return true;
                        }
                        return aLong > 50;
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
                        Log.d("TAG", "aLong=".concat(aLong.toString()));
                        float progress = csb.getProgress();
                        if (aLong > 25) {
                            csb.setProgress(progress - 0.6F);
                        } else {
                            csb.setProgress(progress + 0.6F);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {
                        power = !power;
                        powerWill = power;
                        modified = false;
                        csb.setEnabled(power);
                        btn_cool.setEnabled(power);
                        btn_heat.setEnabled(power);
                        btn_vent.setEnabled(power);
                        btn_send.setEnabled(power);
                        csb.setProgress(0);
                        if (power) {
                            tv_temp.setTextColor(getResources().getColor(R.color.colorSky));
                            tv_tempunit.setTextColor(getResources().getColor(R.color.colorSky));
                        } else {
                            iv_cool.setImageResource(R.drawable.icon_c1);
                            iv_heat.setImageResource(R.drawable.icon_h1);
                            iv_vent.setImageResource(R.drawable.icon_v1);
                            rb_vent.setRating(0);
                            //csb.setProgress(0);
                            tv_temp.setTextColor(getResources().getColor(R.color.colorLightGray));
                            tv_tempunit.setTextColor(getResources().getColor(R.color.colorLightGray));
                        }
                        onDataSync(ac);
                    }
                });
    }

    private void csb_animate(final float n) {
        Log.d("TAG", "n=".concat(String.valueOf(n)));
        final float progress = csb.getProgress();
        Log.d("TAG", "progress=".concat(String.valueOf(progress)));
        Float del = Math.abs((progress - n) / 0.6F);
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
                        if (aLong > delta) {
                            return;
                        }
                        float i = csb.getProgress();
                        if (progress > n) {
                            csb.setProgress(i - 0.6F);
                        } else {
                            csb.setProgress(i + 0.6F);
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
        modified = true;
        mode = 1;
        btn_cool.setEnabled(false);
        btn_heat.setEnabled(true);
        btn_vent.setEnabled(true);
        iv_cool.setImageResource(R.drawable.icon_c2);
        iv_heat.setImageResource(R.drawable.icon_h1);
        iv_vent.setImageResource(R.drawable.icon_v1);
    }

    public void btn_heat_onClick(View view) {
        modified = true;
        mode = 2;
        btn_cool.setEnabled(true);
        btn_heat.setEnabled(false);
        btn_vent.setEnabled(true);
        iv_cool.setImageResource(R.drawable.icon_c1);
        iv_heat.setImageResource(R.drawable.icon_h2);
        iv_vent.setImageResource(R.drawable.icon_v1);
    }

    public void btn_vent_onClick(View view) {
        modified = true;
        mode = 3;
        btn_cool.setEnabled(true);
        btn_heat.setEnabled(true);
        btn_vent.setEnabled(false);
        iv_cool.setImageResource(R.drawable.icon_c1);
        iv_heat.setImageResource(R.drawable.icon_h1);
        iv_vent.setImageResource(R.drawable.icon_v2);
    }

    public void btn_send_onClick(View view) {
        crpv.setVisibility(View.VISIBLE);
        crpv.animateIndeterminate();
        setData();
    }

    private void startDataSync() {
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
                csb_animate(temp - 15);
                //csb.setProgress(temp - 16);
                break;
            case 2:
                btn_heat_onClick(null);
                csb_animate(temp - 15);
                break;
            case 3:
                btn_vent_onClick(null);
                csb_animate(15 * rating / 5);
                break;
            default:
                btn_cool_onClick(null);
                csb_animate(temp - 15);
        }
        rb_vent.setRating(rating);
    }

    private void setData() {
        AirConditioning airConditioning = ac;
        airConditioning.setMode(mode);
        airConditioning.setTemperatureSet(temperature);
        airConditioning.setFanspeed(fanspeed);
        Call<Void> call = acService.setAC(airConditioning);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                Log.d("TAG", "修改数据成功");
                modified = false;
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
                modified = false;
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        crpv.stopAnimateIndeterminate();
                        crpv.setVisibility(View.INVISIBLE);
                    }
                }, 1200);
            }
        });
    }
}
