package com.hd.audiocapture.capture;

import android.content.Context;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.hd.audiocapture.CaptureConfig;
import com.hd.audiocapture.CaptureState;
import com.hd.audiocapture.CaptureType;
import com.hd.audiocapture.callback.CaptureCallback;
import com.hd.audiocapture.callback.PlaybackProgressCallback;
import com.hd.audiocapture.player.Player;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by hd on 2018/5/9 .
 */
public abstract class Capture {

    abstract void startRecord();

    abstract void stopRecord();

    abstract void release();

    private final String TAG = Capture.class.getSimpleName();

    private ExecutorService mExecutorService = Executors.newFixedThreadPool(2);

    private File file;

    AtomicBoolean record = new AtomicBoolean(false);

    CaptureConfig captureConfig;

    String mode = CaptureType.AAC_FORMAT;

    CaptureCallback callback;

    File createAudioFile() {
        try {
            file = captureConfig.getFile();
            if (file == null) {
                String name = captureConfig.getName();
                name = TextUtils.isEmpty(name) ? String.valueOf(System.currentTimeMillis()) : name;
                String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/AudioCapture/" + name + getFilePostfixName();
                file = new File(path);
            }
            boolean su = true;
            if (!file.getParentFile().exists()) {
                su = file.getParentFile().mkdirs();
            }
            if (file.exists()) {
                su = file.delete();
                if (captureConfig.allowLog())
                    Log.d(TAG, "file is exists :" + file + "==" + su);
            }
            if (su && file.createNewFile()) {
                if (callback != null)
                    callback.capturePath(file);
                if (captureConfig.allowLog())
                    Log.d(TAG, "create audio file success :" + file);
                captureConfig.setFile(file);
                return file;
            }
        } catch (IOException e) {
            e.printStackTrace();
            if (captureConfig.allowLog())
                Log.d(TAG, "create audio file error :" + e);
        }
        if (captureConfig.allowLog())
            Log.d(TAG, "create audio file failed ");
        return null;
    }

    void notAllowEnterNextStep() {
        if (callback != null)
            callback.captureStatus(CaptureState.FAILED);
        cancelCapture();
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
        if (callback != null)
            callback.captureStatus(CaptureState.PREPARE);
        cancelCapture();
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
        cancelCapture();
        if (callback != null)
            callback.captureStatus(CaptureState.COMPLETED);
    }

    public void play(Context context) {
        play(context, null);
    }

    public void play(Context context, @Nullable PlaybackProgressCallback callback) {
        new Player(context, file).play(callback);
    }

    private void cancelCapture() {
        if (record.get()) {
            record.set(false);
            mExecutorService.submit(() -> {
                stopRecord();
                release();
            });
        }
        captureConfig.setFile(null);
    }

    private String getFilePostfixName() {
        String postfix;
        switch (mode) {
            case CaptureType.AAC_FORMAT:
                postfix = ".aac";
                break;
            case CaptureType.WAV_FORMAT:
                postfix = ".wav";
                break;
            case CaptureType.MP4_FORMAT:
                postfix = ".mp4";
                break;
            default:
                postfix = "." + mode;
                break;
        }
        return postfix;
    }
}
