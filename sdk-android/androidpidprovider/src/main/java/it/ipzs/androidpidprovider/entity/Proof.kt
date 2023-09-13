package it.ipzs.androidpidprovider.entity

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import it.ipzs.cieidsdk.data.PidCieData
import java.io.Serializable

internal class Proof: Serializable {

    @SerializedName("proof_type")
    @Expose
    var proofType:String? = null

    @SerializedName("jwt")
    @Expose
    var jwt:String? = null

    @SerializedName("cieData")
    @Expose
    var pidCieData: PidCieData? = null

}