package com.hd.audiocapture.player;

import android.content.Context;
import android.support.annotation.NonNull;

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

    public void asyncPlay() {
        if (audioPlayer != null)
            audioPlayer.asyncPlay();
    }

    public void play() {
        if (audioPlayer != null)
            audioPlayer.play();
    }

    public void stop() {
        if (audioPlayer != null) {
            audioPlayer.stop();
            audioPlayer = null;
        }
    }
}
