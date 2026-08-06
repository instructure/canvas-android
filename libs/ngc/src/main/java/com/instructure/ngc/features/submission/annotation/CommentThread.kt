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
package com.instructure.ngc.features.submission.annotation

import com.instructure.canvasapi2.models.canvadocs.CanvaDocAnnotation

/**
 * Everything the Compose-native comment-thread screen needs to load and mutate a single reply thread,
 * built by [PdfAnnotationSubmissionView.showAnnotationComments] from its `docSession`/`apiValues` at the
 * moment the user re-taps the comments button on an annotation that already has replies. Deliberately
 * carries only primitives (not the [com.instructure.canvasapi2.models.DocSession]/
 * [com.instructure.canvasapi2.models.ApiValues] objects themselves, nor the reply list) so it can be
 * threaded through plain Compose nav args instead of a shared holder/ViewModel — the destination screen
 * re-loads the current reply list itself via `GetAnnotationCommentRepliesUseCase`.
 */
data class CommentThreadRequest(
    val headAnnotationId: String,
    val sessionId: String,
    val canvaDocsDomain: String,
    val documentId: String,
    val canWrite: Boolean,
    val canManage: Boolean,
    val canComment: Boolean,
    /**
     * The canvadocs-session-scoped [com.instructure.canvasapi2.models.AnnotationMetadata.userId] — the
     * canonical "own reply" key also used by classic (`AnnotationCommentListRecyclerAdapter`) and the base
     * (`PdfSubmissionView.loadAnnotations`), rather than [com.instructure.canvasapi2.utils.ApiPrefs.user]'s
     * local Canvas id, which can diverge from it (global/cross-shard ids, act-as).
     */
    val currentUserId: String,
)

/**
 * Carries the authoritative reply list for [headAnnotationId] back from the comment-thread screen to
 * [PdfAnnotationSubmissionView] so it can patch its `commentRepliesHashMap`/on-page indicator without a
 * blind second `loadAnnotations()` call (see [PdfAnnotationSubmissionView.onCommentThreadResult] for why
 * a second full reload is unsafe).
 */
data class CommentThreadResult(
    val headAnnotationId: String,
    val replies: List<CanvaDocAnnotation>,
)
