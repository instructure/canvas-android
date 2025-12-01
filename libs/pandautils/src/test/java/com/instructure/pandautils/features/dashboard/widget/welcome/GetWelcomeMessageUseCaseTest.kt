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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class GetWelcomeMessageUseCaseTest {

    private val resources: Resources = mockk()
    private val timeOfDayCalculator: TimeOfDayCalculator = mockk()
    private val random: Random = mockk()

    private val useCase = GetWelcomeMessageUseCase(resources, timeOfDayCalculator, random)

    @Test
    fun `invoke returns first generic message when random returns 0 and time is morning`() {
        val genericMessages = arrayOf("Generic message 1", "Generic message 2")
        val morningMessages = arrayOf("Morning message 1", "Morning message 2")

        every { timeOfDayCalculator.getTimeOfDay() } returns TimeOfDay.MORNING
        every { resources.getStringArray(R.array.welcomeMessagesGeneric) } returns genericMessages
        every { resources.getStringArray(R.array.welcomeMessagesMorning) } returns morningMessages
        every { random.nextInt(4) } returns 0

        val result = useCase()

        assertEquals("Generic message 1", result)
    }

    @Test
    fun `invoke returns first morning-specific message when random returns 2 and time is morning`() {
        val genericMessages = arrayOf("Generic message 1", "Generic message 2")
        val morningMessages = arrayOf("Morning message 1", "Morning message 2")

        every { timeOfDayCalculator.getTimeOfDay() } returns TimeOfDay.MORNING
        every { resources.getStringArray(R.array.welcomeMessagesGeneric) } returns genericMessages
        every { resources.getStringArray(R.array.welcomeMessagesMorning) } returns morningMessages
        every { random.nextInt(4) } returns 2

        val result = useCase()

        assertEquals("Morning message 1", result)
    }

    @Test
    fun `invoke returns afternoon-specific message when random returns 3 and time is afternoon`() {
        val genericMessages = arrayOf("Generic message 1", "Generic message 2")
        val afternoonMessages = arrayOf("Afternoon message 1", "Afternoon message 2")

        every { timeOfDayCalculator.getTimeOfDay() } returns TimeOfDay.AFTERNOON
        every { resources.getStringArray(R.array.welcomeMessagesGeneric) } returns genericMessages
        every { resources.getStringArray(R.array.welcomeMessagesAfternoon) } returns afternoonMessages
        every { random.nextInt(4) } returns 3

        val result = useCase()

        assertEquals("Afternoon message 2", result)
    }

    @Test
    fun `invoke returns evening-specific message when random returns 2 and time is evening`() {
        val genericMessages = arrayOf("Generic message 1", "Generic message 2")
        val eveningMessages = arrayOf("Evening message 1", "Evening message 2")

        every { timeOfDayCalculator.getTimeOfDay() } returns TimeOfDay.EVENING
        every { resources.getStringArray(R.array.welcomeMessagesGeneric) } returns genericMessages
        every { resources.getStringArray(R.array.welcomeMessagesEvening) } returns eveningMessages
        every { random.nextInt(4) } returns 2

        val result = useCase()

        assertEquals("Evening message 1", result)
    }

    @Test
    fun `invoke returns night-specific message when random returns 3 and time is night`() {
        val genericMessages = arrayOf("Generic message 1", "Generic message 2")
        val nightMessages = arrayOf("Night message 1", "Night message 2")

        every { timeOfDayCalculator.getTimeOfDay() } returns TimeOfDay.NIGHT
        every { resources.getStringArray(R.array.welcomeMessagesGeneric) } returns genericMessages
        every { resources.getStringArray(R.array.welcomeMessagesNight) } returns nightMessages
        every { random.nextInt(4) } returns 3

        val result = useCase()

        assertEquals("Night message 2", result)
    }
}