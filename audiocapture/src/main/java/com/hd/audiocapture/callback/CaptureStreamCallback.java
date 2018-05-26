package com.hd.audiocapture.callback;

import android.support.annotation.NonNull;

/**
 * Created by hd on 2018/5/25 .
 */
public interface CaptureStreamCallback extends CaptureCallback{

    /**
     * capture audio content bytes
     */
    void captureContentByte(@NonNull byte[] content);
}
