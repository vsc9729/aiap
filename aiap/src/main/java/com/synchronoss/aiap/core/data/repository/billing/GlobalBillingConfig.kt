package com.synchronoss.aiap.core.data.repository.billing

object GlobalBillingConfig {
    private lateinit var _partnerUserId: String
    private lateinit var _userUUID: String
    private lateinit var _apiKey: String

    var userUUID: String
        get() = _userUUID
        set(value) {
            _userUUID = value
        }

    var partnerUserId: String
        get() = _partnerUserId
        set(value) {
            _partnerUserId = value
        }

    var apiKey: String
        get() = _apiKey
        set(value) {
            _apiKey = value
        }
}