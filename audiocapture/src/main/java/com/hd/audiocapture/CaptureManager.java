package com.hd.audiocapture;

import android.support.annotation.NonNull;

import com.hd.audiocapture.callback.CaptureCallback;
import com.hd.audiocapture.capture.Capture;

import java.io.File;

/**
 * Created by hd on 2018/5/9 .
 */
public final class CaptureManager {

    public static CaptureManager into(Capture capture, String mode) {
        return new CaptureManager(capture, mode);
    }

    private Capture capture;

    private CaptureConfig captureConfig;

    private CaptureManager(@NonNull Capture capture, String mode) {
        this.capture = capture;
        captureConfig = new CaptureConfig();
        captureConfig.setMode(mode);
    }

    public CaptureManager setAudioFile(@NonNull File file) {
        captureConfig.setFile(file);
        return this;
    }

    public CaptureManager setCaptureName(@NonNull String name) {
        captureConfig.setName(name);
        return this;
    }

    public CaptureManager setCaptureCallback(@NonNull CaptureCallback callback) {
        captureConfig.setCaptureCallback(callback);
        return this;
    }

    @NonNull
    public Capture getCapture() {
        capture.setCaptureConfig(captureConfig);
        return capture;
    }

    public void startCapture(long duration) {
        getCapture().startCapture(duration);
    }

}
