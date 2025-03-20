package com.synchronoss.aiap.core.data.repository.billing

import org.junit.After
import org.junit.Before
import org.junit.Test
import java.lang.reflect.Field
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class GlobalBillingConfigTest {

    // Keep track of original field values so we can restore them
    private var originalApiKey: Field? = null
    private var originalUserUUID: Field? = null
    private var originalPartnerUserId: Field? = null
    private var apiKeyWasInitialized = false
    private var userUUIDWasInitialized = false
    private var partnerUserIdWasInitialized = false

    @Before
    fun setUp() {
        // Save the original state
        originalApiKey = GlobalBillingConfig::class.java.getDeclaredField("_apiKey")
        originalUserUUID = GlobalBillingConfig::class.java.getDeclaredField("_userUUID")
        originalPartnerUserId = GlobalBillingConfig::class.java.getDeclaredField("_partnerUserId")

        originalApiKey?.isAccessible = true
        originalUserUUID?.isAccessible = true
        originalPartnerUserId?.isAccessible = true

        // Check if fields were initialized
        val originalApiKeyField = originalApiKey?.get(GlobalBillingConfig)
        val originalUserUUIDField = originalUserUUID?.get(GlobalBillingConfig)
        val originalPartnerUserIdField = originalPartnerUserId?.get(GlobalBillingConfig)

        // Store initialization state
        apiKeyWasInitialized = originalApiKeyField != null
        userUUIDWasInitialized = originalUserUUIDField != null
        partnerUserIdWasInitialized = originalPartnerUserIdField != null
    }

    @After
    fun tearDown() {
        // Restore the original values if they were initialized
        if (apiKeyWasInitialized) {
            GlobalBillingConfig.apiKey = "restored_api_key"
        }
        if (userUUIDWasInitialized) {
            GlobalBillingConfig.userUUID = "restored_user_uuid" 
        }
        if (partnerUserIdWasInitialized) {
            GlobalBillingConfig.partnerUserId = "restored_partner_user_id"
        }
    }

    @Test
    fun `test apiKey getter when initialized`() {
        // Given 
        val testApiKey = "test_api_key"
        GlobalBillingConfig.apiKey = testApiKey

        // When & Then
        assertEquals(testApiKey, GlobalBillingConfig.apiKey)
    }

    @Test
    fun `test apiKey getter when not initialized`() {
        // Given - Make _apiKey uninitialized
        if (apiKeyWasInitialized) {
            val field = GlobalBillingConfig::class.java.getDeclaredField("_apiKey")
            field.isAccessible = true
            field.set(GlobalBillingConfig, null)
        }

        // When & Then
        assertFailsWith<UninitializedPropertyAccessException> {
            GlobalBillingConfig.apiKey
        }
    }

    @Test
    fun `test userUUID getter when initialized`() {
        // Given
        val testUserUUID = "test_user_uuid"
        GlobalBillingConfig.userUUID = testUserUUID

        // When & Then
        assertEquals(testUserUUID, GlobalBillingConfig.userUUID)
    }

    @Test
    fun `test userUUID getter when not initialized`() {
        // Given - Make _userUUID uninitialized
        if (userUUIDWasInitialized) {
            val field = GlobalBillingConfig::class.java.getDeclaredField("_userUUID")
            field.isAccessible = true
            field.set(GlobalBillingConfig, null)
        }

        // When & Then
        assertFailsWith<UninitializedPropertyAccessException> {
            GlobalBillingConfig.userUUID
        }
    }

    @Test
    fun `test partnerUserId getter when initialized`() {
        // Given
        val testPartnerUserId = "test_partner_user_id"
        GlobalBillingConfig.partnerUserId = testPartnerUserId

        // When & Then
        assertEquals(testPartnerUserId, GlobalBillingConfig.partnerUserId)
    }

    @Test
    fun `test partnerUserId getter when not initialized`() {
        // Given - Make _partnerUserId uninitialized
        if (partnerUserIdWasInitialized) {
            val field = GlobalBillingConfig::class.java.getDeclaredField("_partnerUserId")
            field.isAccessible = true
            field.set(GlobalBillingConfig, null)
        }

        // When & Then
        assertFailsWith<UninitializedPropertyAccessException> {
            GlobalBillingConfig.partnerUserId
        }
    }
} 