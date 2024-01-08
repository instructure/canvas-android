package com.instructure.pandautils.compose

import androidx.compose.material.MaterialTheme
import androidx.compose.material.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import com.instructure.pandautils.R

@Composable
fun CanvasTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        typography = typography,
        content = content
    )
}

private val lato = FontFamily(
    Font(R.font.lato_regular)
)


var typography = Typography( // TODO make this dynamic with K5 fonts
    defaultFontFamily = lato,
)