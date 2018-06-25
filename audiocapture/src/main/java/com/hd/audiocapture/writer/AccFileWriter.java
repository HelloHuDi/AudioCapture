package com.hd.audiocapture.writer;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Build;
import android.support.annotation.RequiresApi;

import com.hd.audiocapture.CaptureConfig;
import com.hd.audiocapture.callback.CaptureStreamCallback;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by hd on 2018/5/10 .
 */
public class AccFileWriter extends AudioFileWriter {

    private DataOutputStream mDataOutputStream;

    private CaptureConfig captureConfig;

    @Override
    public void init(DataOutputStream mDataOutputStream, CaptureConfig captureConfig) {
        this.mDataOutputStream = mDataOutputStream;
        this.captureConfig = captureConfig;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public boolean start() {
        if (mediaCodec != null) {
            return true;
        }
        String type = "audio/mp4a-latm";
        try {
            mediaCodec = MediaCodec.createEncoderByType(type);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        MediaFormat format = new MediaFormat();
        format.setString(MediaFormat.KEY_MIME, type);
        format.setInteger(MediaFormat.KEY_CHANNEL_COUNT, captureConfig.getChannelCount());
        format.setInteger(MediaFormat.KEY_SAMPLE_RATE, captureConfig.getSamplingRate());
        format.setInteger(MediaFormat.KEY_BIT_RATE, captureConfig.getBitrate());
        format.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
        bufferInfo = new MediaCodec.BufferInfo();
        mediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        mediaCodec.start();
        return true;
    }

    private MediaCodec mediaCodec = null;

    private ByteBuffer inputBuffer;
    private ByteBuffer outputBuffer;
    private int inputBufferIndex;
    private int outputBufferIndex;
    private MediaCodec.BufferInfo bufferInfo = null;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public boolean writeData(byte[] buffer, int offset, int count) {
        if (mediaCodec == null) {
            return false;
        }
        inputBufferIndex = mediaCodec.dequeueInputBuffer(-1);
        if (inputBufferIndex >= 0) {
            inputBuffer = mediaCodec.getInputBuffers()[inputBufferIndex];
            inputBuffer.clear();
            inputBuffer.put(buffer);
            mediaCodec.queueInputBuffer(inputBufferIndex, offset, count, 0, 0);
        }
        outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 0);
        while (outputBufferIndex >= 0) {
            //------------add acc header--------------
            int outBitsSize = bufferInfo.size;
            int outPacketSize = outBitsSize + 7; // 7 is ADTS size
            byte[] outData = new byte[outPacketSize];
            outputBuffer = mediaCodec.getOutputBuffers()[outputBufferIndex];
            outputBuffer.position(bufferInfo.offset);
            outputBuffer.limit(bufferInfo.offset + bufferInfo.size);
            addADTStoPacket(outData, outPacketSize, captureConfig.getSamplingRate(), captureConfig.getChannelCount());
            outputBuffer.get(outData, 7, outBitsSize);
            try {
                byte[] filterData = null;
                if (captureConfig.getCaptureCallback() != null && captureConfig.getCaptureCallback() instanceof CaptureStreamCallback) {
                    filterData = ((CaptureStreamCallback) captureConfig.getCaptureCallback()).filterContentByte(outData);
                }
                outData = filterData == null || filterData.length <= 0 ? outData : filterData;
                mDataOutputStream.write(outData);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mediaCodec.releaseOutputBuffer(outputBufferIndex, false);
            outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 0);
            if (captureConfig.getCaptureCallback() != null && captureConfig.getCaptureCallback() instanceof CaptureStreamCallback) {
                ((CaptureStreamCallback) captureConfig.getCaptureCallback()).captureContentByte(outData);
            }
        }
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public boolean stop() {
        if (mediaCodec != null) {
            mediaCodec.stop();
            mediaCodec.release();
            mediaCodec = null;
        }
        return true;
    }

    /**
     * add acc file header
     * Add ADTS header at the beginning of each and every AAC packet. This is
     * needed as MediaCodec encoder generates a packet of raw AAC data.
     * Note the packetLen must count in the ADTS header itself.
     **/
    private void addADTStoPacket(byte[] packet, int packetLen, int sampleInHz, int chanCfgCounts) {
        int profile = 2; // AAC LC
        int freqIdx = 8; // 16KHz    39=MediaCodecInfo.CodecProfileLevel.AACObjectELD;
        switch (sampleInHz) {
            case 96000:
                freqIdx = 0;
                break;
            case 88200:
                freqIdx = 1;
                break;
            case 64000:
                freqIdx = 2;
                break;
            case 48000:
                freqIdx = 3;
                break;
            case 44100:
                freqIdx = 4;
                break;
            case 32000:
                freqIdx = 5;
                break;
            case 24000:
                freqIdx = 6;
                break;
            case 22050:
                freqIdx = 7;
                break;
            case 16000:
                freqIdx = 8;
                break;
            case 2000:
                freqIdx = 9;
                break;
            case 11025:
                freqIdx = 10;
                break;
            case 8000:
                freqIdx = 11;
                break;
            default://reserved : 12,13,14,15
                break;
        }
        // fill in ADTS data
        packet[0] = (byte) 0xFF;
        packet[1] = (byte) 0xF9;
        packet[2] = (byte) (((profile - 1) << 6) + (freqIdx << 2) + (chanCfgCounts >> 2));
        packet[3] = (byte) (((chanCfgCounts & 3) << 6) + (packetLen >> 11));
        packet[4] = (byte) ((packetLen & 0x7FF) >> 3);
        packet[5] = (byte) (((packetLen & 7) << 5) + 0x1F);
        packet[6] = (byte) 0xFC;
    }
}
