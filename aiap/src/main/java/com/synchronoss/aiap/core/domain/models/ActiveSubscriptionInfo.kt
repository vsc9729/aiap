package com.synchronoss.aiap.core.domain.models

data class ActiveSubscriptionInfo(
    val subscriptionResponseInfo: SubscriptionResponseInfo?,
    val productUpdateTimeStamp: Long?,
    val themConfigTimeStamp: Long?,
    val userUUID: String?,
    val baseServiceLevel:String?

) 