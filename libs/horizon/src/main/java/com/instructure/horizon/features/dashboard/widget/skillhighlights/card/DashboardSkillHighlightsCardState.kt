/*
 * Copyright (C) 2025 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.instructure.horizon.features.dashboard.widget.skillhighlights.card

import androidx.annotation.StringRes
import com.instructure.horizon.R

data class DashboardSkillHighlightsCardState(
    val skills: List<SkillHighlight> = emptyList()
)

data class SkillHighlight(
    val name: String,
    val proficiencyLevel: SkillHighlightProficiencyLevel
)

enum class SkillHighlightProficiencyLevel(
    @StringRes val skillProficiencyLevelRes: Int,
    val apiString: String,
    val levelOrder: Int
) {
    BEGINNER(R.string.dashboardSkillProficienyLevelBeginner, "beginner", 0),
    PROFICIENT(R.string.dashboardSkillProficienyLevelProficient, "proficient", 1),
    ADVANCED(R.string.dashboardSkillProficienyLevelAdvanced, "advanced",  2),
    EXPERT(R.string.dashboardSkillProficienyLevelExpert, "expert", 3);

    companion object {
        fun fromString(level: String?): SkillHighlightProficiencyLevel? {
            return SkillHighlightProficiencyLevel.entries.firstOrNull { it.apiString.equals(level, ignoreCase = true) }
        }
    }
}