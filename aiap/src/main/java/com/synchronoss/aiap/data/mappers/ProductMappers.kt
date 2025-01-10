package com.synchronoss.aiap.data.mappers

import ActiveSubscriptionInfo
import SubscriptionResponseInfo
import com.synchronoss.aiap.data.remote.ProductDataDto
import com.synchronoss.aiap.data.remote.ProductDto
import com.synchronoss.aiap.domain.models.ProductData
import com.synchronoss.aiap.domain.models.ProductInfo
import com.synchronoss.aiap.data.remote.ActiveSubscriptionResponse
import com.synchronoss.aiap.data.remote.SubscriptionResponseDTO

fun ProductDataDto.toProductInfo(): ProductInfo {
    return ProductInfo(
        productId = productId,
        displayName = displayName,
        description = description,
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
        productId = productId,
        serviceLevel = serviceLevel,
        vendorName = vendorName,
        appName = appName,
        appPlatformID = appPlatformID,
        platform = platform,
        partnerUserId = partnerUserId,
        startDate = startDate,
        endDate = endDate,
        status = status,
        type = type,
        message = message
    )
}


