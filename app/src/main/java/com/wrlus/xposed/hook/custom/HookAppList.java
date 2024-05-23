package com.wrlus.xposed.hook.custom;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.os.Binder;
import android.os.ResultReceiver;
import android.util.Log;

import com.wrlus.xposed.framework.HookInterface;

import java.io.FileDescriptor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created by wrlu on 2023/2/3.
 */
public class HookAppList implements HookInterface {
    private static final String TAG = "HookAppList";

    @Override
    public void onHookPackage(final XC_LoadPackage.LoadPackageParam loadPackageParam) {
        final String packageName = loadPackageParam.packageName;
        if ("com.google.android.webview".equals(packageName) ||
                "com.android.webview".equals(packageName) ||
                "com.huawei.webview".equals(packageName)) {
            return;
        }
        if (!packageName.equals("android")) {
            try {
                hookAOSPAppListInterface(packageName);
            } catch (Exception e) {
                Log.e(TAG, "hookAOSPAppListInterface", e);
            }
        }
    }

    public static void hookAOSPAppListInterface(final String packageName) {
        XposedHelpers.findAndHookMethod("android.content.pm.IPackageManager$Stub$Proxy",
                null, "getPackagesForUid",
                int.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        int uid = (int) param.args[0];
                        Object result = param.getResult();
                        if (result != null) {
                            String[] packageNames = (String[]) result;
                            if (!packageNames[0].equals(packageName)) {
                                Log.i(TAG, "getPackagesForUid, caller = " +
                                        packageName +
                                        ", uid = " + uid + ", pkg = " + packageNames[0]);
                            }
                        }
                    }
                });
        XposedHelpers.findAndHookMethod("android.content.pm.IPackageManager$Stub$Proxy",
                null, "getNameForUid",
                int.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        int uid = (int) param.args[0];
                        Object result = param.getResult();
                        if (result != null) {
                            String name = (String) result;
                            if (!name.equals(packageName)) {
                                Log.i(TAG, "getNameForUid, caller = " +
                                        packageName +
                                        ", uid = " + uid + ", name = " + name);
                            }
                        }
                    }
                });
        XposedHelpers.findAndHookMethod("android.content.pm.IPackageManager$Stub$Proxy",
                null, "getNamesForUids",
                int[].class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        int[] uid = (int[]) param.args[0];
                        Object result = param.getResult();
                        if (result != null) {
                            String[] names = (String[]) result;
                            for (int i = 0; i < names.length; ++i) {
                                if (!names[i].equals(packageName)) {
                                    Log.i(TAG, "getNamesForUids (item " + i + ")," +
                                            " caller = " + packageName +
                                            ", uid = " + uid[i] + ", name = " + names[i]);
                                }
                            }
                        }
                    }
                });
        XposedHelpers.findAndHookMethod("android.content.pm.IPackageManager$Stub$Proxy",
                null, "getInstalledPackages",
                long.class, int.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        Object result = param.getResult();
                        if (result != null) {
                            Log.i(TAG, "getInstalledPackages, caller = " +
                                    packageName +
                                    ", hook result null.");
                            Object newResult = Class.forName("android.content.pm.ParceledListSlice")
                                    .getDeclaredConstructor(List.class)
                                    .newInstance(new ArrayList<PackageInfo>());
                            param.setResult(newResult);
                        }
                    }
                });
        XposedHelpers.findAndHookMethod("android.content.pm.IPackageManager$Stub$Proxy",
                null, "getInstalledApplications",
                long.class, int.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        Object result = param.getResult();
                        if (result != null) {
                            Log.i(TAG, "getInstalledApplications, caller = " +
                                    packageName +
                                    ", hook result null.");
                            Object newResult = Class.forName("android.content.pm.ParceledListSlice")
                                    .getDeclaredConstructor(List.class)
                                    .newInstance(new ArrayList<ApplicationInfo>());
                            param.setResult(newResult);
                        }
                    }
                });
    }
}
