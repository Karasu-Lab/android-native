package com.karasu256.karasulab.logging

import com.karasu256.karasulab.data.FirebaseRepository

object Log {
    @Volatile
    private var firebaseRepository: FirebaseRepository? = null

    fun install(repository: FirebaseRepository) {
        firebaseRepository = repository
    }

    fun v(tag: String, msg: String): Int {
        val result = android.util.Log.v(tag, msg)
        firebaseRepository?.log(msg)
        return result
    }

    fun v(tag: String, msg: String, tr: Throwable): Int {
        val result = android.util.Log.v(tag, msg, tr)
        firebaseRepository?.log(msg)
        firebaseRepository?.recordException(tr)
        return result
    }

    fun d(tag: String, msg: String): Int {
        val result = android.util.Log.d(tag, msg)
        firebaseRepository?.log(msg)
        return result
    }

    fun d(tag: String, msg: String, tr: Throwable): Int {
        val result = android.util.Log.d(tag, msg, tr)
        firebaseRepository?.log(msg)
        firebaseRepository?.recordException(tr)
        return result
    }

    fun i(tag: String, msg: String): Int {
        val result = android.util.Log.i(tag, msg)
        firebaseRepository?.log(msg)
        return result
    }

    fun i(tag: String, msg: String, tr: Throwable): Int {
        val result = android.util.Log.i(tag, msg, tr)
        firebaseRepository?.log(msg)
        firebaseRepository?.recordException(tr)
        return result
    }

    fun w(tag: String, msg: String): Int {
        val result = android.util.Log.w(tag, msg)
        firebaseRepository?.log(msg)
        return result
    }

    fun w(tag: String, msg: String, tr: Throwable): Int {
        val result = android.util.Log.w(tag, msg, tr)
        firebaseRepository?.log(msg)
        firebaseRepository?.recordException(tr)
        return result
    }

    fun w(tag: String, tr: Throwable): Int {
        val result = android.util.Log.w(tag, tr)
        firebaseRepository?.recordException(tr)
        return result
    }

    fun e(tag: String, msg: String): Int {
        val result = android.util.Log.e(tag, msg)
        firebaseRepository?.log(msg)
        return result
    }

    fun e(tag: String, msg: String, tr: Throwable): Int {
        val result = android.util.Log.e(tag, msg, tr)
        firebaseRepository?.log(msg)
        firebaseRepository?.recordException(tr)
        return result
    }

    fun wtf(tag: String, msg: String): Int {
        val result = android.util.Log.wtf(tag, msg)
        firebaseRepository?.log(msg)
        return result
    }

    fun wtf(tag: String, msg: String, tr: Throwable): Int {
        val result = android.util.Log.wtf(tag, msg, tr)
        firebaseRepository?.log(msg)
        firebaseRepository?.recordException(tr)
        return result
    }
}
