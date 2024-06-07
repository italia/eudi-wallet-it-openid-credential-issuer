package it.ipzs.androidpidprovider.external

import java.io.Serializable

class PidProviderConfig internal constructor(
    var logEnabled: Boolean? = false,
    private var baseUrl: String? = null,
    private var walletInstanceAttestation: String? = null,
    private var walletUri: String? = null
) : Serializable {

    class Builder : Serializable {

        private var logEnabled: Boolean? = null
        private var baseUrl: String? = null
        private var walletInstanceAttestation: String? = null
        private var walletUri: String? = null

        fun logEnabled(logEnabled: Boolean?) = apply { this.logEnabled = logEnabled }

        fun baseUrl(baseUrl: String?) = apply { this.baseUrl = baseUrl }

        fun walletInstance(walletInstanceAttestation: String) =
            apply { this.walletInstanceAttestation = walletInstanceAttestation }

        fun walletUri(walletUri: String?) = apply { this.walletUri = walletUri }

        fun build(): PidProviderConfig {
            return PidProviderConfig(
                logEnabled = logEnabled,
                baseUrl = baseUrl,
                walletInstanceAttestation = walletInstanceAttestation,
                walletUri = walletUri
            )
        }

    }

    fun getBaseURL(): String {
        return baseUrl.orEmpty()
    }

    fun isLogEnabled(): Boolean {
        return logEnabled ?: false
    }

    fun getWalletInstanceAttestation(): String? {
        return walletInstanceAttestation
    }

    fun getWalletUri(): String? {
        return walletUri
    }

}