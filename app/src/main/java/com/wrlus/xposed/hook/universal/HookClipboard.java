package com.wrlus.xposed.hook.universal;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;

import com.wrlus.xposed.framework.HookInterface;
import com.wrlus.xposed.framework.MethodHook;
import com.wrlus.xposed.framework.PrintCallback;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HookClipboard implements HookInterface {
    private static final String TAG = "HookClip";
    @Override
    public void onHookPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        final String packageName = loadPackageParam.packageName;
        if (!packageName.equals("android")) {
            hookClip(loadPackageParam.classLoader);
        }
    }

    private static void hookClip(ClassLoader classLoader) {
        MethodHook getPrimClipHook = new MethodHook.Builder(ClipboardManager.class)
                .setMethodName("getPrimaryClip")
                .setCallback(new PrintCallback.Builder()
                        .setHookTime(PrintCallback.HookTime.AFTER)
                        .setTag(TAG)
                        .build())
                .build();
        MethodHook getPrimClipDescHook = new MethodHook.Builder(ClipboardManager.class)
                .setMethodName("getPrimaryClipDescription")
                .setCallback(new PrintCallback.Builder()
                        .setHookTime(PrintCallback.HookTime.AFTER)
                        .setTag(TAG)
                        .build())
                .build();
        MethodHook addListenerHook = new MethodHook.Builder(ClipboardManager.class)
                .setMethodName("addPrimaryClipChangedListener")
                .addParameter(ClipboardManager.OnPrimaryClipChangedListener.class)
                .setCallback(new XC_MethodHook() {
                    final PrintCallback printCallback = new PrintCallback.Builder()
                            .setHookTime(PrintCallback.HookTime.BEFORE)
                            .addParameterPrint("listener")
                            .setTag(TAG).build();

                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        printCallback.onMethodHooked(param, false);
                        ClipboardManager.OnPrimaryClipChangedListener listener =
                                (ClipboardManager.OnPrimaryClipChangedListener) param.args[0];

                        MethodHook onPrimClipChangedHook = new MethodHook.Builder(
                                listener.getClass().getCanonicalName(), classLoader)
                                .setMethodName("onPrimaryClipChanged")
                                .setCallback(new PrintCallback.Builder()
                                        .setHookTime(PrintCallback.HookTime.BEFORE)
                                        .setTag(TAG)
                                        .build())
                                .build();
                        MethodHook.normalInstall(onPrimClipChangedHook);
                    }
                })
                .build();
        MethodHook getDescHook = new MethodHook.Builder(ClipData.class)
                .setMethodName("getDescription")
                .setCallback(new PrintCallback.Builder()
                        .setHookTime(PrintCallback.HookTime.AFTER)
                        .setTag(TAG)
                        .build())
                .build();
        MethodHook getConfidenceScoreHook = new MethodHook.Builder(ClipDescription.class)
                .setMethodName("getConfidenceScore")
                .addParameter(String.class)
                .setCallback(new PrintCallback.Builder()
                        .setHookTime(PrintCallback.HookTime.AFTER)
                        .addParameterPrint("type")
                        .setTag(TAG)
                        .build())
                .build();

        MethodHook.normalInstall(getPrimClipHook,
                getPrimClipDescHook,
                addListenerHook,
                getDescHook,
                getConfidenceScoreHook);
    }
}
