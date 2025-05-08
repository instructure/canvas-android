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
package com.instructure.horizon.features.aiassistant.common

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.horizon.R
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.foundation.SpaceSize
import com.instructure.horizon.horizonui.molecules.IconButton
import com.instructure.horizon.horizonui.molecules.IconButtonColor
import com.instructure.horizon.horizonui.molecules.IconButtonSize

@Composable
fun AiAssistPagination(
    currentPage: Int,
    totalPages: Int,
    onNextPage: () -> Unit,
    onPreviousPage: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            iconRes = R.drawable.chevron_left,
            contentDescription = stringResource(R.string.a11y_aiAssistPaginationPreviousPage),
            color = IconButtonColor.INVERSE,
            size = IconButtonSize.NORMAL,
            onClick = onPreviousPage,
            enabled = currentPage > 1,
        )

        HorizonSpace(SpaceSize.SPACE_24)

        Text(
            text = stringResource(R.string.aiAssistPageLabel, currentPage, totalPages),
            style = HorizonTypography.p1,
            color = HorizonColors.Text.surfaceColored(),
        )

        HorizonSpace(SpaceSize.SPACE_24)

        IconButton(
            iconRes = R.drawable.chevron_right,
            contentDescription = stringResource(R.string.a11y_aiAssistPaginationNextPage),
            color = IconButtonColor.INVERSE,
            size = IconButtonSize.NORMAL,
            onClick = onNextPage,
            enabled = currentPage < totalPages,
        )
    }
}

@Composable
@Preview
private fun AiAssistPaginationPreview() {
    ContextKeeper.appContext = LocalContext.current
    AiAssistPagination(
        currentPage = 1,
        totalPages = 5,
        onNextPage = {},
        onPreviousPage = {},
        modifier = Modifier,
    )
}