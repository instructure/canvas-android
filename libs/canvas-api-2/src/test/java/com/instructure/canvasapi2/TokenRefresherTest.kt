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
 *
 */
package com.instructure.canvasapi2

import android.content.Context
import android.content.Intent
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import okhttp3.Response
import org.greenrobot.eventbus.EventBus
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Provider

class TokenRefresherTest {

    private val context: Context = mockk(relaxed = true)
    private val eventBus: EventBus = mockk(relaxed = true)

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test(timeout = 15_000)
    fun `constructing TokenRefresher does not resolve the router`() {
        val loginRouter = CountingLoginRouterProvider()

        TokenRefresher(context, loginRouter, eventBus)

        assertEquals(0, loginRouter.callCount.get())
    }

    @Test(timeout = 15_000)
    fun `each refresh resolves the router again`() {
        val loginRouter = CountingLoginRouterProvider()
        val tokenRefresher = TokenRefresher(context, loginRouter, eventBus)
        val response: Response = mockk(relaxed = true)

        val firstRouter = driveOneRefresh(tokenRefresher, response, loginRouter)
        tokenRefresher.loggedOut = false
        val secondRouter = driveOneRefresh(tokenRefresher, response, loginRouter)

        assertEquals(2, loginRouter.callCount.get())
        assertNotSame(firstRouter, secondRouter)
    }

    /**
     * Runs [TokenRefresher.refresh] on a background thread (it blocks in a poll loop), waits
     * until [TokenRefresher.launchLogin] has resolved the router and started the login
     * [Intent], then unblocks the poll loop by setting [TokenRefresher.refreshState] to
     * [TokenRefreshState.Failed] from this (the test) thread. Returns the [LoginRouter] instance
     * that was resolved for this call.
     */
    private fun driveOneRefresh(
        tokenRefresher: TokenRefresher,
        response: Response,
        loginRouter: CountingLoginRouterProvider
    ): LoginRouter {
        val launchLoginLatch = CountDownLatch(1)
        val callsBefore = loginRouter.callCount.get()
        loginRouter.onGet = { launchLoginLatch.countDown() }

        val thread = Thread { tokenRefresher.refresh(response) }
        thread.start()

        assertTrue(
            "launchLogin() was not observed in time",
            launchLoginLatch.await(10, TimeUnit.SECONDS)
        )
        tokenRefresher.refreshState = TokenRefreshState.Failed

        thread.join(10_000)
        assertTrue("refresh() did not unblock in time", !thread.isAlive)

        assertEquals(callsBefore + 1, loginRouter.callCount.get())
        val resolvedRouter = loginRouter.lastResolved!!
        verify(exactly = 1) { resolvedRouter.loginIntent() }
        return resolvedRouter
    }

    /** A [Provider] that returns a fresh [LoginRouter] mock on every [get] and counts calls. */
    private class CountingLoginRouterProvider : Provider<LoginRouter> {
        val callCount = AtomicInteger(0)
        var lastResolved: LoginRouter? = null
        var onGet: (() -> Unit)? = null

        override fun get(): LoginRouter {
            callCount.incrementAndGet()
            val intent: Intent = mockk(relaxed = true)
            val router: LoginRouter = mockk(relaxed = true)
            every { router.loginIntent() } returns intent
            lastResolved = router
            onGet?.invoke()
            return router
        }
    }
}
