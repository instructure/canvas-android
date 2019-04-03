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
package com.instructure.parentapp.fragments

import android.graphics.PorterDuff
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.instructure.canvasapi2.apis.AlertAPI
import com.instructure.canvasapi2.models.Alert
import com.instructure.canvasapi2.models.ObserverAlert
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandarecycler.decorations.SpacesItemDecoration
import com.instructure.pandautils.fragments.BaseSyncFragment
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.ParcelableArg
import com.instructure.parentapp.R
import com.instructure.parentapp.activity.NavigationActivity
import com.instructure.parentapp.adapter.AlertListRecyclerAdapter
import com.instructure.parentapp.factorys.AlertPresenterFactory
import com.instructure.parentapp.holders.AlertViewHolder
import com.instructure.parentapp.interfaces.AdapterToFragmentBadgeCallback
import com.instructure.parentapp.presenters.AlertPresenter
import com.instructure.parentapp.util.AnalyticUtils
import com.instructure.parentapp.util.ParentPrefs
import com.instructure.parentapp.util.RecyclerViewUtils
import com.instructure.parentapp.util.RouteMatcher
import com.instructure.parentapp.viewinterface.AlertView
import instructure.androidblueprint.PresenterFactory
import kotlinx.android.synthetic.main.recycler_swipe_refresh_layout.*
import kotlinx.android.synthetic.main.recycler_swipe_refresh_layout.recyclerView as recycler


class AlertFragment : BaseSyncFragment<ObserverAlert, AlertPresenter, AlertView, AlertViewHolder, AlertListRecyclerAdapter>(), AlertView {

    private var student: User by ParcelableArg(key = Const.STUDENT)
    private var forceNetwork: Boolean = false

    private val mAdapterToFragmentCallback = AdapterToFragmentBadgeCallback<ObserverAlert> { it, position, _ ->
        // Open various detail views depending on alert
        // If the alert is a course grade alert, we don't want to route the user
        if (it.alertType != Alert.alertTypeToAPIString(Alert.AlertType.COURSE_GRADE_HIGH) && it.alertType != Alert.alertTypeToAPIString(Alert.AlertType.COURSE_GRADE_LOW)) {
            AnalyticUtils.trackFlow(AnalyticUtils.ALERT_FLOW, AnalyticUtils.ALERT_ITEM_SELECTED)

            // Note: student is only utilized for assignment routes
            val student = (activity as NavigationActivity).currentStudent

            // If it's an institution announcement we need to construct the url
            if (it.alertType == Alert.alertTypeToAPIString(Alert.AlertType.INSTITUTION_ANNOUNCEMENT)) {
                onRefreshStarted()
                val url = ApiPrefs.fullDomain + "/accounts/self/users/" + student.id + "/account_notifications/" + it.contextId
                RouteMatcher.routeUrl(requireContext(), url, student, ApiPrefs.domain)
                onRefreshFinished()
            } else {
                RouteMatcher.routeUrl(requireContext(), it.htmlUrl ?: "", student, ApiPrefs.domain)
            }
        }

        // The student should be set in the adapter
        presenter.updateAlert(it.id, AlertAPI.ALERT_READ, position)
    }

    private val mAdapterItemDismissedCallback = object : AlertListRecyclerAdapter.ItemDismissedInterface {
        override fun itemDismissed(item: ObserverAlert, holder: AlertViewHolder) {
            AnalyticUtils.trackButtonPressed(AnalyticUtils.DISMISS_ALERT)

            adapter.remove(item)

            presenter.updateAlert(item.id, AlertAPI.ALERT_DISMISSED, 0)
            if (!item.isMarkedRead()) {
                presenter.updateUnreadCount()
            }
            // Update the alerts, also updates the cache
            presenter.refresh(true)
        }
    }

    private fun setColor(color: Int) {
        swipeRefreshLayout.setColorSchemeColors(color, color, color, color)
        emptyPandaView.progressBar.indeterminateDrawable.setColorFilter(color, PorterDuff.Mode.SRC_IN)
    }

    override fun onCreateView(view: View?) = Unit

    override fun layoutResId(): Int = R.layout.fragment_alert

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        addSwipeToRefresh(swipeRefreshLayout)
        recycler.addItemDecoration(SpacesItemDecoration(context, R.dimen.med_padding))
    }

    override fun onReadySetGo(presenter: AlertPresenter) {
        recyclerView.adapter = adapter
        presenter.loadData(forceNetwork)
        setColor(ParentPrefs.currentColor)
    }

    override fun getPresenterFactory(): PresenterFactory<AlertPresenter> = AlertPresenterFactory(student)

    override fun onPresenterPrepared(presenter: AlertPresenter) {
        RecyclerViewUtils.buildRecyclerView(mRootView, context, adapter,
                presenter, R.id.swipeRefreshLayout, R.id.recyclerView, R.id.emptyPandaView, getString(R.string.noAlerts))
        addSwipeToRefresh(swipeRefreshLayout)
        addPagination()
    }

    override fun getAdapter(): AlertListRecyclerAdapter {
        if (mAdapter == null) {
            mAdapter = AlertListRecyclerAdapter(requireContext(), presenter, mAdapterToFragmentCallback, mAdapterItemDismissedCallback)
        }
        return mAdapter
    }

    override fun withPagination(): Boolean = true
    override fun getRecyclerView() : RecyclerView = recycler
    override fun perPageCount(): Int = ApiPrefs.perPageCount

    override fun onRefreshStarted() = emptyPandaView.setLoading()

    override fun onRefreshFinished() {
        swipeRefreshLayout.isRefreshing = false
    }

    override fun checkIfEmpty() {
        RecyclerViewUtils.checkIfEmpty(emptyPandaView, recyclerView, swipeRefreshLayout, adapter, presenter.isEmpty)
    }

    override fun markPositionAsRead(position: Int) {
        val itemCount = adapter.itemCount

        if (position < itemCount) {
            val alert = adapter.getItemAtPosition(position)
            if (alert != null) {
                alert.workflowState = AlertAPI.ALERT_READ
                adapter.notifyItemChanged(position)
                forceNetwork = true
            }
        }
        presenter.updateUnreadCount()
    }

    override fun onUpdateUnreadCount(unreadCount: Int) = (activity as? NavigationActivity)?.updateAlertUnreadCount(unreadCount) ?: Unit

    companion object {
        fun newInstance(student: User): AlertFragment = AlertFragment().apply {
            arguments = Bundle().apply {
                putParcelable(Const.STUDENT, student)
            }
        }
    }
}
