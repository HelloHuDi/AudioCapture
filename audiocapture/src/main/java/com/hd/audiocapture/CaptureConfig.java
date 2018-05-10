package com.hd.audiocapture;

import com.hd.audiocapture.callback.CaptureCallback;

import java.io.File;

/**
 * Created by hd on 2018/5/10 .
 */
public class CaptureConfig {
    private String mode = CaptureType.AAC_FORMAT;
    private String name;
    private File file;
    private int samplingRate = 44100;
    private int channelCount = 1;
    private int bitrate = 9600;
    private CaptureCallback captureCallback;

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public int getSamplingRate() {
        return samplingRate;
    }

    public void setSamplingRate(int samplingRate) {
        this.samplingRate = samplingRate;
    }

    public int getChannelCount() {
        return channelCount;
    }

    public void setChannelCount(int channelCount) {
        this.channelCount = channelCount;
    }

    public int getBitrate() {
        return bitrate;
    }

    public void setBitrate(int bitrate) {
        this.bitrate = bitrate;
    }

    public CaptureCallback getCaptureCallback() {
        return captureCallback;
    }

    public void setCaptureCallback(CaptureCallback captureCallback) {
        this.captureCallback = captureCallback;
    }

}
