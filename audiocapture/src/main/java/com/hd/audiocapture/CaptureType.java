package com.hd.audiocapture;

import com.hd.audiocapture.capture.AudioRecordCapture;
import com.hd.audiocapture.capture.Capture;
import com.hd.audiocapture.capture.MediaRecorderCapture;

/**
 * Created by hd on 2018/5/9 .
 */
public final class CaptureType {

    public final static String MEDIA_RECORDER_TYPE = "media_recorder";

    public final static String AUDIO_RECORD_TYPE = "audio_record";

    public final static String AAC_FORMAT = "aac";

    public final static String WAV_FORMAT = "wav";

    public final static String MP4_FORMAT = "mp4";

    CaptureManager of(String tag, String mode) {
        Capture capture;
        if (MEDIA_RECORDER_TYPE.equals(tag)) {
            capture = new MediaRecorderCapture();
        } else {
            capture = new AudioRecordCapture();
        }
        return CaptureManager.into(capture, mode);
    }

}
