package com.synchronoss.aiap.di

import com.squareup.moshi.Moshi
import com.synchronoss.aiap.data.remote.ProductApi
import com.synchronoss.aiap.data.repository.product.ProductMangerImpl
import com.synchronoss.aiap.domain.repository.product.ProductManager
import com.synchronoss.aiap.domain.usecases.product.GetProductsApi
import com.synchronoss.aiap.domain.usecases.product.ProductManagerUseCases
import com.synchronoss.aiap.utils.Constants.BASE_URL

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ProductModule {



    @Provides
    @Singleton
    fun provideProductApi(): ProductApi {

        val logging = HttpLoggingInterceptor().apply {
            setLevel(HttpLoggingInterceptor.Level.BODY)
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://your.api.url/")
            .addConverterFactory(MoshiConverterFactory.create())
            .client(client)
            .build()

        val productApi = retrofit.create(ProductApi::class.java)
        return productApi
    }



    @Provides
    @Singleton
    fun provideProductManager(api: ProductApi): ProductManager {
        return ProductMangerImpl(api)
    }

    @Provides
    @Singleton
    fun provideProductManagerUseCases(productManager: ProductManager): ProductManagerUseCases {
        return ProductManagerUseCases(
            getProductsApi = GetProductsApi(productManager)
        )
    }
}