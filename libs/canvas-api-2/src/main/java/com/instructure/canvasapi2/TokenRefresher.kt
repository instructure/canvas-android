package com.instructure.canvasapi2

import android.content.Context
import android.content.Intent
import android.util.Log
import com.instructure.canvasapi2.apis.OAuthAPI
import com.instructure.canvasapi2.models.CanvasAuthError
import kotlinx.coroutines.Dispatchers
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
            runBlocking(Dispatchers.Unconfined) {
                withTimeoutOrNull(5000) {
                    while (refreshState == TokenRefreshState.Refreshing) {
                        delay(1000)
                        Log.d("ASDFGASDFG", "Waiting for refresh")
                    }
                } ?: run {
                    refreshState = TokenRefreshState.Failed
                }

                withTimeoutOrNull(2 * 60 * 1000) {
                    while (refreshState == TokenRefreshState.LoginStarted) {
                        delay(1000)
                        Log.d("ASDFGASDFG", "Waiting for login")
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
                launchLogin()
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
    data object LoginStarted : TokenRefreshState()
    data object Failed : TokenRefreshState()
    data object Restart : TokenRefreshState()
    data class Success(val token: String) : TokenRefreshState()
}