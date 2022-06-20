/*
* Copyright (C) 2017 - present Instructure, Inc.
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
package com.instructure.teacher.fragments

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.pandautils.analytics.SCREEN_VIEW_EDIT_FAVORITES
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.fragments.BaseSyncFragment
import com.instructure.pandautils.utils.*
import com.instructure.teacher.R
import com.instructure.teacher.adapters.EditFavoritesAdapter
import com.instructure.teacher.factory.EditFavoritesPresenterFactory
import com.instructure.teacher.holders.EditFavoritesViewHolder
import com.instructure.teacher.interfaces.AdapterToEditFavoriteCoursesCallback
import com.instructure.teacher.presenters.EditFavoritesPresenter
import com.instructure.teacher.utils.AppType
import com.instructure.teacher.utils.RecyclerViewUtils
import com.instructure.teacher.utils.setupBackButton
import com.instructure.teacher.viewinterface.CanvasContextView
import kotlinx.android.synthetic.main.fragment_edit_favorites.*

@ScreenView(SCREEN_VIEW_EDIT_FAVORITES)
class EditFavoritesFragment : BaseSyncFragment<
        CanvasContext,
        EditFavoritesPresenter,
        CanvasContextView,
        EditFavoritesViewHolder,
        EditFavoritesAdapter>(), CanvasContextView {

    private lateinit var mRecyclerView: RecyclerView

    // The user type, used when filtering the course list
    private var mAppType: AppType by SerializableArg(default = AppType.TEACHER)
    private lateinit var mLayoutManager : LinearLayoutManager
    override fun layoutResId() = R.layout.fragment_edit_favorites
    override val recyclerView: RecyclerView get() = mRecyclerView

    override fun onCreateView(view: View) {
       mLayoutManager = LinearLayoutManager(
           requireContext(),
           RecyclerView.VERTICAL,
           false
       )
    }

    override fun onResume() {
        super.onResume()
        setupToolbar()
    }

    override fun onReadySetGo(presenter: EditFavoritesPresenter) {
        swipeRefreshLayout.setOnRefreshListener {
            if (!Utils.isNetworkAvailable(ContextKeeper.appContext)) {
                swipeRefreshLayout.isRefreshing = false
            } else {
                presenter.refresh(true)
            }
        }

        mRecyclerView.layoutManager = mLayoutManager
        addSwipeToRefresh(swipeRefreshLayout)
        mRecyclerView.adapter = adapter

        presenter.loadData(true)
    }

    override fun getPresenterFactory() = EditFavoritesPresenterFactory {
        when (mAppType) {
            AppType.TEACHER -> it.isTeacher || it.isTA || it.isDesigner
            AppType.STUDENT -> it.isStudent
            AppType.PARENT -> it.isObserver
        }
    }

    override fun onPresenterPrepared(presenter: EditFavoritesPresenter) {
        mRecyclerView = RecyclerViewUtils.buildRecyclerView(rootView, requireContext(), adapter, presenter, R.id.swipeRefreshLayout,
                R.id.favoritesRecyclerView, R.id.emptyPandaView, getString(R.string.noCourses))
        mRecyclerView.itemAnimator = null
    }

    override fun createAdapter(): EditFavoritesAdapter {
        return EditFavoritesAdapter(
            requireContext(),
            presenter,
            object : AdapterToEditFavoriteCoursesCallback {
                override fun onRowClicked(canvasContext: CanvasContext, isFavorite: Boolean) {
                    presenter.setFavorite(canvasContext, isFavorite)
                }
            }
        )
    }

    private fun setupToolbar() {
        toolbar.title = getString(R.string.edit_courses)
        toolbar.setupBackButton(this)
        ViewStyler.themeToolbarLight(requireActivity(), toolbar)
        ViewStyler.setToolbarElevationSmall(requireContext(), toolbar)
    }

    override fun onRefreshFinished() {
        swipeRefreshLayout.isRefreshing = false
    }

    override fun onRefreshStarted() {
        emptyPandaView.setLoading()
    }

    override fun checkIfEmpty() {
        emptyPandaView.setMessageText(R.string.noCoursesSubtext)
        emptyPandaView.setEmptyViewImage(requireContext().getDrawableCompat(R.drawable.ic_panda_nocourses))
        RecyclerViewUtils.checkIfEmpty(emptyPandaView, mRecyclerView, swipeRefreshLayout, adapter, presenter.isEmpty)
    }

    companion object {
        @JvmStatic val APP_TYPE = "appType"

        fun newInstance(args: Bundle) = EditFavoritesFragment().apply {
            mAppType = args.getSerializable(APP_TYPE) as AppType
        }

        fun makeBundle(appType: AppType): Bundle {
            val args = Bundle()
            args.putSerializable(EditFavoritesFragment.APP_TYPE, appType)
            return args
        }
    }
}
