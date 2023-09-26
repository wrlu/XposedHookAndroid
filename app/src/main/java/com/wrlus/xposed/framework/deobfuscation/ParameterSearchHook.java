package com.wrlus.xposed.framework.deobfuscation;

import android.util.Log;

import com.wrlus.xposed.framework.MethodHook;
import com.wrlus.xposed.util.Utils;

import java.lang.reflect.Method;

/**
 * 未混淆的类名+未混淆的方法名
 * 需匹配混淆后的参数类型进行Hook
 */
public class ParameterSearchHook extends ClassSearchHook {
    private static final String TAG = "ParameterSearchHook";

    protected ParameterSearchHook(Builder builder) {
        super(builder);
    }

    public static class Builder extends ClassSearchHook.Builder {
        public ParameterSearchHook build() {
            return new ParameterSearchHook(this);
        }
    }
    @Override
    public boolean isMethodMatch(MethodHook originMethod, Method targetMethod) {
        String originMethodName = originMethod.getMethodName();
        String targetMethodName = targetMethod.getName();
        if (!originMethodName.equals(targetMethodName)) {
            Log.d(TAG, "isMethodMatch return false, reason is " +
                    "different method name [" + targetMethodName + "].");
            return false;
        }
        Object[] originParamTypes = originMethod.getParameterTypes();
        Class<?>[] targetParamTypes = targetMethod.getParameterTypes();
        if (originParamTypes.length != targetParamTypes.length) {
            Log.d(TAG, "isMethodMatch return false, reason is " +
                    "different parameter count [" + targetParamTypes.length + "].");
            return false;
        }
        for (int i = 0; i < originParamTypes.length; ++i) {
            Object originParamType = originParamTypes[i];
            String originParamTypeName;
            if (originParamType instanceof Class<?>) {
                originParamTypeName = ((Class<?>) originParamType).getName();
            } else {
                originParamTypeName = (String) originParamType;
            }
            String targetParamTypeName = targetParamTypes[i].getName();
            boolean isOriginParamTypeSystem = Utils.isSystemClass(originParamTypeName);
            boolean isTargetParamTypeSystem = Utils.isSystemClass(targetParamTypeName);
            if (isOriginParamTypeSystem && isTargetParamTypeSystem &&
                    !originParamTypeName.equals(targetParamTypeName)) {
//                因为系统类不会混淆，所以如果都是系统类就要求类名必须相同，这样可以增加捕获的准确度。
                Log.d(TAG, "isMethodMatch return false, reason is " +
                        "different system class parameter [" + targetParamTypeName + "].");
                return false;
            } else if (isOriginParamTypeSystem != isTargetParamTypeSystem) {
//                如果两个类型一个是系统类一个不是，那么是存在问题的，理论上不存在此情况
                Log.e(TAG, "Special error: different system class result " +
                        "for same parameter type ["+ originParamTypeName +", " +
                        targetParamTypeName +"]");
                return false;
            }
        }
        Log.d(TAG, "isMethodMatch return true.");
        return true;
    }
}
