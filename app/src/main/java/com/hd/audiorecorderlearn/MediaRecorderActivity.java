package com.hd.audiorecorderlearn;

import android.annotation.SuppressLint;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MediaRecorderActivity extends AppCompatActivity {

    private TextView tv_sendmsg, tv_press_send;
    private ExecutorService mExecutorService;
    private MediaRecorder mMediaRecorder;
    private MediaPlayer mediaPlayer;
    private File mRecorderFile;
    private long startRecorderTime, stopRecorderTime;
    private Handler mHander = new Handler(Looper.getMainLooper());
    private boolean mIsPlaying = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_recorder);
        setTitle("文件录音");
        initView();
        //录音JNI函数不具有线程安全性，因此用单线程
        mExecutorService = Executors.newSingleThreadExecutor();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initView() {
        tv_sendmsg = (TextView) findViewById(R.id.tv_sendmsg);
        tv_press_send = (TextView) findViewById(R.id.tv_press_send);
        tv_press_send.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startRecorder();
                        break;

                    case MotionEvent.ACTION_UP:
                        stopRecorder();
                        break;

                    case MotionEvent.ACTION_CANCEL:

                        break;

                    default:
                        break;
                }
                return true;
            }
        });
    }

    /**
     * 开启录音
     */
    private void startRecorder() {
        tv_press_send.setText("正在说话...");
        //提交后台任务，开始录音
        mExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                //释放上一次的录音
                releaseRecorder();

                //开始录音

                if (!doStart()) {
                    recorderFial();
                }
            }
        });
    }


    /**
     * 释放上一次的录音
     */
    private void releaseRecorder() {
        if (mMediaRecorder != null) {
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
    }

    /**
     * 启动录音
     *
     * @return
     */

    private boolean doStart() {

        try {
            //创建MediaRecorder
            mMediaRecorder = new MediaRecorder();
            //创建录音文件
            mRecorderFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                                             + "/recorderdemo/" + System.currentTimeMillis() + ".m4a");
            if (!mRecorderFile.getParentFile().exists()) mRecorderFile.getParentFile().mkdirs();
            mRecorderFile.createNewFile();


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
            startRecorderTime = System.currentTimeMillis();

        } catch (Exception e) {
            Toast.makeText(MediaRecorderActivity.this, "录音失败，请重试", Toast.LENGTH_SHORT).show();
            return false;
        }


        //记录开始录音时间，用于统计时长，小于3秒中，录音不发送

        return true;
    }

    /**
     * 关闭录音
     *
     * @return
     */
    private boolean doStop() {
        try {
            mMediaRecorder.stop();
            stopRecorderTime = System.currentTimeMillis();
            final int second = (int) (stopRecorderTime - startRecorderTime) / 1000;
            //按住时间小于3秒钟，算作录取失败，不进行发送
            if (second < 3) return false;
            mHander.post(new Runnable() {
                @Override
                public void run() {
                    tv_sendmsg.setText("录制成功：" + second + "秒");
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }


    /**
     * 录音失败逻辑
     */

    private void recorderFial() {
        mRecorderFile = null;
        mHander.post(new Runnable() {
            @Override
            public void run() {
                tv_press_send.setText("录音失败请重新录音");
            }
        });
    }


    /**
     * 停止录音
     */
    private void stopRecorder() {
        tv_press_send.setText("开始录音");
        //提交后台任务，停止录音
        mExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                if (!doStop()) {
                    recorderFial();
                }
                releaseRecorder();

            }
        });
    }


    /**
     * 播放录音
     *
     * @param view
     */
    public void playrecorder(View view) {
        if (!mIsPlaying) {
            mExecutorService.submit(new Runnable() {
                @Override
                public void run() {
                    doPlay(mRecorderFile);
                }
            });

        } else {
            Toast.makeText(MediaRecorderActivity.this, "正在播放", Toast.LENGTH_SHORT).show();
        }
    }


    private void doPlay(File audioFile) {
        try {
            //配置播放器 MediaPlayer
            mediaPlayer = new MediaPlayer();
            //设置声音文件
            mediaPlayer.setDataSource(audioFile.getAbsolutePath());
            //配置音量,中等音量
            mediaPlayer.setVolume(1,1);
            //播放是否循环
            mediaPlayer.setLooping(false);

            //设置监听回调 播放完毕
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    stopPlayer();
                }
            });

            mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    stopPlayer();
                    Toast.makeText(MediaRecorderActivity.this, "播放失败", Toast.LENGTH_SHORT).show();
                    return true;
                }
            });

            //设置播放
            mediaPlayer.prepare();
            mediaPlayer.start();

            //异常处理，防止闪退

        } catch (Exception e) {
            e.printStackTrace();
            stopPlayer();
        }


    }

    private void stopPlayer(){
        mIsPlaying=false;
        mediaPlayer.release();
        mediaPlayer=null;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        //当activity关闭时，停止这个线程，防止内存泄漏
        mExecutorService.shutdownNow();
        releaseRecorder();
    }
}
