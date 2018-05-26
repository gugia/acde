package com.unitslink.acde.listener;

import android.util.Log;
import android.widget.RatingBar;
import android.widget.TextView;

import com.unitslink.acde.global.Constants;
import com.unitslink.acde.service.AcService;
import com.unitslink.acde.service.AcServiceBuilder;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import me.tankery.lib.circularseekbar.CircularSeekBar;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by gugia on 2018/5/26.
 */

public class AcOnCircularSeekBarChangeListener implements CircularSeekBar.OnCircularSeekBarChangeListener {

    RatingBar bar;
    TextView view;

    public AcOnCircularSeekBarChangeListener(RatingBar bar, TextView view) {
        this.bar = bar;
        this.view = view;
    }

    @Override
    public void onProgressChanged(CircularSeekBar circularSeekBar, float progress, boolean fromUser) {
        if (Constants.currentMode == 3) { //通风模式
            double fanSpeed = Math.ceil((progress + 1) / 3); //向上取整
            if (fromUser) { //如果是用户在操作
                bar.setRating((float) fanSpeed);
            }
        } else { //冷暖模式
            int temperature;
            if (progress > 13.5) {
                temperature = 30;
            } else {
                temperature = (int) (progress + 16);
            }
            if (fromUser) { //如果是用户在操作
                view.setText(String.valueOf(temperature));
            }
        }
    }

    @Override
    public void onStopTrackingTouch(CircularSeekBar seekBar) {
        float progress = seekBar.getProgress();
        AcService acService = AcServiceBuilder.getAcService();
        if (Constants.currentMode == 3) { //通风模式
            double fanSpeed = Math.ceil((progress + 1) / 3); //向上取整
            acService.updateACFanspeed((int) fanSpeed)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .timeout(4, TimeUnit.SECONDS)
                    .subscribe(new Observer<String>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onNext(String msg) {

                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.e(this.getClass().getSimpleName(), "更新风扇转速失败");
                            Constants.canUpdate = true;
                        }

                        @Override
                        public void onComplete() {
                            Constants.canUpdate = true;
                        }
                    });
        } else {
            int temperature;
            if (progress > 13.5) {
                temperature = 30;
            } else {
                temperature = (int) (progress + 16);
            }
            acService.updateACTemperatureSet((int) temperature)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .timeout(4, TimeUnit.SECONDS)
                    .subscribe(new Observer<String>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onNext(String msg) {

                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.e(this.getClass().getSimpleName(), "更新温度失败");
                            Constants.canUpdate = true;
                        }

                        @Override
                        public void onComplete() {
                            Constants.canUpdate = true;
                        }
                    });
        }
    }

    @Override
    public void onStartTrackingTouch(CircularSeekBar seekBar) {
        Constants.canUpdate = false; //禁止更新UI
    }
}
