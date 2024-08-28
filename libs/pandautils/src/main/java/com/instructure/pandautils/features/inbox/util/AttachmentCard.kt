package com.instructure.pandautils.features.inbox.util

import android.text.format.Formatter
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.instructure.canvasapi2.models.Attachment
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.composables.Loading
import com.instructure.pandautils.utils.iconRes

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun AttachmentCard(
    attachmentCardItem: AttachmentCardItem,
    onSelect: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    val attachment = attachmentCardItem.attachment
    val status = attachmentCardItem.status

    Card(
        border = BorderStroke(1.dp, colorResource(id = R.color.backgroundMedium)),
        shape = RoundedCornerShape(10.dp),
        modifier = modifier
            .clickable { onSelect() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(96.dp)
                    .background(colorResource(id = R.color.backgroundLight))
            ){
                if (attachment.thumbnailUrl != null) {
                    // If the attachment has been deleted, the thumbnailUrl will be load an empty image.
                    // In this case, we should show the icon instead of the thumbnail.
                    Icon(
                        painter = painterResource(id = attachment.iconRes),
                        contentDescription = null,
                        tint = colorResource(id = R.color.textDark),
                        modifier = Modifier.size(48.dp)
                    )

                    GlideImage(
                        model = attachment.thumbnailUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        painter = painterResource(id = attachment.iconRes),
                        contentDescription = null,
                        tint = colorResource(id = R.color.textDark),
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            Spacer(Modifier.width(8.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
            ){
                Text(
                    attachment.filename ?: "",
                    color = colorResource(id = R.color.textDarkest),
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    Formatter.formatFileSize(LocalContext.current, attachment.size),
                    color = colorResource(id = R.color.textDark),
                    fontSize = 14.sp,
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (!attachmentCardItem.readOnly){
                when (status) {
                    AttachmentStatus.UPLOADING -> {
                        Loading()
                    }

                    AttachmentStatus.UPLOADED -> {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_complete),
                            contentDescription = null,
                            tint = colorResource(id = R.color.textDark)
                        )
                    }

                    AttachmentStatus.FAILED -> {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_no),
                            contentDescription = null,
                            tint = colorResource(id = R.color.textDark)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(onClick = { onRemove() }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_close),
                        contentDescription = null,
                        tint = colorResource(id = R.color.textDark),
                    )
                }
            }
        }
    }
}

@Composable
@Preview
fun AttachmentCardPreview() {
    val context = LocalContext.current
    AttachmentCard(
        AttachmentCardItem(
            Attachment(
                id = 1,
                contentType = "image/png",
                filename = "image.png",
                displayName = "image.png",
                url = "https://www.example.com/image.png",
                thumbnailUrl = null,
                previewUrl = null,
                size = 1024
            ),
            AttachmentStatus.UPLOADED,
            false
        ),
        {},
        {},
    )
}