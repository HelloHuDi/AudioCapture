package com.hd.audiorecorderlearn;

import android.content.Context;
import android.media.AudioManager;

import com.hd.audiocapture.AudioCapture;
import com.hd.audiocapture.CaptureConfig;
import com.hd.audiocapture.CaptureManager;
import com.hd.audiocapture.Utils;
import com.hd.audiocapture.callback.CaptureCallback;
import com.hd.audiocapture.callback.PlaybackProgressCallback;
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

    private CaptureConfig captureConfig;

    private CaptureManager manager = null;

    AudioPresenter(Context context, CaptureCallback callback) {
        if (Utils.isPermissionGranted(context) && Utils.isExternalStorageReady()) {
            this.context = context;
            this.callback = callback;
            AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            if (audioManager != null) {
                audioManager.setSpeakerphoneOn(false);
                audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, 0, AudioManager.STREAM_VOICE_CALL);
                audioManager.setMode(AudioManager.MODE_IN_CALL);
            }
            initConfig();
        } else {
            throw new RuntimeException("permission not grant");
        }
    }

    private void initConfig() {
        captureConfig = new CaptureConfig.Builder().setCaptureCallback(callback).build();
    }

    public void initStyle(int style) {
        switch (style) {
            case MEDIARECORDER_MP4_STYLE:
                suffix = "_medMp4";
                manager = AudioCapture.withMediaRecorderToMP4();
                break;
            case MEDIARECORDER_AAC_STYLE:
                suffix = "_medAAC";
                manager = AudioCapture.withMediaRecorderToAAC();
                break;
            case AUDIORECORD_AAC_STYLE:
                suffix = "_arAAC";
                manager = AudioCapture.withAudioRecordToAAC();
                break;
            case AUDIORECORD_WAV_STYLE:
                suffix = "_arWAV";
                manager = AudioCapture.withAudioRecordToWAV();
                break;
        }
    }

    private String suffix = "";

    private void getCapture() {
        String fileName = String.valueOf(System.currentTimeMillis());
        fileName += suffix;
        captureConfig.setName(fileName);
        capture = manager.setCaptureConfig(captureConfig).getCapture();
    }

    public void start() {
        getCapture();
        if (capture != null)
            capture.startCapture();
    }

    public void pause() {
        if (capture != null)
            capture.pauseCapture();
    }

    public void resume() {
        if (capture != null)
            capture.resumeCapture();
    }

    public void stop() {
        if (capture != null)
            capture.stopCapture();
    }

    public void play(File file, PlaybackProgressCallback callback) {
        if (capture != null)
            capture.play(context, callback);
    }
}
