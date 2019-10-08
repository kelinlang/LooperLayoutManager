package com.xiashengming.widget;

import android.util.Log;

/**
 * 作者：刘淑
 * 日期：2019/7/19 0019.
 * 说明：打印日志
 */
public class MponLog {
    private static final String TAG = "mponLog";

    private static final boolean DEBUG = BuildConfig.DEBUG;


    /**
     *
     * @param tag 日志的tag
     * @param msg 日志信息
     */
    public static void log(String tag, String msg) {
        if (msg == null) {
            return;
        }
        if (DEBUG) {
            Log.d(tag, "" + msg);
        }
    }

    /**
     *
     * @param msg 日志信息
     */
    public static void log(String msg) {
        log(TAG, msg);
    }

    public static void d(String msg) {
        if (DEBUG){
            i(msg);
        }else {
            log(TAG, msg);
        };
    }

    public static void i(String msg) {
        if (msg == null) {
            return;
        }
        Log.i(TAG, "" +msg);
    }

    public static void e(String msg) {
        if (msg == null) {
            return;
        }
        Log.e(TAG, "" +msg);
    }

    public static void e(String msg, Throwable e) {
        Log.e(TAG, "" + msg,e);
    }

    public static void w(String msg) {
        if (msg == null) {
            return;
        }
        Log.w(TAG, "" +msg);
    }
}
