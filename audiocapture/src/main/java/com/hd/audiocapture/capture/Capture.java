package com.hd.audiocapture.capture;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.os.Environment;
import android.os.SystemClock;
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

    private File file;

    private Timer timer;

    ExecutorService mExecutorService = Executors.newFixedThreadPool(5);

    CaptureState state = CaptureState.PREPARE;

    AtomicBoolean record = new AtomicBoolean(false);

    CaptureConfig captureConfig;

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
        reportState(CaptureState.FAILED);
        cancelCapture();
    }

    void reportState(CaptureState captureState) {
        if (state != CaptureState.FAILED) {
            state = captureState;
            if (callback != null)
                callback.captureStatus(state);
        }
    }

    public void setCaptureConfig(CaptureConfig captureConfig) {
        this.captureConfig = captureConfig;
        callback = captureConfig.getCaptureCallback();
    }

    public void startCapture() {
        startCapture(-1);
    }

    public void startCapture(long duration) {
        reportState(CaptureState.PREPARE);
        cancelCapture();
        mExecutorService.submit(this::startRecord);
        if (duration > 0) {
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    stopCapture();
                }
            }, duration);
        }
        reportCaptureTime();
    }

    public void pauseCapture() {
        reportState(CaptureState.PAUSE);
    }

    public void resumeCapture() {
        reportState(CaptureState.RESUME);
    }

    public void stopCapture() {
        reportState(CaptureState.COMPLETED);
        cancelCapture();
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
            SystemClock.sleep(50);
        }
        if (timer != null)
            timer.cancel();
        captureConfig.setFile(null);
    }

    private String getFilePostfixName() {
        String postfix;
        switch (captureConfig.getMode()) {
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
                postfix = "." + captureConfig.getMode();
                break;
        }
        return postfix;
    }

    private void reportCaptureTime() {
        callback.captureTime(0L);
        mExecutorService.submit(() -> {
            MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();
            try {
                while (true) {
                    try {
                        SystemClock.sleep(1000);
                        if (state == CaptureState.FAILED || state == CaptureState.COMPLETED) break;
                        if(state == CaptureState.PAUSE)continue;
                        reportTime(metadataRetriever);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } finally {
                reportTime(metadataRetriever);
                metadataRetriever.release();
            }
        });
    }

    private void reportTime(MediaMetadataRetriever metadataRetriever) {
        metadataRetriever.setDataSource(file.getAbsolutePath());
        String strDuration = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        callback.captureTime(Math.round(Math.ceil(Long.valueOf(strDuration)*1.0/1000)));
    }
}
