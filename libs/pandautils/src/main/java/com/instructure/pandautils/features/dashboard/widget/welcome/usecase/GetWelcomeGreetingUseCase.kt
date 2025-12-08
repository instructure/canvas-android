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
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.R
import com.instructure.pandautils.features.dashboard.widget.welcome.TimeOfDay
import com.instructure.pandautils.features.dashboard.widget.welcome.TimeOfDayCalculator
import javax.inject.Inject

class GetWelcomeGreetingUseCase @Inject constructor(
    private val resources: Resources,
    private val timeOfDayCalculator: TimeOfDayCalculator,
    private val apiPrefs: ApiPrefs
) {

    operator fun invoke(): String {
        val timeOfDay = timeOfDayCalculator.getTimeOfDay()
        val firstName = apiPrefs.user?.shortName

        return if (!firstName.isNullOrBlank()) {
            when (timeOfDay) {
                TimeOfDay.MORNING -> resources.getString(R.string.welcomeGreetingMorningWithName, firstName)
                TimeOfDay.AFTERNOON -> resources.getString(R.string.welcomeGreetingAfternoonWithName, firstName)
                TimeOfDay.EVENING -> resources.getString(R.string.welcomeGreetingEveningWithName, firstName)
                TimeOfDay.NIGHT -> resources.getString(R.string.welcomeGreetingNightWithName, firstName)
            }
        } else {
            when (timeOfDay) {
                TimeOfDay.MORNING -> resources.getString(R.string.welcomeGreetingMorning)
                TimeOfDay.AFTERNOON -> resources.getString(R.string.welcomeGreetingAfternoon)
                TimeOfDay.EVENING -> resources.getString(R.string.welcomeGreetingEvening)
                TimeOfDay.NIGHT -> resources.getString(R.string.welcomeGreetingNight)
            }
        }
    }
}
