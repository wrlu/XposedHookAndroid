package com.wrlus.xposed.hook.custom;

import android.util.Log;

import com.wrlus.xposed.framework.HookInterface;
import com.wrlus.xposed.framework.MethodHook;

import java.lang.reflect.Field;
import java.util.HashMap;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HookCtrip implements HookInterface {
    private static final String TAG = "HookCtrip";

    @Override
    public void onHookPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        if (loadPackageParam.packageName.equals("ctrip.android.view")) {
            MethodHook buildHTTPRequestHook = new MethodHook.Builder("ctrip.android.httpv2.CTHTTPRequest", loadPackageParam.classLoader)
                    .setMethodName("buildHTTPRequest")
                    .addParameter(String.class)
                    .addParameter(Object.class)
                    .addParameter(Class.class)
                    .setCallback(new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) {
                            String path = (String) param.args[0];
                            Object bodyData = param.args[1];
                            if (bodyData instanceof HashMap) {
                                HashMap bodyDataMap = (HashMap) bodyData;
                                Log.e(TAG, "HTTPRequest path = " + path);
                                for (Object key : bodyDataMap.keySet()) {
                                    Object data = bodyDataMap.get(key);
                                    Log.e(TAG, "HTTPRequest body key = " + key + ", data = " + data);
                                }
                            }
                        }
                    }).build();
            MethodHook onResponseHook = new MethodHook.Builder("ctrip.android.view.myctrip.sender.orderInfo.MyCtripOrderSender$b", loadPackageParam.classLoader)
                    .setMethodName("onResponse")
                    .addParameter("ctrip.android.httpv2.CTHTTPResponse")
                    .setCallback(new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            Object cTHTTPResponse = param.args[0];
                            Class<?> httpRepClass = XposedHelpers.findClassIfExists("ctrip.android.httpv2.CTHTTPResponse", loadPackageParam.classLoader);
                            if (httpRepClass == null) {
                                Log.e(TAG, "httpRepClass == null");
                                return;
                            }
                            Field responseBeanField = XposedHelpers.findFieldIfExists(httpRepClass, "responseBean");
                            if (responseBeanField == null) {
                                Log.e(TAG, "responseBeanField == null");
                                return;
                            }
                            Object responseBean = responseBeanField.get(cTHTTPResponse);
                            Log.e(TAG, "HTTPResponse responseBean = " + responseBean);
                        }
                    }).build();
            MethodHook.normalInstall(buildHTTPRequestHook);
            MethodHook.normalInstall(onResponseHook);
        }
    }
}
