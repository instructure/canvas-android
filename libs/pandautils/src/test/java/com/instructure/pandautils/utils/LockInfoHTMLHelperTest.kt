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
 */
package com.instructure.pandautils.utils

import android.content.Context
import android.content.res.Resources
import com.instructure.canvasapi2.models.LockInfo
import com.instructure.canvasapi2.models.LockedModule
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.pandautils.R
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class LockInfoHTMLHelperTest {

    private val context: Context = mockk(relaxed = true)
    private val resources: Resources = mockk(relaxed = true)

    @Before
    fun setup() {
        every { context.resources } returns resources

        every { context.getString(R.string.mustComplete) } returns "You must first complete:"
        every { context.getString(R.string.lockedAssignmentNotModule) } returns "This assignment is locked."
        every { context.getString(R.string.unlockedAt) } returns "It will be unlocked at:"
        every { resources.getString(R.string.goToModules) } returns "Go To Modules"
        every { context.getString(EXPLANATION_RES, *anyVararg()) } returns "Locked module message"

        mockkObject(ApiPrefs)
        every { ApiPrefs.domain } returns "instructure.com"
        every { ApiPrefs.protocol } returns "https"

        mockkObject(DateHelper)
        every { DateHelper.getDateTimeString(any(), any()) } returns "Jan 1 at 12:00 AM"
    }

    @After
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `Includes locked module name and prerequisite list`() {
        val lockInfo = LockInfo(
            contextModule = LockedModule(contextId = 5L, name = "Module 1"),
            modulePrerequisiteNames = arrayListOf("Req 1", "Req 2")
        )

        val html = LockInfoHTMLHelper.getLockedInfoHTML(lockInfo, context, EXPLANATION_RES)

        assertTrue(html.contains("Locked module message"))
        assertTrue(html.contains("<li>Req 1</li>"))
        assertTrue(html.contains("<li>Req 2</li>"))
        verify { context.getString(EXPLANATION_RES, "<b>Module 1</b>") }
    }

    @Test
    fun `Omits prerequisite list when there are no prerequisites`() {
        val lockInfo = LockInfo(
            contextModule = LockedModule(contextId = 5L, name = "Module 1"),
            modulePrerequisiteNames = arrayListOf()
        )

        val html = LockInfoHTMLHelper.getLockedInfoHTML(lockInfo, context, EXPLANATION_RES)

        assertFalse(html.contains("You must first complete"))
    }

    @Test
    fun `Appends modules link with course id when addModulesLink is true`() {
        val lockInfo = LockInfo(contextModule = LockedModule(contextId = 5L, name = "Module 1"))

        val html = LockInfoHTMLHelper.getLockedInfoHTML(lockInfo, context, EXPLANATION_RES, addModulesLink = true)

        assertTrue(html.contains("https://instructure.com/courses/5/modules"))
        assertTrue(html.contains("Go To Modules"))
    }

    @Test
    fun `Omits modules link when addModulesLink is false`() {
        val lockInfo = LockInfo(contextModule = LockedModule(contextId = 5L, name = "Module 1"))

        val html = LockInfoHTMLHelper.getLockedInfoHTML(lockInfo, context, EXPLANATION_RES, addModulesLink = false)

        assertFalse(html.contains("courses/5/modules"))
    }

    @Test
    fun `Ignores unlock dates that are already in the past`() {
        val lockInfo = LockInfo(
            contextModule = LockedModule(contextId = 5L, name = "Module 1"),
            unlockAt = "2000-01-01T00:00:00Z"
        )

        val html = LockInfoHTMLHelper.getLockedInfoHTML(lockInfo, context, EXPLANATION_RES)

        assertTrue(html.contains("Locked module message"))
        assertFalse(html.contains("It will be unlocked at:"))
    }

    @Test
    fun `Shows unlock date message and drops the module message when unlock date is in the future`() {
        val lockInfo = LockInfo(
            contextModule = LockedModule(contextId = 5L, name = "Module 1"),
            unlockAt = "2099-01-01T00:00:00Z"
        )

        val html = LockInfoHTMLHelper.getLockedInfoHTML(lockInfo, context, EXPLANATION_RES)

        assertFalse(html.contains("Locked module message"))
        assertTrue(html.contains("It will be unlocked at:"))
        assertTrue(html.contains("Jan 1 at 12:00 AM"))
    }

    @Test
    fun `Shows locked-assignment message when a future unlock date has no context module`() {
        val lockInfo = LockInfo(contextModule = null, unlockAt = "2099-01-01T00:00:00Z")

        val html = LockInfoHTMLHelper.getLockedInfoHTML(lockInfo, context, EXPLANATION_RES)

        assertTrue(html.contains("This assignment is locked."))
        assertTrue(html.contains("It will be unlocked at:"))
    }

    private companion object {
        const val EXPLANATION_RES = R.string.lockedPageDesc
    }
}
