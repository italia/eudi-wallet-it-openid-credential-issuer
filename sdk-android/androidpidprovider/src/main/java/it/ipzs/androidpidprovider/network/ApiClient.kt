package it.ipzs.androidpidprovider.network

import android.content.Context
import com.google.gson.GsonBuilder
import it.ipzs.androidpidprovider.utils.NetworkUtils
import it.ipzs.androidpidprovider.utils.PidProviderConfigUtils
import okhttp3.Authenticator
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

internal class ApiClient(
    private val context: Context,
    private val baseUrl: String,
    private var dateFormat: String? = null,
    private val authenticator: Authenticator? = null
) {

    fun get(): Retrofit {

        if (dateFormat == null) {
            dateFormat = NetworkUtils.getDefaultDateFormat()
        }
        val builder = OkHttpClient.Builder()
        // Creating Http Interceptor
        val isLogEnabled = PidProviderConfigUtils.isLogEnabled(context)
        if(isLogEnabled) {
            val interceptor = HttpLoggingInterceptor()
            interceptor.level = HttpLoggingInterceptor.Level.BODY
            builder.addInterceptor(interceptor)
        }

        authenticator?.let {
            builder.authenticator(it)
        }

        // Creating Http Client
        val client = builder
            .connectTimeout(NetworkUtils.getConnectionTimeout(), TimeUnit.SECONDS)
            .readTimeout(NetworkUtils.getReadTimeout(), TimeUnit.SECONDS)
            .build()

        // Creating Gson
        val gson = GsonBuilder()
            .setDateFormat(dateFormat)
            .create()

        // Creating Api Service Interface
        return Retrofit.Builder()
            .client(client)
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }
}