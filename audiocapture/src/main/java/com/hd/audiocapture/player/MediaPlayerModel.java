package com.hd.audiocapture.player;

import android.content.Context;
import android.media.AsyncPlayer;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.hd.audiocapture.callback.PlaybackProgressCallback;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by hd on 2018/5/13 .
 */
public class MediaPlayerModel extends AudioPlayer {

    private final String TAG = "MediaPlayerModel";

    public MediaPlayerModel(Context context, @NonNull File audioFile) {
        super(context, audioFile);
    }

    private MediaPlayer mediaPlayer;

    private AsyncPlayer asyncPlayer;

    private AtomicBoolean playing = new AtomicBoolean(false);

    @Override
    public void play(@Nullable PlaybackProgressCallback callback) {
        stop();
        try {
            if (null == callback) {
                playWithAsync();
            } else {
                new Thread(() -> {
                    try {
                        playWithMedia(callback);
                    } catch (IOException e) {
                        stop(e);
                    }
                }).start();
            }
        } catch (Exception e) {
            stop(e);
        }
    }

    private void playWithAsync() {
        asyncPlayer = new AsyncPlayer(TAG);
        asyncPlayer.play(context.getApplicationContext(), Uri.fromFile(audioFile), false, AudioManager.STREAM_MUSIC);
    }

    private void playWithMedia(@NonNull PlaybackProgressCallback callback) throws IOException {
        if (mediaPlayer != null && mediaPlayer.isPlaying())
            stop();
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setDataSource(audioFile.getAbsolutePath());
        mediaPlayer.setVolume(1, 1);
        mediaPlayer.setLooping(false);
        mediaPlayer.setOnCompletionListener(mp -> stop());
        mediaPlayer.setOnErrorListener((mp, what, extra) -> {
            stop();
            Log.e(TAG, "play audio error");
            return true;
        });
        mediaPlayer.setOnPreparedListener(mp -> getDuration(callback, mp));
        playing.set(true);
        mediaPlayer.prepare();
    }

    private void getDuration(@NonNull PlaybackProgressCallback callback, MediaPlayer mp) {
        mp.start();
        int max = mediaPlayer.getDuration();
        mediaPlayer.seekTo(0);
        new Thread() {
            public void run() {
                while (playing.get()) {
                    callback.progress(mediaPlayer.getCurrentPosition(), max);
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                callback.progress(max, max);
            }
        }.start();
    }

    private void stop(Exception e) {
        e.printStackTrace();
        stop();
    }

    @Override
    public void stop() {
        playing.set(false);
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (asyncPlayer != null) {
            asyncPlayer.stop();
            asyncPlayer = null;
        }
    }
}
