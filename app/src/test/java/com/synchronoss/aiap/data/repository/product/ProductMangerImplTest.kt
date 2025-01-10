package com.synchronoss.aiap.domain.usecases.product

import com.synchronoss.aiap.domain.models.ProductInfo
import com.synchronoss.aiap.domain.repository.product.ProductManager
import com.synchronoss.aiap.utils.Resource
import io.mockito.Mock
import io.mockito.MockitoAnnotations
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.`when`

class GetProductsApiTest {

    @Mock
    private lateinit var productManager: ProductManager
    private lateinit var getProductsApi: GetProductsApi

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        getProductsApi = GetProductsApi(productManager)
    }

    @Test
    fun `getProducts returns success with product list`() = runBlocking {
        // Arrange
        val mockProducts = listOf(
            ProductInfo(
                productName = "test_product",
                displayName = "Test Product",
                description = "Test Description",
                ppiId = "test_ppi_id",
                isActive = true,
                duration = 30
            )
        )
        val expectedResult = Resource.Success(mockProducts)
        `when`(productManager.getProducts()).thenReturn(expectedResult)

        // Act
        val result = getProductsApi()

        // Assert
        assertEquals(expectedResult, result)
    }

    @Test
    fun `getProducts returns error when manager fails`() = runBlocking {
        // Arrange
        val errorMessage = "Network error"
        val expectedResult = Resource.Error<List<ProductInfo>>(errorMessage)
        `when`(productManager.getProducts()).thenReturn(expectedResult)

        // Act
        val result = getProductsApi()

        // Assert
        assertEquals(expectedResult, result)
    }
}