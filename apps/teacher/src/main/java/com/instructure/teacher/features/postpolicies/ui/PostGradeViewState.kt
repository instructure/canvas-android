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
package com.instructure.teacher.features.postpolicies.ui

import com.instructure.teacher.features.postpolicies.PostSection

sealed class PostGradeViewState(val visibilities: PostGradeVisibilities) {
    data class Loading(val courseColor: Int) : PostGradeViewState(PostGradeVisibilities(loading = true))

    data class LoadedViewState(
        val courseColor: Int,
        val statusText: String,
        val gradedOnlyText: String?,
        val specificSectionsVisible: Boolean,
        val postText: String?,
        val postProcessing: Boolean,
        val sections: List<PostSection>
    ) : PostGradeViewState(
        PostGradeVisibilities(
            policyView = true,
            postProcessing = postProcessing,
            gradedOnlySelector = !gradedOnlyText.isNullOrEmpty(),
            sectionRecycler = specificSectionsVisible
        )
    )

    data class EmptyViewState(
        val imageResId: Int,
        val emptyTitle: String,
        val emptyMessage: String
    ) : PostGradeViewState(PostGradeVisibilities(emptyView = true))
}

data class PostGradeVisibilities(
    val policyView: Boolean = false,
    val postProcessing: Boolean = false,
    val gradedOnlySelector: Boolean = false,
    val sectionRecycler: Boolean = false,
    val emptyView: Boolean = false,
    val loading: Boolean = false
)