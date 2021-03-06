package com.woaiqw.simpledemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.credic.common.utils.WeakHandler;
import com.woaiqw.scm_api.SCM;
import com.woaiqw.scm_api.utils.Constants;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    private WeakHandler h = new WeakHandler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            String s = (String) msg.obj;
            Toast.makeText(MainActivity.this, s, Toast.LENGTH_SHORT).show();
            tvLoadConfig.setText(s);
            return false;
        }
    });

    private Runnable entryHomeActivityTask = () -> {
        try {
            SCM.get().req(MainActivity.this, "HomeEntry","你好，我是Main", (b, data, tag) -> {
                if (b)
                    Toast.makeText(MainActivity.this, data, Toast.LENGTH_SHORT).show();
            });
        } catch (Exception e) {
            Log.e(Constants.SCM, e.getMessage());
        }
    };
    private TextView tvLoadConfig, tvJumpPage;

    BroadcastReceiver b = new BroadcastReceiver() {
        @Override
        public void onReceive(Context arg0, Intent intent) {
            String action = intent.getAction();
            if ("finish".equals(action)) {
                finish();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvLoadConfig = findViewById(R.id.tv_load_config);
        tvJumpPage = findViewById(R.id.tv_jump_page);
        tvLoadConfig.setOnClickListener(this);
        tvJumpPage.setOnClickListener(this);
        registerReceiver(b, new IntentFilter("finish"));
    }

    @Override
    protected void onResume() {
        final long time = SystemClock.uptimeMillis();
        super.onResume();
        Looper.myQueue().addIdleHandler(() -> {
            // on Measure() -> onDraw() 耗时
            Log.i(MainActivity.this.getClass().getSimpleName(), "onCreate -> idle : " + (SystemClock.uptimeMillis() - time));
            return false;
        });
    }


    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.tv_load_config) {
            //app module 使用用 home module 获取配置
            loadConfigByHomeModule();
        } else if (v.getId() == R.id.tv_jump_page) {
            //3s 后进入 home 进程的 HomeActivity
            h.postDelayed(entryHomeActivityTask, 3000);
        }


    }

    private void loadConfigByHomeModule() {
        try {
            SCM.get().req(MainActivity.this, "LoadConfig", (b, data, tag) -> {
                if (b) {
                    Message obtain = Message.obtain();
                    obtain.obj = data;
                    if (h != null) {
                        h.sendMessage(obtain);
                    } else {
                        Toast.makeText(MainActivity.this, "WeakHandler has been Gc", Toast.LENGTH_SHORT).show();
                    }

                }
            });
        } catch (Exception e) {
            Log.e(Constants.SCM, e.getMessage());
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        h.removeCallbacksAndMessages(null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(b);
    }


}
