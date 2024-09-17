package com.instructure.pandautils.features.inbox.details.composables

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.instructure.pandautils.R

@Composable
fun MessageMenuItem(
    @DrawableRes iconRes: Int,
    label: String
) {
    Row {
        Icon(
            painter = painterResource(id = iconRes),
            tint = colorResource(id = R.color.textDarkest),
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = label,
            color = colorResource(id = R.color.textDarkest),
            modifier = Modifier
                .padding(start = 8.dp)
                .testTag("messageMenuItem")
        )
    }
}