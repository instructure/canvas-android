/*
 * Copyright (C) 2017 - present Instructure, Inc.
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
package com.instructure.teacher.view.edit_rubric

import android.view.View

/**
 * An event posted whenever a user long-presses a [CriterionRatingButton] which should trigger
 * RoobricTooltipView to display a tooltip above the provided view using the provided description.
 * If the anchor is null, any displayed tooltip will be dismissed.
 */
class ShowRatingDescriptionEvent(val anchor: View?, val assigneeId: Long?, val description: String, val isTutorialTip: Boolean = false)

/**
 * An event posted when a new rating has been selected for any criteria. This will generally happen
 * when a [CriterionRatingButton] has been clicked. A null value for [points] indicates the removal
 * of any point rating for the specified criterion.
 */
class RatingSelectedEvent(val points: Double?, val criterionId: String, val studentId: Long)

/**
 * RubricCommentEditedEvent
 */
class RubricCommentEditedEvent(val criterionId: String, val text: String?, val studentId: Long)
