package com.hd.audiorecorderlearn;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.hd.splashscreen.text.SimpleConfig;
import com.hd.splashscreen.text.SimpleSplashFinishCallback;
import com.hd.splashscreen.text.SimpleSplashScreen;

public class SplashActivity extends AppCompatActivity implements SimpleSplashFinishCallback {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
        getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);
        SimpleSplashScreen screen = findViewById(R.id.screen);
        SimpleConfig simpleConfig=new SimpleConfig(this);
        simpleConfig.setCallback(this);
        screen.addConfig(simpleConfig);
        screen.start();
    }

    @Override
    public void loadFinish() {
        startActivity(new Intent(this, AudioActivity.class));
        finish();
    }
}
