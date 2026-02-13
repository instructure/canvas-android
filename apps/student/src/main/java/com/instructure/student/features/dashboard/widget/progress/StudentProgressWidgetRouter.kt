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

package com.instructure.student.features.dashboard.widget.progress

import android.content.Intent
import androidx.fragment.app.FragmentActivity
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.pandautils.features.dashboard.widget.progress.ProgressWidgetRouter
import com.instructure.pandautils.features.offline.sync.progress.SyncProgressFragment
import com.instructure.pandautils.features.shareextension.WORKER_ID
import com.instructure.student.features.files.list.FileListFragment
import com.instructure.student.features.shareextension.StudentShareExtensionActivity
import com.instructure.student.mobius.assignmentDetails.submissionDetails.ui.SubmissionDetailsRepositoryFragment
import com.instructure.student.router.RouteMatcher
import java.util.UUID

class StudentProgressWidgetRouter : ProgressWidgetRouter {

    override fun openProgressDialog(activity: FragmentActivity, workerId: UUID) {
        val intent = Intent(activity, StudentShareExtensionActivity::class.java)
        intent.putExtra(WORKER_ID, workerId)
        activity.startActivity(intent)
    }

    override fun navigateToSubmissionDetails(
        activity: FragmentActivity,
        canvasContext: CanvasContext,
        assignmentId: Long,
        attemptId: Long
    ) {
        RouteMatcher.route(
            activity,
            SubmissionDetailsRepositoryFragment.makeRoute(
                canvasContext,
                assignmentId,
                initialSelectedSubmissionAttempt = attemptId
            )
        )
    }

    override fun navigateToMyFiles(
        activity: FragmentActivity,
        canvasContext: CanvasContext,
        folderId: Long
    ) {
        RouteMatcher.route(
            activity,
            FileListFragment.makeRoute(canvasContext, folderId)
        )
    }

    override fun openSyncProgress(activity: FragmentActivity) {
        RouteMatcher.route(
            activity,
            SyncProgressFragment.makeRoute()
        )
    }
}
