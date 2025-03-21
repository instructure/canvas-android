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
 */
package com.instructure.pandautils.analytics.pageview

import android.view.ViewTreeObserver
import androidx.fragment.app.Fragment
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.Logger
import com.instructure.canvasapi2.utils.weave.weave
import com.instructure.pandautils.analytics.pageview.db.PageViewDao
import com.instructure.pandautils.analytics.pageview.db.PageViewEvent
import java.lang.ref.WeakReference
import java.util.UUID
import java.util.concurrent.TimeUnit

class PageViewUtils(
    private val pageViewDao: PageViewDao,
) {
    private val contextRegex = """/(courses|groups)/([^/]+)""".toRegex()

    private val session = PageViewSession()

    @Suppress("MemberVisibilityCanBePrivate")
    fun startEvent(eventName: String, url: String): PageViewEvent? {
        if (ApiPrefs.getValidToken().isBlank()) return null
        val loginId = ApiPrefs.user?.id ?: return null
        val pandataInfo = ApiPrefs.pandataInfo ?: return null
        val (userId, realUserId) = if (ApiPrefs.isMasquerading) ApiPrefs.masqueradeId to loginId else loginId to null
        val (contextType, contextId) = url.contextInfo
        val event = PageViewEvent(
            eventName = eventName,
            sessionId = session.id,
            postUrl = pandataInfo.postUrl,
            url = url,
            contextType = contextType,
            contextId = contextId,
            signedProperties = pandataInfo.signedProperties,
            domain = ApiPrefs.domain,
            userId = userId,
            realUserId = realUserId
        )
        Logger.d("PageView: Event STARTED $url ($eventName)")
        weave { pageViewDao.insert(event) }
        return event
    }

    fun stopEvent(event: PageViewEvent?) {
        if (event == null || event.eventDuration > 0) return
        val updated =
            event.copy(eventDuration = (System.currentTimeMillis() - event.timestamp.time) / 1000.0)
        Logger.d("PageView: Event STOPPED ${updated.url} (${updated.eventName}) - ${updated.eventDuration} seconds")
        weave {
            inBackground {
                if (updated.eventDuration < MIN_INTERACTION_SECONDS) {
                    pageViewDao.delete(event)
                } else {
                    pageViewDao.update(updated)
                }
            }
        }
    }

    fun saveSingleEvent(eventName: String, url: String) {
        startEvent(eventName, url)?.let {
            weave { pageViewDao.update(it) }
            Logger.d("PageView: Single event SAVED ${it.url} (${it.eventName})")
        }
    }

    private val String.contextInfo: Pair<String?, String?>
        get() {
            val (rawType, id) = contextRegex.find(this)?.destructured ?: return null to null
            return when (rawType) {
                "courses" -> "Course"
                "groups" -> "Group"
                else -> null
            } to id
        }

    companion object {
        private const val MIN_INTERACTION_SECONDS = 1.0
    }
}

class PageViewVisibilityTracker() {

    private var isResumed = true
    private var isUserHint = true
    private var isShowing = true
    private val customConditions = mutableMapOf<String, Boolean>()

    fun addCustomConditions(conditions: List<String>) {
        conditions.forEach { customConditions[it] = false }
    }

    fun isVisible(fragment: Fragment) = isResumed && isUserHint && isShowing && customConditions.values.all { it == true } && fragment.isVisible

    fun trackResume(resumed: Boolean, fragment: Fragment): Boolean {
        isResumed = resumed
        return isVisible(fragment)
    }

    fun trackUserHint(userHint: Boolean, fragment: Fragment): Boolean {
        isUserHint = userHint
        return isVisible(fragment)
    }

    fun trackHidden(hidden: Boolean, fragment: Fragment): Boolean {
        isShowing = !hidden
        return isVisible(fragment)
    }

    fun trackCustom(name: String, value: Boolean, fragment: Fragment): Boolean {
        customConditions[name] = value
        return isVisible(fragment)
    }

}

interface PageViewWindowFocus {
    fun onPageViewWindowFocusChanged(hasFocus: Boolean)
}

class PageViewWindowFocusListener(focusInterface: PageViewWindowFocus) : ViewTreeObserver.OnWindowFocusChangeListener {

    private val ref: WeakReference<PageViewWindowFocus> = WeakReference(focusInterface)

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        ref.get()?.onPageViewWindowFocusChanged(hasFocus)
    }

}

class PageViewSession {

    private var lastCheck: Long = System.currentTimeMillis()

    private var _id: String? = null

    val id: String
        get() {
            val now = System.currentTimeMillis()
            if (_id == null || now > lastCheck + SESSION_TIMEOUT) _id = UUID.randomUUID().toString()
            lastCheck = now
            return _id!!
        }

    fun clear() {
        _id = null
    }

    companion object {
        val SESSION_TIMEOUT = TimeUnit.MINUTES.toMillis(30)
    }

}
