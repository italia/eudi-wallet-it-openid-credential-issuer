package it.ipzs.androidpidprovider.entity

import com.google.gson.annotations.SerializedName
import com.google.gson.annotations.Expose
import java.io.Serializable

internal class WalletInstanceEntity: Serializable {

    @SerializedName("cnf")
    @Expose
    val cnf: Cnf? = null

}