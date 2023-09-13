package it.ipzs.cieidsdk.data

import java.io.Serializable

data class PidCieData(
    var name: String?,
    var surname: String?,
    var fiscalCode: String?,
    var birthDate: String?
) : Serializable
