package com.wrlus.xposed.hook.custom;

import com.wrlus.xposed.framework.HookInterface;
import com.wrlus.xposed.framework.MethodHook;
import com.wrlus.xposed.framework.PrintCallback;

import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HookBaiduInput implements HookInterface {
    private static final String TAG = "HookBaiduInput";
    @Override
    public void onHookPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        if (loadPackageParam.packageName.equals("com.baidu.input")) {
            MethodHook commitTextHook = new MethodHook.Builder("com.baidu.dgg", loadPackageParam.classLoader)
                    .setMethodName("commitText")
                    .addParameter(CharSequence.class)
                    .addParameter(int.class)
                    .setCallback(new PrintCallback.Builder()
                            .setHookTime(PrintCallback.HookTime.BEFORE)
                            .addParameterPrint("char")
                            .addParameterPrint("pos")
                            .setTag(TAG)
                            .build())
                    .build();
            MethodHook.normalInstall(commitTextHook);
        }
    }
}
