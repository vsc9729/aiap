package com.synchronoss.aiap.presentation.state

data class ToastState(
    val isVisible: Boolean = false,
    val heading: String = "",
    val message: String = "",
    val headingResId: Int? = null,
    val messageResId: Int? = null
)
