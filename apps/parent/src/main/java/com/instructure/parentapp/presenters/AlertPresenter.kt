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

import com.instructure.canvasapi2.apis.AlertAPI
import com.instructure.canvasapi2.managers.AlertManager
import com.instructure.canvasapi2.managers.UnreadCountManager
import com.instructure.canvasapi2.models.ObserverAlert
import com.instructure.canvasapi2.models.UnreadCount
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.Logger
import com.instructure.canvasapi2.utils.weave.WeaveJob
import com.instructure.canvasapi2.utils.weave.awaitApi
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryWeave
import com.instructure.parentapp.viewinterface.AlertView
import instructure.androidblueprint.SyncPresenter

class AlertPresenter(var student: User) : SyncPresenter<ObserverAlert, AlertView>(ObserverAlert::class.java) {

    var alertJob: WeaveJob? = null

    init {
        this.student = student
    }

    @Suppress("EXPERIMENTAL_FEATURE_WARNING")
    private fun getAlerts(forceNetwork: Boolean) {
        viewCallback?.let {
            alertJob = tryWeave {
                data.addOrUpdate(awaitApi<List<ObserverAlert>> { AlertManager.getObserverAlerts(student.id, forceNetwork, it) })
                it.onRefreshFinished()
                it.checkIfEmpty()
            } catch {
                viewCallback?.let {
                    it.onRefreshFinished()
                    it.checkIfEmpty()
                }
            }
        }
    }
    override fun loadData(forceNetwork: Boolean) {
        getAlerts(forceNetwork)
    }

    override fun refresh(forceNetwork: Boolean) {
        onRefreshStarted()
        alertJob?.cancel()
        clearData()
        loadData(forceNetwork)
        updateUnreadCount()
    }

    fun setStudent(student: User, refresh: Boolean) {
        this.student = student
        if (refresh) {
            refresh(false)
        }
    }

    @Suppress("EXPERIMENTAL_FEATURE_WARNING")
    fun updateAlert(alertId: Long, workflowState: String, position: Int) {
        viewCallback?.let {
            tryWeave {
                awaitApi<ObserverAlert> { AlertManager.updateObserverAlert(alertId, workflowState, it) }

                if (workflowState == AlertAPI.ALERT_READ) {
                    it.markPositionAsRead(position)
                } else if (workflowState == AlertAPI.ALERT_DISMISSED && data.size() == 0) {
                    // We just removed the last one, refresh the list so there will be an empty state
                    refresh(true)
                    updateUnreadCount()
                }
            } catch {
                Logger.e(it.message)
                Logger.e(it.stackTrace.toString())
            }
        }
    }

    @Suppress("EXPERIMENTAL_FEATURE_WARNING")
    fun updateUnreadCount() {
        viewCallback?.let {
            tryWeave {
                val unreadCount = awaitApi<UnreadCount> { UnreadCountManager.getUnreadAlertCount(student.id, it, true) }
                it.onUpdateUnreadCount(unreadCount.unreadCount)
            } catch {
                Logger.e(it.message)
                Logger.e(it.stackTrace.toString())
            }
        }
    }

    override fun onDestroyed() {
        super.onDestroyed()
        alertJob?.cancel()
    }

    override fun compare(item1: ObserverAlert, item2: ObserverAlert): Int {
        return sortAlerts(item1, item2)
    }

    override fun areContentsTheSame(item1: ObserverAlert, item2: ObserverAlert): Boolean {
        return compareAlerts(item1, item2)
    }

    private fun compareAlerts(oldAlert: ObserverAlert, newAlert: ObserverAlert): Boolean {
        if (oldAlert.title != null && newAlert.title != null) {
            val sameTitle = oldAlert.title == newAlert.title
            val sameState = oldAlert.isMarkedRead() == newAlert.isMarkedRead()
            return sameState && sameTitle
        }
        return false
    }

    private fun sortAlerts(o1: ObserverAlert, o2: ObserverAlert): Int {
        //First compare the read status of the alerts
        val firstCompare = if (o1.isMarkedRead() == o2.isMarkedRead()) 0 else if (o2.isMarkedRead()) -1 else 1

        return when {
            firstCompare != 0 -> firstCompare
            //otherwise, check if the date is null
            (o1.date == null && o2.date == null) -> 0
            (o1.date == null && o2.date != null) -> -1
            (o1.date != null && o2.date == null) -> 1
            else ->
                //If the read status is the same, and the dates aren't null, compare them
                o2.date!!.compareTo(o1.date!!)
        }
    }
}
