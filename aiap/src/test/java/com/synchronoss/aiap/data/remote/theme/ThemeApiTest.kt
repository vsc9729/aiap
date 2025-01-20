package com.synchronoss.aiap.data.remote.theme

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class ThemeApiTest {

    private lateinit var themeApi: ThemeApi
    private lateinit var mockWebServer: MockWebServer
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        themeApi = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(ThemeApi::class.java)
    }

    @Test
    fun `test getATheme returns light and dark response`() {
        // Given
        val mockResponse = """
            {"code":200,"title":"SUCCESS","message":"","data":[{"themeId":"1545f90e-223a-42bd-8a44-e50db6ad197c","themeName":"Sample","logoUrl":"","primaryColor":"#ffffff","secondaryColor":"#000000"},{"themeId":"23af3abb-7398-4e6f-9372-45014350710c","themeName":"Light","logoUrl":"https://irgdigital.com/providers/verizon/assets/images/logo.png","primaryColor":"#CD040B","secondaryColor":"#FECDCE"},{"themeId":"44ce9150-e1f1-415b-b3af-ec6efbe56aaf","themeName":"Dark","logoUrl":"https://i.ibb.co/smzMnGT/Verizon-2024.png","primaryColor":"#CD040B","secondaryColor":"#262627"},{"themeId":"4c5ef80b-bd3f-4d05-a85a-0611f31aed27","themeName":"Sample","logoUrl":"","primaryColor":"#ffffff","secondaryColor":"#000000"},{"themeId":"629dfd8f-1286-4b8c-96eb-b6f4e045e5bf","themeName":"Sample","logoUrl":"","primaryColor":"#ffffff","secondaryColor":"#000000"},{"themeId":"678271e1-c972-431b-840f-3bf9a756691f","themeName":"<string>","logoUrl":"<string>","primaryColor":"<string>","secondaryColor":"<string>"},{"themeId":"777c44bc-d9b6-4982-b131-757767b6c409","themeName":"Sample","logoUrl":"","primaryColor":"#ffffff","secondaryColor":"#000000"},{"themeId":"a5565e5d-d76b-4915-836c-85836fd0d550","themeName":"Sample","logoUrl":"","primaryColor":"#ffffff","secondaryColor":"#32a852"},{"themeId":"d6164145-80ee-4eaf-b887-10697858a15c","themeName":"Sample","logoUrl":"","primaryColor":"#ffffff","secondaryColor":"#000000"}]}
        """.trimIndent()

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(mockResponse)
        )

        // When
        val response = runBlocking { themeApi.getTheme(apiKey = "IAPAppAndroid") }

        // Then

        assertEquals(200, response.code)
        assertEquals("Light", response.data.findLast { it.themeName == "Light"}?.themeName)
        assertEquals("Dark", response.data.findLast { it.themeName == "Dark"}?.themeName)
    }


    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }
}