package it.ipzs.androidpidprovider.entity

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

internal class Cnf : Serializable {

    @SerializedName("jwk")
    @Expose
    val jwk: Jwk? = null

}