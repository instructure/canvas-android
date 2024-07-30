package com.instructure.pandautils.features.inbox.compose.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.pandautils.R
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.backgroundColor

@Composable
fun ContextValueRow(
    label: String,
    value: CanvasContext?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .height(52.dp)
            .clickable { onClick() }
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp)
            .padding(top = 8.dp, bottom = 8.dp)
    ) {
        Text(
            text = label,
            color = colorResource(id = R.color.textDarkest),
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp
        )

        Spacer(Modifier.width(12.dp))

        if (value != null) {
            val color = if (value.type == CanvasContext.Type.USER) ThemePrefs.brandColor else value.backgroundColor

            Box(
                modifier = Modifier
                    .size(18.dp)
                    .background(Color(color), CircleShape)
            )

            Spacer(Modifier.width(4.dp))

            Text(
                text = value.name ?: "",
                color = colorResource(id = R.color.textDarkest),
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Icon(
            painter = painterResource(id = R.drawable.ic_chevron_right),
            contentDescription = stringResource(R.string.a11y_open_course_picker),
            tint = colorResource(id = R.color.textDark),
            modifier = Modifier
                .size(16.dp)
        )
    }
}

@Composable
@Preview
fun ContextValueRowPreview() {
    ContextKeeper.appContext = LocalContext.current
    ContextValueRow(
        label = "Course",
        value = Course(
            id = 1,
            name = "Course 1",
            courseColor = "#FF0000"
        ),
        onClick = {}
    )
}