package com.hd.audiorecorderlearn;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AudioRecordActivity extends AppCompatActivity {
    private Button bt_stream_recorder;
    private TextView tv_stream_msg;
    private ExecutorService mExecutorService;
    private long startRecorderTime, stopRecorderTime;
    private volatile boolean mIsRecording = false;
    private AudioRecord mAudioRecord;
    private FileOutputStream mFileOutputStream;
    private File mAudioRecordFile;
    private byte[] mBuffer;
    //buffer值不能太大，避免OOM
    private static final int BUFFER_SIZE = 2048;
    private boolean mIsPlaying=false;
    private Handler mHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_record);
        setTitle("字节流录音");
        initView();
        mExecutorService = Executors.newSingleThreadExecutor();
        mBuffer = new byte[BUFFER_SIZE];

    }

    private void initView() {
        bt_stream_recorder = (Button) findViewById(R.id.bt_stream_recorder);
        tv_stream_msg = (TextView) findViewById(R.id.tv_stream_msg);

    }

    public void recorderaudio(View view) {
        if (mIsRecording) {
            bt_stream_recorder.setText("开始录音");
            //在开始录音中如果这个值没有变false，则一直进行，当再次点击变false时，录音才停止
            mIsRecording = false;

            //执行停止录音逻辑，这块不用下面代码，只需上面变换mIsRecording这个状态就可以了，下面一直走while
          /*  mExecutorService.submit(new Runnable() {
                @Override
                public void run() {
                    stopRecorder();
                }
            });*/
        } else {

            bt_stream_recorder.setText("停止录音");
            //提交后台任务，执行录音逻辑
            mIsRecording = true;
            //提交后台任务，执行录音逻辑
            mExecutorService.submit(new Runnable() {
                @Override
                public void run() {

                    startRecorder();
                }
            });
        }
    }

    /**
     * 开始录音
     */
    private void startRecorder() {
        // realeseRecorder();
        if (!dostart()) recorderFail();

    }

    /**
     * 停止录音
     */
    private void stopRecorder() {
        mIsRecording=false;
        if (!doStop()) recorderFail();

    }


    private boolean dostart() {
        try {
            //记录开始录音时间
            startRecorderTime = System.currentTimeMillis();
            //创建录音文件
            mAudioRecordFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() +
                                                "/recorderdemo/" + System.currentTimeMillis() + ".pcm");
            if (!mAudioRecordFile.getParentFile().exists())
                mAudioRecordFile.getParentFile().mkdirs();
            mAudioRecordFile.createNewFile();
            //创建文件输出流
            mFileOutputStream = new FileOutputStream(mAudioRecordFile);
            //配置AudioRecord
            int audioSource = MediaRecorder.AudioSource.MIC;
            //所有android系统都支持
            int sampleRate = 44100;
            //单声道输入
            int channelConfig = AudioFormat.CHANNEL_IN_MONO;
            //PCM_16是所有android系统都支持的
            int autioFormat = AudioFormat.ENCODING_PCM_16BIT;
            //计算AudioRecord内部buffer最小
            int minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, autioFormat);
            //buffer不能小于最低要求，也不能小于我们每次我们读取的大小。
            mAudioRecord = new AudioRecord(audioSource, sampleRate, channelConfig, autioFormat, Math.max(minBufferSize, BUFFER_SIZE));


            //开始录音
            mAudioRecord.startRecording();
            //循环读取数据，写入输出流中
            while (mIsRecording) {
                //只要还在录音就一直读取
                int read = mAudioRecord.read(mBuffer, 0, BUFFER_SIZE);
                if(read<=0){
                    return false;
                }else {
                    mFileOutputStream.write(mBuffer, 0, read);
                }

            }

            //退出循环，停止录音，释放资源
            stopRecorder();

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (mAudioRecord != null) {
                mAudioRecord.release();
            }
        }
        return true;
    }

    private boolean recorderFail() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                bt_stream_recorder.setText("开始录音");
                tv_stream_msg.setText("录取失败，请重新录入");

                mIsRecording=false;
                Log.i("Tag8", "go here111111111");
            }
        });

        return false;
    }

    private void realeseRecorder() {
        mAudioRecord.release();
    }

    private boolean doStop() {
        //停止录音，关闭文件输出流
        mAudioRecord.stop();
        mAudioRecord.release();
        mAudioRecord = null;
        Log.i("Tag8", "go here");
        //记录结束时间，统计录音时长
        stopRecorderTime = System.currentTimeMillis();
        //大于3秒算成功，在主线程更新UI
        final int send = (int) (stopRecorderTime - startRecorderTime) / 1000;
        if (send > 3) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    tv_stream_msg.setText("录音成功：" + send + "秒");
                    bt_stream_recorder.setText("开始录音");
                    Log.i("Tag8", "go there");
                }
            });
        } else {
            recorderFail();
            return false;
        }
        return true;
    }

    /**
     * 播放声音
     * @param view
     */
    public void player(View view){
        mExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                if(!mIsPlaying){
                    Log.i("Tag8","go here");
                    mIsPlaying=true;
                    doPlay(mAudioRecordFile);
                }

            }
        });
    }

    private void doPlay(File audioFile) {
        if(audioFile !=null){
            Log.i("Tag8","go there");
            //配置播放器
            //音乐类型，扬声器播放
            int streamType= AudioManager.STREAM_MUSIC;
            //录音时采用的采样频率，所以播放时同样的采样频率
            int sampleRate=44100;
            //单声道，和录音时设置的一样
            int channelConfig= AudioFormat.CHANNEL_OUT_MONO;
            //录音时使用16bit，所以播放时同样采用该方式
            int audioFormat=AudioFormat.ENCODING_PCM_16BIT;
            //流模式
            int mode= AudioTrack.MODE_STREAM;

            //计算最小buffer大小
            int minBufferSize=AudioTrack.getMinBufferSize(sampleRate,channelConfig,audioFormat);

            //构造AudioTrack  不能小于AudioTrack的最低要求，也不能小于我们每次读的大小
            AudioTrack audioTrack=new AudioTrack(streamType,sampleRate,channelConfig,audioFormat,
                                                 Math.max(minBufferSize,BUFFER_SIZE),mode);

            //从文件流读数据
            FileInputStream inputStream=null;
            try{
                //循环读数据，写到播放器去播放
                inputStream=new FileInputStream(audioFile);

                //循环读数据，写到播放器去播放
                int read;
                //只要没读完，循环播放
                while ((read=inputStream.read(mBuffer))>0){
                    Log.i("Tag8","read:"+read);
                    int ret=audioTrack.write(mBuffer,0,read);
                    //检查write的返回值，处理错误
                    switch (ret){
                        case AudioTrack.ERROR_INVALID_OPERATION:
                        case AudioTrack.ERROR_BAD_VALUE:
                        case AudioManager.ERROR_DEAD_OBJECT:
                            playFail();
                            return;
                        default:
                            break;
                    }
                }

            }catch (Exception e){
                e.printStackTrace();
                //读取失败
                playFail();
            }finally {
                mIsPlaying=false;
                //关闭文件输入流
                if(inputStream !=null){
                    closeStream(inputStream);
                }
                //播放器释放
                resetQuietly(audioTrack);
            }

            //循环读数据，写到播放器去播放


            //错误处理，防止闪退

        }
    }

    /**
     * 关闭输入流
     * @param inputStream
     */
    private void closeStream(FileInputStream inputStream){
        try {
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void resetQuietly(AudioTrack audioTrack){
        try{
            audioTrack.stop();
            audioTrack.release();
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    /**
     * 播放失败
     */
    private void playFail() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                tv_stream_msg.setText("播放失败");
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mExecutorService != null) {
            mExecutorService.shutdownNow();
        }
        if (mAudioRecord != null) {
            mAudioRecord.stop();
            mAudioRecord.release();
            mAudioRecord = null;
        }
    }

}
