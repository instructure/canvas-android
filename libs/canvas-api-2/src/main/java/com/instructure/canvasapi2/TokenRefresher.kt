package com.instructure.canvasapi2

import android.content.Intent
import com.instructure.canvasapi2.apis.OAuthAPI
import com.instructure.canvasapi2.managers.OAuthManager
import com.instructure.canvasapi2.models.CanvasAuthError
import com.instructure.canvasapi2.models.OAuthTokenResponse
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.ContextKeeper
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import okhttp3.Request
import okhttp3.Response
import org.greenrobot.eventbus.EventBus

private const val AUTH_HEADER = "Authorization"
private const val RETRY_HEADER = "mobile_refresh"

class TokenRefresher(private val loginRouter: LoginRouter) {

    var refreshState: TokenRefreshState? = null

    fun refresh(response: Response): Request? {
        if (refreshState != null && refreshState is TokenRefreshState.Refreshing) {
            return waitForRefresh(response)
        }
        refreshState = TokenRefreshState.Refreshing
        try {
            val refreshed: OAuthTokenResponse
            runBlocking {
                refreshed = OAuthManager.refreshTokenAsync().await().dataOrThrow
                ApiPrefs.accessToken = refreshed.accessToken!!
            }
            return response.request.newBuilder()
                .header(AUTH_HEADER, OAuthAPI.authBearer(refreshed.accessToken!!))
                .header(
                    RETRY_HEADER, RETRY_HEADER
                ) // Mark retry to prevent infinite recursion
                .build()
        } catch (e: Exception) {
            loginRouter.loginIntent().let {
                it.putExtra("refresh", true)
                it.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                ContextKeeper.appContext.startActivity(it)
                return waitForRefresh(response)
            }
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
                    EventBus.getDefault().post(CanvasAuthError("Failed to authenticate"))
                    null
                }
            }

            is TokenRefreshState.Failed -> {
                EventBus.getDefault().post(CanvasAuthError("Failed to authenticate"))
                newRequest = null
            }

            else -> {
                newRequest = null
            }
        }
        return newRequest
    }
}

sealed class TokenRefreshState {
    data object Refreshing : TokenRefreshState()
    data object Failed : TokenRefreshState()
    data class Success(val token: String) : TokenRefreshState()
}