package com.hd.audiocapture;

import android.media.AudioFormat;
import android.media.MediaRecorder;

import com.hd.audiocapture.callback.CaptureCallback;

import java.io.File;

/**
 * Created by hd on 2018/5/10 .
 */
public class CaptureConfig {
    private String mode = CaptureType.AAC_FORMAT;
    private boolean log= false;
    private String name;
    private File file;
    private int audioSource = MediaRecorder.AudioSource.MIC;
    private int samplingRate = 44100;
    private int channelCount = 1;
    private int bitrate = 64*1024;
    private int audioFormat=AudioFormat.ENCODING_PCM_16BIT;
    private CaptureCallback captureCallback;

    @Override
    public String toString() {
        return "CaptureConfig{" + "mode='" + mode + '\'' + ", name='" + name + '\'' + ", file=" + file + ", samplingRate=" + samplingRate + ", channelCount=" + channelCount + ", bitrate=" + bitrate + ", captureCallback=" + captureCallback + '}';
    }

    public boolean allowLog() {
        return log;
    }

    public void setLog(boolean log) {
        this.log = log;
    }

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

    public int getAudioSource() {
        return audioSource;
    }

    public void setAudioSource(int audioSource) {
        this.audioSource = audioSource;
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

    public int getAudioFormat() {
        return audioFormat;
    }

    public void setAudioFormat(int audioFormat) {
        this.audioFormat = audioFormat;
    }

    public CaptureCallback getCaptureCallback() {
        return captureCallback;
    }

    public void setCaptureCallback(CaptureCallback captureCallback) {
        this.captureCallback = captureCallback;
    }

    public static class Builder{

        private CaptureConfig config;

        public Builder() {
            config=new CaptureConfig();
        }

        public Builder setLog(boolean log){
            config.setLog(log);
            return this;
        }

        public Builder setMode(String mode) {
            config.setMode(mode);
            return this;
        }

        public Builder setName(String name){
            config.setName(name);
            return this;
        }

        public Builder setFile(File file){
            config.setFile(file);
            return this;
        }

        public Builder setAudioSource(int audioSource){
            config.setAudioSource(audioSource);
            return this;
        }

        public Builder setSamplingRate(int samplingRate){
            config.setSamplingRate(samplingRate);
            return this;
        }

        public Builder setChannelCount(int channelCount){
            config.setChannelCount(channelCount);
            return this;
        }

        public Builder setBitrate(int bitrate){
            config.setBitrate(bitrate);
            return this;
        }

        public Builder setAudioFormat(int audioFormat){
            config.setAudioFormat(audioFormat);
            return this;
        }

        public Builder setCaptureCallback(CaptureCallback captureCallback){
            config.setCaptureCallback(captureCallback);
            return this;
        }

        public CaptureConfig build(){
            return config;
        }
    }

}
