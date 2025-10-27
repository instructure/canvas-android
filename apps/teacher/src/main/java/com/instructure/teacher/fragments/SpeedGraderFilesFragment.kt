/*
 * Copyright (C) 2017 - present  Instructure, Inc.
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
 */
package com.instructure.teacher.fragments

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.RecyclerView
import com.instructure.canvasapi2.models.Attachment
import com.instructure.canvasapi2.models.Submission
import com.instructure.pandautils.analytics.SCREEN_VIEW_SPEED_GRADER_FILES
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.binding.viewBinding
import com.instructure.pandautils.fragments.BaseSyncFragment
import com.instructure.pandautils.utils.NullableParcelableArg
import com.instructure.teacher.R
import com.instructure.teacher.adapters.AttachmentAdapter
import com.instructure.teacher.databinding.FragmentSpeedgraderFilesBinding
import com.instructure.teacher.factory.SpeedGraderFilesPresenterFactory
import com.instructure.teacher.holders.AttachmentViewHolder
import com.instructure.teacher.presenters.SpeedGraderFilesPresenter
import com.instructure.teacher.utils.RecyclerViewUtils
import com.instructure.teacher.view.SubmissionFileSelectedEvent
import com.instructure.teacher.view.SubmissionSelectedEvent
import com.instructure.teacher.viewinterface.SpeedGraderFilesView
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

@ScreenView(SCREEN_VIEW_SPEED_GRADER_FILES)
class SpeedGraderFilesFragment : BaseSyncFragment<
        Attachment,
        SpeedGraderFilesPresenter,
        SpeedGraderFilesView,
        AttachmentViewHolder,
        AttachmentAdapter>(), SpeedGraderFilesView {

    private val binding by viewBinding(FragmentSpeedgraderFilesBinding::bind)

    private var mSubmission: Submission? by NullableParcelableArg(default = Submission())

    companion object {
        fun newInstance(submission: Submission?) = SpeedGraderFilesFragment().apply {
            mSubmission = submission
        }
    }

    override val recyclerView: RecyclerView get() = binding.speedGraderFilesRecyclerView
    override fun layoutResId() = R.layout.fragment_speedgrader_files
    override fun getPresenterFactory() = SpeedGraderFilesPresenterFactory(mSubmission)
    override fun onCreateView(view: View) = Unit
    override fun onPresenterPrepared(presenter: SpeedGraderFilesPresenter) {
        RecyclerViewUtils.buildRecyclerView(rootView, requireContext(), adapter, presenter, R.id.swipeRefreshLayout,
                R.id.speedGraderFilesRecyclerView, R.id.speedGraderFilesEmptyView, getString(R.string.no_items_to_display_short))
        binding.swipeRefreshLayout.isEnabled = false
        setupWindowInsets()
    }

    private fun setupWindowInsets() = with(binding) {
        ViewCompat.setOnApplyWindowInsetsListener(speedGraderFilesRecyclerView) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(bottom = systemBars.bottom)
            insets
        }
        if (speedGraderFilesRecyclerView.isAttachedToWindow) {
            ViewCompat.requestApplyInsets(speedGraderFilesRecyclerView)
        }
    }

    override fun onReadySetGo(presenter: SpeedGraderFilesPresenter) {
        recyclerView.adapter = adapter
        presenter.loadData(false)
    }

    override fun createAdapter(): AttachmentAdapter {
        return AttachmentAdapter(requireContext(), presenter) {
            EventBus.getDefault().post(SubmissionFileSelectedEvent(presenter.getSubmission()?.id ?: -1, it))
        }
    }

    override fun checkIfEmpty() {
        RecyclerViewUtils.checkIfEmpty(binding.speedGraderFilesEmptyView, recyclerView, binding.swipeRefreshLayout, adapter, presenter.isEmpty)
    }

    override fun onRefreshFinished() {}
    override fun onRefreshStarted() {}

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onSubmissionSelected(event: SubmissionSelectedEvent) {
        if (event.submission?.id == presenter.getSubmission()?.id) {
            presenter.setSubmission(event.submission)
            adapter.setSelectedPosition(0)
        }
    }
}
