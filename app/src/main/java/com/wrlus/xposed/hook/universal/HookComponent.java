package com.wrlus.xposed.hook.universal;

import android.content.ClipData;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.wrlus.xposed.framework.HookInterface;

import java.util.Arrays;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HookComponent implements HookInterface {
    private static final String TAG = "HookComp";

    @Override
    public void onHookPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        hookStartActivity(loadPackageParam.classLoader);
    }

    public void hookStartActivity(ClassLoader classLoader) {
        XposedHelpers.findAndHookMethod("android.app.ContextImpl", classLoader,
                "startActivity", android.content.Intent.class, android.os.Bundle.class,
                new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                Intent intent = (Intent) param.args[0];
                Bundle bundle = (Bundle) param.args[1];
                logIntent(intent, "startActivity", TAG);
                logBundle(bundle, false, TAG);
            }
        });
    }

    public static void logIntent(Intent intent, String operation, String tag) {
        Log.i(tag, operation);
        if (intent != null) {
            Log.i(tag, "[Intent] " + intent);
            if (intent.getData() != null && !intent.getDataString().isEmpty()) {
                Log.i(tag, "[Data] " + intent.getDataString());
            }
            if (intent.getClipData() != null) {
                ClipData clipData = intent.getClipData();
                for (int i = 0; i < clipData.getItemCount(); ++i) {
                    ClipData.Item item = clipData.getItemAt(i);
                    Log.i(tag, "[ClipData] " + item.getText());
                }
            }
            Bundle extras = intent.getExtras();
            logBundle(extras, true, tag);
        } else {
            Log.e(tag, "intent == null");
        }
    }

    public static void logBundle(Bundle bundle, boolean isExtra, String tag) {
        if (bundle != null) {
            String prefix = isExtra ? "Extra" : "Bundle";
            for (String key : bundle.keySet()) {
                Object value = bundle.get(key);
                if (value != null) {
                    // Another bundle object
                    Class<?> clazz = value.getClass();
                    if (clazz.getName().equals(Bundle.class.getName())) {
                        logBundle((Bundle) value, false, tag);
                    } else if (clazz.isArray()){
                        Class<?> arrayCompType = clazz.getComponentType();
                        if (arrayCompType != null && !arrayCompType.isPrimitive()) {
                            List<Object> valueList = Arrays.asList((Object[]) value);
                            Log.i(tag, "[" + prefix + "] " +
                                    key + " `" + clazz.getName() + "`: " +
                                    logValue(valueList));
                        } else {
                            Log.i(tag, "[" + prefix + "] " +
                                    key + " `" + clazz.getName() + "`: " +
                                    logValue(value));
                        }

                    } else {
                        Log.i(tag, "[" + prefix + "] " +
                                key + " `" + clazz.getName() + "`: " +
                                logValue(value));
                    }
                } else {
                    Log.i(tag, "[" + prefix + "] " +
                            key + ": null");
                }
            }
        }
    }

    public static String logValue(Object value) {
        return value.toString();
    }
}
