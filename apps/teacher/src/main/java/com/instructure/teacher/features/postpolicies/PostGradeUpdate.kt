/*
 * Copyright (C) 2019 - present Instructure, Inc.
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
package com.instructure.teacher.features.postpolicies

import com.instructure.teacher.mobius.common.ui.UpdateInit
import com.spotify.mobius.First
import com.spotify.mobius.Next

class PostGradeUpdate : UpdateInit<PostGradeModel, PostGradeEvent, PostGradeEffect>() {
    override fun performInit(model: PostGradeModel): First<PostGradeModel, PostGradeEffect> {
        return First.first(model.copy(isLoading = true), setOf(PostGradeEffect.LoadData(model.assignment)))
    }

    override fun update(model: PostGradeModel, event: PostGradeEvent): Next<PostGradeModel, PostGradeEffect> {
        return when (event) {
            PostGradeEvent.GradesPosted -> Next.dispatch(setOf<PostGradeEffect>(PostGradeEffect.ShowGradesPosted(model.isHidingGrades)))
            PostGradeEvent.PostGradesClicked -> {
                Next.next(
                    model.copy(isProcessing = true), setOf(
                        if (model.isHidingGrades) {
                            PostGradeEffect.HideGrades(model.assignment.id, getSelectedSectionIds(model))
                        } else {
                            PostGradeEffect.PostGrades(model.assignment.id, getSelectedSectionIds(model), model.postGradedOnly)
                        }
                    )
                )
            }
            PostGradeEvent.SpecificSectionsToggled -> Next.next(model.copy(specificSectionsVisible = !model.specificSectionsVisible))
            is PostGradeEvent.SectionToggled -> Next.next(model.copy(sections = model.sections.map {
                if (it.section.id == event.sectionId) it.copy(selected = !it.selected) else it
            }))
            is PostGradeEvent.GradedOnlySelected -> Next.next(model.copy(postGradedOnly = event.gradedOnly))
            is PostGradeEvent.DataLoaded -> Next.next(
                model.copy(
                    isLoading = false,
                    sections = event.sections.map { PostSection(it, false) },
                    submissions = event.submissions
                )
            )
        }
    }

    private fun getSelectedSectionIds(model: PostGradeModel) = model.sections.filter { it.selected }.map { it.section.id.toString() }
}