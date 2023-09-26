package com.wrlus.xposed.framework.deobfuscation;

import com.wrlus.xposed.framework.MethodHook;
import com.wrlus.xposed.util.Utils;

import java.lang.reflect.Method;

/**
 * 未混淆的类名+未混淆的参数类型
 * 需匹配混淆后的方法名进行Hook
 */
public class MethodSearchHook extends ClassSearchHook {
    protected MethodSearchHook(Builder builder) {
        super(builder);
    }

    public static class Builder extends ClassSearchHook.Builder {
        @Override
        public MethodSearchHook build() {
            return new MethodSearchHook(this);
        }
    }

    @Override
    public boolean isMethodMatch(MethodHook originMethod, Method targetMethod) {
        return Utils.isSameClassArray(targetMethod.getParameterTypes(),
                originMethod.getParameterTypes());
    }
}
