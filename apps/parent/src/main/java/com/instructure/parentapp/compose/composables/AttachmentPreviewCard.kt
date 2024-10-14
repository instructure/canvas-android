@file:OptIn(ExperimentalGlideComposeApi::class)

package com.instructure.parentapp.compose.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.instructure.canvasapi2.models.Attachment
import com.instructure.canvasapi2.models.RemoteFile
import com.instructure.pandautils.R
import com.instructure.pandautils.utils.iconRes

@Composable
fun AttachmentPreviewCard(
    modifier: Modifier = Modifier,
    attachment: Attachment,
    iconColor: Color = colorResource(R.color.textDark),
    onClick: (Attachment) -> (Unit)
) {
    Box(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(id = attachment.iconRes),
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                attachment.filename ?: "",
                color = colorResource(id = R.color.textDarkest),
                fontSize = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        /*GlideImage(
            model = attachment.thumbnailUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .background(colorResource(id = R.color.backgroundLight)),
            loading = placeholder(ColorPainter(Color(0, 0, 0, 0)))
        )*/
    }
}



@Composable
@Preview
fun AttachmentPreviewCardPreview() {
    AttachmentPreviewCard(
        attachment = Attachment(filename = "Filename"),
        onClick = {}
    )
}