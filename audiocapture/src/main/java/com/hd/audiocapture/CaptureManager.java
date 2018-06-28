package com.hd.audiocapture;

import android.support.annotation.NonNull;

import com.hd.audiocapture.capture.Capture;

/**
 * Created by hd on 2018/5/9 .
 */
public final class CaptureManager {

    public static CaptureManager into(Capture capture, String mode) {
        return new CaptureManager(capture, mode);
    }

    private Capture capture;

    private CaptureConfig captureConfig;

    private String mode;

    private CaptureManager(@NonNull Capture capture, String mode) {
        this.capture = capture;
        this.mode = mode;
    }

    public CaptureManager setCaptureConfig(CaptureConfig config) {
        captureConfig = config;
        return this;
    }

    @NonNull
    public Capture getCapture() {
        captureConfig.setMode(mode);
        capture.setCaptureConfig(captureConfig);
        return capture;
    }
}
