/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
package com.instructure.pandautils.features.notes.notebook

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.instructure.canvasapi2.models.NoteType
import com.instructure.instui.compose.indicator.Pill
import com.instructure.instui.compose.indicator.PillVariant
import com.instructure.instui.token.icon.InstUIIcons
import com.instructure.instui.token.icon.circleHelp
import com.instructure.instui.token.icon.pin
import com.instructure.instui.token.semantic.InstUISemanticColors
import com.instructure.pandautils.R

@Composable
fun NoteType.label(): String? = when (this) {
    NoteType.IMPORTANT -> stringResource(R.string.notebookNoteTypeImportant)
    NoteType.CONFUSING -> stringResource(R.string.notebookNoteTypeUnclear) // display label "Unclear", not the wire value
    NoteType.OTHER -> null
}

val NoteType.pillVariant: PillVariant get() = when (this) {
    NoteType.IMPORTANT -> PillVariant.Info
    NoteType.CONFUSING -> PillVariant.Warning
    NoteType.OTHER -> PillVariant.Default
}

val NoteType.pillIcon: ImageVector? get() = when (this) {
    NoteType.IMPORTANT -> InstUIIcons.pin
    NoteType.CONFUSING -> InstUIIcons.circleHelp
    NoteType.OTHER -> null
}

/** Read-only reaction indicator. Renders nothing for [NoteType.OTHER] (no reaction to show). */
@Composable
fun NoteTypePill(noteType: NoteType, modifier: Modifier = Modifier) {
    val label = noteType.label() ?: return
    Pill(text = label, variant = noteType.pillVariant, icon = noteType.pillIcon, modifier = modifier)
}

/**
 * The accent color used for the quote excerpt's underline/text on the list row (ties the excerpt
 * visually back to its pill). Mirrors Pill.kt's own per-variant color resolution exactly: Pill's
 * [PillVariant.Info] uses [InstUIPill.infoTextColor][com.instructure.instui.token.component.InstUIPill.infoTextColor]
 * (= [InstUISemanticColors.Text.info]) and [PillVariant.Warning] uses
 * [InstUIPill.warningTextColor][com.instructure.instui.token.component.InstUIPill.warningTextColor]
 * (= [InstUISemanticColors.Text.warning]) - reusing the same semantic tokens here keeps the pill and
 * the quote accent always in sync.
 */
@Composable
fun NoteType.accentColor(): Color = when (this) {
    NoteType.IMPORTANT -> InstUISemanticColors.Text.info()
    NoteType.CONFUSING -> InstUISemanticColors.Text.warning()
    NoteType.OTHER -> InstUISemanticColors.Text.base()
}
