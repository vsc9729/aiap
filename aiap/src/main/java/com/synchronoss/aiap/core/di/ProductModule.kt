package com.synchronoss.aiap.core.di

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.synchronoss.aiap.core.data.remote.product.ProductApi
import com.synchronoss.aiap.core.data.repository.product.ProductManagerImpl
import com.synchronoss.aiap.core.domain.repository.billing.BillingManager
import com.synchronoss.aiap.core.domain.repository.product.ProductManager
import com.synchronoss.aiap.core.domain.usecases.product.GetActiveSubscription
import com.synchronoss.aiap.core.domain.usecases.product.GetProductsApi
import com.synchronoss.aiap.core.domain.usecases.product.HandlePurchase
import com.synchronoss.aiap.core.domain.usecases.product.ProductManagerUseCases
import com.synchronoss.aiap.utils.CacheManager
import com.synchronoss.aiap.utils.Constants.SSLPinning.PUBLIC_KEY_HASH
import com.synchronoss.aiap.utils.UrlManager
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.CertificatePinner
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

/**
 * Dagger module for providing product-related dependencies.
 * Provides singleton instances of network and product management components.
 */
@Module
object ProductModule {

    /**
     * Provides a singleton instance of Moshi for JSON parsing.
     * @return A new instance of Moshi with Kotlin support
     */
    @Provides
    @Singleton
    fun provideMoshi(): Moshi {
        return Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    /**
     * Provides a singleton instance of OkHttpClient.
     * Configures logging and SSL pinning for network requests.
     * @return A new instance of OkHttpClient with custom configuration
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            setLevel(HttpLoggingInterceptor.Level.BODY)
        }

        val clientBuilder = OkHttpClient.Builder().addInterceptor(logging)

        // Conditionally apply SSL pinning
        if (!UrlManager.useEventCloudIap) {
            val certificatePinner = CertificatePinner.Builder()
                .add(UrlManager.getApiHostname(), PUBLIC_KEY_HASH)
                .build()
            clientBuilder.certificatePinner(certificatePinner)
        }

        return clientBuilder.build()
    }

    /**
     * Provides a singleton instance of Retrofit.
     * @param moshi The Moshi instance for JSON parsing
     * @param okHttpClient The OkHttpClient instance for network requests
     * @return A new instance of Retrofit configured with the base URL and converters
     */
    @Provides
    @Singleton
    fun provideRetrofit(moshi: Moshi, okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(UrlManager.getBaseUrl())
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .client(okHttpClient)
            .build()
    }

    /**
     * Provides a singleton instance of ProductApi.
     * @param retrofit The Retrofit instance
     * @return A new instance of ProductApi implementation
     */
    @Provides
    @Singleton
    fun provideProductApi(retrofit: Retrofit): ProductApi {
        return retrofit.create(ProductApi::class.java)
    }

    /**
     * Provides a singleton instance of ProductManager.
     * @param api The ProductApi instance
     * @param cacheManager The CacheManager instance
     * @return A new instance of ProductManagerImpl
     */
    @Provides
    @Singleton
    fun provideProductManager(api: ProductApi, cacheManager: CacheManager): ProductManager {
        return ProductManagerImpl(api, cacheManager)
    }

    /**
     * Provides a singleton instance of ProductManagerUseCases.
     * @param productManager The ProductManager instance
     * @return A new instance of ProductManagerUseCases with all required use cases
     */
    @Provides
    @Singleton
    fun provideProductManagerUseCases(productManager: ProductManager): ProductManagerUseCases {
        return ProductManagerUseCases(
            getProductsApi = GetProductsApi(productManager),
            getActiveSubscription = GetActiveSubscription(productManager),
            handlePurchase = HandlePurchase(productManager)
        )
    }
}