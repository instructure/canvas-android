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
package com.instructure.student.mobius.assignmentDetails.submission.file

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.instructure.canvasapi2.models.postmodels.FileSubmitObject
import com.instructure.canvasapi2.utils.ProgressEvent
import com.instructure.canvasapi2.utils.exhaustive
import com.instructure.pandautils.utils.Const
import com.instructure.student.db.Db
import com.instructure.student.db.getInstance
import com.instructure.student.mobius.assignmentDetails.submission.file.ui.UploadStatusSubmissionView
import com.instructure.student.mobius.common.ui.EffectHandler
import com.instructure.student.mobius.common.ui.SubmissionService
import com.spotify.mobius.Connection
import com.spotify.mobius.functions.Consumer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class UploadStatusSubmissionEffectHandler(val context: Context, val submissionId: Long) : EffectHandler<UploadStatusSubmissionView, UploadStatusSubmissionEvent, UploadStatusSubmissionEffect>() {

    internal val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (context == null || intent?.hasExtra(Const.SUBMISSION) == false) {
                return // stop early if we don't have a context or if there's no submission id
            }

            val submissionId = intent!!.extras!!.getLong(Const.SUBMISSION)

            if (submissionId != this@UploadStatusSubmissionEffectHandler.submissionId) {
                return // Since there could be multiple submissions at the same time, we only care about events for our submission id
            }
            launch {
                val data = loadPersistedData(submissionId, context)
                consumer.accept(
                    UploadStatusSubmissionEvent.OnFilesRefreshed(
                        data.first,
                        submissionId,
                        data.second
                    )
                )
            }
        }
    }

    @Suppress("unused", "UNUSED_PARAMETER")
    @Subscribe
    fun onUploadProgress(event: ProgressEvent) {
        if (event.submissionId != submissionId) {
            return // Since there could be multiple submissions at the same time, we only care about events for our submission id
        }
        consumer.accept(
            UploadStatusSubmissionEvent.OnUploadProgressChanged(
                event.fileIndex,
                event.submissionId,
                event.uploaded
            )
        )
    }

    override fun connect(output: Consumer<UploadStatusSubmissionEvent>): Connection<UploadStatusSubmissionEffect> {
        EventBus.getDefault().register(this)
        context.registerReceiver(receiver, IntentFilter(SubmissionService.FILE_SUBMISSION_FINISHED))
        return super.connect(output)
    }

    override fun dispose() {
        EventBus.getDefault().unregister(this)
        context.unregisterReceiver(receiver)
        super.dispose()
    }

    override fun accept(effect: UploadStatusSubmissionEffect) {
        when (effect) {
            is UploadStatusSubmissionEffect.LoadPersistedFiles -> {
                launch(Dispatchers.Main) {
                    val data = loadPersistedData(effect.submissionId, context)
                    consumer.accept(
                        UploadStatusSubmissionEvent.OnPersistedSubmissionLoaded(
                            data.first,
                            data.second
                        )
                    )
                }
            }
        }.exhaustive
    }

    private fun loadPersistedData(
        submissionId: Long,
        context: Context
    ): Pair<Boolean, List<FileSubmitObject>> {
        val db = Db.getInstance(context)
        val error = db.submissionQueries.getSubmissionById(submissionId).executeAsOne().errorFlag
        val files = db.fileSubmissionQueries.getFilesForSubmissionId(submissionId).executeAsList()
            .map { file ->
                FileSubmitObject(
                    file.name!!,
                    file.size!!,
                    file.contentType!!,
                    file.fullPath!!,
                    file.error
                )
            }

        return Pair(error, files)
    }
}
