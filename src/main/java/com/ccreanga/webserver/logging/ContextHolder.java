package com.ccreanga.webserver.logging;

/**
 * This class is used for contextual logging - internally it uses an thread local
 * It acts like a global variable per thread
 * Do not forget to invoke cleanup after the thread processing is done
 */
public class ContextHolder {
    private static final ThreadLocal<Context> threadLocal = new ThreadLocal<>();

    private ContextHolder() {
    }

    public static void put(Context context) {
        threadLocal.set(context);
    }

    public static Context get() {
        return threadLocal.get();
    }

    public static void cleanup() {
        threadLocal.remove();
    }
}
