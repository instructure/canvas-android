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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.composables.Shimmer
import com.instructure.pandautils.domain.models.courses.GroupCardItem
import com.instructure.pandautils.utils.getFragmentActivityOrNull

@Composable
fun GroupCard(
    groupCard: GroupCardItem,
    onGroupClick: (FragmentActivity, Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val activity = LocalActivity.current?.getFragmentActivityOrNull()

    val cardShape = RoundedCornerShape(16.dp)

    Card(
        modifier = modifier
            .testTag("GroupCard_${groupCard.id}")
            .fillMaxWidth()
            .clip(cardShape)
            .clickable { activity?.let { onGroupClick(it, groupCard.id) } },
        shape = cardShape,
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
                        .size(72.dp)
                        .background(
                            color = Color(groupCard.color),
                            shape = RoundedCornerShape(14.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Spacer(modifier = Modifier.weight(1f))

                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Text(
                                text = groupCard.memberCount.toString(),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(groupCard.color),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                lineHeight = 21.sp
                            )
                        }

                        Text(
                            text = pluralStringResource(R.plurals.groupMemberCount, groupCard.memberCount),
                            fontSize = 14.sp,
                            color = Color.White,
                            lineHeight = 19.sp
                        )

                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp, top = 6.dp, bottom = 6.dp, end = 16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                groupCard.parentCourseName?.let { courseName ->
                    Text(
                        text = courseName,
                        fontSize = 14.sp,
                        color = Color(groupCard.color),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 19.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                }

                Text(
                    text = groupCard.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colorResource(R.color.textDarkest),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 21.sp
                )
            }
        }
    }
}

@Composable
fun GroupCardShimmer(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
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
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun GroupCardPreview() {
    GroupCard(
        groupCard = GroupCardItem(
            id = 1,
            name = "Project Team Alpha",
            parentCourseName = "Introduction to Computer Science",
            parentCourseId = 1,
            color = 0xFF4CAF50.toInt(),
            memberCount = 5
        ),
        onGroupClick = {_, _ -> }
    )
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun GroupCardShimmerPreview() {
    GroupCardShimmer()
}