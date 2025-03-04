package com.synchronoss.aiap.presentation.state

data class ToastState(
    val isVisible: Boolean = false,
    val heading: String = "",
    val message: String = "",
    val isPending: Boolean = false,
    val isSuccess: Boolean = false,
    val formatArgs: Any? = null,
    val headingResId: Int? = null,
    val messageResId: Int? = null
)
