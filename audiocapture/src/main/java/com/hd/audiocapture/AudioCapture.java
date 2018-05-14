package com.hd.audiocapture;

/**
 * Created by hd on 2018/5/9 .
 * audio capture, create aac audio file
 */
public final class AudioCapture {

    private static CaptureManager getCaptureManager(String tag, String mode) {
        CaptureType captureType = new CaptureType();
        return captureType.of(tag, mode);
    }

    private static CaptureManager with(String tag, String mode) {
        return getCaptureManager(tag, mode);
    }

    public static CaptureManager withAudioRecordToAAC() {
        return with(CaptureType.AUDIO_RECORD_TYPE, CaptureType.AAC_FORMAT);
    }

    public static CaptureManager withAudioRecordToWAV() {
        return with(CaptureType.AUDIO_RECORD_TYPE, CaptureType.WAV_FORMAT);
    }

    public static CaptureManager withMediaRecorderToAAC() {
        return with(CaptureType.MEDIA_RECORDER_TYPE, CaptureType.AAC_FORMAT);
    }

    public static CaptureManager withMediaRecorderToMP4() {
        return with(CaptureType.MEDIA_RECORDER_TYPE, CaptureType.MP4_FORMAT);
    }

    public static CaptureManager withDefault() {
        return withAudioRecordToWAV();
    }

}
