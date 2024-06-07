@file:Suppress("SpellCheckingInspection")

package it.ipzs.androidpidprovider.facade

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import it.ipzs.androidpidprovider.constant.UrlConstant
import it.ipzs.androidpidprovider.entity.Proof
import it.ipzs.androidpidprovider.exception.PIDProviderException
import it.ipzs.androidpidprovider.external.PidCredential
import it.ipzs.androidpidprovider.network.datasource.PidProviderDataSource
import it.ipzs.androidpidprovider.storage.PidProviderSDKShared
import it.ipzs.androidpidprovider.utils.*
import it.ipzs.cieidsdk.data.PidCieData
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

internal class PKCEFacade(
    private val context: Context,
    private val dataSource: PidProviderDataSource
) : CoroutineScope {

    fun generateUnsignedJwtForPar(): String? {
        try {
            val sharedPref = PidProviderSDKShared.getInstance(context)
            val codeVerifier = PKCEUtils.createCodeVerifier()
            sharedPref.saveCodeVerifier(codeVerifier)
            val codeChallenge = PKCEUtils.createCodeChallenge(codeVerifier)
            sharedPref.saveCodeChallenge(codeChallenge)
            val walletInstanceJwt =
                PidProviderConfigUtils.getWalletInstanceAttestation(context)
            val redirectUri = sharedPref.getWalletUri()
            val jwkThumbprint = PKCEUtils.computeThumbprint(walletInstanceJwt)
            sharedPref.saveClientId(jwkThumbprint)
            return PKCEUtils.generateJWTForPar(
                jwkThumbprint,
                codeChallenge,
                redirectUri
            )
        } catch (exception: Throwable) {
            return null
        }
    }

    suspend fun requestPar(signedJwtForPar: String): String {
        val cdRequestUri = CompletableDeferred<String>()
        withContext(Dispatchers.IO) {
            try {
                val sharedPref = PidProviderSDKShared.getInstance(context)
                val codeChallenge = sharedPref.getCodeChallenge()
                val walletInstanceJwt =
                    PidProviderConfigUtils.getWalletInstanceAttestation(context)
                val jwkThumbprint = PKCEUtils.computeThumbprint(walletInstanceJwt)
                sharedPref.saveClientId(jwkThumbprint)

                val parResponse = dataSource.requestPar(
                    responseType = PKCEConstant.JWT_RESPONSE_TYPE_VALUE,
                    clientId = jwkThumbprint,
                    codeChallenge = codeChallenge,
                    codeChallengeMethod = PKCEConstant.JWT_CODE_CHALLENGE_METHOD_VALUE,
                    clientAssertionType = PKCEConstant.JWT_CLIENT_ASSERTION_TYPE_VALUE,
                    clientAssertion = walletInstanceJwt,
                    request = signedJwtForPar
                )

                if (parResponse != null) {
                    cdRequestUri.complete(parResponse.requestUri.orEmpty())
                } else {
                    throw PIDProviderException("par response is null")
                }
            } catch (error: Throwable) {
                cdRequestUri.cancel(error.message.toString())
                PidSdkStartCallbackManager.invokeOnError(error)
            }
        }
        return cdRequestUri.await()
    }

    @SuppressLint("SetJavaScriptEnabled")
    fun loadAuthorizeWebview(
        activity: AppCompatActivity,
        requestUri: String,
        cdCode: CompletableDeferred<String?>
    ) {
        val clientId = PidProviderSDKShared.getInstance(activity).getClientId()
        val url = UrlUtils.buildAuthorizeUrl(activity, clientId, requestUri)
        val walletUri = PidProviderConfigUtils.getWalletUri(activity)

        activity.runOnUiThread {
            val webView = WebView(activity)
            webView.apply {
                webViewClient = object : WebViewClient() {

                    override fun shouldOverrideUrlLoading(
                        view: WebView?,
                        request: WebResourceRequest?
                    ): Boolean {
                        val currentUri = request?.url
                        if (currentUri.toString().contains(walletUri, true)) {
                            val code = currentUri?.getQueryParameters(UrlConstant.CODE_PARAM)?.first().orEmpty()
                            cdCode.complete(code)
                        }
                        return super.shouldOverrideUrlLoading(view, request)
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        if (url?.contains("login") == true) {
                            val jsScript = "document.getElementById('username').value = 'user'; " +
                                    "document.getElementById('username').readOnly = true; " +
                                    "document.getElementById('password').value = 'password'; " +
                                    "document.getElementById('password').readOnly = true;" +
                                    "document.getElementsByClassName('form-signin')[0].submit()"
                            view?.evaluateJavascript(jsScript, null)
                        }
                    }
                    override fun onReceivedError(
                        view: WebView?,
                        request: WebResourceRequest?,
                        error: WebResourceError?
                    ) {
                        super.onReceivedError(view, request, error)
                        PidSdkStartCallbackManager.invokeOnError(Exception(error.toString()))
                    }
                }
                settings.javaScriptEnabled = true
                loadUrl(url)
            }

        }
    }

    suspend fun getToken(code: String, redirectUri: String): String {
        val cdProof = CompletableDeferred<String>()
        withContext(Dispatchers.IO) {
            try {
                val sharedPreferences = PidProviderSDKShared.getInstance(context)
                val walletInstanceJwt =
                    PidProviderConfigUtils.getWalletInstanceAttestation(context)
                val jwkThumbprint = PKCEUtils.computeThumbprint(walletInstanceJwt)
                val codeVerifier = sharedPreferences.getCodeVerifier()

                val tokenUrl = UrlUtils.buildTokenUrl(context)
                val dPopToken = DPoPUtils.generateDPoP(context, "POST", tokenUrl)
                val tokenResponse = dataSource.requestToken(
                    dPop = dPopToken,
                    grantType = "authorization code",
                    clientId = jwkThumbprint,
                    code = code,
                    codeVerifier = codeVerifier,
                    clientAssertionType = "urn:ietf:params:oauth:client-assertion-type:jwt-bearer",
                    clientAssertion = walletInstanceJwt,
                    redirectUri = redirectUri
                )
                if (tokenResponse != null) {
                    val accessToken = tokenResponse.accessToken.orEmpty()
                    sharedPreferences.saveToken(accessToken)
                    val jwtProof = PKCEUtils.generateJWTForProof(
                        context,
                        jwkThumbprint,
                        tokenResponse.cNonce.orEmpty()
                    )
                    cdProof.complete(jwtProof)
                } else {
                    throw PIDProviderException("token response is null")
                }
            } catch (error: Throwable) {
                cdProof.cancel(error.message.toString())
                PidSdkStartCallbackManager.invokeOnError(error)
            }
        }
        return cdProof.await()
    }

    suspend fun getCredential(pidCieData: PidCieData?): PidCredential {
        val cdRequestUri = CompletableDeferred<PidCredential>()
        withContext(Dispatchers.IO) {
            try {
                val sharedPreferences = PidProviderSDKShared.getInstance(context)
                val accessToken = sharedPreferences.getToken()

                val credentialUrl = UrlUtils.buildCredentialUrl(context)
                val dPopCredential = DPoPUtils.generateDPoP(context, "POST", credentialUrl)
                val signedJwtForProof = sharedPreferences.getSignedJWTProof()
                val proof = Proof().apply {
                    this.proofType = PKCEConstant.PROOF_TYPE
                    this.jwt = signedJwtForProof
                    this.pidCieData = pidCieData
                }
                val credentialResponse = dataSource.requestCredential(
                    dPop = dPopCredential,
                    authorization = accessToken,
                    credentialDefinition = PKCEConstant.CREDENTIAL_DEFINITION_VALUE,
                    format = PKCEConstant.FORMAT_CREDENTIAL_DEFINITION,
                    proof = Gson().toJson(proof)
                )

                if (credentialResponse != null) {
                    val pidCredential = PidCredential(
                        credentialResponse.format,
                        credentialResponse.credential,
                        credentialResponse.cNonce,
                        credentialResponse.cNonceExpiresIn
                    )
                    cdRequestUri.complete(pidCredential)
                } else {
                    throw PIDProviderException("credential response is null")
                }
            } catch (error: Throwable) {
                cdRequestUri.cancel(error.message.toString())
                PidSdkCompleteCallbackManager.invokeOnError(error)
            }
        }
        return cdRequestUri.await()
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + Job()
}