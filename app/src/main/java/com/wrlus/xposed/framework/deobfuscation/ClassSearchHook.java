package com.wrlus.xposed.framework.deobfuscation;

import android.util.Log;

import com.wrlus.xposed.framework.MethodHook;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.XposedHelpers;

public abstract class ClassSearchHook implements SearchHook {
    private static final String TAG = "ClassSearchHook";
    protected final String className;
    protected final ClassLoader classLoader;
    protected final MethodHook[] methodHooks;

    protected ClassSearchHook(Builder builder) {
        this.className = builder.className;
        this.classLoader = builder.classLoader;
        this.methodHooks = builder.methodHooks.toArray(new MethodHook[0]);
    }

    public abstract static class Builder {
        private String className;
        private ClassLoader classLoader;
        private final List<MethodHook> methodHooks;

        public Builder() {
            methodHooks = new ArrayList<>();
        }

        public Builder setClassName(String className) {
            this.className = className;
            return this;
        }

        public Builder setClassLoader(ClassLoader classLoader) {
            this.classLoader = classLoader;
            return this;
        }

        public Builder addSearchMethod(MethodHook method) {
            methodHooks.add(method);
            return this;
        }

        public abstract ClassSearchHook build();
    }

    @Override
    public void searchAndInstall() {
        Class<?> classObj =
                XposedHelpers.findClassIfExists(className, classLoader);
        Method[] declaredMethods = classObj.getDeclaredMethods();
        for (MethodHook methodHook : methodHooks) {
            int foundCount = 0;
            for (Method method : declaredMethods) {
                if (isMethodMatch(methodHook, method)) {
                    MethodHook newHooker = new MethodHook.Builder(className, classLoader)
                            .setMethodName(method.getName())
                            .addParameters(method.getParameterTypes())
                            .setCallback(methodHook.getCallback())
                            .build();
                    MethodHook.normalInstall(newHooker);
                    ++foundCount;
                }
            }
            Log.d(TAG, "Find " + foundCount +
                    " result(s) for deobfuscation " + methodHook);
        }
    }
    public abstract boolean isMethodMatch(MethodHook originMethod, Method targetMethod);
}
