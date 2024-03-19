package com.wrlus.xposed.framework;

import android.util.Log;

import com.wrlus.xposed.hook.universal.HookVCam;

import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created by wrlu on 2023/2/2.
 */
public class HookRouter implements IXposedHookLoadPackage {
    private static final String TAG = "HookRouter";
    private static final List<Class<? extends HookInterface>> hookers = new ArrayList<>();

    static {
        hookers.add(HookVCam.class);
    }

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam loadPackageParam) {
        Log.d(TAG, "Load package = " + loadPackageParam.packageName +
                ", process = " + loadPackageParam.processName);
        for (Class<? extends HookInterface> hooker : hookers) {
            try {
                HookInterface hookInterface = hooker.newInstance();
                Log.d(TAG, "Load " +
                        hookInterface.getClass().getName());
                hookInterface.onHookPackage(loadPackageParam);
            } catch (ReflectiveOperationException e) {
                XposedBridge.log(e);
            }
        }
    }
}
