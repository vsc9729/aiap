package com.synchronoss.aiap.core.di

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.synchronoss.aiap.core.data.remote.product.ProductApi
import com.synchronoss.aiap.core.data.repository.product.ProductManagerImpl
import com.synchronoss.aiap.core.domain.repository.billing.BillingManager
import com.synchronoss.aiap.core.domain.repository.product.ProductManager
import com.synchronoss.aiap.core.domain.usecases.product.GetActiveSubscription
import com.synchronoss.aiap.core.domain.usecases.product.GetProductsApi
import com.synchronoss.aiap.core.domain.usecases.product.ProductManagerUseCases
import com.synchronoss.aiap.utils.CacheManager
import com.synchronoss.aiap.utils.Constants.BASE_URL

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import java.security.SecureRandom
import java.security.cert.X509Certificate

@Module
@InstallIn(SingletonComponent::class)
object ProductModule {



    @Provides
    @Singleton
    fun provideProductApi(): ProductApi {
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        val logging = HttpLoggingInterceptor().apply {
            setLevel(HttpLoggingInterceptor.Level.BODY)
        }

        // Create a trust manager that does not validate certificate chains
        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        })

        // Install the all-trusting trust manager
        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, trustAllCerts, SecureRandom())

        val client = OkHttpClient.Builder()
            .sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
            .hostnameVerifier { _, _ -> true }
            .addInterceptor(logging)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .client(client)
            .build()

        return retrofit.create(ProductApi::class.java)
    }



    @Provides
    @Singleton
    fun provideProductManager(api: ProductApi, billingManager: BillingManager, cacheManager: CacheManager ): ProductManager {
        return ProductManagerImpl(api, billingManager, cacheManager)
    }

    @Provides
    @Singleton
    fun provideProductManagerUseCases(productManager: ProductManager): ProductManagerUseCases {
        return ProductManagerUseCases(
            getProductsApi = GetProductsApi(productManager),
            getActiveSubscription = GetActiveSubscription(productManager),
        )
    }
}