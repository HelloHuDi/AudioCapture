package com.hd.audiocapture.capture;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import com.hd.audiocapture.CaptureType;
import com.hd.audiocapture.writer.AccFileWriter;
import com.hd.audiocapture.writer.AudioFileWriter;
import com.hd.audiocapture.writer.WavFileWriter;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by hd on 2018/5/9 .
 */
public class AudioRecordCapture extends Capture {

    private final String TAG = AudioRecordCapture.class.getSimpleName();

    private volatile AtomicBoolean record = new AtomicBoolean(false);

    @Override
    void startRecord() {
        if (initFile() && initAudioRecord()) {
            record.set(true);
            startReadData();
        } else {
            if (callback != null)
                callback.captureStatus(false);
        }
    }

    @Override
    void stopRecord() {
        record.set(false);
        if (audioRecord != null)
            audioRecord.stop();
        if (mDataOutputStream != null) {
            try {
                mDataOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Log.d("tag", "AudioRecordCapture stop record");
    }

    @Override
    void release() {
        record.set(false);
        if (audioRecord != null)
            audioRecord.release();
        Log.d("tag", "AudioRecordCapture release");
    }

    private AudioRecord audioRecord;

    private boolean initAudioRecord() {
        int minBufferSize = AudioRecord.getMinBufferSize(captureConfig.getSamplingRate(), //
                                                         AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        if (minBufferSize == AudioRecord.ERROR_BAD_VALUE) {
            Log.e(TAG, "Invalid parameter !");
            return false;
        }
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, captureConfig.getSamplingRate(),//
                                      AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, minBufferSize * 4);
        if (audioRecord.getState() == AudioRecord.STATE_UNINITIALIZED) {
            Log.e(TAG, "AudioRecord initialize fail !");
            return false;
        }
        audioRecord.startRecording();
        return true;
    }

    private boolean initFile() {
        try {
            File file = createAudioFile();
            if (file == null)
                return false;
            mDataOutputStream = new DataOutputStream(new FileOutputStream(file.getAbsolutePath()));
            return writeHeader();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    private DataOutputStream mDataOutputStream;

    private AudioFileWriter audioFileWriter;

    private boolean writeHeader() {
        if (CaptureType.AAC_FORMAT.equals(mode)) {
            audioFileWriter = new AccFileWriter();
        } else {
            audioFileWriter = new WavFileWriter();
        }
        audioFileWriter.init(mDataOutputStream, captureConfig);
        return audioFileWriter.start();
    }

    // Make sure the sample size is the same in different devices
    private static final int SAMPLES_PER_FRAME = 1024;

    private void startReadData() {
        while (record.get()) {
            byte[] buffer = new byte[SAMPLES_PER_FRAME * 2];
            int ret = audioRecord.read(buffer, 0, buffer.length);
            if (ret == AudioRecord.ERROR_INVALID_OPERATION) {
                Log.e(TAG, "Error ERROR_INVALID_OPERATION");
            } else if (ret == AudioRecord.ERROR_BAD_VALUE) {
                Log.e(TAG, "Error ERROR_BAD_VALUE");
            } else {
                boolean su = audioFileWriter.writeData(buffer, 0, buffer.length);
                Log.d("TAG", "Audio captured: " + buffer.length + "==" + su);
            }
        }
        boolean success = audioFileWriter.stop();
        Log.d(TAG, "writer close complete :" + success);
        try {
            mDataOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            mDataOutputStream = null;
        }
    }
}
