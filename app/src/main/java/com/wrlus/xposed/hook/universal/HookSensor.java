package com.wrlus.xposed.hook.universal;

import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.os.Handler;

import com.wrlus.xposed.framework.HookInterface;
import com.wrlus.xposed.framework.MethodHook;
import com.wrlus.xposed.framework.PrintCallback;

import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HookSensor implements HookInterface {
    private static final String TAG = "HookSensor";

    @Override
    public void onHookPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        hookSensor(loadPackageParam.classLoader);
    }

    private void hookSensor(ClassLoader classLoader) {
        MethodHook oldRegister = new MethodHook.Builder(
                "android.hardware.SensorManager", classLoader)
                .setMethodName("registerListener")
                .addParameter(SensorListener.class)
                .addParameter(int.class)
                .addParameter(int.class)
                .setCallback(new PrintCallback.Builder()
                        .setHookTime(PrintCallback.HookTime.BEFORE)
                        .setTag(TAG)
                        .addParameterPrint("listener", PrintCallback.PrintPolicy.CLASSNAME)
                        .addParameterPrint("sensor")
                        .addParameterPrint("period")
                        .build())
                .build();
        MethodHook newRegister1 = new MethodHook.Builder(
                "android.hardware.SensorManager", classLoader)
                .setMethodName("registerListener")
                .addParameter(SensorEventListener.class)
                .addParameter(Sensor.class)
                .addParameter(int.class)
                .addParameter(int.class)
                .setCallback(new PrintCallback.Builder()
                        .setHookTime(PrintCallback.HookTime.BEFORE)
                        .setTag(TAG)
                        .addParameterPrint("listener", PrintCallback.PrintPolicy.CLASSNAME)
                        .addParameterPrint("sensor")
                        .addParameterPrint("samplingPeriodUs")
                        .addParameterPrint("maxReportLatencyUs")
                        .build())
                .build();
        MethodHook newRegister2 = new MethodHook.Builder(
                "android.hardware.SensorManager", classLoader)
                .setMethodName("registerListener")
                .addParameter(SensorEventListener.class)
                .addParameter(Sensor.class)
                .addParameter(int.class)
                .addParameter(Handler.class)
                .setCallback(new PrintCallback.Builder()
                        .setHookTime(PrintCallback.HookTime.BEFORE)
                        .setTag(TAG)
                        .addParameterPrint("listener", PrintCallback.PrintPolicy.CLASSNAME)
                        .addParameterPrint("sensor")
                        .addParameterPrint("samplingPeriodUs")
                        .addParameterPrint("handler", PrintCallback.PrintPolicy.CLASSNAME)
                        .build())
                .build();
        MethodHook newRegister3 = new MethodHook.Builder(
                "android.hardware.SensorManager", classLoader)
                .setMethodName("registerListener")
                .addParameter(SensorEventListener.class)
                .addParameter(Sensor.class)
                .addParameter(int.class)
                .addParameter(int.class)
                .addParameter(Handler.class)
                .setCallback(new PrintCallback.Builder()
                        .setHookTime(PrintCallback.HookTime.BEFORE)
                        .setTag(TAG)
                        .addParameterPrint("listener", PrintCallback.PrintPolicy.CLASSNAME)
                        .addParameterPrint("sensor")
                        .addParameterPrint("samplingPeriodUs")
                        .addParameterPrint("maxReportLatencyUs")
                        .addParameterPrint("handler", PrintCallback.PrintPolicy.CLASSNAME)
                        .build())
                .build();
        MethodHook.normalInstall(oldRegister, newRegister1, newRegister2, newRegister3);
    }
}
