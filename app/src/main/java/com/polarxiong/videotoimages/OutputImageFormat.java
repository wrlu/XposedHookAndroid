package com.polarxiong.videotoimages;

import androidx.annotation.NonNull;

/**
 * Created by zhantong on 16/9/8.
 */
public enum OutputImageFormat {
    I420("I420"),
    NV21("NV21"),
    JPEG("JPEG");
    private final String friendlyName;

    OutputImageFormat(String friendlyName) {
        this.friendlyName = friendlyName;
    }

    @NonNull
    public String toString() {
        return friendlyName;
    }
}