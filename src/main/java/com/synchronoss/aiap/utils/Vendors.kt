package com.synchronoss.aiap.utils

enum class Vendors {
    Verizon,
    BT,
    Capsyl
}


val vendorThemeMap: Map<Vendors , String> = mutableMapOf(
    Vendors.Verizon to "Verizon_theme.json",
    Vendors.BT to "BT_theme.json",
    Vendors.Capsyl to "Capsyl_theme.json",

)