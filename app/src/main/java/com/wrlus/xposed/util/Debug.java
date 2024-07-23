package com.wrlus.xposed.util;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class Debug {
    public static void printStackTrace(String tag) {
        try {
            throw new Exception("get stack");
        } catch (Exception e) {
            StackTraceElement[] originElements = e.getStackTrace();
            List<StackTraceElement> finalElements = new ArrayList<>(originElements.length);
            for (StackTraceElement originElement : originElements) {
                if (stackTraceFilter(originElement)) {
                    continue;
                }
                finalElements.add(originElement);
            }
            e.setStackTrace(finalElements.toArray(new StackTraceElement[0]));
            Log.v(tag, "[StackTrace] ", e);
        }
    }

    public static boolean stackTraceFilter(StackTraceElement element) {
        return element.getClassName().contains("com.wrlus.xposed") ||
                element.getClassName().contains("Xposed") ||
                element.getClassName().contains("LSPHooker_") ||
                element.getClassName().equals("J") && element.getMethodName().equals("callback");
    }
}
