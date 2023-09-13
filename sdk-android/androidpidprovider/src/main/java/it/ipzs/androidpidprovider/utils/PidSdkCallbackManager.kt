package it.ipzs.androidpidprovider.utils

import it.ipzs.androidpidprovider.external.IPidSdkCallback
import it.ipzs.androidpidprovider.external.PidCredential

internal open class PidSDKCallbackManager<T> {

    private var sdkCallback: IPidSdkCallback<T>? = null

    fun setSDKCallback(sdkCallback: IPidSdkCallback<T>) {
        this.sdkCallback = sdkCallback
    }

    fun invokeOnComplete(result: T) {
        sdkCallback?.onComplete(result)
    }

    fun invokeOnError(throwable: Throwable) {
        sdkCallback?.onError(throwable)
    }
}

internal object PidSdkStartCallbackManager: PidSDKCallbackManager<Boolean>()

internal object PidSdkCompleteCallbackManager: PidSDKCallbackManager<PidCredential>()