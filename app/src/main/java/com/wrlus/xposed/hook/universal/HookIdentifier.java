package com.wrlus.xposed.hook.universal;

import android.content.ContentResolver;
import android.provider.Settings;

import com.wrlus.xposed.framework.HookInterface;
import com.wrlus.xposed.framework.MethodHook;
import com.wrlus.xposed.framework.PrintCallback;

import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HookIdentifier implements HookInterface {
    private static final String TAG = "HookIdentifier";

    @Override
    public void onHookPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        MethodHook settingsSecureGetStringHook = new MethodHook.Builder(Settings.Secure.class)
                .setMethodName("getString")
                .addParameter(ContentResolver.class)
                .addParameter(String.class)
                .setCallback(new PrintCallback.Builder()
                        .setHookTime(PrintCallback.HookTime.BEFORE)
                        .addParameterPrint("contentResolver")
                        .addParameterPrint("key")
                        .setTag(TAG)
                        .build())
                .build();
        MethodHook.normalInstall(settingsSecureGetStringHook);
    }
}
