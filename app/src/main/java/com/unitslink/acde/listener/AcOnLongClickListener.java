package com.unitslink.acde.listener;

import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.ethanco.lib.PasswordDialog;
import com.ethanco.lib.abs.OnPositiveButtonListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.unitslink.acde.PanelActivity;
import com.unitslink.acde.R;
import com.unitslink.acde.global.Constants;
import com.unitslink.acde.service.AcService;
import com.unitslink.acde.service.AcServiceBuilder;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.unitslink.acde.global.Constants.isAdminMode;

/**
 * Created by gugia on 2018/5/26.
 */

public class AcOnLongClickListener implements View.OnLongClickListener {

    Context context;
    AcService acService;
    TextView textView;

    public AcOnLongClickListener(Context context) {
        this.context = context;
        acService = AcServiceBuilder.getAcService();
    }

    /**
     * Called when a view has been clicked and held.
     *
     * @param view The view that was clicked and held.
     * @return true if the callback consumed the long click, false otherwise.
     */
    @Override
    public boolean onLongClick(View view) {
        this.textView = (TextView) view;
        if (isAdminMode) {
            Call<String> call = acService.setAdmin(Constants.EXIT_EXCLUSIVE_PASSWORD);
            call.enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    Toast.makeText(context, "Exit exclusive mode.", Toast.LENGTH_SHORT).show();
                    isAdminMode = false;
                    textView.setTextColor(context.getResources().getColor(R.color.colorDarkGray));
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Toast.makeText(context, context.getResources().getString(R.string.fail_on_exit_exclusive),
                            Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            PasswordDialog.Builder builder = new PasswordDialog.Builder(context)
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
                                        Toast.makeText(context, "Enter exclusive mode.", Toast.LENGTH_SHORT).show();
                                        isAdminMode = true;
                                        textView.setTextColor(context.getResources().getColor(R.color.colorPrimary));
                                    } else {
                                        Toast.makeText(context, "Wrong password.", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onFailure(Call<String> call, Throwable t) {
                                    Log.d("ERROR", t.toString());
                                    Toast.makeText(context,
                                            context.getResources().getString(R.string.fail_on_enter_exclusive),
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });
            builder.create().show();
        }
        return true;
    }
}
