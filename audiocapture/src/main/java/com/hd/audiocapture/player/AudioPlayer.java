package com.hd.audiocapture.player;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.hd.audiocapture.callback.PlaybackProgressCallback;

import java.io.File;

/**
 * Created by hd on 2018/5/13 .
 */
public abstract class AudioPlayer {

    protected Context context;

    protected File audioFile;

    public AudioPlayer(Context context,@NonNull File audioFile) {
        this.context = context.getApplicationContext();
        this.audioFile = audioFile;
    }

    public abstract void play(@Nullable PlaybackProgressCallback callback);

    public abstract void stop();
}
