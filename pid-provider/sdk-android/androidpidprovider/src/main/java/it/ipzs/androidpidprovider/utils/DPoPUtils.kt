package it.ipzs.androidpidprovider.utils

import android.content.Context
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.oauth2.sdk.dpop.DPoPProofFactory
import com.nimbusds.oauth2.sdk.dpop.DefaultDPoPProofFactory
import it.ipzs.androidpidprovider.storage.PidProviderSDKShared
import java.net.URI

internal object DPoPUtils {

    fun generateDPoP(context: Context, httpMethod: String, httpUrl: String): String {
        val jwk = PidProviderSDKShared.getInstance(context).getJWK()
        val proofFactory: DPoPProofFactory = DefaultDPoPProofFactory(
            jwk,
            JWSAlgorithm.ES256
        )
        val proof = proofFactory.createDPoPJWT(
            httpMethod,
            URI(httpUrl)
        )
        return proof.serialize()
    }

}