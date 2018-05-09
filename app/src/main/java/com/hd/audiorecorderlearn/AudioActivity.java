package com.hd.audiorecorderlearn;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.hd.audiocapture.callback.CaptureCallback;

import java.io.File;


/**
 * Created by hd on 2018/5/8 .
 */
public class AudioActivity extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener, CaptureCallback {
    private TextView tvAudioFilePath;
    private File file;
    private AudioPresenter audioPresenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio);
        init();
    }

    private void init() {
        tvAudioFilePath = findViewById(R.id.tvAudioFilePath);
        ((RadioGroup) findViewById(R.id.rgAudio)).setOnCheckedChangeListener(this);
        audioPresenter = new AudioPresenter(this, this);
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        RadioButton radioButton = group.findViewById(checkedId);
        if (radioButton != null && radioButton.isChecked()) {
            int style = AudioPresenter.MEDIARECORDER_STYLE;
            switch (checkedId) {
                case R.id.rbMediaRecorder:
                    style = AudioPresenter.MEDIARECORDER_STYLE;
                    break;
                case R.id.rbAudioRecord:
                    style = AudioPresenter.AUDIORECORD_STYLE;
                    break;
            }
            audioPresenter.initStyle(style);
        }
    }

    public void start(View view) {
        audioPresenter.start();
    }

    public void stop(View view) {
        audioPresenter.stop();
    }

    public void play(View view) {
        audioPresenter.play(file);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void capturePath(final File file) {
        this.file = file;
        runOnUiThread(() -> tvAudioFilePath.setText("音频地址==> " + file.getAbsolutePath()));
    }
}
