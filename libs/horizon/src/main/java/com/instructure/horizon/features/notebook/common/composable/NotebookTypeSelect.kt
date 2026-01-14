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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.instructure.horizon.R
import com.instructure.horizon.features.notebook.common.model.NotebookType
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.molecules.DropdownChip
import com.instructure.horizon.horizonui.molecules.DropdownItem

@Composable
fun NotebookTypeSelect(
    selected: NotebookType?,
    onSelect: (NotebookType?) -> Unit,
    showIcons: Boolean,
    showAllOption: Boolean,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    verticalPadding: Dp = 6.dp
) {
    val context = LocalContext.current
    val defaultBackgroundColor = HorizonColors.PrimitivesGrey.grey12()
    val importantBgColor = HorizonColors.PrimitivesSea.sea12()
    val confusingBgColor = HorizonColors.PrimitivesRed.red12()
    val allNotesItem = DropdownItem(
        value = null as NotebookType?,
        label = context.getString(R.string.notebookTypeAllNotes),
        iconRes = R.drawable.menu,
        iconTint = HorizonColors.Icon.default(),
        backgroundColor = defaultBackgroundColor
    )
    val typeItems = remember {
        buildList {
            if (showAllOption) {
                add(allNotesItem)
            }
            add(
                DropdownItem(
                    value = NotebookType.Important,
                    label = context.getString(NotebookType.Important.labelRes),
                    iconRes = NotebookType.Important.iconRes,
                    iconTint = Color(context.getColor(NotebookType.Important.lineColor)),
                    backgroundColor = importantBgColor
                )
            )
            add(
                DropdownItem(
                    value = NotebookType.Confusing,
                    label = context.getString(NotebookType.Confusing.labelRes),
                    iconRes = NotebookType.Confusing.iconRes,
                    iconTint = Color(context.getColor(NotebookType.Confusing.lineColor)),
                    backgroundColor = confusingBgColor
                )
            )
        }
    }
    val selectedTypeItem =
        if (selected == null) allNotesItem else typeItems.find { it.value == selected }


    DropdownChip(
        items = typeItems,
        selectedItem = selectedTypeItem,
        onItemSelected = { item -> onSelect(item?.value) },
        placeholder = stringResource(R.string.notebookFilterTypePlaceholder),
        dropdownWidth = 178.dp,
        verticalPadding = verticalPadding,
        showIconCollapsed = showIcons,
        enabled = enabled,
        borderColor = if (showIcons) {
            selectedTypeItem?.iconTint ?: HorizonColors.LineAndBorder.lineStroke()
        } else {
            HorizonColors.LineAndBorder.lineStroke()
        },
        contentColor = if (showIcons) {
            selectedTypeItem?.iconTint ?: HorizonColors.Text.body()
        } else {
            HorizonColors.Text.body()
        },
        modifier = modifier
    )
}

@Composable
@Preview
private fun NotebookTypeSelectAllPreview() {
    NotebookTypeSelect(null, {}, true, true)
}

@Composable
@Preview
private fun NotebookTypeSelectImportantPreview() {
    NotebookTypeSelect(NotebookType.Important, {}, true, true)
}

@Composable
@Preview
private fun NotebookTypeSelectConfusingPreview() {
    NotebookTypeSelect(NotebookType.Confusing, {}, true, true)
}