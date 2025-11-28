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
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.R
import com.instructure.pandautils.features.dashboard.widget.welcome.usecase.GetWelcomeGreetingUseCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GetWelcomeGreetingUseCaseTest {

    private val resources: Resources = mockk()
    private val timeOfDayCalculator: TimeOfDayCalculator = mockk()
    private val apiPrefs: ApiPrefs = mockk()

    private lateinit var useCase: GetWelcomeGreetingUseCase

    @Before
    fun setUp() {
        mockkObject(ApiPrefs)
        useCase = GetWelcomeGreetingUseCase(resources, timeOfDayCalculator, apiPrefs)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `invoke returns morning greeting with name when user has short name`() {
        val user = User(shortName = "Riley")
        every { apiPrefs.user } returns user
        every { timeOfDayCalculator.getTimeOfDay() } returns TimeOfDay.MORNING
        every { resources.getString(R.string.welcomeGreetingMorningWithName, "Riley") } returns "Good morning, Riley!"

        val result = useCase()

        assertEquals("Good morning, Riley!", result)
    }

    @Test
    fun `invoke returns afternoon greeting with name when user has short name`() {
        val user = User(shortName = "Riley")
        every { apiPrefs.user } returns user
        every { timeOfDayCalculator.getTimeOfDay() } returns TimeOfDay.AFTERNOON
        every { resources.getString(R.string.welcomeGreetingAfternoonWithName, "Riley") } returns "Good afternoon, Riley!"

        val result = useCase()

        assertEquals("Good afternoon, Riley!", result)
    }

    @Test
    fun `invoke returns evening greeting with name when user has short name`() {
        val user = User(shortName = "Riley")
        every { apiPrefs.user } returns user
        every { timeOfDayCalculator.getTimeOfDay() } returns TimeOfDay.EVENING
        every { resources.getString(R.string.welcomeGreetingEveningWithName, "Riley") } returns "Good evening, Riley!"

        val result = useCase()

        assertEquals("Good evening, Riley!", result)
    }

    @Test
    fun `invoke returns night greeting with name when user has short name`() {
        val user = User(shortName = "Riley")
        every { apiPrefs.user } returns user
        every { timeOfDayCalculator.getTimeOfDay() } returns TimeOfDay.NIGHT
        every { resources.getString(R.string.welcomeGreetingNightWithName, "Riley") } returns "Good night, Riley!"

        val result = useCase()

        assertEquals("Good night, Riley!", result)
    }

    @Test
    fun `invoke returns morning greeting without name when user has null short name`() {
        val user = User(shortName = null)
        every { apiPrefs.user } returns user
        every { timeOfDayCalculator.getTimeOfDay() } returns TimeOfDay.MORNING
        every { resources.getString(R.string.welcomeGreetingMorning) } returns "Good morning!"

        val result = useCase()

        assertEquals("Good morning!", result)
    }

    @Test
    fun `invoke returns morning greeting without name when user has blank short name`() {
        val user = User(shortName = "  ")
        every { apiPrefs.user } returns user
        every { timeOfDayCalculator.getTimeOfDay() } returns TimeOfDay.MORNING
        every { resources.getString(R.string.welcomeGreetingMorning) } returns "Good morning!"

        val result = useCase()

        assertEquals("Good morning!", result)
    }

    @Test
    fun `invoke returns morning greeting without name when user is null`() {
        every { apiPrefs.user } returns null
        every { timeOfDayCalculator.getTimeOfDay() } returns TimeOfDay.MORNING
        every { resources.getString(R.string.welcomeGreetingMorning) } returns "Good morning!"

        val result = useCase()

        assertEquals("Good morning!", result)
    }

    @Test
    fun `invoke returns afternoon greeting without name when user is null`() {
        every { apiPrefs.user } returns null
        every { timeOfDayCalculator.getTimeOfDay() } returns TimeOfDay.AFTERNOON
        every { resources.getString(R.string.welcomeGreetingAfternoon) } returns "Good afternoon!"

        val result = useCase()

        assertEquals("Good afternoon!", result)
    }

    @Test
    fun `invoke returns evening greeting without name when user is null`() {
        every { apiPrefs.user } returns null
        every { timeOfDayCalculator.getTimeOfDay() } returns TimeOfDay.EVENING
        every { resources.getString(R.string.welcomeGreetingEvening) } returns "Good evening!"

        val result = useCase()

        assertEquals("Good evening!", result)
    }

    @Test
    fun `invoke returns night greeting without name when user is null`() {
        every { apiPrefs.user } returns null
        every { timeOfDayCalculator.getTimeOfDay() } returns TimeOfDay.NIGHT
        every { resources.getString(R.string.welcomeGreetingNight) } returns "Good night!"

        val result = useCase()

        assertEquals("Good night!", result)
    }
}