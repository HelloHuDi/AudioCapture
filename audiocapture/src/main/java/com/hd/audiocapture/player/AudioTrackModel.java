package com.hd.audiocapture.player;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.hd.audiocapture.Utils;
import com.hd.audiocapture.callback.PlaybackProgressCallback;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by hd on 2018/5/13 .
 */
public class AudioTrackModel extends AudioPlayer {

    private final String TAG = "AudioTrackModel";
    private ExecutorService mExecutorService = Executors.newSingleThreadExecutor();
    private final int SAMPLES_PER_FRAME = 1024;
    private final int DEFAULT_STREAM_TYPE = AudioManager.STREAM_MUSIC;
    private final int DEFAULT_SAMPLE_RATE = 44100;
    private final int DEFAULT_CHANNEL_CONFIG = AudioFormat.CHANNEL_OUT_MONO;
    private final int DEFAULT_AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private final int DEFAULT_PLAY_MODE = AudioTrack.MODE_STREAM;
    private AudioTrack mAudioTrack;
    private DataInputStream mDataInputStream;
    private boolean initCompleted;
    private volatile boolean mIsPlayStarted = false;
    private PlaybackProgressCallback callback;

    public AudioTrackModel(Context context, @NonNull File audioFile) {
        super(context, audioFile);
        initCompleted = initFileStream();
        if (initCompleted) {
            initCompleted = initPlayer(DEFAULT_STREAM_TYPE, DEFAULT_SAMPLE_RATE, DEFAULT_CHANNEL_CONFIG, DEFAULT_AUDIO_FORMAT);
        }
    }

    @Override
    public void play(@Nullable PlaybackProgressCallback callback) {
        if (initCompleted && !mIsPlayStarted) {
            this.callback = callback;
            mIsPlayStarted = true;
            mExecutorService.submit(AudioPlayRunnable);
        }
    }

    @Override
    public void stop() {
        mIsPlayStarted = false;
        initCompleted = false;
        mExecutorService.shutdownNow();
        if (mAudioTrack != null) {
            if (mAudioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
                mAudioTrack.stop();
            }
            mAudioTrack.release();
        }
        if (mDataInputStream != null) {
            try {
                mDataInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Log.i(TAG, "Stop AudioTrack success !");
    }

    private boolean initFileStream() {
        try {
            mDataInputStream = new DataInputStream(new FileInputStream(audioFile));
            readHeader();
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean initPlayer(int streamType, int sampleRateInHz, int channelConfig, int audioFormat) {
        int bufferSizeInBytes = AudioTrack.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat);
        if (bufferSizeInBytes == AudioTrack.ERROR_BAD_VALUE) {
            Log.e(TAG, "Invalid parameter !");
            return false;
        }
        Log.i(TAG, "getMinBufferSize = " + bufferSizeInBytes + " bytes !");
        mAudioTrack = new AudioTrack(streamType, sampleRateInHz, channelConfig, audioFormat, bufferSizeInBytes, DEFAULT_PLAY_MODE);
        if (mAudioTrack.getState() == AudioTrack.STATE_UNINITIALIZED) {
            Log.e(TAG, "AudioTrack initialize fail !");
            return false;
        }
        Log.i(TAG, "Start audio player success !");
        return true;
    }

    private int readData(byte[] buffer, int offset, int count) {
        try {
            int nbytes = mDataInputStream.read(buffer, offset, count);
            if (nbytes == -1) {
                return 0;
            }
            return nbytes;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    private void play(byte[] audioData, int offsetInBytes, int sizeInBytes) {
        if (mAudioTrack.write(audioData, offsetInBytes, sizeInBytes) != sizeInBytes) {
            Log.e(TAG, "Could not write all the samples to the audio device !");
        }
        mAudioTrack.play();
    }

    private Runnable AudioPlayRunnable = () -> {
        byte[] buffer = new byte[SAMPLES_PER_FRAME * 2];
        int allLen = 0, len;
        while (mIsPlayStarted && (len = readData(buffer, 0, buffer.length)) > 0) {
            play(buffer, 0, buffer.length);
            allLen += len;
            if (null != callback)
                callback.progress(allLen, audioFile.length());
        }
        if (callback != null)
            callback.progress(audioFile.length(), audioFile.length());
        stop();
    };


    private void readHeader() {
        String mChunkID = "RIFF";
        int mChunkSize = 0;
        String mFormat = "WAVE";

        String mSubChunk1ID = "fmt ";
        int mSubChunk1Size = 16;
        short mAudioFormat = 1;
        short mNumChannel = 1;
        int mSampleRate = 8000;
        int mByteRate = 0;
        short mBlockAlign = 0;
        short mBitsPerSample = 8;

        String mSubChunk2ID = "data";
        int mSubChunk2Size = 0;

        byte[] intValue = new byte[4];
        byte[] shortValue = new byte[2];
        int readNum;
        try {
            mChunkID = "" + (char) mDataInputStream.readByte() + (char) mDataInputStream.readByte() + //
                    (char) mDataInputStream.readByte() + (char) mDataInputStream.readByte();
            Log.d(TAG, "Read file chunkID:" + mChunkID);

            readNum = mDataInputStream.read(intValue);
            mChunkSize = Utils.byteArrayToInt(intValue);
            Log.d(TAG, "Read file chunkSize:" + mChunkSize);

            mFormat = "" + (char) mDataInputStream.readByte() + (char) mDataInputStream.readByte() + //
                    (char) mDataInputStream.readByte() + (char) mDataInputStream.readByte();
            Log.d(TAG, "Read file format:" + mFormat);

            mSubChunk1ID = "" + (char) mDataInputStream.readByte() + (char) mDataInputStream.readByte() + //
                    (char) mDataInputStream.readByte() + (char) mDataInputStream.readByte();
            Log.d(TAG, "Read fmt chunkID:" + mSubChunk1ID);

            readNum = mDataInputStream.read(intValue);
            mSubChunk1Size = Utils.byteArrayToInt(intValue);
            Log.d(TAG, "Read fmt chunkSize:" + mSubChunk1Size);

            readNum = mDataInputStream.read(shortValue);
            mAudioFormat = Utils.byteArrayToShort(shortValue);
            Log.d(TAG, "Read audioFormat:" + mAudioFormat);

            readNum = mDataInputStream.read(shortValue);
            mNumChannel = Utils.byteArrayToShort(shortValue);
            Log.d(TAG, "Read channel number:" + mNumChannel);

            readNum = mDataInputStream.read(intValue);
            mSampleRate = Utils.byteArrayToInt(intValue);
            Log.d(TAG, "Read samplerate:" + mSampleRate);

            readNum = mDataInputStream.read(intValue);
            mByteRate = Utils.byteArrayToInt(intValue);
            Log.d(TAG, "Read byterate:" + mByteRate);

            readNum = mDataInputStream.read(shortValue);
            mBlockAlign = Utils.byteArrayToShort(shortValue);
            Log.d(TAG, "Read blockalign:" + mBlockAlign);

            readNum = mDataInputStream.read(shortValue);
            mBitsPerSample = Utils.byteArrayToShort(shortValue);
            Log.d(TAG, "Read bitspersample:" + mBitsPerSample);

            mSubChunk2ID = "" + (char) mDataInputStream.readByte() + (char) mDataInputStream.readByte() + //
                    (char) mDataInputStream.readByte() + (char) mDataInputStream.readByte();
            Log.d(TAG, "Read data chunkID:" + mSubChunk2ID);

            readNum = mDataInputStream.read(intValue);
            mSubChunk2Size = Utils.byteArrayToInt(intValue);
            Log.d(TAG, "Read data chunkSize:" + mSubChunk2Size);

            Log.d(TAG, "Read wav file success !");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
