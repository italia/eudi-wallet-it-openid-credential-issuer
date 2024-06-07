@file:Suppress("unused")

package it.ipzs.androidpidprovider.network.response


import com.google.gson.annotations.SerializedName
import com.google.gson.annotations.Expose
import java.io.Serializable

internal class TokenResponse: Serializable{

    @SerializedName("access_token")
    @Expose
    val accessToken: String? = null

    @SerializedName("token_type")
    @Expose
    val tokenType: String? = null

    @SerializedName("expires_in")
    @Expose
    val expiresIn: Int? = null

    @SerializedName("c_nonce")
    @Expose
    val cNonce: String? = null

    @SerializedName("c_nonce_expires_in")
    @Expose
    val cNonceExpiresIn: Int? = null

}