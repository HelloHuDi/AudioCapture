package com.hd.audiocapture.callback;

/**
 * Created by hd on 2018/6/19 .
 */
public interface PlaybackProgressCallback {

    void progress(long currentDuration, long maxDuration);

}
