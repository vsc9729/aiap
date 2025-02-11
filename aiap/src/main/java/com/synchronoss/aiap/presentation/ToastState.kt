package com.synchronoss.aiap.presentation

data class ToastState(
    val isVisible: Boolean = false,
    val heading: String = "",
    val message: String = "",
    val headingResId: Int? = null,
    val messageResId: Int? = null
)
