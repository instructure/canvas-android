/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
package com.instructure.pandautils.utils

import android.util.Log
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.pageview.PageView
import com.instructure.canvasapi2.utils.pageview.PageViewEvent
import com.instructure.canvasapi2.utils.pageview.PageViewUrl
import com.instructure.canvasapi2.utils.pageview.PageViewUrlParam
import com.instructure.canvasapi2.utils.pageview.PageViewUtils

class PageViewAnnotationProcessor(private val enclosingClass: Class<*>, private val enclosingObject: Any) {

    fun startEvent() {
        if (pageViewEvent == null && hasPageViewAnnotation) {
            pageViewEvent = PageViewUtils.startEvent(pageViewEventName, getPageViewUrl())
            Log.d("asdasd", "startEvent: $pageViewEventName ${getPageViewUrl()}")
        }
    }

    fun stopEvent() {
        if (hasPageViewAnnotation) {
            PageViewUtils.stopEvent(pageViewEvent)
            pageViewEvent = null
            Log.d("asdasd", "stopEvent: $pageViewEventName ${getPageViewUrl()}")
        }
    }

    private val hasPageViewAnnotation: Boolean = enclosingClass.isAnnotationPresent(PageView::class.java)

    private var pageViewEvent: PageViewEvent? = null

    private val pageViewEventName: String
        get() {
            return if (enclosingClass.getAnnotation(PageView::class.java)?.name.isNullOrBlank()) {
                enclosingClass.simpleName
            } else {
                enclosingClass.getAnnotation(PageView::class.java)?.name.orEmpty()
            }
        }

    private fun getPageViewUrl(): String {
        enclosingClass.declaredMethods.find { it.isAnnotationPresent(PageViewUrl::class.java) }?.let {
            return it.invoke(enclosingObject, null) as String
        }

        var rawUrl = enclosingClass.getAnnotation(PageView::class.java)?.url.orEmpty()

        if (rawUrl.isEmpty()) return ApiPrefs.fullDomain

        val regex = Regex("\\{([^}]+)\\}")
        val urlParams =  regex.findAll(rawUrl).map { matchResult ->
            matchResult.groupValues[1]
        }.toSet()

        val paramFields = enclosingClass.fields.filter { it.isAnnotationPresent(PageViewUrlParam::class.java) }
            .associate {
                val key = it.getAnnotation(PageViewUrlParam::class.java).name
                val valueObject = it.get(enclosingObject)
                val value = if (valueObject is CanvasContext) valueObject.toAPIString().drop(1) else valueObject.toString()
                key to value
            }

        val paramMethods = enclosingClass.methods.filter { it.isAnnotationPresent(PageViewUrlParam::class.java) }
            .associate {
                val key = it.getAnnotation(PageViewUrlParam::class.java).name
                val valueObject = it.invoke(enclosingObject)
                val value = if (valueObject is CanvasContext) valueObject.toAPIString().drop(1) else valueObject.toString()
                key to value
            }

        val params = paramFields + paramMethods

        urlParams.forEach { param ->
            val value = params[param]
            rawUrl = rawUrl.replace("{$param}", value.orEmpty())
        }

        return "${ApiPrefs.fullDomain}/$rawUrl"
    }
}