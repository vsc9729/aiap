package com.synchronoss.aiap.common

import java.nio.charset.StandardCharsets
import java.util.UUID

object UuidGenerator {
    fun generateUUID(packageName: String): String {
        // Generate UUID based on the package name
        val uuid = UUID.nameUUIDFromBytes(packageName.toByteArray(StandardCharsets.UTF_8))
        return uuid.toString()
    }
}