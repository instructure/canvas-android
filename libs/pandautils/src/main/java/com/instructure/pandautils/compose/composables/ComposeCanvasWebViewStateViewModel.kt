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
package com.instructure.pandautils.compose.composables

import android.os.Bundle
import androidx.lifecycle.ViewModel

/**
 * Holds WebView instance state in memory so it survives configuration changes without
 * going through the Binder IPC boundary (which has a ~1 MB limit). This prevents
 * TransactionTooLargeException when the WebView state is large.
 */
internal class ComposeCanvasWebViewStateViewModel : ViewModel() {
    val webViewState = Bundle()
}
