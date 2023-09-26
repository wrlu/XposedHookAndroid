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
                if (stackTraceClassFilter(originElement.getClassName())) {
                    continue;
                }
                finalElements.add(originElement);
            }
            e.setStackTrace(finalElements.toArray(new StackTraceElement[0]));
            Log.v(tag, "[StackTrace] ", e);
        }
    }

    public static boolean stackTraceClassFilter(String className) {
        return className.contains("xposed") || className.equals("LSPHooker_");
    }
}
