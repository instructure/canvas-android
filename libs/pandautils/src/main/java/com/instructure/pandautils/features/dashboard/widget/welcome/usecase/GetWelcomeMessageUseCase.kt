/*
 * Copyright (C) 2025 - present Instructure, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.instructure.pandautils.features.dashboard.widget.welcome.usecase

import android.content.res.Resources
import com.instructure.pandautils.R
import com.instructure.pandautils.features.dashboard.widget.welcome.TimeOfDay
import com.instructure.pandautils.features.dashboard.widget.welcome.TimeOfDayCalculator
import javax.inject.Inject
import kotlin.random.Random

class GetWelcomeMessageUseCase @Inject constructor(
    private val resources: Resources,
    private val timeOfDayCalculator: TimeOfDayCalculator,
    private val random: Random
) {

    operator fun invoke(): String {
        val timeOfDay = timeOfDayCalculator.getTimeOfDay()

        val genericMessages = resources.getStringArray(R.array.welcomeMessagesGeneric)
        val timeSpecificMessages = when (timeOfDay) {
            TimeOfDay.MORNING -> resources.getStringArray(R.array.welcomeMessagesMorning)
            TimeOfDay.AFTERNOON -> resources.getStringArray(R.array.welcomeMessagesAfternoon)
            TimeOfDay.EVENING -> resources.getStringArray(R.array.welcomeMessagesEvening)
            TimeOfDay.NIGHT -> resources.getStringArray(R.array.welcomeMessagesNight)
        }

        val allMessages = genericMessages + timeSpecificMessages
        return allMessages[random.nextInt(allMessages.size)]
    }
}
