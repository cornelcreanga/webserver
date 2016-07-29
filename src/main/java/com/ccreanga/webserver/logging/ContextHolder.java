package com.ccreanga.webserver.logging;

/**
 * This class is used for contextual logging
 */
public class ContextHolder {
    private static final ThreadLocal<Context> threadLocal = new ThreadLocal<>();

    private ContextHolder() {}

    public static void put(Context context) {
        threadLocal.set(context);
    }

    public static Context get() {
        return threadLocal.get();
    }

    public static void cleanup(){
        threadLocal.remove();
    }
}
