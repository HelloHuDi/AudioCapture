package com.hd.audiocapture.capture;

import android.media.MediaRecorder;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;

import com.hd.audiocapture.CaptureState;
import com.hd.audiocapture.CaptureType;

import java.io.File;

/**
 * Created by hd on 2018/5/9 .
 */
public class MediaRecorderCapture extends Capture {

    private final String TAG = MediaRecorderCapture.class.getSimpleName();

    private MediaRecorder mMediaRecorder;

    @Override
    void startRecord() {
        try {
            mMediaRecorder = new MediaRecorder();
            File mRecorderFile = createAudioFile();
            if (mRecorderFile == null) {
                if (captureConfig.allowLog())
                    Log.e(TAG, "create file error");
                notAllowEnterNextStep();
                return;
            }
            record.set(true);
            reportState(CaptureState.START);
            mMediaRecorder.setAudioSource(captureConfig.getAudioSource());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                mMediaRecorder.setOutputFormat(CaptureType.AAC_FORMAT.equals(captureConfig.getMode()) ?//
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
            reportState(CaptureState.RESUME);
            initVolumeThread();
        } catch (Exception e) {
            e.printStackTrace();
            if (captureConfig.allowLog())
                Log.e(TAG, "create media recorder error :" + e);
            notAllowEnterNextStep();
        }
    }

    /**
     * recommend {@link AudioRecordCapture}
     * <p>
     * This function is not supported before api 24 ,if you want to implement such a function,
     * you can try to record multiple files and merge them at last.
     */
    @Override
    public void pauseCapture() {
        mExecutorService.submit(() -> {
            if (mMediaRecorder != null) {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        reportState(CaptureState.PAUSE);
                        mMediaRecorder.pause();
                        SystemClock.sleep(50);
                        if (callback != null) callback.captureVolume(0d);
                    } else {
                        Log.e(TAG, "This function is not supported before api 24 ");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    notAllowEnterNextStep();
                }
            }
        });
    }

    /**
     * recommend {@link AudioRecordCapture}
     * <p>
     * This function is not supported before api 24 ,if you want to implement such a function,
     * you can try to record multiple files and merge them at last.
     */
    @Override
    public void resumeCapture() {
        mExecutorService.submit(() -> {
            if (mMediaRecorder != null) {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        reportState(CaptureState.RESUME);
                        mMediaRecorder.resume();
                    } else {
                        Log.e(TAG, "This function is not supported before api 24 ");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    notAllowEnterNextStep();
                }
            }
        });
    }

    @Override
    void stopRecord() {
        if (mMediaRecorder != null)
            try {
                mMediaRecorder.stop();
                mMediaRecorder.reset();
            } catch (Exception e) {
                e.printStackTrace();
            }
        if (captureConfig.allowLog())
            Log.d(TAG, "MediaRecorderCapture stop record");
    }

    @Override
    void release() {
        if (mMediaRecorder != null) {
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
        if (captureConfig.allowLog())
            Log.d(TAG, "MediaRecorderCapture release");
    }

    private void initVolumeThread() {
        mExecutorService.submit(() -> {
            while (record.get() && mMediaRecorder != null) {
                try {
                    if (CaptureState.RESUME == state) {
                        double ratio = (double) mMediaRecorder.getMaxAmplitude() / 1;
                        if (ratio > 1) {
                            double db = 20 * Math.log10(ratio);
                            if (callback != null)
                                callback.captureVolume(db);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
