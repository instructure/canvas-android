package com.instructure.canvasapi2

import android.content.Context
import android.content.Intent
import com.instructure.canvasapi2.apis.OAuthAPI
import com.instructure.canvasapi2.managers.OAuthManager
import com.instructure.canvasapi2.models.CanvasAuthError
import com.instructure.canvasapi2.models.OAuthTokenResponse
import com.instructure.canvasapi2.utils.ApiPrefs
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import okhttp3.Request
import okhttp3.Response
import org.greenrobot.eventbus.EventBus

private const val AUTH_HEADER = "Authorization"
private const val RETRY_HEADER = "mobile_refresh"

class TokenRefresher(
    private val context: Context,
    private val loginRouter: LoginRouter,
    private val apiPrefs: ApiPrefs,
    private val eventBus: EventBus) {

    var refreshState: TokenRefreshState? = null

    fun refresh(response: Response): Request? {
        if (refreshState != null && refreshState is TokenRefreshState.Refreshing) {
            return waitForRefresh(response)
        }

        try {
            val refreshed: OAuthTokenResponse
            runBlocking {
                refreshed = OAuthManager.refreshTokenAsync().await().dataOrThrow
                apiPrefs.accessToken = refreshed.accessToken!!
            }
            return response.request.newBuilder()
                .header(AUTH_HEADER, OAuthAPI.authBearer(refreshed.accessToken!!))
                .header(
                    RETRY_HEADER, RETRY_HEADER
                ) // Mark retry to prevent infinite recursion
                .build()
        } catch (e: Exception) {
            refreshState = TokenRefreshState.Refreshing
            launchLogin()
            return waitForRefresh(response)
        }
    }

    private fun waitForRefresh(response: Response): Request? {
        runBlocking {
            while (refreshState is TokenRefreshState.Refreshing) {
                delay(1000)
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
                    eventBus.post(CanvasAuthError("Failed to authenticate"))
                    null
                }
            }

            is TokenRefreshState.Failed -> {
                eventBus.post(CanvasAuthError("Failed to authenticate"))
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
    data object Failed : TokenRefreshState()
    data object Restart : TokenRefreshState()
    data class Success(val token: String) : TokenRefreshState()
}