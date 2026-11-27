package com.karasu256.karasulab.logging

import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics

object Log {
    fun v(tag: String, msg: String): Int {
        val result = android.util.Log.v(tag, msg)
        Firebase.crashlytics.log(msg)
        return result
    }

    fun v(tag: String, msg: String, tr: Throwable): Int {
        val result = android.util.Log.v(tag, msg, tr)
        Firebase.crashlytics.log(msg)
        Firebase.crashlytics.recordException(tr)
        return result
    }

    fun d(tag: String, msg: String): Int {
        val result = android.util.Log.d(tag, msg)
        Firebase.crashlytics.log(msg)
        return result
    }

    fun d(tag: String, msg: String, tr: Throwable): Int {
        val result = android.util.Log.d(tag, msg, tr)
        Firebase.crashlytics.log(msg)
        Firebase.crashlytics.recordException(tr)
        return result
    }

    fun i(tag: String, msg: String): Int {
        val result = android.util.Log.i(tag, msg)
        Firebase.crashlytics.log(msg)
        return result
    }

    fun i(tag: String, msg: String, tr: Throwable): Int {
        val result = android.util.Log.i(tag, msg, tr)
        Firebase.crashlytics.log(msg)
        Firebase.crashlytics.recordException(tr)
        return result
    }

    fun w(tag: String, msg: String): Int {
        val result = android.util.Log.w(tag, msg)
        Firebase.crashlytics.log(msg)
        return result
    }

    fun w(tag: String, msg: String, tr: Throwable): Int {
        val result = android.util.Log.w(tag, msg, tr)
        Firebase.crashlytics.log(msg)
        Firebase.crashlytics.recordException(tr)
        return result
    }

    fun w(tag: String, tr: Throwable): Int {
        val result = android.util.Log.w(tag, tr)
        Firebase.crashlytics.recordException(tr)
        return result
    }

    fun e(tag: String, msg: String): Int {
        val result = android.util.Log.e(tag, msg)
        Firebase.crashlytics.log(msg)
        return result
    }

    fun e(tag: String, msg: String, tr: Throwable): Int {
        val result = android.util.Log.e(tag, msg, tr)
        Firebase.crashlytics.log(msg)
        Firebase.crashlytics.recordException(tr)
        return result
    }

    fun wtf(tag: String, msg: String): Int {
        val result = android.util.Log.wtf(tag, msg)
        Firebase.crashlytics.log(msg)
        return result
    }

    fun wtf(tag: String, msg: String, tr: Throwable): Int {
        val result = android.util.Log.wtf(tag, msg, tr)
        Firebase.crashlytics.log(msg)
        Firebase.crashlytics.recordException(tr)
        return result
    }
}
