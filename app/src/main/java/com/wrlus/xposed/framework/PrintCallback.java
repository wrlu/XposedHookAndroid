package com.wrlus.xposed.framework;

import android.util.Log;

import com.wrlus.xposed.util.Debug;
import com.wrlus.xposed.util.Utils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.robv.android.xposed.XC_MethodHook;

public class PrintCallback extends XC_MethodHook {
    private static final String TAG = "PrintCallback";
    private final String tag;
    private final int hookTime;
    private final String[] parameterNames;
    private final Integer[] parameterPrintPolicies;
    private final int retValuePrintPolicy;
    private final Map<String, Method> specPrintMethodMap;
    private final Method retValueSpecPrintMethod;

    public static class HookTime {
        public static final int BEFORE = 1;
        public static final int AFTER = 1 << 1;
    }

    public static class PrintPolicy {
        public static final int DEFAULT = 1;
        public static final int CLASSNAME = 1 << 1;
        public static final int SPECIFIC = 1 << 2;
    }

    protected PrintCallback(Builder builder) {
        this.tag = builder.tag;
        this.hookTime = builder.hookTime;
        this.parameterNames = builder.parameterNames.toArray(new String[0]);
        this.parameterPrintPolicies = builder.parameterPrintPolicies.toArray(new Integer[0]);
        this.retValuePrintPolicy = builder.retValuePrintPolicy;
        this.specPrintMethodMap = builder.specPrintMethodMap;
        this.retValueSpecPrintMethod = builder.retValueSpecPrintMethod;
    }

    public static class Builder {
        private String tag;
        private int hookTime;
        private final List<String> parameterNames;
        private final List<Integer> parameterPrintPolicies;
        private int retValuePrintPolicy;
        private final Map<String, Method> specPrintMethodMap;
        private Method retValueSpecPrintMethod;

        public Builder() {
            parameterNames = new ArrayList<>();
            parameterPrintPolicies = new ArrayList<>();
            retValuePrintPolicy = PrintPolicy.DEFAULT;
            specPrintMethodMap = new HashMap<>();
            retValueSpecPrintMethod = null;
        }

        public Builder addParameterPrint(String parameterName) {
            return addParameterPrint(parameterName, PrintPolicy.DEFAULT);
        }

        public Builder addParameterPrint(String parameterName, int printPolicy) {
            parameterNames.add(parameterName);
            this.parameterPrintPolicies.add(printPolicy);
            return this;
        }

        public Builder addSpecParameterPrint(String parameterName, Method specPrintMethod) {
            specPrintMethodMap.put(parameterName, specPrintMethod);
            return addParameterPrint(parameterName, PrintPolicy.SPECIFIC);
        }

        public Builder setReturnValuePrint(int printPolicy) {
            retValuePrintPolicy = printPolicy;
            return this;
        }

        public Builder setSpecReturnValuePrint(Method specPrintMethod) {
            retValueSpecPrintMethod = specPrintMethod;
            return setReturnValuePrint(PrintPolicy.SPECIFIC);
        }

        public Builder setTag(String tag) {
            this.tag = tag;
            return this;
        }

        public Builder setHookTime(int hookTime) {
            this.hookTime = hookTime;
            return this;
        }

        public PrintCallback build() {
            return new PrintCallback(this);
        }
    }

    @Override
    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
        if ((hookTime & HookTime.BEFORE) == HookTime.BEFORE) {
            onMethodHooked(param, false);
        }
    }

    @Override
    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
        if ((hookTime & HookTime.AFTER) == HookTime.AFTER) {
            onMethodHooked(param, true);
        }
    }

    protected void onMethodHooked(MethodHookParam param, boolean canPrintRetValue) {
        StringBuilder printStr = new StringBuilder();
        printStr.append(param.method.getDeclaringClass().getCanonicalName());
        printStr.append("#");
        printStr.append(param.method.getName());
        printStr.append(": ");

        printStr.append("thisObject");
        Object thisValue = Utils.checkNonNull(param.thisObject);
        printStr.append(" = [");
        printStr.append(thisValue);
        printStr.append("]; ");

        for (int i = 0; i < parameterNames.length; ++i) {
            String parameterName = parameterNames[i];
            int printPolicy = parameterPrintPolicies[i];
            printStr.append(parameterNames[i]);
            Object paramValue = Utils.checkNonNull(param.args[i]);

            printStr.append(" = [");
            if ((printPolicy & PrintPolicy.DEFAULT) ==
                    PrintPolicy.DEFAULT) {
                printStr.append("value: ");
                printStr.append(paramValue);
                printStr.append(" ");
            }
            if ((printPolicy & PrintPolicy.CLASSNAME) ==
                    PrintPolicy.CLASSNAME) {
                printStr.append("type: ");
                printStr.append(paramValue.getClass().getName());
                printStr.append(" ");
            }
            if ((printPolicy & PrintPolicy.SPECIFIC) ==
                    PrintPolicy.SPECIFIC) {
                Method specMethod = specPrintMethodMap.get(parameterNames[i]);
                if (specMethod != null) {
                    try {
                        Object specResult = specMethod.invoke(paramValue);
                        printStr.append("specific: ");
                        printStr.append(specResult);
                        printStr.append(" ");
                    } catch (ReflectiveOperationException e) {
                        Log.w(TAG, "Unable to call specific print method for parameter: " +
                                parameterName);
                    }
                } else {
                    Log.w(TAG, "Missing specific print method for parameter: " +
                            parameterName);
                }
            }
            printStr.append("]; ");
        }
        if (canPrintRetValue) {
            Object retValue = Utils.checkNonNull(param.getResult());
            printStr.append("retValue = [");
            if ((retValuePrintPolicy & PrintPolicy.DEFAULT) ==
                    PrintPolicy.DEFAULT) {
                printStr.append("value: ");
                printStr.append(retValue);
                printStr.append(" ");
            }
            if ((retValuePrintPolicy & PrintPolicy.CLASSNAME) ==
                    PrintPolicy.CLASSNAME) {
                printStr.append("type: ");
                printStr.append(retValue.getClass().getName());
                printStr.append(" ");
            }
            if ((retValuePrintPolicy & PrintPolicy.SPECIFIC) ==
                    PrintPolicy.SPECIFIC) {
                if (retValueSpecPrintMethod != null) {
                    try {
                        Object specResult = retValueSpecPrintMethod.invoke(retValuePrintPolicy);
                        printStr.append("specific: ");
                        printStr.append(specResult);
                        printStr.append(" ");
                    } catch (ReflectiveOperationException e) {
                        Log.w(TAG, "Unable to call specific print method for return value.");
                    }
                } else {
                    Log.w(TAG, "Missing specific print method for return value.");
                }
            }
            printStr.append("]");
            printStr.append("; ");
        }
        Log.i(tag, printStr.toString());
        Debug.printStackTrace(tag);
    }
}