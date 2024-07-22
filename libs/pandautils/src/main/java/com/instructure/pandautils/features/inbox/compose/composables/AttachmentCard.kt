/*
 * Copyright (C) 2024 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.instructure.pandautils.features.inbox.compose.composables

import android.content.Context
import android.text.format.Formatter
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.instructure.canvasapi2.models.Attachment
import com.instructure.pandautils.R
import com.instructure.pandautils.utils.iconRes

@Composable
fun AttachmentCard(
    attachment: Attachment,
    status: AttachmentStatus,
    onRemove: () -> Unit,
    context: Context
) {
    Card(
        border = BorderStroke(1.dp, colorResource(id = R.color.backgroundMedium)),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .padding(horizontal = 16.dp)
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
            ){
                if (attachment.thumbnailUrl != null) {
                    AsyncImage(
                        model = attachment.thumbnailUrl,
                        contentDescription = attachment.filename
                    )
                } else {
                    Icon(
                        painter = painterResource(id = attachment.iconRes),
                        contentDescription = null,
                        tint = colorResource(id = R.color.textDark)
                    )
                }
            }

            Spacer(Modifier.width(8.dp))

            Column {
                Text(
                    attachment.filename ?: "",
                    color = colorResource(id = R.color.textDarkest),
                    fontSize = 20.sp
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    Formatter.formatFileSize(context, attachment.size),
                    color = colorResource(id = R.color.textDark),
                    fontSize = 16.sp
                )
            }

            Spacer(Modifier.weight(1f))

            when (status) {
                AttachmentStatus.UPLOADING -> {
                    CircularProgressIndicator()
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

            IconButton(onClick = { onRemove() }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_close),
                    contentDescription = null,
                    tint = colorResource(id = R.color.textDark)
                )
            }
        }
    }
}

enum class AttachmentStatus {
    UPLOADING,
    UPLOADED,
    FAILED

}

@Composable
@Preview
fun AttachmentCardPreview() {
    val context = LocalContext.current
    AttachmentCard(
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
        {},
        context
    )
}