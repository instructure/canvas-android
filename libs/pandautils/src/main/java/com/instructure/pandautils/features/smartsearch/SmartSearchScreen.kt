/*
 * Copyright (C) 2024 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package com.instructure.pandautils.features.smartsearch

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.SmartSearchContentType
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.CanvasTheme
import com.instructure.pandautils.compose.composables.EmptyContent
import com.instructure.pandautils.compose.composables.ErrorContent
import com.instructure.pandautils.compose.composables.Loading
import com.instructure.pandautils.compose.composables.SearchBar
import com.instructure.pandautils.utils.color

@Composable
fun SmartSearchScreen(
    uiState: SmartSearchUiState,
    navigationItemClick: () -> Unit
) {
    CanvasTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    backgroundColor = Color(uiState.canvasContext.color),
                    navigationIcon = {
                        IconButton(onClick = { navigationItemClick() }) {
                            Icon(
                                painterResource(id = R.drawable.ic_back_arrow),
                                contentDescription = stringResource(R.string.contentDescription_back),
                                tint = colorResource(R.color.textLightest)
                            )
                        }
                    },
                    title = {
                        SearchBar(
                            modifier = Modifier.testTag("searchBar"),
                            icon = R.drawable.ic_smart_search,
                            tintColor = colorResource(R.color.textLightest),
                            onExpand = {},
                            onSearch = { uiState.actionHandler(SmartSearchAction.Search(it)) },
                            placeholder = stringResource(R.string.smartSearchPlaceholder),
                            collapsable = false,
                            searchQuery = uiState.query
                        )
                    },
                )
            }
        ) { padding ->
            when {
                uiState.loading -> {
                    Loading(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(colorResource(R.color.backgroundLight))
                            .testTag("loading"),
                        title = stringResource(R.string.smartSearchLoadingTitle),
                        message = stringResource(R.string.smartSearchLoadingSubtitle),
                        animation = R.raw.panda_reading
                    )
                }

                uiState.error -> {
                    ErrorContent(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(colorResource(R.color.backgroundLight))
                            .testTag("error"),
                        errorMessage = stringResource(R.string.errorOccurred),
                        retryClick = {
                            uiState.actionHandler(
                                SmartSearchAction.Search(
                                    uiState.query
                                )
                            )
                        }
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .background(colorResource(R.color.backgroundLight))
                    ) {
                        item {
                            CourseHeader(uiState.canvasContext.name.orEmpty())
                        }
                        if (uiState.results.isNotEmpty()) {
                            items(uiState.results) {
                                ResultItem(
                                    it,
                                    Color(uiState.canvasContext.color),
                                    uiState.actionHandler
                                )
                            }
                        } else {
                            item {
                                EmptyContent(
                                    modifier = Modifier.fillMaxHeight().testTag("empty"),
                                    emptyTitle = stringResource(R.string.smartSearchEmptyTitle),
                                    emptyMessage = stringResource(R.string.smartSearchEmptyMessage),
                                    imageRes = R.drawable.ic_smart_search_empty
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CourseHeader(title: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        backgroundColor = colorResource(R.color.backgroundLightestElevated)
    ) {
        Column(
            modifier = Modifier.padding(
                top = 8.dp,
                start = 16.dp,
                end = 16.dp,
                bottom = 16.dp
            )
        ) {
            Text(
                stringResource(R.string.smartSearchCourseHeaderTitle),
                color = colorResource(R.color.textDark),
                fontSize = 16.sp,
            )
            Text(
                modifier = Modifier.testTag("courseTitle"),
                text = title,
                color = colorResource(R.color.textDarkest),
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                maxLines = 2
            )
        }
    }
}

@Composable
private fun ResultItem(
    result: SmartSearchResultUiState,
    color: Color,
    actionHandler: (SmartSearchAction) -> Unit
) {
    fun getContentTypeTitle(type: SmartSearchContentType): Int {
        return when (type) {
            SmartSearchContentType.ANNOUNCEMENT -> R.string.smartSearchAnnouncementTitle
            SmartSearchContentType.DISCUSSION_TOPIC -> R.string.smartSearchDiscussionTitle
            SmartSearchContentType.ASSIGNMENT -> R.string.smartSearchAssignmentTitle
            SmartSearchContentType.WIKI_PAGE -> R.string.smartSearchPageTitle
        }
    }

    fun getContentTypeIcon(type: SmartSearchContentType): Int {
        return when (type) {
            SmartSearchContentType.ANNOUNCEMENT -> R.drawable.ic_announcement
            SmartSearchContentType.DISCUSSION_TOPIC -> R.drawable.ic_discussion
            SmartSearchContentType.ASSIGNMENT -> R.drawable.ic_assignment
            SmartSearchContentType.WIKI_PAGE -> R.drawable.ic_pages
        }
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colorResource(R.color.backgroundLightest))
            .clickable { actionHandler(SmartSearchAction.Route(result.url)) }
            .testTag("resultItem"),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier
                .padding(start = 24.dp, top = 14.dp)
                .size(20.dp)
                .align(Alignment.Top),
            painter = painterResource(getContentTypeIcon(result.type)),
            contentDescription = null,
            tint = color
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 18.dp, top = 12.dp, bottom = 14.dp)
        ) {
            Text(
                modifier = Modifier.testTag("resultTitle"),
                text = result.title,
                maxLines = 2,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = colorResource(R.color.textDarkest)
            )
            Text(
                modifier = Modifier.testTag("resultType"),
                text = stringResource(getContentTypeTitle(result.type)),
                maxLines = 1,
                fontSize = 16.sp,
                color = color
            )
            if (result.body.isNotEmpty()) {
                Text(
                    modifier = Modifier.testTag("resultBody"),
                    text = result.body,
                    maxLines = 3,
                    fontSize = 16.sp,
                    color = colorResource(R.color.textDark)
                )
            }
        }
        Relevance(result.relevance)
    }
}

@Composable
private fun Relevance(relevance: Int) {
    val color = when {
        relevance >= 50 -> colorResource(R.color.borderSuccess)
        relevance in 25..49 -> colorResource(R.color.borderWarning)
        else -> colorResource(R.color.borderDanger)
    }

    val count = (relevance / 25) + 1

    Row(modifier = Modifier.padding(horizontal = 16.dp)) {
        for (i in 0 until 4) {
            Box(
                modifier = Modifier
                    .testTag("relevanceDot ${if (i < count) "filled" else "empty"}")
                    .size(4.dp)
                    .clip(RoundedCornerShape(1.dp))
                    .background(if (i < count) color else colorResource(R.color.borderMedium))
            )
            Spacer(modifier = Modifier.size(2.dp))
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
fun SmartSearchPreview() {
    ContextKeeper.appContext = LocalContext.current
    SmartSearchScreen(
        SmartSearchUiState(
            loading = false,
            query = "query",
            canvasContext = Course(name = "Test course"),
            results = listOf(
                SmartSearchResultUiState(
                    title = "Title",
                    body = "Body",
                    relevance = 23,
                    type = SmartSearchContentType.ASSIGNMENT,
                    url = "url"
                ),
                SmartSearchResultUiState(
                    title = "Not to lay peacefully between its four familiar walls.",
                    body = "...nsformed in his bed into a horrible vermin. He lessoned on his armour-like back, and if he lifted his head a...",
                    relevance = 75,
                    type = SmartSearchContentType.WIKI_PAGE,
                    url = "url"
                )
            )
        ) {}) {}
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SmartSearchDarkPreview() {
    ContextKeeper.appContext = LocalContext.current
    SmartSearchScreen(
        SmartSearchUiState(
            loading = false,
            query = "query",
            canvasContext = Course(name = "Test course"),
            results = listOf(
                SmartSearchResultUiState(
                    title = "Title",
                    body = "Body",
                    relevance = 75,
                    type = SmartSearchContentType.ANNOUNCEMENT,
                    url = "url"
                ),
                SmartSearchResultUiState(
                    title = "Not to lay peacefully between its four familiar walls.",
                    body = "...nsformed in his bed into a horrible vermin. He lessoned on his armour-like back, and if he lifted his head a...",
                    relevance = 50,
                    type = SmartSearchContentType.DISCUSSION_TOPIC,
                    url = "url"
                )
            )
        ) {}) {}
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
fun SmartSearchLoadingPreview() {
    ContextKeeper.appContext = LocalContext.current
    SmartSearchScreen(
        SmartSearchUiState(
            loading = true,
            query = "query",
            canvasContext = Course(name = "Test course"),
            results = emptyList()
        ) {}) {}
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SmartSearchLoadingDarkPreview() {
    ContextKeeper.appContext = LocalContext.current
    SmartSearchScreen(
        SmartSearchUiState(
            loading = true,
            query = "query",
            canvasContext = Course(name = "Test course"),
            results = emptyList()
        ) {}) {}
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
fun SmartSearchErrorPreview() {
    ContextKeeper.appContext = LocalContext.current
    SmartSearchScreen(
        SmartSearchUiState(
            loading = false,
            query = "query",
            canvasContext = Course(name = "Test course"),
            results = emptyList(),
            error = true
        ) {}) {}
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SmartSearchErrorDarkPreview() {
    ContextKeeper.appContext = LocalContext.current
    SmartSearchScreen(
        SmartSearchUiState(
            loading = false,
            query = "query",
            canvasContext = Course(name = "Test course"),
            results = emptyList(),
            error = true
        ) {}) {}
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
fun SmartSearchEmptyPreview() {
    ContextKeeper.appContext = LocalContext.current
    SmartSearchScreen(
        SmartSearchUiState(
            loading = false,
            query = "query",
            canvasContext = Course(name = "Test course"),
            results = emptyList()
        ) {}) {}
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SmartSearchEmptyDarkPreview() {
    ContextKeeper.appContext = LocalContext.current
    SmartSearchScreen(
        SmartSearchUiState(
            loading = false,
            query = "query",
            canvasContext = Course(name = "Test course"),
            results = emptyList()
        ) {}) {}
}