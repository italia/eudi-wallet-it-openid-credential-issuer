@file:Suppress("unused")

package it.ipzs.androidpidprovider.utils

import android.content.Context
import android.util.Log

internal object LogHelper {

    fun v(context: Context,tag: String, message: String) {
        if (PidProviderConfigUtils.isLogEnabled(context)) {
            Log.v(tag, message)
        }
    }

    fun i(context: Context,tag: String, message: String) {
        if (PidProviderConfigUtils.isLogEnabled(context)) {
            Log.i(tag, message)
        }
    }

    fun e(context: Context,tag: String, message: String) {
        if (PidProviderConfigUtils.isLogEnabled(context)) {
            Log.e(tag, message)
        }
    }

    fun d(context: Context,tag: String, message: String) {
        if (PidProviderConfigUtils.isLogEnabled(context)) {
            Log.d(tag, message)
        }
    }

    fun printStackTrace(context: Context,tag: String, throwable: Throwable) {
        if (PidProviderConfigUtils.isLogEnabled(context)) {
            Log.i(tag, Log.getStackTraceString(throwable))
        }
    }
}