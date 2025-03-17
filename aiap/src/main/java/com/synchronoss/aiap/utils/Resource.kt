package com.synchronoss.aiap.utils

sealed class Resource<T>(
    val data: T? = null,
    val message: String? = null
) {
    class Success<T>(data: T) : Resource<T>(data) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Success<*>) return false
            return data == other.data
        }

        override fun hashCode(): Int {
            return data?.hashCode() ?: 0
        }
    }

    class Error<T>(message: String, data: T? = null) : Resource<T>(data, message) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Error<*>) return false
            return message == other.message && data == other.data
        }

        override fun hashCode(): Int {
            var result = message?.hashCode() ?: 0
            result = 31 * result + (data?.hashCode() ?: 0)
            return result
        }
    }
}