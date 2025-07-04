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
package com.instructure.horizon.features.notebook.common.composable

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.horizon.features.notebook.common.model.NotebookType
import com.instructure.horizon.horizonui.molecules.Pill
import com.instructure.horizon.horizonui.molecules.PillCase
import com.instructure.horizon.horizonui.molecules.PillSize
import com.instructure.horizon.horizonui.molecules.PillStyle
import com.instructure.horizon.horizonui.molecules.PillType

@Composable
fun NotebookPill(
    type: NotebookType,
    modifier: Modifier = Modifier
) {
    val pillType = when (type) {
        NotebookType.Confusing -> PillType.DANGER
        NotebookType.Important -> PillType.INSTITUTION
    }

    Pill(
        label = stringResource(type.labelRes),
        style = PillStyle.OUTLINE,
        type = pillType,
        case = PillCase.UPPERCASE,
        size = PillSize.REGULAR,
        iconRes = type.iconRes,
        modifier = modifier
    )
}

@Composable
@Preview
private fun NotebookPillConfusingPreview() {
    ContextKeeper.appContext = LocalContext.current
    NotebookPill(type = NotebookType.Confusing)
}

@Composable
@Preview
private fun NotebookPillImportantPreview() {
    ContextKeeper.appContext = LocalContext.current
    NotebookPill(type = NotebookType.Important)
}