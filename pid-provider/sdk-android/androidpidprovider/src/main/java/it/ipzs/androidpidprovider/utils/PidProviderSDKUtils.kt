package it.ipzs.androidpidprovider.utils

import android.content.Context
import com.nimbusds.jose.jwk.JWK
import it.ipzs.androidpidprovider.exception.PIDProviderException
import it.ipzs.androidpidprovider.extension.isValidUrl
import it.ipzs.androidpidprovider.external.PidProviderConfig
import it.ipzs.androidpidprovider.storage.PidProviderSDKShared

internal object PidProviderSDKUtils {

    fun configure(context:Context,pidProviderConfig: PidProviderConfig){
        saveBaseUrl(context, pidProviderConfig.getBaseURL())
        saveLogEnabled(context, pidProviderConfig.isLogEnabled())
        saveWalletInstance(context, pidProviderConfig.getWalletInstanceAttestation())
        saveWalletUri(context, pidProviderConfig.getWalletUri())
    }

    private fun saveBaseUrl(context: Context, baseUrl: String) {
        if (baseUrl.isNotEmpty()) {
            if(baseUrl.isValidUrl()) {
                PidProviderSDKShared.getInstance(context).saveBaseURL(baseUrl)
            } else {
                throw PIDProviderException("base url is not valid")
            }
        } else {
            throw PIDProviderException("base url can't be empty")
        }
    }

    private fun saveLogEnabled(context: Context, isLogEnabled: Boolean) {
        PidProviderSDKShared.getInstance(context).saveLogEnabled(isLogEnabled)
    }

    private fun saveWalletInstance(context: Context, walletInstance: String?) {
        if (!walletInstance.isNullOrEmpty()) {
            PidProviderSDKShared.getInstance(context).saveWalletInstanceAttestation(walletInstance)
        } else {
            throw PIDProviderException("wallet instance attestation can't be empty")
        }
    }

    private fun saveWalletUri(context: Context, walletUri: String?) {
        if (!walletUri.isNullOrEmpty()) {
            PidProviderSDKShared.getInstance(context).saveWalletUri(walletUri)
        } else {
            throw PIDProviderException("wallet uri can't be empty")
        }
    }

}