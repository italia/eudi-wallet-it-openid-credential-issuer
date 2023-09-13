package it.ipzs.androidpidprovider.extension

internal fun String?.isValidUrl(): Boolean {
    this?.let {
        if (it.isNotEmpty()) {
            return !(!it.startsWith("http://") && !it.startsWith("https://"))
        }
    }
    return false
}