package com.hd.audiorecorderlearn;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by hd on 2018/5/8 .
 */
public abstract class AudioModel {

    abstract void startRecord();

    abstract void stopRecord();

    abstract void release();

    private ExecutorService mExecutorService = Executors.newFixedThreadPool(2);

    private AudioCallback callback;

    private String path;

    AudioModel(AudioCallback callback, String tag) {
        this.callback = callback;
        path = Environment.getExternalStorageDirectory().getAbsolutePath() //
                + "/recorderDemo/" + System.currentTimeMillis() + tag + ".aac";
    }

    File createAudioFile() {
        try {
            File mRecorderFile = new File(path);
            boolean su = true;
            if (!mRecorderFile.getParentFile().exists()) {
                su = mRecorderFile.getParentFile().mkdirs();
            }
            if (su && mRecorderFile.createNewFile()) {
                callback.audioPath(mRecorderFile);
                Log.d("tag", "create audio file success :" + mRecorderFile);
                return mRecorderFile;
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("tag", "create audio file error :" + e);
        }
        Log.d("tag", "create audio file failed ");
        return null;
    }

    public void start() {
        stop();
        mExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                startRecord();
            }
        });
    }

    public void stop() {
        mExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                stopRecord();
                release();
            }
        });
    }
}
