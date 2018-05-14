package com.hd.audiorecorderlearn;

import android.content.Context;
import android.media.AudioManager;

import com.hd.audiocapture.AudioCapture;
import com.hd.audiocapture.Utils;
import com.hd.audiocapture.callback.CaptureCallback;
import com.hd.audiocapture.capture.Capture;

import java.io.File;

/**
 * Created by hd on 2018/5/8 .
 */
public class AudioPresenter {

    public final static int MEDIARECORDER_MP4_STYLE = 0;

    public final static int MEDIARECORDER_AAC_STYLE = 1;

    public final static int AUDIORECORD_AAC_STYLE = 2;

    public final static int AUDIORECORD_WAV_STYLE = 3;

    private Capture capture;

    private Context context;

    private CaptureCallback callback;

    AudioPresenter(Context context, CaptureCallback callback) {
        if (Utils.isPermissionGranted(context) && Utils.isExternalStorageReady()) {
            this.context = context;
            this.callback = callback;
            AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            audioManager.setSpeakerphoneOn(false);
            audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, 0, AudioManager.STREAM_VOICE_CALL);
            audioManager.setMode(AudioManager.MODE_IN_CALL);
        } else {
            throw new RuntimeException("permission not grant");
        }
    }

    public void initStyle(int style) {
        switch (style) {
            case MEDIARECORDER_MP4_STYLE:
                capture = AudioCapture.withMediaRecorderToMP4()//
                                      .setCaptureName(System.currentTimeMillis() + "_medMp4")//
                                      .setCaptureCallback(callback)//
                                      .getCapture();
                break;
            case MEDIARECORDER_AAC_STYLE:
                capture = AudioCapture.withMediaRecorderToAAC()//
                                      .setCaptureName(System.currentTimeMillis() + "_medAAC")//
                                      .setCaptureCallback(callback)//
                                      .getCapture();//
                break;
            case AUDIORECORD_AAC_STYLE:
                capture = AudioCapture.withAudioRecordToAAC()//
                                      .setCaptureName(System.currentTimeMillis() + "_arAAC")//
                                      .setCaptureCallback(callback)//
                                      .getCapture();//
                break;
            case AUDIORECORD_WAV_STYLE:
                capture = AudioCapture.withAudioRecordToWAV()//
                                      .setCaptureName(System.currentTimeMillis() + "_arWAV")//
                                      .setCaptureCallback(callback)//
                                      .getCapture();//
                break;
        }
    }

    public void start() {
        if (capture != null)
            capture.startCapture(/*5000*/);
    }

    public void stop() {
        if (capture != null)
            capture.stopCapture();
    }

    public void play(File file) {
        if (capture != null)
            capture.play(context, file);
    }
}
