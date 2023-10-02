
EUDI-it-wallet-pid-provider-android-sdk

EUDI-it-wallet-pid-provider-android-sdk is an sdk developed in Kotlin that include the functionality to obtain the Italian PID (Person Identification Data) credential
according to [Italian EUDI Wallet Technical Specifications](https://italia.github.io/eudi-wallet-it-docs/en/pid-issuance.html).

# Technical requirements:

EUDI-it-wallet-pid-provider-android-sdk is compatible with android min sdk 23 and above. Is mandatory to have internet connection and NFC technology.

# Integration requirements:

//TODO

# How to use it:

In the sdk is present a demo application called AndroidPidProviderDemo that show how to integrate it easily.

# Configuration :

This method is used for intialize the EUDI-it-wallet-pid-provider-android-sdk :


    initialize(context: Context, pidProviderConfig: PidProviderConfig? = null)

	
	class PidProviderConfig internal constructor(
	    var logEnabled: Boolean? = false,
	    private var baseUrl:String? = null,
	    private var walletInstanceAttestation: String? = null,
	    private var walletUri: String? = null
	) : Serializable  
	

	
	Initialize method is used to configure sdk with following parameters : 

	- log enabled : if true enable sdk logs
	- baseUrl : Url for the PID provider api needed by the sdk
 	- walletInstanceAttestation : As defined in the techical specification
	- walletUri : Domain of the wallet application

After initialize the sdk methods are to be called in the following order:

- initJwtForPar(context:Context):String?
  This method is used for create Jwt for par request described in PKCE flow. Returns an unsigned JWT that the calling application will have to sign it and pass it into the next API startAuthFlow.

- fun startAuthFlow(activity: AppCompatActivity, signedJwtForPar: String, jwkForDPoP: String, pidSdkCallback: IPidSdkCallback<Boolean>){

  interface IPidSdkCallback<T> {

  fun onComplete(result: T?)

  fun onError(throwable: Throwable)

  }

This method is used for start the process to obtain the PID. It requires the signed jwt of the previous api and a jwk for the DPoP described in the PKCE Flow. Returns true when the process is finished with success result.

DPoP jwk example:

{
"typ": "dpop+jwt",
"alg": "ES256",
"jwk": {
"kty": "EC",
"use": "sig",
"crv": "P-256",
"kid": "0b922c6d-4a52-4898-a39b-13a0d1468c57",
"x": "usMMFcVYD_K3WpbS8lOU_YqW2LtXhKU7Y7ZbRL9hRiY",
"y": "Ly4MR09mIi85aQFfz0BIwkZWnGgVHvt7mkCHM4zy6NU"
}
}
.
{
"htm": "POST",
"htu": "http://localhost:8080/credential",
"iat": 1688496689,
"jti": "zqhl40Vt2p9mMKoB"
}

- fun getUnsignedJwtForProof(context: Context): String
  This method is used to recover the unsigned jwt. The calling application will have to sign it and pass it in the api completeAuthFlow to retrieve the credentials.

- fun completeAuthFlow(activity: AppCompatActivity, signedJwtForProof: String, pidSdkCallback: IPidSdkCallback<PidCredential>)
  This method complete the PKCE flow and returns the italian PID Credential.

data class PidCredential(
var format: String?,
var credential: String?,
var nonce: String?,
var nonceExpires: Long?
) : Serializable

The format of this data class is defined by Italian EUDI Wallet Technical Specifications

# License: Apache License Version 2.0





 

 


