package com.synchronoss.aiap.domain.models

data class ActiveSubscriptionInfo(
    val subscriptionResponseInfo: SubscriptionResponseInfo?,
    val productUpdateTimeStamp: Long?,
    val themConfigTimeStamp: Long?
) 