package com.hd.audiocapture.capture;

import android.media.MediaRecorder;
import android.util.Log;

import java.io.File;

/**
 * Created by hd on 2018/5/9 .
 */
public class MediaRecorderCapture extends Capture {

    private MediaRecorder mMediaRecorder;

    @Override
    void startRecord() {
        try {
            //创建MediaRecorder
            mMediaRecorder = new MediaRecorder();
            //创建录音文件
            File mRecorderFile = createAudioFile();
            if (mRecorderFile == null) {
                Log.e("tag", "create file error");
                return;
            }
            //配置MediaRecorder

            //从麦克风采集
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);

            //保存文件为MP4格式
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS);

            //所有android系统都支持的适中采样的频率
            mMediaRecorder.setAudioSamplingRate(44100);

            //通用的AAC编码格式
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

            //设置音质频率
            mMediaRecorder.setAudioEncodingBitRate(96000);

            //设置文件录音的位置
            mMediaRecorder.setOutputFile(mRecorderFile.getAbsolutePath());

            //开始录音
            mMediaRecorder.prepare();
            mMediaRecorder.start();

        } catch (Exception e) {
            e.printStackTrace();
            Log.e("tag", "create media recorder error :" + e);
        }
    }

    @Override
    void stopRecord() {
        if (mMediaRecorder != null)
            try {
                mMediaRecorder.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
        Log.d("tag", "MediaRecorderCapture stop record");
    }

    @Override
    void release() {
        if (mMediaRecorder != null) {
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
        Log.d("tag", "MediaRecorderCapture release");
    }
}
