package com.wrlus.xposed.hook.universal;

import android.os.Binder;
import android.os.ResultReceiver;
import android.util.Log;

import com.wrlus.xposed.framework.HookInterface;

import java.io.FileDescriptor;
import java.util.Arrays;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HookShellCommand implements HookInterface {
    private static final String TAG = "HookShellCommand";

    @Override
    public void onHookPackage(final XC_LoadPackage.LoadPackageParam loadPackageParam) {
        final String packageName = loadPackageParam.packageName;
        if (packageName.equals("android")) {
            hootShellCommands(loadPackageParam.classLoader);
        }
    }

    public static void hootShellCommands(ClassLoader classLoader) {
        XposedHelpers.findAndHookMethod("android.os.ShellCommand",
                classLoader, "exec",
                Binder.class, FileDescriptor.class, FileDescriptor.class, FileDescriptor.class,
                String[].class, "android.os.ShellCallback", ResultReceiver.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        String[] args = (String[]) param.args[4];
                        try {
                            throw new Exception("stack");
                        } catch (Exception e) {
                            String className = getSourceClass(e);
                            Log.i(TAG,  "ShellCommand.exec: " + Arrays.asList(args) +
                                    ", className: " + className +
                                    ", callingUid: " + Binder.getCallingUid() +
                                    ", callingPid: " + Binder.getCallingPid());
                        }
                    }
                });
    }

    private static String getSourceClass(Exception e) {
        StackTraceElement[] stackTraces = e.getStackTrace();
        String className = "unknown";
        for (StackTraceElement trace : stackTraces) {
            if (trace.getMethodName().contains("onTransact") &&
                    !trace.getClassName().contains(Binder.class.getSimpleName())) {
                className = trace.getClassName();
            }
        }
        return className;
    }
}
