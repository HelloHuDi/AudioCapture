package com.hd.audiorecorderlearn;

import android.content.Context;
import android.util.Log;

import java.io.File;

/**
 * Created by hd on 2018/5/8 .
 */
public class AudioPresenter {

    public final static int MEDIARECORDER_STYLE = 0;

    public final static int AUDIORECORD_STYLE = 1;

    private Context context;

    private MediaRecorderModel mediaRecorderModel;

    private AudioRecordModel audioRecordModel;

    private AudioModel audioModel;

    public AudioPresenter(Context context, AudioCallback callback) {
        this.context = context.getApplicationContext();
        mediaRecorderModel = new MediaRecorderModel(callback);
        audioRecordModel = new AudioRecordModel(callback);
    }

    public void initStyle(int style) {
        if (style == MEDIARECORDER_STYLE) {
            audioModel = mediaRecorderModel;
        } else {
            audioModel = audioRecordModel;
        }
        Log.d("tag","init recorder style :"+audioModel);
    }

    public void start() {
        audioModel.start();
    }

    public void stop() {
        audioModel.stop();
    }

    public void play(File file) {
        new Player().asyncPlay(context, file);
    }
}
