package com.hd.audiorecorderlearn;

import android.support.annotation.NonNull;

import java.io.File;

/**
 * Created by hd on 2018/5/8 .
 */
public interface AudioCallback {

    void audioPath(@NonNull File file);

}
