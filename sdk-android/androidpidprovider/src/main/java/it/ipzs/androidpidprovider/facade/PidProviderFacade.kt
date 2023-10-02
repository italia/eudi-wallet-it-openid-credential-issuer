@file:Suppress("SpellCheckingInspection")

package it.ipzs.androidpidprovider.facade

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import it.ipzs.androidpidprovider.network.datasource.PidProviderDataSource
import it.ipzs.androidpidprovider.network.datasource.PidProviderDataSourceImpl
import it.ipzs.androidpidprovider.storage.PidProviderSDKShared
import it.ipzs.androidpidprovider.utils.PidSdkCompleteCallbackManager
import it.ipzs.androidpidprovider.utils.PidSdkStartCallbackManager
import it.ipzs.cieidsdk.data.PidCieData
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

internal class PidProviderFacade(
    var context: Context
) {

    private var dataSourceImpl: PidProviderDataSource = PidProviderDataSourceImpl(context)
    private val pkceFacade = PKCEFacade(context, dataSourceImpl)

    fun generateJwtForPar(): String? {
        return pkceFacade.generateUnsignedJwtForPar()
    }

    fun startAuthFlow(activity: AppCompatActivity, signedJwtForPar: String, jwkForDPoP: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val cdAuthorize = CompletableDeferred<String?>()
            val sharedPreferences = PidProviderSDKShared.getInstance(context)
            sharedPreferences.saveJWK(jwkForDPoP)
            val requestUri = pkceFacade.requestPar(signedJwtForPar)
            pkceFacade.loadAuthorizeWebview(activity, requestUri,cdAuthorize)
            val authorizeResponse = cdAuthorize.await().orEmpty()
            val redirectUri = sharedPreferences.getWalletUri()
            val proof = pkceFacade.getToken(authorizeResponse, redirectUri)
            sharedPreferences.saveUnsignedJWTProof(proof)
            PidSdkStartCallbackManager.invokeOnComplete(true)
        }
    }

    fun getCredential(pidCieData: PidCieData?) {
        CoroutineScope(Dispatchers.IO).launch {
            val credential = pkceFacade.getCredential(pidCieData)
            PidSdkCompleteCallbackManager.invokeOnComplete(credential)
        }
    }

}