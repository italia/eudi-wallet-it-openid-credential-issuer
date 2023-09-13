package it.ipzs.androidpidprovider.network.datasource

import android.content.Context
import it.ipzs.androidpidprovider.network.ApiClient
import it.ipzs.androidpidprovider.network.response.CredentialResponse
import it.ipzs.androidpidprovider.network.response.ParResponse
import it.ipzs.androidpidprovider.network.response.TokenResponse
import it.ipzs.androidpidprovider.utils.PidProviderConfigUtils

internal class PidProviderDataSourceImpl(
    context: Context
) : PidProviderDataSource {

    private val apiClient by lazy {
        ApiClient(context, PidProviderConfigUtils.getBaseUrl(context)).get()
    }

    private val pidProviderService by lazy {
        apiClient.create(PidProviderService::class.java)
    }


    override suspend fun requestPar(
        responseType: String,
        clientId: String,
        codeChallenge: String,
        codeChallengeMethod: String,
        clientAssertionType: String,
        clientAssertion: String,
        request: String
    ): ParResponse? =
        pidProviderService?.requestPar(
            responseType,
            clientId,
            codeChallenge,
            codeChallengeMethod,
            clientAssertionType,
            clientAssertion,
            request
        )!!

    override suspend fun requestToken(
        dPop: String,
        grantType: String,
        clientId: String,
        code: String,
        codeVerifier: String,
        clientAssertionType: String,
        clientAssertion: String,
        redirectUri: String
    ): TokenResponse? =
        pidProviderService?.requestToken(
            dPop,
            grantType,
            clientId,
            code,
            codeVerifier,
            clientAssertionType,
            clientAssertion,
            redirectUri
        )!!

    override suspend fun requestCredential(
        dPop: String,
        authorization: String,
        credentialDefinition: String,
        format: String,
        proof: String?
    ): CredentialResponse? = pidProviderService?.requestCredential(
        dPop,
        authorization,
        credentialDefinition,
        format,
        proof
    )!!

}