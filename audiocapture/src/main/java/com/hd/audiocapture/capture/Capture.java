package com.hd.audiocapture.capture;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.hd.audiocapture.CaptureConfig;
import com.hd.audiocapture.CaptureType;
import com.hd.audiocapture.callback.CaptureCallback;
import com.hd.audiocapture.player.Player;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by hd on 2018/5/9 .
 */
public abstract class Capture {

    abstract void startRecord();

    abstract void stopRecord();

    abstract void release();

    private ExecutorService mExecutorService = Executors.newFixedThreadPool(2);

    private File file;

    CaptureConfig captureConfig;

    String mode = CaptureType.AAC_FORMAT;

    CaptureCallback callback;

    File createAudioFile() {
        try {
            file = captureConfig.getFile();
            if (file == null) {
                String name = captureConfig.getName();
                name = TextUtils.isEmpty(name) ? String.valueOf(System.currentTimeMillis()) : name;
                String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/recorderDemo/" + name + getFilePostfixName();
                file = new File(path);
            }
            boolean su = true;
            if (!file.getParentFile().exists()) {
                su = file.getParentFile().mkdirs();
            }
            if (file.exists()) {
                su = file.delete();
                Log.d("tag", "file is exists :" + file + "==" + su);
            }
            if (su && file.createNewFile()) {
                if (callback != null)
                    callback.capturePath(file);
                Log.d("tag", "create audio file success :" + file);
                captureConfig.setFile(file);
                return file;
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("tag", "create audio file error :" + e);
        }
        Log.d("tag", "create audio file failed ");
        return null;
    }

    public void setCaptureConfig(CaptureConfig captureConfig) {
        this.captureConfig = captureConfig;
        mode = captureConfig.getMode();
        callback = captureConfig.getCaptureCallback();
    }

    public void startCapture() {
        startCapture(-1);
    }

    public void startCapture(long duration) {
        stopCapture();
        mExecutorService.submit(this::startRecord);
        if (duration > 0)
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    stopCapture();
                }
            }, duration);
    }

    public void stopCapture() {
        captureConfig.setFile(null);
        mExecutorService.submit(() -> {
            stopRecord();
            release();
        });
    }

    public void play(Context context, File file) {
        new Player(context, file).asyncPlay();
    }

    public void play(Context context) {
        play(context, file);
    }

    private String getFilePostfixName() {
        String postfix = ".aac";
        switch (mode) {
            case CaptureType.WAV_FORMAT:
                postfix = ".wav";
                break;
            case CaptureType.MP4_FORMAT:
                postfix = ".mp4";
                break;
        }
        return postfix;
    }
}
