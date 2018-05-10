package com.hd.audiocapture.writer;

import com.hd.audiocapture.CaptureConfig;

import java.io.DataOutputStream;

/**
 * Created by hd on 2018/5/10 .
 */
public abstract class AudioFileWriter {

    public abstract void init(DataOutputStream mDataOutputStream,CaptureConfig captureConfig);

    public abstract boolean start();

    public abstract boolean writeData(byte[] buffer, int offset, int count);

    public abstract boolean stop();
}
