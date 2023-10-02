@file:Suppress("PrivatePropertyName")

package it.ipzs.androidpidproviderdemo

import android.app.Application
import it.ipzs.androidpidprovider.external.PidProviderConfig
import it.ipzs.androidpidprovider.external.PidProviderSdk

class PidProviderDemoApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        initializeSDK()
    }

    private fun initializeSDK() {
        val walletInstance = "eyJhbGciOiJFUzI1NiIsImtpZCI6IjV0NVlZcEJoTi1FZ0lFRUk1aVV6cjZyME1SMDJMblZRME9tZWttTktjalkiLCJ0cnVzdF9jaGFpbiI6WyJleUpoYkdjaU9pSkZVei4uLjZTMEEiLCJleUpoYkdjaU9pSkZVei4uLmpKTEEiLCJleUpoYkdjaU9pSkZVei4uLkg5Z3ciXSwidHlwIjoidmErand0IiwieDVjIjpbIk1JSUJqRENDIC4uLiBYRmVoZ0tRQT09Il19.eyJpc3MiOiJodHRwczovL3dhbGxldC1wcm92aWRlci5leGFtcGxlLm9yZyIsInN1YiI6InZiZVhKa3NNNDV4cGh0QU5uQ2lHNm1DeXVVNGpmR056b3BHdUt2b2dnOWMiLCJ0eXBlIjoiV2FsbGV0SW5zdGFuY2VBdHRlc3RhdGlvbiIsInBvbGljeV91cmkiOiJodHRwczovL3dhbGxldC1wcm92aWRlci5leGFtcGxlLm9yZy9wcml2YWN5X3BvbGljeSIsInRvc191cmkiOiJodHRwczovL3dhbGxldC1wcm92aWRlci5leGFtcGxlLm9yZy9pbmZvX3BvbGljeSIsImxvZ29fdXJpIjoiaHR0cHM6Ly93YWxsZXQtcHJvdmlkZXIuZXhhbXBsZS5vcmcvbG9nby5zdmciLCJhc2MiOiJodHRwczovL3dhbGxldC1wcm92aWRlci5leGFtcGxlLm9yZy9Mb0EvYmFzaWMiLCJjbmYiOnsiandrIjp7ImNydiI6IlAtMjU2Iiwia3R5IjoiRUMiLCJ4IjoiNEhOcHRJLXhyMnBqeVJKS0dNbno0V21kblFEX3VKU3E0Ujk1Tmo5OGI0NCIsInkiOiJMSVpuU0IzOXZGSmhZZ1MzazdqWEU0cjMtQ29HRlF3WnRQQklScXBObHJnIiwia2lkIjoidmJlWEprc000NXhwaHRBTm5DaUc2bUN5dVU0amZHTnpvcEd1S3ZvZ2c5YyJ9fSwiYXV0aG9yaXphdGlvbl9lbmRwb2ludCI6ImV1ZGl3OiIsInJlc3BvbnNlX3R5cGVzX3N1cHBvcnRlZCI6WyJ2cF90b2tlbiJdLCJ2cF9mb3JtYXRzX3N1cHBvcnRlZCI6eyJqd3RfdnBfanNvbiI6eyJhbGdfdmFsdWVzX3N1cHBvcnRlZCI6WyJFUzI1NiJdfSwiand0X3ZjX2pzb24iOnsiYWxnX3ZhbHVlc19zdXBwb3J0ZWQiOlsiRVMyNTYiXX19LCJyZXF1ZXN0X29iamVjdF9zaWduaW5nX2FsZ192YWx1ZXNfc3VwcG9ydGVkIjpbIkVTMjU2Il0sInByZXNlbnRhdGlvbl9kZWZpbml0aW9uX3VyaV9zdXBwb3J0ZWQiOmZhbHNlLCJpYXQiOjE2ODcyODExOTUsImV4cCI6MTY4NzI4ODM5NX0.OTuPik6p3o9j6VOx-uCyxRvHwoh1pDiiZcBQFNQt2uE3dK-8izGNflJVETi_uhGSZOf25Enkq-UvEin9NrbJNw"

        val pidProviderConfig = PidProviderConfig
            .Builder()
            .baseUrl("https://api.eudi-wallet-it-pid-provider.it/")
            .walletInstance(walletInstance)
            .walletUri("https://www.google.com")
            .logEnabled(true)
            .build()
        PidProviderSdk.initialize(
            applicationContext,
            pidProviderConfig = pidProviderConfig
        )
    }

}