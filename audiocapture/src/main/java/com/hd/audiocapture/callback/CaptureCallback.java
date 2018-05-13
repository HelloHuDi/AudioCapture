package com.hd.audiocapture.callback;

import com.hd.audiocapture.CaptureState;

import java.io.File;

/**
 * Created by hd on 2018/5/9 .
 */
public interface CaptureCallback {

    void capturePath(File file);

    void captureStatus(CaptureState state);

    void captureVolume(double volume);
}
