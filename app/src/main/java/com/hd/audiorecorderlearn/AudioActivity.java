package com.hd.audiorecorderlearn;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.hd.audiocapture.CaptureState;
import com.hd.audiocapture.callback.CaptureStreamCallback;
import com.hd.audiocapture.callback.PlaybackProgressCallback;

import java.io.File;


/**
 * Created by hd on 2018/5/8 .
 */
public class AudioActivity extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener, CaptureStreamCallback, PlaybackProgressCallback {
    private TextView tvAudioFilePath, tvAudioDuration, tvAudioVolume, tvAudioState, tvAudioProgress;
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
        tvAudioDuration = findViewById(R.id.tvAudioDuration);
        tvAudioVolume = findViewById(R.id.tvAudioVolume);
        tvAudioState = findViewById(R.id.tvAudioState);
        tvAudioProgress = findViewById(R.id.tvAudioProgress);
        ((RadioGroup) findViewById(R.id.rgAudio)).setOnCheckedChangeListener(this);
        audioPresenter = new AudioPresenter(this, this);
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        RadioButton radioButton = group.findViewById(checkedId);
        if (radioButton != null && radioButton.isChecked()) {
            int style = AudioPresenter.MEDIARECORDER_MP4_STYLE;
            switch (checkedId) {
                case R.id.rbMediaRecorderMP4:
                    style = AudioPresenter.MEDIARECORDER_MP4_STYLE;
                    break;
                case R.id.rbMediaRecorderAAC:
                    style = AudioPresenter.MEDIARECORDER_AAC_STYLE;
                    break;
                case R.id.rbAudioRecordAAC:
                    style = AudioPresenter.AUDIORECORD_AAC_STYLE;
                    break;
                case R.id.rbAudioRecordWAV:
                    style = AudioPresenter.AUDIORECORD_WAV_STYLE;
                    break;
            }
            audioPresenter.initStyle(style);
        }
    }

    public void start(View view) {
        audioPresenter.start();
    }

    public void pause(View view) {
        audioPresenter.pause();
    }

    public void resume(View view) {
        audioPresenter.resume();
    }

    public void stop(View view) {
        audioPresenter.stop();
    }

    public void play(View view) {
        audioPresenter.play(file, this);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void progress(long currentDuration, long maxDuration) {
        runOnUiThread(() -> tvAudioProgress.setText("progress==> " + currentDuration + "===" + maxDuration//
                           + "====" + (currentDuration * 1.0 / maxDuration) * 100 + "%"));
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void capturePath(final File file) {
        this.file = file;
        runOnUiThread(() -> tvAudioFilePath.setText("filePath==> " + file.getAbsolutePath()));
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void captureStatus(CaptureState state) {
        runOnUiThread(() -> tvAudioState.setText("captureState==> " + state));
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void captureVolume(double volume) {
        runOnUiThread(() -> tvAudioVolume.setText("audioVolume==> " + volume));
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void captureTime(long duration) {
        runOnUiThread(() -> tvAudioDuration.setText("audioDuration==> " + DateUtils.formatElapsedTime(duration)));
    }

    @Override
    public byte[] filterContentByte(@NonNull byte[] content) {
        return new byte[0];
    }

    @Override
    public void captureContentByte(@NonNull byte[] content) {
        //Log.d("tag", "====" + Arrays.toString(content));
    }
}
