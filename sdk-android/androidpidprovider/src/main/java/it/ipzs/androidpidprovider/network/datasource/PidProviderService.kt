package it.ipzs.androidpidprovider.network.datasource

import it.ipzs.androidpidprovider.network.response.CredentialResponse
import it.ipzs.androidpidprovider.network.response.ParResponse
import it.ipzs.androidpidprovider.network.response.TokenResponse
import retrofit2.http.*

internal interface PidProviderService {

    @FormUrlEncoded
    @Headers("Accept: application/json",
        "Content-Type: application/x-www-form-urlencoded")
    @POST("as/par")
    suspend fun requestPar(
        @Field("response_type") responseType: String,
        @Field("client_id") clientId: String,
        @Field("code_challenge") codeChallenge: String,
        @Field("code_challenge_method") codeChallengeMethod: String,
        @Field("client_assertion_type") clientAssertionType: String,
        @Field("client_assertion") clientAssertion: String,
        @Field("request") request: String
    ): ParResponse

    @FormUrlEncoded
    @POST("token")
    suspend fun requestToken(
        @Header("DPoP") dPop: String,
        @Field("grant_type") grantType: String,
        @Field("client_id") clientId: String,
        @Field("code") code: String,
        @Field("code_verifier") codeVerifier: String,
        @Field("client_assertion_type") clientAssertionType: String,
        @Field("client_assertion") clientAssertion: String,
        @Field("redirect_uri") redirectUri: String
    ): TokenResponse

    @FormUrlEncoded
    @POST("credential")
    suspend fun requestCredential(
        @Header("DPoP") dPop: String,
        @Header("Authorization") authorization: String,
        @Field("credential_definition") credentialDefinition: String,
        @Field("format") format: String,
        @Field("proof") proof: String?
    ): CredentialResponse

}