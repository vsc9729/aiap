package com.synchronoss.aiap.utils

object Constants {
    const val BASE_URL = "https://sync-api.blr0.geekydev.com/"
    const val PURCHASE = "purchase"
    const val STORAGE_TAGLINE = "Add more storage to keep everything in one place"
    const val PURCHASE_REQUIRED = "A purchase is required to use this app."
    const val MONTHLY = "Monthly"
    const val YEARLY = "Yearly"
    const val SELECTED_PLAN_INDICATOR = "Selected Plan Indicator"
    
    // SSL Pinning Configuration
    object SSLPinning {
        // Extract hostname from the BASE_URL
        val API_HOSTNAME = BASE_URL.replace("https://", "").replace("/", "")
        
        // Public key hash obtained from server certificate
        const val PUBLIC_KEY_HASH = "sha256/DuoT1aaVH1iGP0LdH+on/FPguR9jTKjwAwha/1n1OsM="
    }
}
