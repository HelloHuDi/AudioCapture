package com.hd.audiocapture;

/**
 * Created by hd on 2018/5/9 .
 * audio capture, create aac audio file
 *
 */
public final class AudioCapture {

    private static CaptureManager getCaptureManager(String tag) {
        CaptureType captureType = new CaptureType();
        return captureType.of(tag);
    }

    private static CaptureManager use(String tag) {
        return getCaptureManager(tag);
    }

    public static CaptureManager useAudioRecord() {
        return use(CaptureType.MEDIA_RECORDER_TYPE);
    }

    public static CaptureManager useMediaRecorder() {
        return use(CaptureType.AUDIO_RECORD_TYPE);
    }

}
