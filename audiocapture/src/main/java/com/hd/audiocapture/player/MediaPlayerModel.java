package com.hd.audiocapture.player;

import android.content.Context;
import android.media.AsyncPlayer;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.File;

/**
 * Created by hd on 2018/5/13 .
 */
public class MediaPlayerModel extends AudioPlayer{

    public MediaPlayerModel(Context context,@NonNull File audioFile) {
        super(context, audioFile);
    }

    private MediaPlayer mediaPlayer;

    private AsyncPlayer asyncPlayer;

    @Override
    public void asyncPlay() {
        stop();
        asyncPlayer = new AsyncPlayer("MediaPlayerModel_player");
        asyncPlayer.play(context.getApplicationContext(), Uri.fromFile(audioFile), false, AudioManager.STREAM_MUSIC);
    }

    @Override
    public void play() {
        try {
            if (mediaPlayer != null && mediaPlayer.isPlaying())
                stop();
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(audioFile.getAbsolutePath());
            mediaPlayer.setVolume(1, 1);
            mediaPlayer.setLooping(false);
            mediaPlayer.setOnCompletionListener(mp -> stop());
            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                stop();
                Log.e("player", "play audio error");
                return true;
            });
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
            stop();
        }
    }

    @Override
    public void stop() {
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
