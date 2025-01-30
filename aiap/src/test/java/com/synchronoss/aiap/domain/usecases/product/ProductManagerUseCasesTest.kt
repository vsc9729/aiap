import com.synchronoss.aiap.domain.models.ProductInfo
import com.synchronoss.aiap.domain.repository.product.ProductManager
import com.synchronoss.aiap.domain.usecases.product.GetProductsApi
import com.synchronoss.aiap.utils.Resource
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class ProductManagerUseCasesTest {
    @MockK
    private lateinit var productManager: ProductManager

    private lateinit var getProductsApi: GetProductsApi

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        assertNotNull(productManager)
        getProductsApi = GetProductsApi(productManager)
    }

    @Test
    fun `invoke delegates to product manager`() = runTest {
        // Given
        val mockProducts = listOf(
            ProductInfo(
                productId = "test_product",
                displayName = "Test Product",
                description = "Test Description",
                vendorName = "Test Vendor",
                appName = "Test App",
                price = 9.99,
                displayPrice = "$9.99",
                platform = "ANDROID",
                serviceLevel = "TEST_SERVICE",
                isActive = true,
                recurringPeriodCode = "P1M",
                productType = "SUBSCRIPTION",
                entitlementId = null
            )
        )

        coEvery {
            productManager.getProducts(any())
        } returns Resource.Success(mockProducts)

        // When
        val result = getProductsApi(123L)

        // Then
        assertTrue(result is Resource.Success)
        assertEquals(mockProducts, (result as Resource.Success).data)

        // Verify
        coVerify { productManager.getProducts(123L) }
    }
}