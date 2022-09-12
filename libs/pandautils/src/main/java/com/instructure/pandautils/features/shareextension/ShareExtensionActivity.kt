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
import androidx.appcompat.app.AppCompatActivity
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
import com.instructure.pandautils.features.file.upload.FileUploadDialogFragment
import com.instructure.pandautils.features.file.upload.FileUploadType
import com.instructure.pandautils.features.shareextension.progress.ShareExtensionProgressDialogFragment
import com.instructure.pandautils.features.shareextension.status.ShareExtensionStatus
import com.instructure.pandautils.features.shareextension.status.ShareExtensionStatusDialogFragment
import com.instructure.pandautils.features.shareextension.target.ShareExtensionTargetFragment
import com.instructure.pandautils.utils.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.activity_share_file.*
import kotlinx.coroutines.Job
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*

const val WORKER_ID = "workerId"

@Parcelize
data class ShareFileSubmissionTarget(
        val course: Course,
        val assignment: Assignment
) : Parcelable

@AndroidEntryPoint
abstract class ShareExtensionActivity : AppCompatActivity() {

    private val shareExtensionViewModel: ShareExtensionViewModel by viewModels()

    private var loadCoursesJob: Job? = null
    private var uploadFileSourceFragment: DialogFragment? = null

    private val submissionTarget: ShareFileSubmissionTarget? by lazy {
        intent?.extras?.getParcelable(Const.SUBMISSION_TARGET)
    }

    private val workerId: UUID? by lazy {
        intent?.extras?.getSerializable(WORKER_ID) as? UUID
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_share_file)
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
                val bundle = FileUploadDialogFragment.createAssignmentBundle(action.fileUri, action.course as Course, action.assignment)
                showUploadDialog(bundle, action.dialogCallback, action.workerCallback)
            }
            is ShareExtensionAction.ShowMyFilesUploadDialog -> {
                val bundle = FileUploadDialogFragment.createFilesBundle(action.fileUri, null)
                showUploadDialog(bundle, action.dialogCallback, action.workerCallback)
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
                ShareExtensionStatusDialogFragment.newInstance(ShareExtensionStatus.SUCCEEDED, action.fileUploadType).show(supportFragmentManager, ShareExtensionStatusDialogFragment.TAG)
            }
            is ShareExtensionAction.ShowProgressDialog -> {
                showProgressDialog(action.uuid)
            }
            is ShareExtensionAction.ShowErrorDialog -> {
                ShareExtensionStatusDialogFragment.newInstance(ShareExtensionStatus.FAILED, action.fileUploadType).show(supportFragmentManager, ShareExtensionStatusDialogFragment.TAG)
            }
        }
    }

    private fun showProgressDialog(uuid: UUID) {
        ShareExtensionProgressDialogFragment.newInstance(uuid).show(supportFragmentManager, ShareExtensionProgressDialogFragment.TAG)
    }

    private fun showUploadDialog(bundle: Bundle, dialogCallback: (Int) -> Unit, workerCallback: (UUID, LiveData<WorkInfo>) -> Unit) {
        ValueAnimator.ofObject(ArgbEvaluator(), ContextCompat.getColor(this, R.color.login_studentAppTheme), getColor(bundle)).let {
            it.addUpdateListener { animation -> rootView!!.setBackgroundColor(animation.animatedValue as Int) }
            it.duration = 500
            it.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator) {
                    FileUploadDialogFragment.newInstance(bundle, callback = dialogCallback, workerLiveDataCallback = workerCallback).show(supportFragmentManager, FileUploadDialogFragment.TAG)
                }
            })
            it.start()
        }
    }

    private fun revealBackground() {
        rootView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                AnimationHelpers.removeGlobalLayoutListeners(rootView, this)
                AnimationHelpers.createRevealAnimator(rootView).start()
            }
        })
    }

    abstract fun exitActivity()

    override fun onBackPressed() {
        uploadFileSourceFragment?.dismissAllowingStateLoss()
        super.onBackPressed()
    }

    override fun onDestroy() {
        uploadFileSourceFragment?.dismissAllowingStateLoss()
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
        uploadFileSourceFragment = ShareExtensionTargetFragment()
        uploadFileSourceFragment!!.show(supportFragmentManager, ShareExtensionTargetFragment.TAG)
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
            val color = ColorKeeper.getOrGenerateColor(bundle.getParcelable<Parcelable>(Const.CANVAS_CONTEXT) as CanvasContext)
            ViewStyler.setStatusBarDark(this, color)
            color
        } else {
            val color = ContextCompat.getColor(this, R.color.login_studentAppTheme)
            ViewStyler.setStatusBarDark(this, color)
            color
        }
    }
}
