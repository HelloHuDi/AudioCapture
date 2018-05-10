package com.hd.audiocapture.capture;

import android.media.MediaRecorder;
import android.os.Build;
import android.util.Log;

import com.hd.audiocapture.CaptureType;

import java.io.File;

/**
 * Created by hd on 2018/5/9 .
 */
public class MediaRecorderCapture extends Capture {

    private MediaRecorder mMediaRecorder;

    @Override
    void startRecord() {
        try {
            mMediaRecorder = new MediaRecorder();
            File mRecorderFile = createAudioFile();
            if (mRecorderFile == null) {
                Log.e("tag", "create file error");
                if(callback!=null)callback.captureStatus(false);
                return;
            }
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                mMediaRecorder.setOutputFormat(CaptureType.AAC_FORMAT.equals(mode) ?//
                     MediaRecorder.OutputFormat.AAC_ADTS : MediaRecorder.OutputFormat.MPEG_4);
            } else {
                mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            }
            mMediaRecorder.setAudioSamplingRate(captureConfig.getSamplingRate());
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mMediaRecorder.setAudioEncodingBitRate(captureConfig.getBitrate());
            mMediaRecorder.setOutputFile(mRecorderFile.getAbsolutePath());
            mMediaRecorder.prepare();
            mMediaRecorder.start();
            if(callback!=null)callback.captureStatus(true);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("tag", "create media recorder error :" + e);
        }
    }

    @Override
    void stopRecord() {
        if (mMediaRecorder != null)
            try {
                mMediaRecorder.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
        Log.d("tag", "MediaRecorderCapture stop record");
    }

    @Override
    void release() {
        if (mMediaRecorder != null) {
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
        Log.d("tag", "MediaRecorderCapture release");
    }
}
