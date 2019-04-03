/*
 * Copyright (C) 2016 - present  Instructure, Inc.
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
package com.instructure.parentapp.presenters

import com.instructure.canvasapi2.managers.EnrollmentManager
import com.instructure.canvasapi2.models.Enrollment
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.isValid
import com.instructure.canvasapi2.utils.weave.WeaveJob
import com.instructure.canvasapi2.utils.weave.awaitApi
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryWeave
import com.instructure.parentapp.viewinterface.SettingsView
import instructure.androidblueprint.SyncPresenter

@Suppress("EXPERIMENTAL_FEATURE_WARNING")
class SettingsPresenter : SyncPresenter<User, SettingsView>(User::class.java) {

    private var settingsJob: WeaveJob? = null
    private var students = listOf<User>()

    override fun loadData(forceNetwork: Boolean) {
        if (students.isEmpty()) getStudents()
    }

    @Suppress("ConvertCallChainIntoSequence")
    private fun getStudents() {
        viewCallback?.let { settingsView ->
            settingsView.onRefreshStarted()
            settingsJob = tryWeave {
                students = awaitApi<List<Enrollment>> {
                    EnrollmentManager.getObserveeEnrollments(true, it)
                }.mapNotNull { it.observedUser }
                        .filter { it.name.isValid() }
                        .distinct()
                data.addAll(students)
                viewCallback?.hasStudent(!isEmpty)
                settingsView.onRefreshFinished()
                settingsView.checkIfEmpty()
            } catch {
                settingsView.onRefreshFinished()
                settingsView.checkIfEmpty()
            }
        }
    }

    override fun refresh(forceNetwork: Boolean) {
        onRefreshStarted()
        settingsJob?.cancel()

        // Clear in case there are students left over from another user
        students = emptyList()
        clearData()
        loadData(forceNetwork)
    }

    override fun compare(item1: User?, item2: User?): Int {
        if (item1 == null || item2 == null) return -1
        return if (item1.shortName == null || item2.shortName == null) {
            java.lang.Long.compare(item1.id, item2.id)
        } else item1.shortName!!.compareTo(item2.shortName!!)
    }

    override fun areContentsTheSame(item1: User?, item2: User?): Boolean {
        return if (item1 == null || item2 == null) false else item1.id == item2.id
    }

    override fun areItemsTheSame(item1: User?, item2: User?): Boolean {
        return if (item1 == null || item2 == null) false else item1.id == item2.id
    }

    override fun onDestroyed() {
        super.onDestroyed()
        settingsJob?.cancel()
    }
}
