package com.hd.audiorecorderlearn;

import android.content.Context;
import android.media.AsyncPlayer;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;

import java.io.File;

/**
 * Created by hd on 2018/5/8 .
 */
public class Player {

    private MediaPlayer mediaPlayer;

    private AsyncPlayer asyncPlayer;

    public void asyncPlay(Context context,File audioFile){
        stop();
        asyncPlayer=new AsyncPlayer("player");
        asyncPlayer.play(context.getApplicationContext(), Uri.fromFile(audioFile), false, AudioManager.STREAM_MUSIC);
    }

    public void play(File audioFile) {
        try {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) stop();
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(audioFile.getAbsolutePath());
            mediaPlayer.setVolume(1, 1);
            mediaPlayer.setLooping(false);
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    stop();
                }
            });
            mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    stop();
                    Log.e("player", "play audio error");
                    return true;
                }
            });
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
            stop();
        }

    }

    public void stop() {
        if(mediaPlayer!=null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if(asyncPlayer!=null){
            asyncPlayer.stop();
            asyncPlayer=null;
        }
    }
}
