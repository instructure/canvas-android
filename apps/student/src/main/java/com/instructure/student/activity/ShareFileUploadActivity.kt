 /*
 * Copyright (C) 2016 - present Instructure, Inc.
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

package com.instructure.student.activity

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.view.ViewTreeObserver
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.instructure.canvasapi2.managers.CourseManager
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.StorageQuotaExceededError
import com.instructure.canvasapi2.utils.weave.awaitApi
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryWeave
import com.instructure.pandautils.dialogs.UploadFilesDialog
import com.instructure.pandautils.utils.*
import com.instructure.student.R
import com.instructure.student.dialog.ShareFileDestinationDialog
import com.instructure.pandautils.features.shareextension.ShareExtensionViewModel
import com.instructure.pandautils.features.shareextension.target.ShareExtensionTargetFragment
import com.instructure.student.util.Analytics
import com.instructure.pandautils.utils.AnimationHelpers
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.activity_share_file.*
import kotlinx.coroutines.Job
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*

@Parcelize
data class ShareFileSubmissionTarget(
    val course: Course,
    val assignment: Assignment
) : Parcelable

@AndroidEntryPoint
class ShareFileUploadActivity : AppCompatActivity(), ShareFileDestinationDialog.DialogCloseListener {

    private val shareExtensionViewModel: ShareExtensionViewModel by viewModels()

    private val PERMISSION_REQUEST_WRITE_STORAGE = 0

    private var loadCoursesJob: Job? = null
    private var uploadFileSourceFragment: DialogFragment? = null
    private var courses: ArrayList<Course>? = null

    private val submissionTarget: ShareFileSubmissionTarget? by lazy {
        intent?.extras?.getParcelable<ShareFileSubmissionTarget>(Const.SUBMISSION_TARGET)
    }

    private var sharedURI: Uri? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_share_file)
        ViewStyler.setStatusBarDark(this, ContextCompat.getColor(this, R.color.studentDocumentSharingColor))
        if (shareExtensionViewModel.checkIfLoggedIn()) {
            revealBackground()
            Analytics.trackAppFlow(this)
            sharedURI = shareExtensionViewModel.parseIntentType(intent)
            if (submissionTarget != null) {
                // If targeted for submission, skip the picker and go immediately to the submission workflow
                val bundle = UploadFilesDialog.createAssignmentBundle(
                    sharedURI,
                    submissionTarget!!.course,
                    submissionTarget!!.assignment
                )
                onNext(bundle)
            } else {
                getCourses()
            }
            askForStoragePermissionIfNecessary()
        } else {
            exitActivity()
        }
    }

    private fun askForStoragePermissionIfNecessary() {
        if ((sharedURI?.scheme?.equals("file") == true || sharedURI?.scheme?.equals("content") == true) && !PermissionUtils.hasPermissions(this, PermissionUtils.WRITE_EXTERNAL_STORAGE)) {
            ActivityCompat.requestPermissions(this, PermissionUtils.makeArray(PermissionUtils.WRITE_EXTERNAL_STORAGE), PERMISSION_REQUEST_WRITE_STORAGE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == UploadFilesDialog.CAMERA_PIC_REQUEST ||
                requestCode == UploadFilesDialog.PICK_FILE_FROM_DEVICE ||
                requestCode == UploadFilesDialog.PICK_IMAGE_GALLERY) {
            //File Dialog Fragment will not be notified of onActivityResult(), alert manually
            OnActivityResults(ActivityResult(requestCode, resultCode, data), null).postSticky()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode) {
            PERMISSION_REQUEST_WRITE_STORAGE -> {
                if (!PermissionUtils.allPermissionsGrantedResultSummary(grantResults)) {
                    Toast.makeText(this, R.string.permissionDenied, Toast.LENGTH_LONG).show()
                    finish()
                }
            }
        }
    }

    private fun getCourses() {
        loadCoursesJob = tryWeave {
            val courses = awaitApi<List<Course>> { CourseManager.getCourses(true, it) }
            if (courses.isNotEmpty()) {
                this@ShareFileUploadActivity.courses = ArrayList(courses)
                if (uploadFileSourceFragment == null) showDestinationDialog()
            } else {
                Toast.makeText(applicationContext, R.string.uploadingFromSourceFailed, Toast.LENGTH_LONG).show()
                exitActivity()
            }
        } catch {
            Toast.makeText(this@ShareFileUploadActivity, R.string.uploadingFromSourceFailed, Toast.LENGTH_LONG).show()
            exitActivity()
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

    private fun exitActivity() {
        val intent = LoginActivity.createIntent(this)
        startActivity(intent)
        finish()
    }

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
        if (sharedURI == null) {
            Toast.makeText(applicationContext, R.string.uploadingFromSourceFailed, Toast.LENGTH_LONG).show()
        } else {
            val fragment = ShareExtensionTargetFragment()
            fragment.show(supportFragmentManager, ShareFileDestinationDialog.TAG)
            /*uploadFileSourceFragment = ShareFileDestinationDialog.newInstance(ShareFileDestinationDialog.createBundle(sharedURI!!, courses!!))
            uploadFileSourceFragment!!.show(supportFragmentManager, ShareFileDestinationDialog.TAG)*/
        }
    }

    override fun onCancel(dialog: DialogInterface?) {
        finish()
    }


    @Suppress("unused", "UNUSED_PARAMETER")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onQuotaExceeded(errorCode: StorageQuotaExceededError) {
        toast(R.string.fileQuotaExceeded)
    }

    private fun getColor(bundle: Bundle?): Int {
        return if(bundle != null && bundle.containsKey(Const.CANVAS_CONTEXT)) {
            val color = ColorKeeper.getOrGenerateColor(bundle.getParcelable<Parcelable>(Const.CANVAS_CONTEXT) as CanvasContext)
            ViewStyler.setStatusBarDark(this, color)
            color
        } else {
            val color = ContextCompat.getColor(this, R.color.login_studentAppTheme)
            ViewStyler.setStatusBarDark(this, color)
            color
        }
    }

    override fun onNext(bundle: Bundle) {
        ValueAnimator.ofObject(ArgbEvaluator(), ContextCompat.getColor(this, R.color.login_studentAppTheme), getColor(bundle)).let {
            it.addUpdateListener { animation -> rootView!!.setBackgroundColor(animation.animatedValue as Int) }
            it.duration = 500
            it.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator) {
                    UploadFilesDialog.show(supportFragmentManager, bundle) { event ->
                        if(event == UploadFilesDialog.EVENT_ON_UPLOAD_BEGIN || event == UploadFilesDialog.EVENT_DIALOG_CANCELED) {
                            finish()
                        }
                    }
                }
            })
            it.start()
        }
    }
}
