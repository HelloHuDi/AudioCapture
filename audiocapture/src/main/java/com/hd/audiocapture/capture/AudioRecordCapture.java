package com.hd.audiocapture.capture;

import android.media.AudioRecord;
import android.media.audiofx.AcousticEchoCanceler;
import android.media.audiofx.AutomaticGainControl;
import android.media.audiofx.NoiseSuppressor;
import android.os.Build;
import android.util.Log;

import com.hd.audiocapture.CaptureState;
import com.hd.audiocapture.CaptureType;
import com.hd.audiocapture.writer.AccFileWriter;
import com.hd.audiocapture.writer.AudioFileWriter;
import com.hd.audiocapture.writer.WavFileWriter;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by hd on 2018/5/9 .
 */
public class AudioRecordCapture extends Capture {

    private final String TAG = AudioRecordCapture.class.getSimpleName();

    @Override
    void startRecord() {
        if (initFile() && initAudioRecord()) {
            record.set(true);
            startReadData();
        } else {
            notAllowEnterNextStep();
        }
    }

    @Override
    void stopRecord() {
        if (null != audioFileWriter) {
            boolean success = audioFileWriter.stop();
            if (captureConfig.allowLog())
                Log.d(TAG, "writer close complete :" + success);
        }
        if (null != audioRecord) {
            audioRecord.stop();
        }
        if (null != mDataOutputStream) {
            try {
                mDataOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                mDataOutputStream = null;
            }
        }
        if (captureConfig.allowLog())
            Log.d(TAG, "AudioRecordCapture stop record");
    }

    @Override
    void release() {
        if (null != audioRecord) {
            audioRecord.release();
            audioRecord = null;
        }
        if (null != echoCanceler) {
            echoCanceler.setEnabled(false);
            echoCanceler.release();
            echoCanceler = null;
        }
        if (null != noiseSuppressor) {
            noiseSuppressor.setEnabled(false);
            noiseSuppressor.release();
            noiseSuppressor = null;
        }
        if (null != gainControl) {
            gainControl.setEnabled(false);
            gainControl.release();
            gainControl = null;
        }
        if (captureConfig.allowLog())
            Log.d(TAG, "AudioRecordCapture release");
    }

    private AudioRecord audioRecord;

    private AcousticEchoCanceler echoCanceler;

    private NoiseSuppressor noiseSuppressor;

    private AutomaticGainControl gainControl;

    private boolean initAudioRecord() {
        if (callback != null)
            callback.captureStatus(CaptureState.START);
        int minBufferSize = AudioRecord.getMinBufferSize(captureConfig.getSamplingRate(), //
                                                         captureConfig.getChannelCount(), captureConfig.getAudioFormat());
        if (minBufferSize == AudioRecord.ERROR_BAD_VALUE) {
            if (captureConfig.allowLog())
                Log.e(TAG, "Invalid parameter !");
            return false;
        }
        audioRecord = new AudioRecord(captureConfig.getAudioSource(), captureConfig.getSamplingRate(),//
                                      captureConfig.getChannelCount(), captureConfig.getAudioFormat(), minBufferSize * 4);
        if (audioRecord.getState() == AudioRecord.STATE_UNINITIALIZED) {
            if (captureConfig.allowLog())
                Log.e(TAG, "AudioRecord initialize fail !");
            return false;
        }
        try {
            addAEC();
            audioRecord.startRecording();
            if (audioRecord.getRecordingState() != AudioRecord.RECORDSTATE_RECORDING) {
                if (captureConfig.allowLog())
                    Log.e(TAG, "unable to recordings,recording equipment may be occupied");
                stopCapture();
                return false;
            }
            return true;
        } catch (Exception e) {
            if (captureConfig.allowLog())
                Log.e(TAG, "please check audio permission");
            release();
            return false;
        }
    }

    private void addAEC() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            int audioSession = audioRecord.getAudioSessionId();
            if (AcousticEchoCanceler.isAvailable()) {
                echoCanceler = AcousticEchoCanceler.create(audioSession);
                echoCanceler.setEnabled(true);
                if(captureConfig.allowLog()) Log.d(TAG, "start-up acoustic echo canceler");
            }
            if (NoiseSuppressor.isAvailable()) {
                noiseSuppressor = NoiseSuppressor.create(audioSession);
                noiseSuppressor.setEnabled(true);
                if(captureConfig.allowLog()) Log.d(TAG, "start-up noise suppressor");
            }
            if (AutomaticGainControl.isAvailable()) {
                gainControl = AutomaticGainControl.create(audioSession);
                gainControl.setEnabled(true);
                if(captureConfig.allowLog()) Log.d(TAG, "start-up automatic gain control");
            }
        }
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
        if (callback != null)
            callback.captureStatus(CaptureState.CAPTURING);
        while (record.get()) {
            byte[] buffer = new byte[SAMPLES_PER_FRAME * 2];
            int ret = audioRecord.read(buffer, 0, buffer.length);
            if (ret == AudioRecord.ERROR_INVALID_OPERATION) {
                if (captureConfig.allowLog())
                    Log.e(TAG, "Error ERROR_INVALID_OPERATION");
            } else if (ret == AudioRecord.ERROR_BAD_VALUE) {
                if (captureConfig.allowLog())
                    Log.e(TAG, "Error ERROR_BAD_VALUE");
            } else {
                if (ret <= 0) {
                    if (captureConfig.allowLog())
                        Log.e(TAG, "read data length error ==>" + ret);
                } else {
                    getVolume(buffer, ret);
                    boolean su = audioFileWriter.writeData(buffer, 0, buffer.length);
                    if (captureConfig.allowLog())
                        Log.d(TAG, "Audio captured: " + buffer.length + "==" + su + "==" + ret);
                }
            }
        }
    }

    private void getVolume(byte[] buffer, int ret) {
        short[] sa = new short[ret / 2];
        ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(sa);
        getVolume(sa, sa.length);
    }

    private void getVolume(short[] buffer, int ret) {
        calc1(buffer, 0, ret);
        long v = 0;
        for (short aBuffer : buffer) {
            v += aBuffer * aBuffer;
        }
        double mean = v / ret;
        double volume = 10 * Math.log10(mean);
        if (callback != null)
            callback.captureVolume(volume);
    }

    private void calc1(short[] lin, int off, int len) {
        int i, j;
        for (i = 0; i < len; i++) {
            j = lin[i + off];
            lin[i + off] = (short) (j >> 2);
        }
    }

}
