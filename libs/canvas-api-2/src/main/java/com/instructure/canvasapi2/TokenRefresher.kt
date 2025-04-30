/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
 *
 */
package com.instructure.canvasapi2

import android.content.Context
import android.content.Intent
import com.instructure.canvasapi2.apis.OAuthAPI
import com.instructure.canvasapi2.models.CanvasAuthError
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import okhttp3.Request
import okhttp3.Response
import org.greenrobot.eventbus.EventBus

private const val AUTH_HEADER = "Authorization"
private const val RETRY_HEADER = "mobile_refresh"

class TokenRefresher(
    private val context: Context,
    private val loginRouter: LoginRouter,
    private val eventBus: EventBus
) {

    var refreshState: TokenRefreshState? = null
    var loggedOut = false

    fun refresh(response: Response): Request? {
        refreshState = TokenRefreshState.Refreshing
        return waitForRefresh(response)
    }

    private fun waitForRefresh(response: Response): Request? = synchronized(this) {
        if (refreshState == TokenRefreshState.Refreshing) {
            launchLogin()
        }

        while (refreshState == TokenRefreshState.Refreshing) {
            runBlocking {
                withTimeoutOrNull(2 * 60 * 1000) {
                    while (refreshState == TokenRefreshState.Refreshing) {
                        delay(1000)
                    }
                } ?: run {
                    refreshState = TokenRefreshState.Failed
                }
            }
        }

        val newRequest: Request?
        when (refreshState) {
            is TokenRefreshState.Success -> {
                newRequest = try {
                    response.request.newBuilder()
                        .header(
                            AUTH_HEADER,
                            OAuthAPI.authBearer((refreshState as TokenRefreshState.Success).token)
                        )
                        .header(
                            RETRY_HEADER,
                            RETRY_HEADER
                        ) // Mark retry to prevent infinite recursion
                        .build()
                } catch (e: Exception) {
                    if (!loggedOut) {
                        eventBus.post(CanvasAuthError("Failed to authenticate"))
                        loggedOut = true
                    }
                    null
                }
            }

            is TokenRefreshState.Failed -> {
                if (!loggedOut) {
                    eventBus.post(CanvasAuthError("Failed to authenticate"))
                    loggedOut = true
                }
                newRequest = null
            }

            is TokenRefreshState.Restart -> {
                refreshState = TokenRefreshState.Refreshing
                newRequest = waitForRefresh(response)
            }

            else -> {
                newRequest = null
            }
        }
        return newRequest
    }

    private fun launchLogin() {
        val intent = loginRouter.loginIntent()
        intent.putExtra(TOKEN_REFRESH, true)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }

    companion object {
        const val TOKEN_REFRESH = "token_refresh"
    }
}

sealed class TokenRefreshState {
    data object Refreshing : TokenRefreshState()
    data object Failed : TokenRefreshState()
    data object Restart : TokenRefreshState()
    data class Success(val token: String) : TokenRefreshState()
}