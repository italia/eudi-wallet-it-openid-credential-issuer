package it.ipzs.androidpidprovider.utils

import android.content.Context
import it.ipzs.androidpidprovider.storage.PidProviderSDKShared

internal object PidProviderConfigUtils {

    fun getBaseUrl(context: Context): String {
        return PidProviderSDKShared.getInstance(context).getBaseURL()
    }

    fun isLogEnabled(context: Context): Boolean {
        return PidProviderSDKShared.getInstance(context).isLogEnabled()
    }

    fun getWalletInstanceAttestation(context: Context): String {
        return PidProviderSDKShared.getInstance(context).getWalletInstanceAttestation()
    }

    fun getWalletUri(context: Context): String {
        return PidProviderSDKShared.getInstance(context).getWalletUri()
    }

    @Suppress("unused")
    fun getToken(context: Context): String {
        return PidProviderSDKShared.getInstance(context).getToken()
    }
}