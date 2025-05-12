package com.instructure.student.widget.glance

import androidx.compose.ui.unit.sp
import androidx.glance.text.FontFamily
import androidx.glance.text.FontWeight
import androidx.glance.text.TextStyle


object WidgetTextStyles {

    val normalDarkest = TextStyle(
        color = WidgetColors.textDarkest,
        fontSize = 16.sp,
        fontWeight = FontWeight.Normal,
        fontFamily = FontFamily("lato_regular")
    )

    val normalDark = TextStyle(
        color = WidgetColors.textDark,
        fontSize = 16.sp,
        fontWeight = FontWeight.Normal,
        fontFamily = FontFamily("lato_regular")
    )

    val mediumDarkest = TextStyle(
        color = WidgetColors.textDarkest,
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium,
        fontFamily = FontFamily("lato_regular")
    )

    val mediumDark = TextStyle(
        color = WidgetColors.textDark,
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium,
        fontFamily = FontFamily("lato_regular")
    )
}
