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
package com.instructure.horizon.features.aiassistant.navigation

import android.net.Uri
import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.instructure.horizon.features.aiassistant.chat.AiAssistChatScreen
import com.instructure.horizon.features.aiassistant.chat.AiAssistChatViewModel
import com.instructure.horizon.features.aiassistant.common.model.AiAssistContext
import com.instructure.horizon.features.aiassistant.common.model.AiAssistMessage
import com.instructure.horizon.features.aiassistant.flashcard.AiAssistFlashcardScreen
import com.instructure.horizon.features.aiassistant.flashcard.AiAssistFlashcardViewModel
import com.instructure.horizon.features.aiassistant.main.AiAssistMainScreen
import com.instructure.horizon.features.aiassistant.quiz.AiAssistQuizScreen
import kotlinx.serialization.json.Json
import kotlin.reflect.typeOf

@Composable
fun AiAssistNavigation(
    navController: NavHostController,
    onDismiss: () -> Unit,
    aiContext: AiAssistContext,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController,
        startDestination = AiAssistRoute.AiAssistMain.route,
        modifier = modifier
    ) {
        composable(AiAssistRoute.AiAssistMain.route) {
            AiAssistMainScreen(navController, aiContext, onDismiss)
        }
        composable<AiAssistRoute.AiAssistChat>(
            typeMap = AiAssistNavigationTypeMap
        ) {
            val viewModel: AiAssistChatViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsState()
            AiAssistChatScreen(navController, onDismiss, state)
        }

        composable<AiAssistRoute.AiAssistQuiz>(
            typeMap = AiAssistNavigationTypeMap
        ) {
            AiAssistQuizScreen(navController, onDismiss)
        }

        composable<AiAssistRoute.AiAssistFlashcard>(
            typeMap = AiAssistNavigationTypeMap
        ) {
            val viewModel: AiAssistFlashcardViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsState()
            AiAssistFlashcardScreen(navController, state, onDismiss)
        }
    }
}

val AiAssistNavigationTypeMap = mapOf(
    typeOf<AiAssistRoute.AiAssistChat>() to navTypeOf<AiAssistRoute.AiAssistChat>(isNullableAllowed = true),
    typeOf<AiAssistContext>() to navTypeOf<AiAssistContext>(isNullableAllowed = true),
    typeOf<Map<String, String>>() to navTypeOf<Map<String, String>>(isNullableAllowed = true),
    typeOf<List<AiAssistMessage>>() to navTypeOf<List<AiAssistMessage>>(isNullableAllowed = true),
)

private inline fun <reified T> navTypeOf(
    isNullableAllowed: Boolean = false,
    json: Json = Json,
) = object : NavType<T>(isNullableAllowed = isNullableAllowed) {
    override fun get(bundle: Bundle, key: String): T? =
        bundle.getString(key)?.let(json::decodeFromString)

    override fun parseValue(value: String): T = json.decodeFromString(Uri.decode(value))

    override fun serializeAsValue(value: T): String = Uri.encode(json.encodeToString(value))

    override fun put(bundle: Bundle, key: String, value: T) =
        bundle.putString(key, json.encodeToString(value))

}