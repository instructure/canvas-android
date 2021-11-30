/*
 * Copyright (C) 2020 - present Instructure, Inc.
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
package com.instructure.student.holders

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.instructure.canvasapi2.models.Conference
import com.instructure.pandautils.utils.onClick
import com.instructure.pandautils.utils.setVisible
import com.instructure.student.R
import com.instructure.student.interfaces.CourseAdapterToFragmentCallback
import com.instructure.student.util.StudentPrefs
import kotlinx.android.synthetic.main.viewholder_dashboard_conference_card.view.*
import kotlinx.coroutines.*

class DashboardConferenceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    var isJoining = false
    var launchJob: Job? = null

    fun bind(conference: Conference, callback: CourseAdapterToFragmentCallback) = with(itemView) {
        launchJob?.cancel()

        // Set course/group name, fall back to conference title
        subtitle.text = conference.canvasContext.name ?: conference.title

        updateJoiningState(conference, callback)

        dismissButton.onClick {
            // Add conference to blacklist
            val newBlacklist = StudentPrefs.conferenceDashboardBlacklist + conference.id.toString()
            StudentPrefs.conferenceDashboardBlacklist = newBlacklist

            // Invoke adapter callback to remove this conference from the list
            callback.onDismissConference(conference)
        }
    }

    private fun updateJoiningState(conference: Conference, callback: CourseAdapterToFragmentCallback): Unit = with(itemView) {
        progressBar.setVisible(isJoining)
        dismissButton.setVisible(!isJoining)

        if (isJoining) {
            setOnClickListener(null)
        } else {
            onClick {
                launchJob = GlobalScope.launch(Dispatchers.Main) {
                    isJoining = true
                    updateJoiningState(conference, callback)
                    callback.onConferenceSelected(conference)
                    delay(3000)
                    isJoining = false
                    updateJoiningState(conference, callback)
                }
            }
        }
    }

    companion object {
        const val HOLDER_RES_ID: Int = R.layout.viewholder_dashboard_conference_card
    }
}
