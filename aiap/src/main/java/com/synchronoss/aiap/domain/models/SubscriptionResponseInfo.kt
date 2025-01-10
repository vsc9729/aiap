data class SubscriptionResponseInfo(
    val productId: String,
    val serviceLevel: String,
    val vendorName: String,
    val appName: String,
    val appPlatformID: String,
    val platform: String,
    val partnerUserId: String,
    val startDate: Long,
    val endDate: Long,
    val status: String,
    val type: String,
    val message: String?
) 