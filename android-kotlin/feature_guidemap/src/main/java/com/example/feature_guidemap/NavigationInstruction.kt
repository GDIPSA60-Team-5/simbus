package com.example.feature_guidemap

data class NavigationInstruction(
    val text: String,
    val distance: String,
    val direction: TurnDirection,
    val iconResId: Int
)
