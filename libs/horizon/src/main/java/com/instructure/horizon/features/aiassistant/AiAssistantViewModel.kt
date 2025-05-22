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
package com.instructure.horizon.features.aiassistant

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.managers.CedarApiManager
import com.instructure.canvasapi2.managers.DocumentSource
import com.instructure.canvasapi2.managers.PineApiManager
import com.instructure.canvasapi2.managers.RedwoodApiManager
import com.instructure.pine.type.MessageInput
import com.instructure.pine.type.Role
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AiAssistantViewModel @Inject constructor(
    private val cedarApiManager: CedarApiManager,
    private val pineApiManager: PineApiManager,
    private val redwoodApiManager: RedwoodApiManager,
): ViewModel() {
    init {
        viewModelScope.launch {
            val notes = redwoodApiManager.getNotes()
            Log.d("AI Assistant", "Notes from Redwood: $notes")

//            val ping = pineApiManager.ping()
//            Log.d("AI Assistant", "Ping from Pine: $ping")

            val answer = pineApiManager.queryDocument(
                messages = listOf(
                    MessageInput(
                        role = Role.User,
                        text = "Hello, world!",
                    )
                ),
                metadata = emptyMap(),
                source = DocumentSource.canvas,
            )
            Log.d("AI Assistant", "Answer from Pine: $answer")

            val hello = cedarApiManager.sayHello()
            Log.d("AI Assistant", "Hello from Cedar: $hello")

            val translated = cedarApiManager.translateText("Hello, world!", "es")
            Log.d("AI Assistant", "Translated text: $translated")
        }
    }
}