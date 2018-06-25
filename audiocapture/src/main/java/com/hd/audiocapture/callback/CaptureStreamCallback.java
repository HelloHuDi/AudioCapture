package com.hd.audiocapture.callback;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by hd on 2018/5/25 .
 */
public interface CaptureStreamCallback extends CaptureCallback{

    /**
     * filter audio content bytes,e.g. add AEC/AECM
     */
    @Nullable byte[] filterContentByte(@NonNull byte[] content);
    /**
     * capture audio content bytes
     */
    void captureContentByte(@NonNull byte[] content);
}
