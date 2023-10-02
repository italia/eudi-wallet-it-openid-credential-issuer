package it.ipzs.androidpidprovider.utils

import android.content.Context
import android.net.Uri
import it.ipzs.androidpidprovider.constant.UrlConstant

internal object UrlUtils {

    fun buildAuthorizeUrl(context: Context, clientId: String, requestUri: String): String {
        val baseUri = Uri.parse(PidProviderConfigUtils.getBaseUrl(context))
        val baseUriAuthority = baseUri.authority
        val baseUriPath = baseUri.path
        val uri = Uri.Builder()
            .scheme(UrlConstant.SCHEME)
            .authority(baseUriAuthority)
            .path(baseUriPath)
            .appendPath(UrlConstant.AUTHORIZE_URL)
            .appendQueryParameter(UrlConstant.CLIENT_ID_PARAM, clientId)
            .appendQueryParameter(UrlConstant.REQUEST_URI_PARAM, requestUri)
            .build()
        return uri.toString()
    }

    fun buildTokenUrl(context: Context): String {
        val baseUri = Uri.parse(PidProviderConfigUtils.getBaseUrl(context))
        val baseUriAuthority = baseUri.authority
        val baseUriPath = baseUri.path
        val uri = Uri.Builder()
            .scheme(UrlConstant.SCHEME)
            .authority(baseUriAuthority)
            .path(baseUriPath)
            .appendPath(UrlConstant.TOKEN_URL)
            .build()
        return uri.toString()
    }

    fun buildCredentialUrl(context: Context): String {
        val baseUri = Uri.parse(PidProviderConfigUtils.getBaseUrl(context))
        val baseUriAuthority = baseUri.authority
        val baseUriPath = baseUri.path
        val uri = Uri.Builder()
            .scheme(UrlConstant.SCHEME)
            .authority(baseUriAuthority)
            .path(baseUriPath)
            .appendPath(UrlConstant.CREDENTIAL_URL)
            .build()
        return uri.toString()
    }

}