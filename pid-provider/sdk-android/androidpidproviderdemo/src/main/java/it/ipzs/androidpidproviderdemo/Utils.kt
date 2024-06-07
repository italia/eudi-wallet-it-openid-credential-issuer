package it.ipzs.androidpidproviderdemo

import android.security.keystore.KeyProperties
import android.util.Base64
import com.authlete.sd.Disclosure
import com.nimbusds.jose.jwk.Curve
import com.nimbusds.jose.jwk.ECKey
import com.nimbusds.jose.jwk.KeyUse
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.json.JSONObject
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.interfaces.ECPrivateKey
import java.security.interfaces.ECPublicKey
import java.util.*

object Utils {

    fun signJwt(keyPair: KeyPair, jwtJsonString: String?): String {
        val unsignedJwt = Jwts.parser().parse(jwtJsonString)
        return Jwts.builder()
            .setHeader(unsignedJwt.header)
            .setClaims(unsignedJwt.body as Claims)
            .signWith(SignatureAlgorithm.ES256, keyPair.private)
            .compact()
    }

    fun generateKeyPair(): KeyPair {
        val keyPairGenerator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_EC)
        keyPairGenerator.initialize(Curve.P_256.toECParameterSpec())
        return keyPairGenerator.generateKeyPair()
    }

    fun generateJWK(keyPair: KeyPair): String? {
        return try {
            val jwk = ECKey.Builder(Curve.P_256, keyPair.public as ECPublicKey)
                .privateKey(keyPair.private as ECPrivateKey)
                .keyUse(KeyUse.SIGNATURE)
                .keyID(UUID.randomUUID().toString())
                .build()
            jwk.toJSONString()
        } catch (e:Throwable){
            null
        }
    }

    fun decodeJwt(credential: String?): String? {
        return try {
            val credentialArray = credential?.split(".")
            val jwtBodyString = credentialArray?.get(1)
            val jsonBodyString = Base64.decode(jwtBodyString, Base64.DEFAULT).toString(charset("UTF-8"))
            val jsonObject = JSONObject(jsonBodyString)
            jsonObject.toString(4)
        } catch (e: Throwable) {
            e.message.toString()
        }
    }

    fun decodeClaims(credentialJwt: String?): ArrayList<Disclosure> {
        val disclosureList: ArrayList<Disclosure> = arrayListOf()
        return try {
            val sdJwtArray = credentialJwt?.split("~") ?: emptyList()
            sdJwtArray.forEachIndexed { index, value ->
                if (index != 0) {
                    val disclosure = Disclosure.parse(value)
                    disclosureList.add(disclosure)
                }
            }
            disclosureList
        } catch (e: Throwable) {
            disclosureList
        }
    }
}