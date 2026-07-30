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
package com.instructure.ngc

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.toArgb
import com.instructure.instui.token.semantic.InstUISemanticColors
import com.instructure.pandautils.views.CanvasWebView
import com.instructure.pandautils.views.CanvasWebViewWrapper
import com.instructure.pandautils.views.HtmlFormatColors

/**
 * WebView content colors resolved from the InstUI semantic tokens (theme-aware, no static resources).
 * Set as [CanvasWebView.overrideHtmlFormatColors] so NGC WebViews render with the design-system colors.
 */
@Composable
fun ngcHtmlFormatColors(): HtmlFormatColors = HtmlFormatColors(
    backgroundColorInt = InstUISemanticColors.Background.page().toArgb(),
    textColorInt = InstUISemanticColors.Text.base().toArgb(),
    linkColorInt = InstUISemanticColors.Text.info().toArgb(),
    visitedLinkColorInt = InstUISemanticColors.Text.info().toArgb(),
)

/**
 * Forced-light NGC WebView colors, used when the user switches a WebView to light appearance so the
 * switched content matches NGC's light theme instead of the generic defaults. Set as
 * [CanvasWebView.overrideHtmlFormatColorsSwitched]. Not theme-aware by design (always the light variants).
 */
fun ngcHtmlFormatColorsLight(): HtmlFormatColors = HtmlFormatColors(
    backgroundColorInt = InstUISemanticColors.Light.Background.page.toArgb(),
    textColorInt = InstUISemanticColors.Light.Text.base.toArgb(),
    linkColorInt = InstUISemanticColors.Light.Text.info.toArgb(),
    visitedLinkColorInt = InstUISemanticColors.Light.Text.info.toArgb(),
)

/**
 * Theme-switch button colors matching NGC's WebView appearance: the button surface uses the page
 * background and the border/text use the base text color, so the control blends with the content.
 * Set as [CanvasWebViewWrapper.themeSwitchButtonColors].
 */
@Composable
fun ngcThemeSwitchButtonColors(): CanvasWebViewWrapper.ThemeSwitchButtonColors =
    CanvasWebViewWrapper.ThemeSwitchButtonColors(
        surface = InstUISemanticColors.Background.page().toArgb(),
        foreground = InstUISemanticColors.Text.base().toArgb(),
    )

/**
 * Forced-light NGC theme-switch button colors, used when the WebView is switched to light appearance
 * so the control matches NGC's light theme. Set as [CanvasWebViewWrapper.themeSwitchButtonColorsSwitched].
 */
fun ngcThemeSwitchButtonColorsLight(): CanvasWebViewWrapper.ThemeSwitchButtonColors =
    CanvasWebViewWrapper.ThemeSwitchButtonColors(
        surface = InstUISemanticColors.Light.Background.page.toArgb(),
        foreground = InstUISemanticColors.Light.Text.base.toArgb(),
    )
