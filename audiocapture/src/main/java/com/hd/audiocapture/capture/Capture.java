package com.hd.audiocapture.capture;

import android.content.Context;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;

import com.hd.audiocapture.Player;
import com.hd.audiocapture.callback.CaptureCallback;

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

    CaptureCallback callback;

    File createAudioFile() {
        try {
            if (file == null)
                setCaptureName(String.valueOf(System.currentTimeMillis()));
            boolean su = true;
            if (!file.getParentFile().exists()) {
                su = file.getParentFile().mkdirs();
            }
            if (su && file.createNewFile()) {
                if (callback != null)
                    callback.capturePath(file);
                Log.d("tag", "create audio file success :" + file);
                return file;
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("tag", "create audio file error :" + e);
        }
        Log.d("tag", "create audio file failed ");
        return null;
    }

    public void setAudioFile(@NonNull File file) {
        this.file = file;
    }

    public void setCaptureName(@NonNull String name) {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/recorderDemo/" + name + ".aac";
        setAudioFile(new File(path));
    }

    public void setCaptureCallback(@NonNull CaptureCallback callback) {
        this.callback = callback;
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
        mExecutorService.submit(() -> {
            stopRecord();
            release();
        });
    }

    public void play(Context context, File file) {
        new Player().asyncPlay(context, file);
    }

    public void play(Context context) {
        play(context, file);
    }
}
