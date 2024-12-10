package com.wrlus.xposed.hook.custom;

import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.media.Image;

import com.wrlus.xposed.framework.HookInterface;
import com.wrlus.xposed.framework.MethodHook;
import com.wrlus.xposed.framework.PrintCallback;

import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HookHwDMSDP implements HookInterface {
    private static final String TAG = "HookHwDMSDP";
    @Override
    public void onHookPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        if (loadPackageParam.packageName.equals("com.huawei.dmsdpdevice")) {
            startHook(loadPackageParam.classLoader);
        }
    }

    private void startHook(ClassLoader classLoader) {
        MethodHook hook = new MethodHook.Builder(
                "com.huawei.dmsdpdevice.camera.libdmsdpcamera.androidcamera.AndroidCamera2Manager", classLoader)
                .setMethodName("processCameraImage")
                .addParameter(Image.class)
                .addParameter(int.class)
                .addParameter("com.huawei.dmsdpdevice.camera.libdmsdpcamera.util.CameraRotationPara")
                .setCallback(new PrintCallback.Builder()
                        .setHookTime(PrintCallback.HookTime.BEFORE)
                        .setTag(TAG)
                        .addParameterPrint("image")
                        .addParameterPrint("i")
                        .addParameterPrint("para")
                        .build())
                .build();
        MethodHook.normalInstall(hook);
    }
}
