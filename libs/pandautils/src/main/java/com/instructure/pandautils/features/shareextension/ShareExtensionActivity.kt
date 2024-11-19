/*
 * Copyright (C) 2022 - present Instructure, Inc.
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

package com.instructure.pandautils.features.shareextension

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.os.Bundle
import android.os.Parcelable
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ImageView
import androidx.activity.viewModels
import com.instructure.pandautils.base.BaseCanvasActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.LiveData
import androidx.work.WorkInfo
import com.airbnb.lottie.LottieAnimationView
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.StorageQuotaExceededError
import com.instructure.pandautils.R
import com.instructure.pandautils.binding.viewBinding
import com.instructure.pandautils.databinding.ActivityShareFileBinding
import com.instructure.pandautils.features.file.upload.FileUploadDialogFragment
import com.instructure.pandautils.features.file.upload.FileUploadDialogParent
import com.instructure.pandautils.features.file.upload.FileUploadType
import com.instructure.pandautils.features.shareextension.progress.ShareExtensionProgressDialogFragment
import com.instructure.pandautils.features.shareextension.status.ShareExtensionStatus
import com.instructure.pandautils.features.shareextension.status.ShareExtensionStatusDialogFragment
import com.instructure.pandautils.features.shareextension.target.ShareExtensionTargetFragment
import com.instructure.pandautils.utils.AnimationHelpers
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.color
import com.instructure.pandautils.utils.toast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.parcelize.Parcelize
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.UUID

const val WORKER_ID = "workerId"

@Parcelize
data class ShareFileSubmissionTarget(
        val course: Course,
        val assignment: Assignment
) : Parcelable

@AndroidEntryPoint
abstract class ShareExtensionActivity : BaseCanvasActivity(), FileUploadDialogParent {

    private val shareExtensionViewModel: ShareExtensionViewModel by viewModels()

    private var loadCoursesJob: Job? = null
    private var currentFragment: DialogFragment? = null

    private val binding by viewBinding(ActivityShareFileBinding::inflate)

    private val submissionTarget: ShareFileSubmissionTarget? by lazy {
        intent?.extras?.getParcelable(Const.SUBMISSION_TARGET)
    }

    private val workerId: UUID? by lazy {
        intent?.extras?.getSerializable(WORKER_ID) as? UUID
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        ViewStyler.setStatusBarDark(this, ContextCompat.getColor(this, R.color.studentDocumentSharingColor))
        if (shareExtensionViewModel.checkIfLoggedIn()) {
            revealBackground()
            if (workerId != null) {
                showProgressDialog(workerId!!)
            } else {
                shareExtensionViewModel.parseIntentType(intent)
                if (submissionTarget != null) {
                    shareExtensionViewModel.showUploadDialog(
                        submissionTarget!!.course,
                        submissionTarget!!.assignment,
                        FileUploadType.ASSIGNMENT
                    )
                } else {
                    showDestinationDialog()
                }
            }
        } else {
            exitActivity()
        }

        shareExtensionViewModel.events.observe(this) {
            it.getContentIfNotHandled()?.let {
                handleAction(it)
            }
        }
    }

    private fun handleAction(action: ShareExtensionAction) {
        when (action) {
            is ShareExtensionAction.ShowAssignmentUploadDialog -> {
                val bundle = FileUploadDialogFragment.createAssignmentBundle(action.fileUris, action.course as Course, action.assignment)
                showUploadDialog(bundle, action.dialogCallback)
            }
            is ShareExtensionAction.ShowMyFilesUploadDialog -> {
                val bundle = FileUploadDialogFragment.createFilesBundle(action.fileUris, null)
                showUploadDialog(bundle, action.dialogCallback)
            }
            is ShareExtensionAction.ShowToast -> {
                toast(action.toast)
            }
            is ShareExtensionAction.Finish -> {
                finish()
            }
            is ShareExtensionAction.ShowConfetti -> {
                showConfetti()
            }
            is ShareExtensionAction.ShowSuccessDialog -> {
                binding.rootView.postDelayed({
                    currentFragment = ShareExtensionStatusDialogFragment.newInstance(ShareExtensionStatus.SUCCEEDED, action.fileUploadType)
                    currentFragment?.show(supportFragmentManager, ShareExtensionStatusDialogFragment.TAG)
                }, 250)
            }
            is ShareExtensionAction.ShowProgressDialog -> {
                showProgressDialog(action.uuid)
            }
            is ShareExtensionAction.ShowErrorDialog -> {
                currentFragment = ShareExtensionStatusDialogFragment.newInstance(ShareExtensionStatus.FAILED, action.fileUploadType)
                currentFragment?.show(supportFragmentManager, ShareExtensionStatusDialogFragment.TAG)
            }
        }
    }

    private fun showProgressDialog(uuid: UUID) {
        currentFragment = ShareExtensionProgressDialogFragment.newInstance(uuid)
        currentFragment?.show(supportFragmentManager, ShareExtensionProgressDialogFragment.TAG)
    }

    private fun showUploadDialog(bundle: Bundle, dialogCallback: (Int) -> Unit) {
        ValueAnimator.ofObject(ArgbEvaluator(), ContextCompat.getColor(this, R.color.studentDocumentSharingColor), getColor(bundle)).let {
            it.addUpdateListener { animation -> binding.rootView.setBackgroundColor(animation.animatedValue as Int) }
            it.duration = 500
            it.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator) {
                    currentFragment = FileUploadDialogFragment.newInstance(bundle, callback = dialogCallback)
                    currentFragment?.show(supportFragmentManager, FileUploadDialogFragment.TAG)
                }
            })
            it.start()
        }
    }

    override fun workInfoLiveDataCallback(uuid: UUID?, workInfoLiveData: LiveData<WorkInfo>) {
        uuid?.let {
            shareExtensionViewModel.workerCallback(it)
        }
    }

    private fun revealBackground() = with(binding) {
        rootView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                AnimationHelpers.removeGlobalLayoutListeners(rootView, this)
                AnimationHelpers.createRevealAnimator(rootView).start()
            }
        })
    }

    abstract fun exitActivity()

    override fun onBackPressed() {
        currentFragment?.dismissAllowingStateLoss()
        finish()
    }

    override fun onDestroy() {
        currentFragment?.dismissAllowingStateLoss()
        loadCoursesJob?.cancel()
        super.onDestroy()
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    private fun showDestinationDialog() {
        currentFragment = ShareExtensionTargetFragment()
        currentFragment?.show(supportFragmentManager, ShareExtensionTargetFragment.TAG)
    }

    private fun showConfetti() {
        runOnUiThread {
            val root = window.decorView.rootView as ViewGroup
            val animation = LottieAnimationView(this).apply {
                setAnimation("confetti.json")
                scaleType = ImageView.ScaleType.CENTER_CROP;
            }
            animation.addAnimatorUpdateListener {
                if (it.animatedFraction >= 1.0) root.removeView(animation)
            }
            root.addView(animation, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            animation.playAnimation()
        }
    }

    @Suppress("unused", "UNUSED_PARAMETER")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onQuotaExceeded(errorCode: StorageQuotaExceededError) {
        toast(R.string.fileQuotaExceeded)
    }

    private fun getColor(bundle: Bundle?): Int {
        return if (bundle != null && bundle.containsKey(Const.CANVAS_CONTEXT)) {
            val color = (bundle.getParcelable<Parcelable>(Const.CANVAS_CONTEXT) as CanvasContext).color
            ViewStyler.setStatusBarDark(this, color)
            color
        } else {
            val color = ContextCompat.getColor(this, R.color.studentDocumentSharingColor)
            ViewStyler.setStatusBarDark(this, color)
            color
        }
    }
}
