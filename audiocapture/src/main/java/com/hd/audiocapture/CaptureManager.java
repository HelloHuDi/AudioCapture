package com.hd.audiocapture;

import android.support.annotation.NonNull;

import com.hd.audiocapture.callback.CaptureCallback;
import com.hd.audiocapture.capture.Capture;

import java.io.File;

/**
 * Created by hd on 2018/5/9 .
 */
public final class CaptureManager {

    public static CaptureManager into(Capture capture) {
        return new CaptureManager(capture);
    }

    private Capture capture;

    private CaptureManager(@NonNull Capture capture) {
        this.capture = capture;
    }

    public CaptureManager setAudioFile(@NonNull File file) {
        capture.setAudioFile(file);
        return this;
    }

    public CaptureManager setCaptureName(@NonNull String name) {
        capture.setCaptureName(name);
        return this;
    }

    public CaptureManager setCaptureCallback(@NonNull CaptureCallback callback) {
        capture.setCaptureCallback(callback);
        return this;
    }

    @NonNull
    public Capture getCapture() {
        return capture;
    }

    public void startCapture(long duration) {
        getCapture().startCapture(duration);
    }

}
