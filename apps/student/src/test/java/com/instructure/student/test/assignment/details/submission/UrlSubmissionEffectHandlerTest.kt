/*
 * Copyright (C) 2019 - present Instructure, Inc.
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
package com.instructure.student.test.assignment.details.submission

import com.instructure.student.mobius.assignmentDetails.submission.url.UrlSubmissionEffectHandler
import com.instructure.student.mobius.assignmentDetails.submission.url.UrlSubmissionEvent
import com.instructure.student.mobius.assignmentDetails.submission.url.ui.UrlSubmissionView
import com.spotify.mobius.functions.Consumer
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.test.setMain
import org.junit.Assert
import org.junit.Before
import java.util.concurrent.Executors
import kotlin.reflect.KClass

class UrlSubmissionEffectHandlerTest : Assert() {

    private val view: UrlSubmissionView = mockk(relaxed = true)
    private val eventConsumer: Consumer<UrlSubmissionEvent> = mockk(relaxed = true)
    private val effectHandler = UrlSubmissionEffectHandler()
    private val connection = effectHandler.connect(eventConsumer)

    @Before
    fun setup() {
        effectHandler.view = view
        Dispatchers.setMain(Executors.newSingleThreadExecutor().asCoroutineDispatcher())
    }
}