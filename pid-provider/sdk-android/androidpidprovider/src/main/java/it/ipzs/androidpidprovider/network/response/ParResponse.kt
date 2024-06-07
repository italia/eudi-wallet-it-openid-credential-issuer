@file:Suppress("unused")

package it.ipzs.androidpidprovider.network.response


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep
import com.google.gson.annotations.Expose
import java.io.Serializable

@Keep
internal class ParResponse: Serializable{

    @SerializedName("request_uri")
    @Expose
    val requestUri: String? = null

    @SerializedName("expires_in")
    @Expose
    val expiresIn: Int? = null

}