package com.synchronoss.aiap.core.domain.usecases.billing

import com.synchronoss.aiap.core.domain.repository.billing.BillingManager
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertTrue

class StartConnectionTest {

    private lateinit var startConnection: StartConnection
    private lateinit var mockBillingManager: BillingManager
    
    @Before
    fun setup() {
        mockBillingManager = mockk()
        startConnection = StartConnection(billingManager = mockBillingManager)
    }
    
    @Test
    fun `test invoke delegates to billingManager startConnection`() = runTest {
        // Given
        val deferredResult = CompletableDeferred<Unit>()
        deferredResult.complete(Unit)
        coEvery { mockBillingManager.startConnection() } returns deferredResult
        
        // When
        val result = startConnection()
        
        // Then
        assertTrue(result.isCompleted)
        assertTrue(result === deferredResult, "The use case should return the same CompletableDeferred instance from the repository")
    }
}