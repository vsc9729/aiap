package com.synchronoss.aiap.core.data.mappers

import com.synchronoss.aiap.core.data.remote.product.ActiveSubscriptionResponse
import com.synchronoss.aiap.core.data.remote.product.ProductDataDto
import com.synchronoss.aiap.core.data.remote.product.SubscriptionResponseDTO
import com.synchronoss.aiap.core.domain.models.ActiveSubscriptionInfo
import com.synchronoss.aiap.core.domain.models.ProductInfo
import com.synchronoss.aiap.core.domain.models.SubscriptionResponseInfo


fun ProductDataDto.toProductInfo(): ProductInfo {
    return ProductInfo(
        id = id,
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
        themConfigTimeStamp = themConfigTimeStamp,
        userUUID = userUUID,
        baseServiceLevel = baseServiceLevel
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