package com.synchronoss.aiap.utils

object Constants {
    const val PURCHASE = "purchase"
    const val STORAGE_TAGLINE = "Add more storage to keep everything in one place"
    const val PURCHASE_REQUIRED = "A purchase is required to use this app."
    const val MONTHLY = "Monthly"
    const val YEARLY = "Yearly"
    const val SELECTED_PLAN_INDICATOR = "Selected Plan Indicator"
    
    // SSL Pinning Configuration
    object SSLPinning {
        // Public key hash for sync-api.blr0.geekydev.com
        const val GEEKYANTS_PUBLIC_KEY_HASH = "sha256/DuoT1aaVH1iGP0LdH+on/FPguR9jTKjwAwha/1n1OsM="

        // Public key hash for common-coredev-eks-wlpc.cloud.synchronoss.net
        const val EVENTCLOUD_PUBLIC_KEY_HASH = "sha256/UzjiyoNRnhPMbDtZ+BaH7bR7jFxHGbVkYWlzGtvxhKI="
    }
}
