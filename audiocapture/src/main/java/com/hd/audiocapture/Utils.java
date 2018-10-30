package com.hd.audiocapture;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
import android.support.v4.content.ContextCompat;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class Utils {

    public static boolean isExternalStorageReady() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());

    }

    public static boolean isPermissionGranted(Context context) {
        String permission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }

    public static byte[] intToByteArray(int data) {
        return ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(data).array();
    }

    public static byte[] shortToByteArray(short data) {
        return ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(data).array();
    }

    public static short byteArrayToShort(byte[] b) {
        return ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).getShort();
    }

    public static int byteArrayToInt(byte[] b) {
        return ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).getInt();
    }

    /**
     * check the recording permission below android 6.0
     */
    public static boolean checkAudioPermission() {
        final int audioSource = MediaRecorder.AudioSource.MIC;
        final int sampleRateInHz = 44100;
        final int channelConfig = AudioFormat.CHANNEL_IN_STEREO;
        final int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
        int bufferSizeInBytes = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat);
        AudioRecord audioRecord = new AudioRecord(audioSource, sampleRateInHz, channelConfig, audioFormat, bufferSizeInBytes);
        try {
            try {
                audioRecord.startRecording();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
            if (audioRecord.getRecordingState() != AudioRecord.RECORDSTATE_RECORDING //
                    && audioRecord.getRecordingState() != AudioRecord.RECORDSTATE_STOPPED) {
                return false;
            }
            if (audioRecord.getRecordingState() == AudioRecord.RECORDSTATE_STOPPED) {
                return false;
            }
            byte[] bytes = new byte[1024];
            int readSize = audioRecord.read(bytes, 0, 1024);
            return readSize != AudioRecord.ERROR_INVALID_OPERATION && readSize > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            audioRecord.stop();
            audioRecord.release();
            audioRecord = null;
        }
    }
}

