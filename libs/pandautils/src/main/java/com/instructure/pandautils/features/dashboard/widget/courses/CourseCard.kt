/*
 * Copyright (C) 2025 - present Instructure, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.instructure.pandautils.features.dashboard.widget.courses

import android.content.res.Configuration
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.composables.Shimmer
import com.instructure.pandautils.compose.composables.rememberWithRequireNetwork
import com.instructure.pandautils.features.dashboard.widget.courses.model.CourseCardItem
import com.instructure.pandautils.features.dashboard.widget.courses.model.GradeDisplay
import com.instructure.pandautils.utils.color
import com.instructure.pandautils.utils.getFragmentActivityOrNull

internal val COURSE_CARD_HEIGHT = 76.dp

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun CourseCard(
    courseCard: CourseCardItem,
    showGrade: Boolean,
    showColorOverlay: Boolean,
    onCourseClick: (FragmentActivity, Long) -> Unit,
    modifier: Modifier = Modifier,
    onManageOfflineContent: ((FragmentActivity, Long) -> Unit)? = null,
    onCustomizeCourse: ((FragmentActivity, Long) -> Unit)? = null,
    onAnnouncementClick: ((FragmentActivity, Long) -> Unit)? = null,
) {
    var showMenu by remember { mutableStateOf(false) }
    val hasMenu = onManageOfflineContent != null && onCustomizeCourse != null

    val activity = LocalActivity.current?.getFragmentActivityOrNull()

    val openMenuClick = rememberWithRequireNetwork {
        showMenu = true
    }

    val cardShape = RoundedCornerShape(16.dp)

    Box(modifier = modifier.testTag("CourseCard_${courseCard.id}")) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (courseCard.isClickable) 1f else 0.5f),
        shape = cardShape,
        colors = CardDefaults.cardColors(
            containerColor = colorResource(R.color.backgroundLightest)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (courseCard.isClickable) 2.dp else 0.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(COURSE_CARD_HEIGHT)
                .clickable(enabled = courseCard.isClickable) { activity?.let { onCourseClick(it, courseCard.id) } },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .padding(start = 2.dp, top = 2.dp, bottom = 2.dp)
                    .size(72.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            color = Color(courseCard.color),
                            shape = RoundedCornerShape(14.dp)
                        )
                )

                if (courseCard.imageUrl != null) {
                    GlideImage(
                        model = courseCard.imageUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(14.dp))
                            .alpha(if (showColorOverlay) 0.4f else 1f),
                        contentScale = ContentScale.Crop
                    )
                }

                if (hasMenu && courseCard.isClickable) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(start = 8.dp, top = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clickable(onClick = openMenuClick)
                                .background(
                                    color = colorResource(R.color.backgroundLightest),
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_kebab),
                                contentDescription = stringResource(R.string.a11y_contentDescription_moreOptions),
                                modifier = Modifier.size(16.dp),
                                tint = Color(CanvasContext.emptyCourseContext(id = courseCard.id).color)
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            modifier = Modifier.width(200.dp),
                            shape = RoundedCornerShape(8.dp),
                            containerColor = colorResource(R.color.backgroundLightest)
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = stringResource(R.string.course_menu_manage_offline_content),
                                        fontSize = 16.sp,
                                        color = colorResource(R.color.textDarkest)
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    activity?.let { onManageOfflineContent.invoke(it, courseCard.id) }
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = stringResource(R.string.customizeCourse),
                                        fontSize = 16.sp,
                                        color = colorResource(R.color.textDarkest)
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    activity?.let { onCustomizeCourse.invoke(it, courseCard.id) }
                                }
                            )
                        }
                    }
                }

                if (showGrade && courseCard.grade !is GradeDisplay.Hidden) {
                    GradeBadge(
                        grade = courseCard.grade,
                        courseColor = Color(CanvasContext.emptyCourseContext(id = courseCard.id).color),
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(bottom = 8.dp, start = 8.dp)
                    )
                }
            }

            Text(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp, end = 8.dp),
                text = courseCard.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = colorResource(R.color.textDarkest),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 21.sp
            )

            if (courseCard.isSynced) {
                Icon(
                    painter = painterResource(R.drawable.ic_offline_synced),
                    contentDescription = stringResource(R.string.offline_content_available),
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .size(24.dp),
                    tint = colorResource(R.color.textDark)
                )
            }

            if (courseCard.announcements.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .size(48.dp)
                        .clickable(enabled = courseCard.isClickable) {
                            activity?.let {
                                onAnnouncementClick?.invoke(
                                    it,
                                    courseCard.id
                                )
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_announcement),
                        contentDescription = stringResource(R.string.announcements),
                        modifier = Modifier.requiredSize(24.dp),
                        tint = colorResource(R.color.textDark)
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = (-8).dp, y = 6.dp)
                            .background(
                                color = Color(CanvasContext.emptyCourseContext(id = courseCard.id).color),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = courseCard.announcements.size.toString(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            lineHeight = 8.sp
                        )
                    }
                }
            }
        }
    }
    }
}

@Composable
private fun GradeBadge(
    grade: GradeDisplay,
    courseColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorResource(R.color.backgroundLightest)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
            contentAlignment = Alignment.Center
        ) {
            when (grade) {
                is GradeDisplay.Percentage -> {
                    Text(
                        text = grade.value,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = courseColor,
                        lineHeight = 19.sp
                    )
                }
                is GradeDisplay.Letter -> {
                    Text(
                        text = grade.grade,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = courseColor,
                        lineHeight = 19.sp
                    )
                }
                GradeDisplay.Locked -> {
                    Icon(
                        painter = painterResource(R.drawable.ic_lock),
                        contentDescription = stringResource(R.string.locked),
                        modifier = Modifier.size(14.dp),
                        tint = courseColor
                    )
                }
                GradeDisplay.NotAvailable -> {
                    Text(
                        text = stringResource(R.string.noGradeText),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = courseColor,
                        lineHeight = 19.sp
                    )
                }
                GradeDisplay.Hidden -> {
                    // Don't show anything for hidden grades
                }
            }
        }
    }
}

@Composable
fun CourseCardShimmer(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorResource(R.color.backgroundLightest)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(COURSE_CARD_HEIGHT)
                .padding(start = 2.dp, top = 2.dp, bottom = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Shimmer(
                modifier = Modifier.size(72.dp),
                shape = RoundedCornerShape(14.dp)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Shimmer(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(16.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CourseCardPreview() {
    ContextKeeper.appContext = LocalContext.current
    CourseCard(
        courseCard = CourseCardItem(
            id = 1L,
            name = "Introduction to Computer Science",
            courseCode = "CS 101",
            imageUrl = null,
            grade = GradeDisplay.Percentage("85%"),
            announcements = listOf(
                DiscussionTopicHeader(id = 1L, title = "Announcement")
            ),
            isSynced = true,
            isClickable = true,
            color = android.graphics.Color.RED
        ),
        showGrade = true,
        showColorOverlay = true,
        onCourseClick = {_, _ ->},
        onCustomizeCourse = {_, _ ->},
        onManageOfflineContent = {_, _ ->},
        onAnnouncementClick = {_, _ ->}
    )
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CourseCardShimmerPreview() {
    ContextKeeper.appContext = LocalContext.current
    CourseCardShimmer()
}