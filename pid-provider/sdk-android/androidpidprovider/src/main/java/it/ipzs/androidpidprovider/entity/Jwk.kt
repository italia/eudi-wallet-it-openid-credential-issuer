@file:Suppress("unused")

package it.ipzs.androidpidprovider.entity

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

internal class Jwk: Serializable {

    @SerializedName("kty")
    @Expose
    val kty: String? = null

    @SerializedName("kid")
    @Expose
    val kid: String? = null

    @SerializedName("crv")
    @Expose
    val crv: String? = null

    @SerializedName("x")
    @Expose
    val x: String? = null

    @SerializedName("y")
    @Expose
    val y: String? = null

}