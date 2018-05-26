package com.hd.audiocapture.callback;

import com.hd.audiocapture.CaptureState;

import java.io.File;

/**
 * Created by hd on 2018/5/9 .
 */
public interface CaptureCallback {

    /**
     * capture audio file path
     */
    void capturePath(File file);

    /**
     * capture state
     */
    void captureStatus(CaptureState state);

    /**
     * real-time volume
     */
    void captureVolume(double volume);

}
