package com.wrlus.xposed.framework;

import android.util.Log;

import com.wrlus.xposed.hook.custom.ByteHookAweme;
import com.wrlus.xposed.hook.priv.ByteHookBridgeSdk;
import com.wrlus.xposed.hook.priv.ByteHookIesJsBridge;
import com.wrlus.xposed.hook.priv.ByteHookJsBridge2;
import com.wrlus.xposed.hook.priv.ByteHookXBridge2;
import com.wrlus.xposed.hook.priv.ByteHookXBridge3;
import com.wrlus.xposed.hook.universal.HookWebView;

import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created by wrlu on 2023/2/2.
 */
public class HookRouter implements IXposedHookLoadPackage {
    private static final String TAG = "HookRouter";
    private static final List<Class<? extends HookInterface>> hookers = new ArrayList<>();

    static {
//        hookers.add(ByteHookAweme.class);
        hookers.add(HookWebView.class);
    }

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam loadPackageParam) {
        Log.d(TAG, "Load package = " + loadPackageParam.packageName +
                ", process = " + loadPackageParam.processName);
        for (Class<? extends HookInterface> hooker : hookers) {
            try {
                HookInterface hookInterface = hooker.newInstance();
                Log.d(TAG, "Load " +
                        hookInterface.getClass().getCanonicalName());
                hookInterface.onHookPackage(loadPackageParam);
            } catch (ReflectiveOperationException e) {
                e.printStackTrace();
            }
        }
    }
}