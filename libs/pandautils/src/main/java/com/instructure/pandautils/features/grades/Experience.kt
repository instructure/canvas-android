/*
 * Copyright (C) 2026 - present Instructure, Inc.
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

package com.instructure.pandautils.features.grades

/**
 * Identifies which user-facing experience the grades feature is rendering inside.
 *
 * - [Academic] — the legacy Canvas experience used by Student/Parent/Teacher apps.
 * - [NGC] — the Next Generation Canvas experience embedded inside the Student app.
 *
 * The same [GradesViewModel] handles both. NGC adds extras (filter, group totals, sticky header,
 * etc.) on top of the Academic baseline. Propagated via [SavedStateHandle] under [EXPERIENCE_KEY].
 */
enum class Experience {
    Academic,
    NGC,
}

/** SavedStateHandle / Bundle key carrying an [Experience] name. */
const val EXPERIENCE_KEY = "experience"