package com.hd.audiocapture.player;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.hd.audiocapture.callback.PlaybackProgressCallback;

import java.io.File;

/**
 * Created by hd on 2018/5/8 .
 */
public final class Player {

    private AudioPlayer audioPlayer;

    public Player(Context context, @NonNull File audioFile) {
        if (audioFile.getAbsolutePath().endsWith(".wav")) {
            audioPlayer = new AudioTrackModel(context, audioFile);
        } else {
            audioPlayer = new MediaPlayerModel(context, audioFile);
        }
    }

    @Deprecated
    public void asyncPlay() {
        play();
    }

    public void play() {
        play(null);
    }

    public void play(@Nullable PlaybackProgressCallback callback) {
        if (audioPlayer != null)
            audioPlayer.play(callback);
    }

    public void stop() {
        if (audioPlayer != null) {
            audioPlayer.stop();
            audioPlayer = null;
        }
    }
}
