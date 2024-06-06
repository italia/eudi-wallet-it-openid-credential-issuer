package it.ipzs.androidpidprovider.external

import java.io.Serializable
data class PidCredential(
    var format: String?,
    var credential: String?,
    var nonce: String?,
    var nonceExpires: Long?
) : Serializable