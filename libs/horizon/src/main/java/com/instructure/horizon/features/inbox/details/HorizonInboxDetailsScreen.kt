/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.horizon.features.inbox.details

import androidx.annotation.DrawableRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.instructure.horizon.R
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonCornerRadius
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.foundation.SpaceSize
import com.instructure.horizon.horizonui.molecules.Button
import com.instructure.horizon.horizonui.molecules.ButtonColor
import com.instructure.horizon.horizonui.molecules.ButtonIconPosition
import com.instructure.horizon.horizonui.molecules.HorizonDivider
import com.instructure.horizon.horizonui.molecules.filedrop.FileDropItem
import com.instructure.horizon.horizonui.molecules.filedrop.FileDropItemState
import com.instructure.horizon.horizonui.organisms.inputs.textarea.TextArea
import com.instructure.horizon.horizonui.organisms.inputs.textarea.TextAreaState
import com.instructure.horizon.horizonui.platform.LoadingStateWrapper
import com.instructure.pandautils.utils.toFormattedString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HorizonInboxDetailsScreen(
    state: HorizonInboxDetailsUiState,
    navController: NavHostController
) {
    Scaffold(
        containerColor = HorizonColors.Surface.pagePrimary(),
        topBar = { HorizonInboxDetailsHeader(state.title, state.titleIcon, navController) },
    ) { innerPadding ->
        LoadingStateWrapper(state.loadingState, modifier = Modifier.padding(innerPadding)) {
            HorizonInboxDetailsContent(state)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HorizonInboxDetailsHeader(
    title: String,
    @DrawableRes titleIcon: Int?,
    navController: NavHostController
) {
    CenterAlignedTopAppBar(
        title = {
            Row(
                modifier = Modifier,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (titleIcon != null) {
                    Icon(
                        painterResource(id = titleIcon),
                        contentDescription = null,
                        tint = HorizonColors.Surface.institution(),
                        modifier = Modifier.size(16.dp)
                    )

                    HorizonSpace(SpaceSize.SPACE_4)
                }

                Text(
                    text = title,
                    style = HorizonTypography.labelLargeBold,
                    color = HorizonColors.Surface.institution(),
                    maxLines = 2
                )
            }
        },
        navigationIcon = {
            Image(
                painter = painterResource(id = R.drawable.arrow_back),
                contentDescription = null,
                modifier = Modifier
                    //.size(24.dp)
                    .padding(horizontal = 10.dp)
                    .clickable {
                        navController.popBackStack()
                    }
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = HorizonColors.Surface.pagePrimary(),
            titleContentColor = HorizonColors.Text.title(),
            navigationIconContentColor = HorizonColors.Icon.default()
        )
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HorizonInboxDetailsContent(
    state: HorizonInboxDetailsUiState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .clip(HorizonCornerRadius.level4Top)
            .background(HorizonColors.Surface.pageSecondary())
    ){
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(HorizonColors.Surface.pageSecondary()),
            reverseLayout = state.bottomLayout,
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            if (state.replyState != null) {
                stickyHeader { HorizonInboxReplyContent(state.replyState) }
            }
            items(state.items) {
                Column {
                    HorizonInboxDetailsItem(it)
                    if (it != state.items.last()) {
                        HorizonDivider()
                    }
                }
            }
        }
    }
}

@Composable
private fun HorizonInboxDetailsItem(
    item: HorizonInboxDetailsItem,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(HorizonColors.Surface.pageSecondary())
            .padding(top = 16.dp, bottom = 24.dp, start = 24.dp, end = 24.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ){
            Text(
                text = item.author,
                style = HorizonTypography.labelLargeBold,
                color = HorizonColors.Text.body(),
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = item.date.toFormattedString(),
                style = HorizonTypography.p3,
                color = HorizonColors.Text.timestamp(),
            )
        }

        HorizonSpace(SpaceSize.SPACE_8)

        val annotatedString = AnnotatedString.fromHtml(
            item.content,
            linkStyles = TextLinkStyles(
                style = SpanStyle(
                    textDecoration = TextDecoration.Underline,
                    color = HorizonColors.Text.link()
                )
            )
        )
        Text(
            text = annotatedString,
            style = HorizonTypography.p1,
            color = HorizonColors.Text.body(),
        )

        item.attachments.forEach {
            HorizonSpace(SpaceSize.SPACE_8)

            FileDropItem(
                state = FileDropItemState.NoLongerEditable(
                    fileName = it.displayName.orEmpty(),
                    onClick = { /* Handle attachment click */ }
                ),
                hasBorder = true,
                borderColor = HorizonColors.LineAndBorder.lineStroke()
            )
        }
    }
}

@Composable
private fun HorizonInboxReplyContent(state: HorizonInboxReplyState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(HorizonColors.Surface.pagePrimary())
            .padding(horizontal = 24.dp, vertical = 16.dp)

    ) {
        var isFocused by remember { mutableStateOf(false) }
        val textAreaState = TextAreaState(
            value = state.replyTextValue,
            onValueChange = state.onReplyTextValueChange,
            placeHolderText = "Reply",
            isFocused = isFocused,
            onFocusChanged = { isFocused = it },
        )
        TextArea(textAreaState)

        HorizonSpace(SpaceSize.SPACE_16)

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                label = "Attach file",
                color = ButtonColor.Inverse,
                iconPosition = ButtonIconPosition.Start(R.drawable.attach_file),
                onClick = {},
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                label = "Send",
                color = ButtonColor.Institution,
                onClick = {},
            )
        }
    }
}