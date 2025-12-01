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

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.composables.Shimmer
import com.instructure.pandautils.domain.models.courses.CourseCardItem
import com.instructure.pandautils.domain.models.courses.GradeDisplay

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun CourseCard(
    courseCard: CourseCardItem,
    showGrade: Boolean,
    showColorOverlay: Boolean,
    onCourseClick: (Long) -> Unit,
    onMenuClick: ((Long) -> Unit)? = null,
    onManageOfflineContent: ((Long) -> Unit)? = null,
    onCustomizeCourse: ((Long) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    val hasMenu = onManageOfflineContent != null && onCustomizeCourse != null

    Box(modifier = modifier) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = courseCard.isClickable) { onCourseClick(courseCard.id) }
            .alpha(if (courseCard.isClickable) 1f else 0.5f),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorResource(R.color.backgroundLightestElevated)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(76.dp),
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
                            shape = RoundedCornerShape(22.dp)
                        )
                )

                if (courseCard.imageUrl != null) {
                    GlideImage(
                        model = courseCard.imageUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(22.dp))
                            .alpha(if (showColorOverlay) 0.4f else 1f),
                        contentScale = ContentScale.Crop
                    )
                }

                if (hasMenu) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(start = 8.dp, top = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clickable { showMenu = true }
                                .background(
                                    color = Color.White,
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_kebab),
                                contentDescription = stringResource(R.string.a11y_contentDescription_moreOptions),
                                modifier = Modifier.size(16.dp),
                                tint = Color(courseCard.color)
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            modifier = Modifier.width(200.dp),
                            shape = RoundedCornerShape(8.dp),
                            containerColor = colorResource(R.color.backgroundLightestElevated)
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
                                    onManageOfflineContent!!(courseCard.id)
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
                                    onCustomizeCourse!!(courseCard.id)
                                }
                            )
                        }
                    }
                }

                if (showGrade && courseCard.grade !is GradeDisplay.Hidden) {
                    GradeBadge(
                        grade = courseCard.grade,
                        courseColor = Color(courseCard.color),
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(bottom = 8.dp, start = 8.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp, top = 6.dp, bottom = 6.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = courseCard.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colorResource(R.color.textDarkest),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 21.sp
                )

                courseCard.courseCode?.let { code ->
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = code,
                        fontSize = 14.sp,
                        color = colorResource(R.color.textDark),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (courseCard.announcementCount > 0) {
                Box(
                    modifier = Modifier.padding(start = 8.dp, end = 16.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_announcement),
                        contentDescription = stringResource(R.string.announcements),
                        modifier = Modifier.size(24.dp),
                        tint = colorResource(R.color.textDark)
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 8.dp, y = (-8).dp)
                            .background(
                                color = Color(courseCard.color),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = courseCard.announcementCount.toString(),
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
            containerColor = Color.White
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
                        text = stringResource(R.string.not_graded),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = courseColor
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
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorResource(R.color.backgroundLightestElevated)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(76.dp)
                .padding(start = 2.dp, top = 2.dp, bottom = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Shimmer(
                modifier = Modifier.size(72.dp),
                shape = RoundedCornerShape(22.dp)
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

                Shimmer(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .height(14.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CourseCardPreview() {
    CourseCard(
        courseCard = CourseCardItem(
            id = 1,
            name = "Introduction to Computer Science",
            courseCode = "CS 101",
            color = 0xFF2196F3.toInt(),
            imageUrl = null,
            grade = GradeDisplay.Percentage("85%"),
            announcementCount = 0,
            isSynced = true,
            isClickable = true
        ),
        showGrade = true,
        showColorOverlay = true,
        onCourseClick = {},
        onMenuClick = {}
    )
}

@Preview(showBackground = true)
@Composable
private fun CourseCardShimmerPreview() {
    CourseCardShimmer()
}