package com.wrlus.xposed.util;

public class Utils {
    public static boolean isClassExists(String className, ClassLoader classLoader) {
        try {
            Class.forName(className, false, classLoader);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static boolean isSameClassName(Class<?> class1, Class<?> class2) {
        return class1.getName().equals(class2.getName());
    }

    public static boolean isSameClassName(Class<?> class1, String className2) {
        return class1.getName().equals(className2);
    }

    public static boolean isSameClassArray(Class<?>[] class1, Class<?>[] class2) {
        if (class1.length != class2.length) return false;
        for (int i = 0; i < class1.length; ++i) {
            if (!isSameClassName(class1[i], class2[i])) {
                return false;
            }
        }
        return true;
    }

    public static boolean isSameClassArray(Class<?>[] class1, Object[] class2) {
        if (class1.length != class2.length) return false;
        for (int i = 0; i < class1.length; ++i) {
            Object class2Item = class2[i];
            if (class2Item instanceof Class<?>) {
                if (!isSameClassName(class1[i], (Class<?>) class2Item)) {
                    return false;
                }
            } else if (class2Item instanceof String) {
                if (!isSameClassName(class1[i], (String) class2Item)) {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean isSystemClass(String className) {
        return isClassExists(className, ClassLoader.getSystemClassLoader());
    }

    public static Object checkNonNull(Object obj) {
        return (obj != null) ? obj : new NoneType();
    }
}
