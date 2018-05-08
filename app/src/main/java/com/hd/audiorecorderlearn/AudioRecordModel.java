package com.hd.audiorecorderlearn;

import android.annotation.SuppressLint;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaRecorder;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by hd on 2018/5/8 .
 */
public class AudioRecordModel extends AudioModel {

    public class AudioData {
        public ByteBuffer buffer; //存储原始音频数据的buffer
        public int size; //buffer大小
    }

    private int mBufferSizeInBytes = 0;//最小缓冲区
    private int mSampleRateInHz;//采样率
    private int mChannelConfig;//通道数
    private int mBitSize;//量化位数

    private AudioRecord mAudioRecord = null;   //录音器
    private MediaCodec mediaCodec = null;      //编码器

    private ByteBuffer[] inputBuffers;   //可用编码的bytebuffer的数组
    private ByteBuffer[] outputBuffers;  //编码完成的bytebuffer的数组
    private ByteBuffer inputBuffer;
    private ByteBuffer outputBuffer;
    private int inputBufferIndex;        //可用的编码的bytebuffer的数组的索引
    private int outputBufferIndex;       //可用的编码完成的bytebuffer的数组的索引
    private MediaCodec.BufferInfo bufferInfo = null;//存储的信息

    private FileOutputStream ou = null;

    private volatile AtomicBoolean record=new AtomicBoolean(false);

    public AudioRecordModel(AudioCallback callback) {
        super(callback,"_audioRecord");
    }

    @Override
    void startRecord() {
        int result = initialize(16000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        if (result == 0) {//初始化成功
            Recording();
        }
    }

    @Override
    void stopRecord() {
        record.set(false);
        if (mAudioRecord != null)
            mAudioRecord.stop();
        if (mediaCodec != null)
            mediaCodec.stop();
        mBufferSizeInBytes = 0;
        Log.d("tag","AudioRecordModel stop record");
    }

    @Override
    void release() {
        record.set(false);
        if (mAudioRecord != null) {
            mAudioRecord.release();
            mAudioRecord = null;
        }
        if (mediaCodec != null) {
            mediaCodec.release();
            mediaCodec = null;
        }
        mBufferSizeInBytes = 0;
        Log.d("tag","AudioRecordModel release");

    }

    //初始化音频采集参数
    private int initialize(int sampleRate, int channelCount, int bitSize) {
        //当然这里写的还是有所欠缺，需要对这些参数进行合法性的判断
        mSampleRateInHz = sampleRate;
        mChannelConfig = channelCount;
        mBitSize = bitSize;
        mBufferSizeInBytes = AudioRecord.getMinBufferSize(mSampleRateInHz, mChannelConfig, mBitSize);
        if (mBufferSizeInBytes == AudioRecord.ERROR_BAD_VALUE) {
            return -1;
        }
        return 0;
    }

    //录音以及编码
    private void Recording() {
        record.set(true);
        File file = null;
        int result = startRecorder();//开始录音

        if (result == 0) {
            file = createAudioFile();
        }
        if (file != null) {
            try {
                ou = new FileOutputStream(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Log.e("tag", "创建输出流出错");
            }
        }

        int result1 = createEncoder(); //创建编码器

        if (result1 == 0) {
            Log.e("tag", "创建编码器成功");
        }

        AudioData data = new AudioData();

        while (record.get()) {
            int result2 = readData(data);
            if (result2 == 0) {
                encode(data);
                Log.e("tag", "录音成功");
            }
        }

        stopRecord();
        release();

        if (ou != null) {
            try {
                ou.close();
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("tag", "关闭输出流出错");
            }
        }
    }

    //开始录音
    private int startRecorder() {
        //说明其未进行初始化
        if (mBufferSizeInBytes == 0) {
            return -1;
        }
        //防止多次点击重复创建AudioRecord
        if (mAudioRecord != null) {
            return 0;
        }
        mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, mSampleRateInHz, mChannelConfig, mBitSize, mBufferSizeInBytes);
        mAudioRecord.startRecording();
        return 0;
    }


    //读取音频数据（原始音频数据）
    private int readData(AudioData data) {

        if (mAudioRecord == null) {//检查是否初始化
            return -1;
        }

        if (data == null) {
            return -1;
        }

        //开辟大小为640字节的byteBuffer
        if (data.buffer == null) {
            data.buffer = ByteBuffer.allocateDirect(640);
        } else {
            if (data.buffer.capacity() < 640) {
                data.buffer = ByteBuffer.allocate(640);
            }
        }

        //把音频读取到data.buffer中，期望读取640个byte,返回值表示实际读取多个byte
        data.size = mAudioRecord.read(data.buffer, 640);

        if (data.size == AudioRecord.ERROR_BAD_VALUE) {//AudioRecord对象参数不可用
            return -1;
        }
        return 0;
    }

    //创建编码器
    @SuppressLint("NewApi")
    private int createEncoder() {
        //防止重复创建编码器
        if (mediaCodec != null) {
            return 0;
        }

        String type = "audio/mp4a-latm";

        try {
            mediaCodec = MediaCodec.createEncoderByType(type);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }

        // AAC 硬编码器
        MediaFormat format = new MediaFormat();
        format.setString(MediaFormat.KEY_MIME, type);
        format.setInteger(MediaFormat.KEY_CHANNEL_COUNT, 1); //声道数（这里是数字）
        format.setInteger(MediaFormat.KEY_SAMPLE_RATE, mSampleRateInHz); //采样率
        format.setInteger(MediaFormat.KEY_BIT_RATE, 9600); //码率
        format.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);

        bufferInfo = new MediaCodec.BufferInfo();//记录编码完成的buffer的信息
        mediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);// MediaCodec.CONFIGURE_FLAG_ENCODE 标识为编码器
        mediaCodec.start();
        return 0;
    }

    //编码
    @SuppressLint("NewApi")
    private int encode(AudioData result) {
        if (mediaCodec == null) {
            return -1;
        }

        //把数据拷贝到byte数组中
        byte[] data = new byte[result.size];
        result.buffer.get(data);
        result.buffer.flip();

        inputBuffers = mediaCodec.getInputBuffers();
        outputBuffers = mediaCodec.getOutputBuffers();

        //  <0一直等待可用的byteBuffer 索引;=0 马上返回索引 ;>0 等待相应的毫秒数返回索引
        inputBufferIndex = mediaCodec.dequeueInputBuffer(-1); //一直等待（阻塞）
        if (inputBufferIndex >= 0) {  //拿到可用的buffer索引了
            inputBuffer = inputBuffers[inputBufferIndex];
            inputBuffer.clear();
            inputBuffer.put(data);
            mediaCodec.queueInputBuffer(inputBufferIndex, 0, result.size, 0, 0); //投放到编码队列里去
        }

        //获取已经编码成的buffer的索引  0表示马上获取 ，>0表示最多等待多少毫秒获取
        outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 0);

        while (outputBufferIndex >= 0) {
            //------------添加头信息--------------
            int outBitsSize = bufferInfo.size;
            int outPacketSize = outBitsSize + 7; // 7 is ADTS size
            byte[] outData = new byte[outPacketSize];

            outputBuffer = outputBuffers[outputBufferIndex];
            outputBuffer.position(bufferInfo.offset);
            outputBuffer.limit(bufferInfo.offset + bufferInfo.size);

            addADTStoPacket(outData, outPacketSize, mSampleRateInHz, 1);//添加头
            outputBuffer.get(outData, 7, outBitsSize);

            try {
                ou.write(outData);
            } catch (IOException e) {
                e.printStackTrace();
            }

            mediaCodec.releaseOutputBuffer(outputBufferIndex, false);
            outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 0);
        }

        return 0;
    }

    /**
     * 添加头部信息
     * Add ADTS header at the beginning of each and every AAC packet. This is
     * needed as MediaCodec encoder generates a packet of raw AAC data.
     * Note the packetLen must count in the ADTS header itself.
     * packet 数据
     * packetLen 数据长度
     * sampleInHz 采样率
     * chanCfgCounts 通道数
     **/
    private void addADTStoPacket(byte[] packet, int packetLen, int sampleInHz, int chanCfgCounts) {
        int profile = 2; // AAC LC
        int freqIdx = 8; // 16KHz    39=MediaCodecInfo.CodecProfileLevel.AACObjectELD;

        switch (sampleInHz) {
            case 8000: {
                freqIdx = 11;
                break;
            }
            case 16000: {
                freqIdx = 8;
                break;
            }
            default:
                break;
        }
        int chanCfg = chanCfgCounts; // CPE
        // fill in ADTS data
        packet[0] = (byte) 0xFF;
        packet[1] = (byte) 0xF9;
        packet[2] = (byte) (((profile - 1) << 6) + (freqIdx << 2) + (chanCfg >> 2));
        packet[3] = (byte) (((chanCfg & 3) << 6) + (packetLen >> 11));
        packet[4] = (byte) ((packetLen & 0x7FF) >> 3);
        packet[5] = (byte) (((packetLen & 7) << 5) + 0x1F);
        packet[6] = (byte) 0xFC;

    }

}
