package it.ipzs.androidpidprovider.storage

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.nimbusds.jose.jwk.JWK
import it.ipzs.androidpidprovider.R
import it.ipzs.androidpidprovider.utils.SingletonHolder


internal class PidProviderSDKShared private constructor(context: Context) {

    companion object :
        SingletonHolder<PidProviderSDKShared, Context>(::PidProviderSDKShared) {
        private val KEY_SECURE_KEY_SHARED_NAME = R::class.java.name.plus(".pid_provider_shared")
        private val KEY_SECURE_KEY_BASE_URL = KEY_SECURE_KEY_SHARED_NAME.plus(".pid_base_url")
        private val KEY_SECURE_KEY_LOG_ENABLED = KEY_SECURE_KEY_SHARED_NAME.plus(".pid_log_enabled")
        private val KEY_SECURE_KEY_WALLET_INSTANCE_ATTESTATION = KEY_SECURE_KEY_SHARED_NAME.plus(".pid_instance_attestation")
        private val KEY_SECURE_KEY_WALLET_URI = KEY_SECURE_KEY_SHARED_NAME.plus(".pid_wallet_uri")
        private val KEY_SECURE_KEY_CODE_VERIFIER = KEY_SECURE_KEY_SHARED_NAME.plus(".pid_code_verifier")
        private val KEY_SECURE_KEY_CODE_CHALLENGE = KEY_SECURE_KEY_SHARED_NAME.plus(".pid_code_challenge")
        private val KEY_SECURE_KEY_CLIENT_ID = KEY_SECURE_KEY_SHARED_NAME.plus(".pid_client_id")
        private val KEY_SECURE_KEY_TOKEN = KEY_SECURE_KEY_SHARED_NAME.plus(".pid_token")
        private val KEY_SECURE_KEY_JWT_PAR = KEY_SECURE_KEY_SHARED_NAME.plus(".pid_jwt_par")
        private val KEY_SECURE_KEY_UNSIGNED_JWT_PROOF = KEY_SECURE_KEY_SHARED_NAME.plus(".pid_unsigned_jwt_proof")
        private val KEY_SECURE_KEY_SIGNED_JWT_PROOF = KEY_SECURE_KEY_SHARED_NAME.plus(".pid_signed_jwt_proof")
        private val KEY_SECURE_KEY_JWK = KEY_SECURE_KEY_SHARED_NAME.plus(".pid_jwk")
    }

    private val sharedPreferences: SharedPreferences by lazy { getSecureSharedPreferences(context) }

    private fun getSecureSharedPreferences(context: Context): SharedPreferences {
        try {
            val spec = KeyGenParameterSpec.Builder(
                MasterKey.DEFAULT_MASTER_KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()

            val masterKey: MasterKey = MasterKey.Builder(context)
                .setKeyGenParameterSpec(spec)
                .build()

            return EncryptedSharedPreferences.create(
                context,
                KEY_SECURE_KEY_SHARED_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Throwable) {
            return context.getSharedPreferences(
                KEY_SECURE_KEY_SHARED_NAME,
                Context.MODE_PRIVATE
            )
        }
    }

    // BaseUrl

    fun saveBaseURL(baseURL: String) {
        sharedPreferences.edit().apply {
            putString(KEY_SECURE_KEY_BASE_URL, baseURL)
            apply()
        }
    }

    fun getBaseURL(): String {
        return sharedPreferences.getString(KEY_SECURE_KEY_BASE_URL, "").orEmpty()
    }

    // IsLog

    fun saveLogEnabled(logEnabled: Boolean) {
        sharedPreferences.edit().apply {
            putBoolean(KEY_SECURE_KEY_LOG_ENABLED, logEnabled)
            apply()
        }
    }

    fun isLogEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_SECURE_KEY_LOG_ENABLED, false)
    }

    // Wallet Instance Attestation

    fun saveWalletInstanceAttestation(walletInstanceAttestation: String) {
        sharedPreferences.edit().apply {
            putString(KEY_SECURE_KEY_WALLET_INSTANCE_ATTESTATION, walletInstanceAttestation)
            apply()
        }
    }

    fun getWalletInstanceAttestation(): String {
        return sharedPreferences.getString(KEY_SECURE_KEY_WALLET_INSTANCE_ATTESTATION, "").orEmpty()
    }

    // Wallet Uri

    fun saveWalletUri(wallerUri: String) {
        sharedPreferences.edit().apply {
            putString(KEY_SECURE_KEY_WALLET_URI, wallerUri)
            apply()
        }
    }

    fun getWalletUri(): String {
        return sharedPreferences.getString(KEY_SECURE_KEY_WALLET_URI, "").orEmpty()
    }

    // Code verifier

    fun saveCodeVerifier(codeVerifier: String) {
        sharedPreferences.edit().apply {
            putString(KEY_SECURE_KEY_CODE_VERIFIER, codeVerifier)
            apply()
        }
    }

    fun getCodeVerifier(): String {
        return sharedPreferences.getString(KEY_SECURE_KEY_CODE_VERIFIER, "").orEmpty()
    }

    // Code challenge

    fun saveCodeChallenge(codeChallenge: String) {
        sharedPreferences.edit().apply {
            putString(KEY_SECURE_KEY_CODE_CHALLENGE, codeChallenge)
            apply()
        }
    }

    fun getCodeChallenge(): String {
        return sharedPreferences.getString(KEY_SECURE_KEY_CODE_CHALLENGE, "").orEmpty()
    }

    // Client Id

    fun saveClientId(clientId: String) {
        sharedPreferences.edit().apply {
            putString(KEY_SECURE_KEY_CLIENT_ID, clientId)
            apply()
        }
    }

    fun getClientId(): String {
        return sharedPreferences.getString(KEY_SECURE_KEY_CLIENT_ID, "").orEmpty()
    }

    // Token

    fun saveToken(token: String) {
        sharedPreferences.edit().apply {
            putString(KEY_SECURE_KEY_TOKEN, token)
            apply()
        }
    }

    fun getToken(): String {
        return sharedPreferences.getString(KEY_SECURE_KEY_TOKEN, "").orEmpty()
    }

    // JWT PAR

    fun saveUnsignedJWTPar(jwt: String) {
        sharedPreferences.edit().apply {
            putString(KEY_SECURE_KEY_JWT_PAR, jwt)
            apply()
        }
    }

    fun getUnsignedJWTPar(): String {
        return sharedPreferences.getString(KEY_SECURE_KEY_JWT_PAR, "").orEmpty()
    }

    // JWT PROOF

    fun saveUnsignedJWTProof(jwt: String) {
        sharedPreferences.edit().apply {
            putString(KEY_SECURE_KEY_UNSIGNED_JWT_PROOF, jwt)
            apply()
        }
    }

    fun getUnsignedJWTProof(): String {
        return sharedPreferences.getString(KEY_SECURE_KEY_UNSIGNED_JWT_PROOF, "").orEmpty()
    }

    fun saveSignedJWTProof(jwt: String) {
        sharedPreferences.edit().apply {
            putString(KEY_SECURE_KEY_SIGNED_JWT_PROOF, jwt)
            apply()
        }
    }

    fun getSignedJWTProof(): String {
        return sharedPreferences.getString(KEY_SECURE_KEY_SIGNED_JWT_PROOF, "").orEmpty()
    }

    // JWK

    fun saveJWK(jwkJsonString: String) {
        sharedPreferences.edit().apply {
            putString(KEY_SECURE_KEY_JWK, jwkJsonString)
            apply()
        }
    }

    fun getJWK(): JWK {
        val jwkJsonString = sharedPreferences.getString(KEY_SECURE_KEY_JWK, "").orEmpty()
        return JWK.parse(jwkJsonString)
    }
}