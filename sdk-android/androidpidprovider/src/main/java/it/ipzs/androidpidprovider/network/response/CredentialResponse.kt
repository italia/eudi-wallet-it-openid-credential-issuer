@file:Suppress("unused")

package it.ipzs.androidpidprovider.network.response

import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep
import com.google.gson.annotations.Expose
import java.io.Serializable

@Keep
internal class CredentialResponse : Serializable {

    @SerializedName("format")
    @Expose
    val format: String? = null

    @SerializedName("credential")
    @Expose
    val credential: String? = null

    @SerializedName("c_nonce")
    @Expose
    val cNonce: String? = null

    @SerializedName("c_nonce_expires_in")
    @Expose
    val cNonceExpiresIn: Long? = null
}