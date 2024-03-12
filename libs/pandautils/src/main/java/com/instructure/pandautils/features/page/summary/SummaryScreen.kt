package com.instructure.pandautils.features.page.summary

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.CanvasTheme
import com.instructure.pandautils.features.progress.ProgressAction
import com.instructure.pandautils.features.progress.ProgressState
import com.instructure.pandautils.features.progress.composables.ProgressContent
import com.instructure.pandautils.features.progress.composables.ProgressTopBar

@Composable
fun SummaryScreen(pageName: String, summary: String, onBackClicked: () -> Unit) {
    CanvasTheme {
        Scaffold(
            modifier = Modifier.clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)),
            backgroundColor = colorResource(id = R.color.backgroundLightest),
            topBar = {
                SummaryAppBar(title = "tl;dr", pageName = pageName, onBackClicked = onBackClicked)
            }
        ) { padding ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(padding),
                color = colorResource(id = R.color.backgroundLightest)
            ) {
                Column {
                    Text(
                        text = summary,
                        fontSize = 18.sp,
                        color = colorResource(id = R.color.textDarkest),
                        modifier = Modifier.padding(16.dp)
                    )

                    Button(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        onClick = {  }) {
                        Text(text = "Quiz me on this!")
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryAppBar(
    modifier: Modifier = Modifier,
    title: String,
    pageName: String,
    onBackClicked: () -> Unit
) {
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { onBackClicked() }) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = stringResource(id = R.string.a11y_closeProgress),
                    tint = colorResource(id = R.color.textDarkest)
                )
            }
            Column {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    color = colorResource(id = R.color.textDarkest),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = pageName,
                    fontSize = 12.sp,
                    color = colorResource(id = R.color.textDark)
                )
            }

        }
        Divider(color = colorResource(id = R.color.backgroundMedium))
    }

}

@Preview
@Composable
fun SummaryScreenPreview() {
    SummaryScreen(
        "Page Name",
        "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vestibulum tortor massa, consectetur eu orci ut, dignissim vulputate elit. Praesent a lectus dui. Vivamus ac augue vitae arcu eleifend feugiat ut sit amet mauris. Integer id blandit nibh, vehicula vestibulum orci. Quisque elementum tortor et mauris bibendum, eget lacinia turpis sodales. Praesent feugiat ligula magna, eu commodo velit ullamcorper non. Aenean pharetra eros nec justo dignissim, efficitur lobortis urna aliquet. Vestibulum quis diam ut eros luctus tincidunt vitae dictum sapien. Vestibulum dictum aliquam metus ut porta. Nunc ullamcorper interdum arcu, vel sollicitudin leo ullamcorper non. Nunc ac faucibus orci. Mauris efficitur in eros in lacinia. Sed non libero venenatis, mollis leo fermentum, ornare diam."
    ) {}
}