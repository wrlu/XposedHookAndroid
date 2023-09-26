package com.wrlus.xposed.framework;

import de.robv.android.xposed.callbacks.XC_LoadPackage;

public interface HookInterface {
    public void onHookPackage(XC_LoadPackage.LoadPackageParam loadPackageParam);
}
