@file:Suppress("SpellCheckingInspection")

package it.ipzs.androidpidprovider.utils

internal object PKCEConstant {

    const val HEADER_JWT_ALG_KEY = "alg"

    const val HEADER_JWT_ALG_VALUE = "s256"

    const val HEADER_JWT_KID_KEY = "kid"

    const val HEADER_JWT_TYP_KEY = "typ"

    const val HEADER_JWT_TYP_VALUE = "openid4vci-proof+jwt"

    const val JWT_CLAIM_VALUE = "eu.eudiw.pid.it"

    const val JWT_TYPE_VALUE = "type"

    const val JWT_FORMAT_VALUE = "vc+sd-jwt"

    const val JWT_RESPONSE_TYPE_KEY = "response_type"

    const val JWT_RESPONSE_TYPE_VALUE = "code"

    const val JWT_CLIENT_ID_KEY = "client_id"

    const val JWT_CODE_CHALLENGE_KEY = "code_challenge"

    const val JWT_CODE_CHALLENGE_METHOD_KEY = "code_challenge_method"

    const val JWT_CODE_CHALLENGE_METHOD_VALUE = "s256"

    const val JWT_REDIRECT_URI_KEY = "redirect_uri"

    const val JWT_CLIENT_ASSERTION_TYPE_KEY = "client_assertion_type"

    const val JWT_CLIENT_ASSERTION_TYPE_VALUE = "urn:ietf:params:oauth:client-assertion-type:jwt-bearer"

    const val JWT_CLIENT_ASSERTION_KEY = "client_assertion"

    const val JWT_AUTHORIZATION_DETAILS_KEY = "authorization_details"

    const val JWT_STATE_KEY = "state"

    const val JWT_ISS_KEY = "iss"

    const val JWT_AUD_KEY = "aud"

    const val JWT_IAT_KEY = "iat"

    const val JWT_NONCE_KEY = "nonce"

    const val CREDENTIAL_DEFINITION_VALUE = "{\"type\":[\"eu.eudiw.pid.it\"]}"

    const val FORMAT_CREDENTIAL_DEFINITION = "vc+sd-jwt"

    const val PROOF_TYPE = "jwt"
}