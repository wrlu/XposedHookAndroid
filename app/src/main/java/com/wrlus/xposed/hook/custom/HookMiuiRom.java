package com.wrlus.xposed.hook.custom;

import android.util.Log;

import com.wrlus.xposed.framework.HookInterface;

import java.lang.reflect.Field;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HookMiuiRom implements HookInterface {
    private static final String TAG = "HookMiui";
    @Override
    public void onHookPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        if (loadPackageParam.packageName.equals("android") || loadPackageParam.packageName.equals("com.miui.rom")) {
            hookMiui(loadPackageParam.classLoader);
        }
    }

    public void hookMiui(ClassLoader classLoader) {
        XposedHelpers.findAndHookMethod("miui.security.WakePathChecker", classLoader,
                "checkAllowStartActivity",
                String.class, String.class, int.class, int.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                try {
                    Class<?> clazzWakePathChecker =
                            Class.forName("miui.security.WakePathChecker", false, classLoader);
                    Field mLauncherPackageNamesField =
                            clazzWakePathChecker.getDeclaredField("mLauncherPackageNames");
                    mLauncherPackageNamesField.setAccessible(true);
                    List<String> mLauncherPackageNames = (List<String>)
                            mLauncherPackageNamesField.get(param.thisObject);
                    Log.i(TAG, "mLauncherPackageNames: " + mLauncherPackageNames);
                } catch (ReflectiveOperationException e) {
                    Log.e(TAG, "ReflectiveOperationException", e);
                }
            }
        });
    }
}
