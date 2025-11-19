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
package com.instructure.pandautils.features.dashboard.widget.welcome

import android.content.res.Resources
import com.instructure.pandautils.R
import com.instructure.pandautils.features.dashboard.widget.welcome.usecase.GetWelcomeMessageUseCase
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertTrue
import org.junit.Test

class GetWelcomeMessageUseCaseTest {

    private val resources: Resources = mockk()
    private val timeOfDayCalculator: TimeOfDayCalculator = mockk()

    private val useCase = GetWelcomeMessageUseCase(resources, timeOfDayCalculator)

    @Test
    fun `invoke returns message from generic or morning pool when time is morning`() {
        val genericMessages = arrayOf("Generic message 1", "Generic message 2")
        val morningMessages = arrayOf("Morning message 1", "Morning message 2")

        every { timeOfDayCalculator.getTimeOfDay() } returns TimeOfDay.MORNING
        every { resources.getStringArray(R.array.welcomeMessagesGeneric) } returns genericMessages
        every { resources.getStringArray(R.array.welcomeMessagesMorning) } returns morningMessages

        val result = useCase()

        val allMessages = genericMessages + morningMessages
        assertTrue(allMessages.contains(result))
    }

    @Test
    fun `invoke returns message from generic or afternoon pool when time is afternoon`() {
        val genericMessages = arrayOf("Generic message 1", "Generic message 2")
        val afternoonMessages = arrayOf("Afternoon message 1", "Afternoon message 2")

        every { timeOfDayCalculator.getTimeOfDay() } returns TimeOfDay.AFTERNOON
        every { resources.getStringArray(R.array.welcomeMessagesGeneric) } returns genericMessages
        every { resources.getStringArray(R.array.welcomeMessagesAfternoon) } returns afternoonMessages

        val result = useCase()

        val allMessages = genericMessages + afternoonMessages
        assertTrue(allMessages.contains(result))
    }

    @Test
    fun `invoke returns message from generic or evening pool when time is evening`() {
        val genericMessages = arrayOf("Generic message 1", "Generic message 2")
        val eveningMessages = arrayOf("Evening message 1", "Evening message 2")

        every { timeOfDayCalculator.getTimeOfDay() } returns TimeOfDay.EVENING
        every { resources.getStringArray(R.array.welcomeMessagesGeneric) } returns genericMessages
        every { resources.getStringArray(R.array.welcomeMessagesEvening) } returns eveningMessages

        val result = useCase()

        val allMessages = genericMessages + eveningMessages
        assertTrue(allMessages.contains(result))
    }

    @Test
    fun `invoke returns message from generic or night pool when time is night`() {
        val genericMessages = arrayOf("Generic message 1", "Generic message 2")
        val nightMessages = arrayOf("Night message 1", "Night message 2")

        every { timeOfDayCalculator.getTimeOfDay() } returns TimeOfDay.NIGHT
        every { resources.getStringArray(R.array.welcomeMessagesGeneric) } returns genericMessages
        every { resources.getStringArray(R.array.welcomeMessagesNight) } returns nightMessages

        val result = useCase()

        val allMessages = genericMessages + nightMessages
        assertTrue(allMessages.contains(result))
    }

    @Test
    fun `invoke returns different messages on multiple calls`() {
        val genericMessages = Array(20) { "Generic message $it" }
        val morningMessages = Array(10) { "Morning message $it" }

        every { timeOfDayCalculator.getTimeOfDay() } returns TimeOfDay.MORNING
        every { resources.getStringArray(R.array.welcomeMessagesGeneric) } returns genericMessages
        every { resources.getStringArray(R.array.welcomeMessagesMorning) } returns morningMessages

        val results = List(30) { useCase() }
        val uniqueResults = results.toSet()

        // With random selection from 30 messages and 30 calls, we should see multiple unique messages
        assertTrue(uniqueResults.size > 1)
    }
}