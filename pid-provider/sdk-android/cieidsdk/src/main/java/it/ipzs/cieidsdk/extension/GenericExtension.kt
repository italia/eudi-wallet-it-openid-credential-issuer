@file:Suppress("SpellCheckingInspection")

package it.ipzs.cieidsdk.extension

import org.spongycastle.asn1.ASN1ObjectIdentifier
import org.spongycastle.asn1.x500.RDN
import org.spongycastle.asn1.x500.X500Name
import org.spongycastle.asn1.x500.style.IETFUtils

fun getValueFromRdns(dn: X500Name?, key: ASN1ObjectIdentifier?): RDN? {
    return try {
        dn?.getRDNs(key)?.get(0)
    } catch (e: Throwable) {
        return null
    }
}

fun rdnToString(rdn: RDN?): String? {
    return try {
        IETFUtils.valueToString(rdn?.first?.value)
    } catch (e: Throwable) {
        return ""
    }
}