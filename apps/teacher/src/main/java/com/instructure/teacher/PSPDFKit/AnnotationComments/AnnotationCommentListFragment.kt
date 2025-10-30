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
package com.instructure.teacher.PSPDFKit.AnnotationComments

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialog
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.instructure.annotations.AnnotationDialogs.AnnotationCommentDialog
import com.instructure.canvasapi2.models.ApiValues
import com.instructure.canvasapi2.models.DocSession
import com.instructure.canvasapi2.models.canvadocs.CanvaDocAnnotation
import com.instructure.pandautils.binding.viewBinding
import com.instructure.pandautils.fragments.BaseListFragment
import com.instructure.pandautils.utils.*
import com.instructure.pandautils.utils.applyHorizontalSystemBarInsets
import com.instructure.teacher.R
import com.instructure.teacher.databinding.FragmentAnnotationCommentListBinding
import com.instructure.teacher.utils.getColorCompat
import com.instructure.teacher.utils.setupBackButton
import java.util.*

class AnnotationCommentListFragment : BaseListFragment<
        CanvaDocAnnotation,
        AnnotationCommentListPresenter,
        AnnotationCommentListView,
        AnnotationCommentViewHolder,
        AnnotationCommentListAdapter>(), AnnotationCommentListView {

    private val binding by viewBinding(FragmentAnnotationCommentListBinding::bind)

    private var mAnnotationList by ParcelableArrayListArg<CanvaDocAnnotation>()
    private var mAssigneeId by LongArg()
    private var mDocSession by ParcelableArg<DocSession>()
    private var mApiValues by ParcelableArg<ApiValues>()
    private var mHeadAnnotationId by StringArg()

    override fun createAdapter(): AnnotationCommentListAdapter {
        return AnnotationCommentListAdapter(requireContext(), presenter, { annotation, position ->
            AnnotationCommentDialog.getInstance(requireFragmentManager(), annotation.contents ?: "", getString(R.string.editComment)) { cancelled, text ->
                if(!cancelled) {
                    annotation.contents = text
                    presenter.editComment(annotation, position)
                }
            }.show(requireFragmentManager(), AnnotationCommentDialog::class.java.simpleName)
        }, { annotation, position ->
            val builder = AlertDialog.Builder(requireContext())
            //we want to show a different title for the head annotation
            builder.setTitle(if(position == 0) R.string.deleteAnnotation else R.string.deleteComment)
            builder.setMessage(if(position == 0) R.string.deleteHeadCommentConfirmation else R.string.deleteCommentConfirmation)
            builder.setPositiveButton(getString(R.string.delete).uppercase(Locale.getDefault())) { _, _ ->
                presenter.deleteComment(annotation, position)
            }
            builder.setNegativeButton(getString(R.string.cancel).uppercase(Locale.getDefault()), null)
            val dialog = builder.create()
            dialog.setOnShowListener {
                dialog.getButton(AppCompatDialog.BUTTON_POSITIVE).setTextColor(ThemePrefs.textButtonColor)
                dialog.getButton(AppCompatDialog.BUTTON_NEGATIVE).setTextColor(ThemePrefs.textButtonColor)
            }
            dialog.show()
        })
    }

    override val recyclerView: RecyclerView get() = binding.annotationCommentsRecyclerView
    override fun layoutResId() = R.layout.fragment_annotation_comment_list
    override fun checkIfEmpty() {} // we don't display this view if its empty, so no need to check
    override fun onRefreshFinished() {}
    override fun onRefreshStarted() {}
    override fun getPresenterFactory() = AnnotationCommentListPresenterFactory(mAnnotationList, mDocSession, mApiValues, mAssigneeId, mHeadAnnotationId)

    override fun onPresenterPrepared(presenter: AnnotationCommentListPresenter) {
        val layoutManager = LinearLayoutManager(requireContext())
        layoutManager.orientation = RecyclerView.VERTICAL
        recyclerView.layoutManager = layoutManager
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.adapter = adapter
    }

    override fun onReadySetGo(presenter: AnnotationCommentListPresenter) {
        binding.root.applyHorizontalSystemBarInsets()
        setupToolbar()
        presenter.loadData(false)
        setupCommentInput()
        setupWindowInsets()
    }

    private fun setupWindowInsets() = with(binding) {
        toolbar.applyTopSystemBarInsets()

        ViewCompat.setOnApplyWindowInsetsListener(annotationCommentsRecyclerView) { view, insets ->
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(bottom = maxOf(ime.bottom, systemBars.bottom))
            insets
        }

        ViewCompat.setOnApplyWindowInsetsListener(commentInputContainer) { view, insets ->
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(bottom = maxOf(ime.bottom, systemBars.bottom))
            insets
        }
    }

    fun setupToolbar() {
        binding.toolbar.title = getString(R.string.sg_tab_comments)
        binding.toolbar.setupBackButton(this)
        ViewStyler.themeToolbarLight(requireActivity(), binding.toolbar)
        ViewStyler.setToolbarElevationSmall(requireContext(), binding.toolbar)
    }

    private fun setupCommentInput() {
        // We only want to enable comments if the user has write permissions or greater
        if(presenter.docSession.annotationMetadata?.canWrite() != true) {
            binding.commentInputContainer.setVisible(false)
        } else {
            binding.commentInputContainer.setVisible(true)
            binding.sendCommentButton.imageTintList = ViewStyler.generateColorStateList(
                    intArrayOf(-android.R.attr.state_enabled) to requireContext().getColorCompat(R.color.textDark),
                    intArrayOf() to ThemePrefs.textButtonColor
            )

            binding.sendCommentButton.isEnabled = false
            binding.commentEditText.onTextChanged { binding.sendCommentButton.isEnabled = it.isNotBlank() }
            binding.sendCommentButton.onClickWithRequireNetwork {
                presenter.sendComment(binding.commentEditText.text.toString())
            }
        }
    }

    override fun showSendingStatus() {
        binding.sendCommentButton.setInvisible()
        binding.sendingProgressBar.setVisible()
        binding.sendingProgressBar.announceForAccessibility(getString(R.string.sendingSimple))
        binding.sendingErrorTextView.setGone()
        binding.commentEditText.isEnabled = false
    }

    override fun hideSendingStatus(success: Boolean) {
        binding.sendingProgressBar.setGone()
        binding.sendCommentButton.setVisible()
        binding.commentEditText.isEnabled = true
        if (success) {
            binding.commentEditText.setText("")
            binding.commentEditText.hideKeyboard()
        } else {
            binding.sendingErrorTextView.setVisible()
        }
    }

    override fun notifyItemChanged(position: Int) {
        adapter.notifyItemChanged(position)
    }

    override fun headAnnotationDeleted() {
        requireActivity().onBackPressed()
    }

    companion object {
        @JvmStatic val ANNOTATIONS = "mAnnotationList"
        @JvmStatic val ASSIGNEE_ID = "mAssigneeId"
        @JvmStatic val DOC_SESSION = "mDocSession"
        @JvmStatic val API_VALUES = "mApiValues"
        @JvmStatic val HEAD_ANNOTATION_ID = "mHeadAnnotationId"

        fun newInstance(bundle: Bundle) = AnnotationCommentListFragment().apply { arguments = bundle }

        fun makeBundle(annotations: ArrayList<CanvaDocAnnotation>, headAnnotationId: String, docSession: DocSession, apiValues: ApiValues, assigneeId: Long): Bundle {
            val args = Bundle()
            args.putParcelableArrayList(ANNOTATIONS, annotations)
            args.putLong(ASSIGNEE_ID, assigneeId)
            args.putParcelable(DOC_SESSION, docSession)
            args.putParcelable(API_VALUES, apiValues)
            args.putString(HEAD_ANNOTATION_ID, headAnnotationId)
            return args
        }
    }
}
