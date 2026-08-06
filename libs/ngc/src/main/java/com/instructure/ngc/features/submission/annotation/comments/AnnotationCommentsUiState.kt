/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
 */
package com.instructure.ngc.features.submission.annotation.comments

/** A single `COMMENT_REPLY` rendered by [AnnotationCommentsBottomSheet]. */
data class AnnotationCommentUiState(
    val id: String,
    val authorName: String,
    val timestampText: String,
    val body: String,
    /** [com.instructure.canvasapi2.models.DocSession.annotationMetadata] `canWrite` AND this reply belongs to the current user. */
    val canEdit: Boolean,
    /** Session `canManage`, OR (`canWrite` AND this reply belongs to the current user) — mirrors classic `AnnotationCommentListRecyclerAdapter`. */
    val canDelete: Boolean,
)

/**
 * UI state for [AnnotationCommentsBottomSheet]. One-shot events ([errorMessage], [exitEvent]) follow the
 * nullable-field + consume-lambda idiom (see Horizon's `AddSubmissionUiState`) rather than a
 * `Channel`/`SharedFlow`.
 */
data class AnnotationCommentsUiState(
    val loading: Boolean = true,
    val loadError: String? = null,
    val onRetry: () -> Unit = {},
    val replies: List<AnnotationCommentUiState> = emptyList(),
    /** Whether the reply composer is shown at all — mirrors classic's `canRead() != false && canComment`. */
    val canComment: Boolean = true,
    val inputText: String = "",
    val onInputTextChanged: (String) -> Unit = {},
    /** Non-null while editing an existing reply (its id); the composer then edits instead of adding. */
    val editingReplyId: String? = null,
    val sending: Boolean = false,
    val onSendClick: () -> Unit = {},
    val onEditClick: (String) -> Unit = {},
    val onCancelEditClick: () -> Unit = {},
    val onDeleteClick: (String) -> Unit = {},
    /** One-shot inline error (add/edit/delete failure); consumed via [onErrorMessageConsumed]. */
    val errorMessage: String? = null,
    val onErrorMessageConsumed: () -> Unit = {},
    /**
     * One-shot "leave this screen now" signal — fired after the last remaining reply is deleted
     * (mirrors classic `AnnotationCommentListFragment.headAnnotationDeleted`'s auto-`onBackPressed`).
     * The screen reacts by calling its `onNavigateBack` callback with the current (now empty) result
     * and then calling [onExitEventConsumed].
     */
    val exitEvent: Boolean = false,
    val onExitEventConsumed: () -> Unit = {},
)
