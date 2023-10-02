@file:Suppress("SpellCheckingInspection")

package it.ipzs.androidpidprovider.utils

internal object NetworkUtils {

    private const val REST_DEFAULT_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssssss"

    fun getConnectionTimeout():Long{
        return 100L
    }

    fun getReadTimeout():Long{
        return 100L
    }

    fun getDefaultDateFormat():String{
        return REST_DEFAULT_DATE_FORMAT
    }
}