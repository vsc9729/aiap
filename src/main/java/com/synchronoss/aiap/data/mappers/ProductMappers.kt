package com.synchronoss.aiap.data.mappers


import com.synchronoss.aiap.data.remote.product.ProductDataDto
import com.synchronoss.aiap.domain.models.ProductInfo
import com.synchronoss.aiap.data.remote.product.ActiveSubscriptionResponse
import com.synchronoss.aiap.data.remote.product.SubscriptionResponseDTO
import com.synchronoss.aiap.domain.models.ActiveSubscriptionInfo
import com.synchronoss.aiap.domain.models.SubscriptionResponseInfo

fun ProductDataDto.toProductInfo(): ProductInfo {
    return ProductInfo(
        productId = productId,
        displayName = displayName,
        description = description ?: "Test Description",
        vendorName = vendorName,
        appName = appName,
        price = price,
        displayPrice = displayPrice,
        platform = platform,
        serviceLevel = serviceLevel,
        isActive = isActive,
        recurringPeriodCode = recurringPeriodCode,
        productType = productType,
        entitlementId = entitlementId
    )
}

fun ActiveSubscriptionResponse.toActiveSubscriptionInfo(): ActiveSubscriptionInfo {
    return ActiveSubscriptionInfo(
        subscriptionResponseInfo = subscriptionResponseDTO?.toSubscriptionResponseInfo(),
        productUpdateTimeStamp = productUpdateTimeStamp,
        themConfigTimeStamp = themConfigTimeStamp
    )
}

fun SubscriptionResponseDTO.toSubscriptionResponseInfo(): SubscriptionResponseInfo {
    return SubscriptionResponseInfo(
        product = product?.toProductInfo(),
        vendorName = vendorName,
        appName = appName,
        appPlatformID = appPlatformID,
        platform = platform,
        partnerUserId = partnerUserId,
        startDate = startDate,
        endDate = endDate,
        status = status,
        type = type,
    )
}


