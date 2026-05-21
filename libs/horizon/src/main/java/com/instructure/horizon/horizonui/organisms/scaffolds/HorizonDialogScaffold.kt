/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
package com.instructure.horizon.horizonui.organisms.scaffolds

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.instructure.horizon.R
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.foundation.horizonBorder
import com.instructure.horizon.horizonui.molecules.Button
import com.instructure.horizon.horizonui.molecules.ButtonColor
import com.instructure.horizon.horizonui.molecules.ButtonHeight
import com.instructure.horizon.horizonui.molecules.ButtonWidth
import com.instructure.horizon.horizonui.molecules.IconButton
import com.instructure.horizon.horizonui.molecules.IconButtonColor
import com.instructure.horizon.horizonui.molecules.IconButtonSize
import com.instructure.horizon.horizonui.molecules.LoadingButton
import com.instructure.horizon.util.zeroScreenInsets

@Composable
fun HorizonDialogScaffold(
    title: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    confirmColor: ButtonColor = ButtonColor.Black,
    dismissLabel: String,
    onDismiss: () -> Unit,
    isConfirmButtonLoading: Boolean = false,
    statusBarColor: Color? = HorizonColors.Surface.pageSecondary(),
    navigationBarColor: Color? = HorizonColors.Surface.pageSecondary(),
    containerColor: Color = HorizonColors.Surface.pageSecondary(),
    content: @Composable (contentPadding: PaddingValues) -> Unit
) {
    EdgeToEdgeScaffold(
        statusBarColor = statusBarColor,
        navigationBarColor = navigationBarColor,
        containerColor = containerColor,
        topBar = { paddingValues ->
            HorizonDialogScaffoldTopBar(
                title,
                onDismiss,
                Modifier.padding(paddingValues)
            )
        },
        bottomBar = { paddingValues ->
            HorizonDialogScaffoldBottomBar(
                confirmLabel,
                dismissLabel,
                isConfirmButtonLoading,
                confirmColor,
                onConfirm,
                onDismiss,
                Modifier.padding(paddingValues)
            )
        }
    ) { contentPadding ->
        content(contentPadding)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HorizonDialogScaffoldTopBar(
    title: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        windowInsets = WindowInsets.zeroScreenInsets,
        title = {
            Text(
                text = title,
                style = HorizonTypography.h3,
                color = HorizonColors.Text.title()
            )
        },
        actions = {
            IconButton(
                iconRes = R.drawable.close,
                contentDescription = stringResource(R.string.close),
                onClick = onDismiss,
                size = IconButtonSize.SMALL,
                color = IconButtonColor.Ghost
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = HorizonColors.Surface.pageSecondary(),
            titleContentColor = HorizonColors.Text.title(),
            navigationIconContentColor = HorizonColors.Icon.default()
        ),
        modifier = modifier
            .horizonBorder(
                HorizonColors.LineAndBorder.lineStroke(),
                bottom = 1.dp
            )
            .background(HorizonColors.Surface.pageSecondary())
    )
}

@Composable
private fun HorizonDialogScaffoldBottomBar(
    confirmLabel: String,
    dismissLabel: String,
    isConfirmButtonLoading: Boolean,
    confirmColor: ButtonColor,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    BottomAppBar(
        windowInsets = WindowInsets.zeroScreenInsets,
        contentPadding = PaddingValues(vertical = 12.dp, horizontal = 16.dp),
        containerColor = HorizonColors.Surface.pageSecondary(),
        modifier = modifier
            .horizonBorder(
                HorizonColors.LineAndBorder.lineStroke(),
                top = 1.dp
            )
            .background(HorizonColors.Surface.pageSecondary())
    ) {
        Button(
            label = dismissLabel,
            width = ButtonWidth.RELATIVE,
            height = ButtonHeight.NORMAL,
            color = ButtonColor.Ghost,
            onClick = onDismiss
        )

        Spacer(Modifier.weight(1f))

        LoadingButton(
            loading = isConfirmButtonLoading,
            label = confirmLabel,
            width = ButtonWidth.RELATIVE,
            height = ButtonHeight.NORMAL,
            color = confirmColor,
            contentAlignment = Alignment.Center,
            fixedLoadingSize = true,
            onClick = onConfirm
        )
    }
}