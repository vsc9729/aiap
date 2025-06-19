package com.synchronoss.aiap.utils

object UrlManager {
    private const val DEFAULT_BASE_URL = "https://sync-api.blr0.geekydev.com/"
    private const val EVENTCLOUD_IAP_BASE_URL = "https://common-coredev-eks-wlpc.cloud.synchronoss.net/eventcloud-iap/"

    // This flag should be set externally based on build configuration or other criteria.
    var useEventCloudIap: Boolean = false

    fun getBaseUrl(): String {
        return if (useEventCloudIap) {
            EVENTCLOUD_IAP_BASE_URL
        } else {
            DEFAULT_BASE_URL
        }
    }

    fun getApiHostname(): String {
        return getBaseUrl().replace("https://", "").split("/")[0]
    }
} 