package it.ipzs.androidpidproviderdemo

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import it.ipzs.androidpidprovider.external.IPidSdkCallback
import it.ipzs.androidpidprovider.external.PidCredential
import it.ipzs.androidpidprovider.external.PidProviderSdk
import it.ipzs.androidpidproviderdemo.Utils.generateJWK
import it.ipzs.androidpidproviderdemo.Utils.generateKeyPair
import it.ipzs.androidpidproviderdemo.Utils.signJwt
import it.ipzs.androidpidproviderdemo.base.ABaseActivity
import it.ipzs.androidpidproviderdemo.cie.DisclosureAdapter
import it.ipzs.androidpidproviderdemo.cie.NfcReaderDialog
import it.ipzs.androidpidproviderdemo.databinding.ActivityMainDemoBinding
import it.ipzs.cieidsdk.common.Callback
import it.ipzs.cieidsdk.common.CieIDSdk
import it.ipzs.cieidsdk.data.PidCieData
import it.ipzs.cieidsdk.event.Event


class MainDemoActivity: ABaseActivity<ActivityMainDemoBinding>(), Callback {

    companion object {
        val TAG: String = MainDemoActivity::class.java.simpleName
        const val serviceProviderUrl = "https://sp.collaudo.idserver.servizicie.interno.gov.it/"
    }

    override fun setBinding(): ActivityMainDemoBinding =
        ActivityMainDemoBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerForContextMenu(binding.tvCredential)
        startSDKFlow()
    }

    private fun startSDKFlow() {

        // Retrieves the unsigned jwt for the par request
        val unsignedJwtForPar = PidProviderSdk.initJwtForPar(this)

        // Generates a keypair with the Elliptic-curve cryptography to sign the jwt and jwk
        val keyPair = generateKeyPair()

        // Signs the jwt for par with the generated keypair
        val signedJwtForPar = signJwt(keyPair, unsignedJwtForPar)

        // Generates a JWK for DPoP with the generated keypair
        val jwkForDPoP = generateJWK(keyPair) ?: ""

        // Starts the authentication flow with the signed jwt for par and the jwk for DPoP
        PidProviderSdk.startAuthFlow(
            this,
            signedJwtForPar,
            jwkForDPoP,
            object : IPidSdkCallback<Boolean> {
                override fun onComplete(result: Boolean?) {
                    runOnUiThread {
                        binding.progress.isVisible = false
                        loadServiceProvider()
                    }
                }

                override fun onError(throwable: Throwable) {
                    runOnUiThread {
                        binding.progress.isVisible = false
                        Toast.makeText(this@MainDemoActivity, throwable.message, Toast.LENGTH_LONG)
                            .show()
                    }
                    Log.e(TAG, throwable.message.toString())
                }
            })
    }

    private fun completeAuthFlow(cieData: PidCieData?) {

        // Retrieves the unsigned jwt for proof
        val unsignedJwtForProof = PidProviderSdk.getUnsignedJwtForProof(this)

        // Generates a keypair with the Elliptic-curve cryptography to sign the jwt and jwk
        val keyPair = generateKeyPair()

        // Signs the jwt for proof with the generated keypair
        val signedJwtForProof = signJwt(keyPair, unsignedJwtForProof)

        // Completes the authentication flow with the signed jwt for proof
        PidProviderSdk.completeAuthFlow(this, cieData, signedJwtForProof, object : IPidSdkCallback<PidCredential> {
            override fun onComplete(result: PidCredential?) {
                val credential = result?.credential
                Utils.decodeClaims(credential)
                val jwtJsonBody = Utils.decodeJwt(credential)
                val disclosureArrayList = Utils.decodeClaims(credential)
                if(!jwtJsonBody.isNullOrEmpty()){
                    runOnUiThread {
                        binding.fragmentContainerView.isVisible = false
                        binding.resultScrollView.isVisible = true
                        binding.tvCredential.text = jwtJsonBody
                        binding.disclosureList.apply {
                            adapter = DisclosureAdapter(disclosureArrayList)
                            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
                        }
                    }
                }
            }

            override fun onError(throwable: Throwable) {
                Log.e(TAG, throwable.message.toString())
                runOnUiThread {
                    Toast.makeText(this@MainDemoActivity, throwable.message, Toast.LENGTH_LONG)
                        .show()
                }
            }
        })
    }


    @SuppressLint("SetJavaScriptEnabled")
    private fun loadServiceProvider() {
        binding.webViewPid.apply {
            webViewClient = object : WebViewClient() {

                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    val currentUri = request?.url
                    if (currentUri.toString().contains("idp/login/livello3?opId")) {
                        CieIDSdk.setUrl(currentUri.toString())
                        binding.fragmentContainerView.isVisible = true
                        return true
                    }
                    return super.shouldOverrideUrlLoading(view, request)
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    if (url == serviceProviderUrl) {
                        // Run script to click on level 3
                        view?.evaluateJavascript("document.getElementsByName(\"f3\")[0].submit()", null)
                    }
                }
            }
            settings.javaScriptEnabled = true
            loadUrl(serviceProviderUrl)
        }
    }

    override fun onResume() {
        super.onResume()
        CieIDSdk.startNFCListening(this)
    }

    override fun onPause() {
        super.onPause()
        CieIDSdk.stopNFCListening(this)
    }

    // CieIdSdk Callback

    override fun onEvent(event: Event) {
        Log.d(TAG, "onEvent: $event")
    }

    override fun onError(error: Throwable) {
        Log.d(TAG, "onError: ${error.message.toString()}")
        runOnUiThread {
            NfcReaderDialog.hide()
            Toast.makeText(this@MainDemoActivity, error.message, Toast.LENGTH_LONG)
                .show()
        }
    }

    override fun onSuccess(url: String, pinCieData: PidCieData?) {
        runOnUiThread {
            NfcReaderDialog.hide()
        }
        completeAuthFlow(pinCieData)
    }

}