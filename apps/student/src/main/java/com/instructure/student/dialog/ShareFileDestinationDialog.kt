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
package com.instructure.student.dialog

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.*
import android.view.animation.AnimationUtils
import android.widget.AdapterView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.instructure.canvasapi2.managers.AssignmentManager.getAllAssignments
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.Pronouns.span
import com.instructure.canvasapi2.utils.weave.awaitApi
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryWeave
import com.instructure.pandautils.dialogs.UploadFilesDialog
import com.instructure.pandautils.dialogs.UploadFilesDialog.Companion.createAssignmentBundle
import com.instructure.pandautils.dialogs.UploadFilesDialog.Companion.createFilesBundle
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.ParcelableArg
import com.instructure.pandautils.utils.ParcelableArrayListArg
import com.instructure.pandautils.utils.ThemePrefs.buttonColor
import com.instructure.pandautils.utils.setVisible
import com.instructure.student.R
import com.instructure.student.adapter.FileUploadAssignmentsAdapter
import com.instructure.student.adapter.FileUploadAssignmentsAdapter.Companion.getOnlineUploadAssignmentsList
import com.instructure.student.adapter.FileUploadCoursesAdapter
import com.instructure.student.adapter.FileUploadCoursesAdapter.Companion.getFilteredCourseList
import com.instructure.pandautils.utils.AnimationHelpers.createRevealAnimator
import com.instructure.pandautils.utils.AnimationHelpers.removeGlobalLayoutListeners
import com.instructure.student.util.UploadCheckboxManager
import com.instructure.student.util.UploadCheckboxManager.OnOptionCheckedListener
import kotlinx.android.synthetic.main.upload_file_destination.*
import kotlinx.coroutines.Job
import java.util.*

@SuppressLint("InflateParams")
class ShareFileDestinationDialog : DialogFragment(), OnOptionCheckedListener {
    // Dismiss interface
    interface DialogCloseListener {
        fun onCancel(dialog: DialogInterface?)
        fun onNext(bundle: Bundle)
    }

    private var uri: Uri by ParcelableArg(key = Const.URI)
    private var courses: ArrayList<Course> by ParcelableArrayListArg(key = Const.COURSES)
    private var user: User = ApiPrefs.user!!

    private lateinit var checkboxManager: UploadCheckboxManager
    private lateinit var rootView: View

    private var assignmentJob: Job? = null

    private var selectedAssignment: Assignment? = null
    private var studentEnrollmentsAdapter: FileUploadCoursesAdapter? = null

    override fun onStart() {
        super.onStart()
        // Don't dim the background when the dialog is created.
        dialog?.window?.apply {
            val params = attributes
            params.dimAmount = 0f
            params.flags = params.flags or WindowManager.LayoutParams.FLAG_DIM_BEHIND
            attributes = params
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        dialog?.window?.let {
            it.attributes.windowAnimations = R.style.FileDestinationDialogAnimation
            it.setWindowAnimations(R.style.FileDestinationDialogAnimation)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        rootView = LayoutInflater.from(activity).inflate(R.layout.upload_file_destination, null)
        val alertDialog = AlertDialog.Builder(requireContext())
            .setView(rootView)
            .setPositiveButton(R.string.next) { _, _ -> validateAndShowNext() }
            .setNegativeButton(R.string.cancel) { _, _ -> dismissAllowingStateLoss() }
            .setCancelable(true)
            .create()

        alertDialog.setOnShowListener {
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(buttonColor)
            alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(buttonColor)
        }

        return alertDialog
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        userName.text = span(user.name, user.pronouns)

        // Init checkboxes
        checkboxManager = UploadCheckboxManager(this, selectionIndicator)
        checkboxManager.add(myFilesCheckBox)
        checkboxManager.add(assignmentCheckBox)

        setRevealContentsListener()
        assignmentContainer.setVisible()
    }

    override fun onCancel(dialog: DialogInterface) {
        (activity as? DialogCloseListener)?.onCancel(dialog)
    }

    override fun onDestroyView() {
        if (retainInstance) dialog?.dismiss()
        super.onDestroyView()
    }

    private fun validateAndShowNext() {
        // Validate selections
        val errorString = validateForm()
        if (errorString.isNotEmpty()) {
            Toast.makeText(activity, errorString, Toast.LENGTH_SHORT).show()
        } else {
            (activity as? DialogCloseListener)?.onNext(uploadBundle)
            dismiss()
        }
    }

    /**
     * Checks if user has filled out form completely.
     * @return Returns an error string if the form is not valid.
     */
    private fun validateForm(): String {
        // Make sure the user has selected a course and an assignment
        val uploadType = checkboxManager.selectedType

        // Make sure an assignment & course was selected if FileUploadType.Assignment
        if (uploadType == UploadFilesDialog.FileUploadType.ASSIGNMENT) {
            if (studentCourseSpinner.selectedItem == null) {
                return getString(R.string.noCourseSelected)
            } else if (assignmentSpinner.selectedItem == null || (assignmentSpinner.selectedItem as? Assignment)?.id == Long.MIN_VALUE) {
                return getString(R.string.noAssignmentSelected)
            }
        }
        return ""
    }

    private val uploadBundle: Bundle
        get() = when (checkboxManager.selectedCheckBox!!.id) {
            R.id.myFilesCheckBox -> createFilesBundle(uri, null)
            R.id.assignmentCheckBox -> createAssignmentBundle(
                uri,
                (studentCourseSpinner.selectedItem as Course),
                (assignmentSpinner.selectedItem as Assignment)
            )
            else -> createFilesBundle(uri, null)
        }

    private fun setAssignmentsSpinnerToLoading() {
        val loading = Assignment()
        val courseAssignments = ArrayList<Assignment>()
        loading.name = getString(R.string.loadingAssignments)
        loading.id = Long.MIN_VALUE
        courseAssignments.add(loading)
        assignmentSpinner.adapter = FileUploadAssignmentsAdapter(requireContext(), courseAssignments)
    }

    fun fetchAssignments(courseId: Long) {
        assignmentJob?.cancel()
        assignmentJob = tryWeave {
            val assignments = awaitApi<List<Assignment>> { getAllAssignments(courseId, false, it) }
            if (assignments.isNotEmpty() && courseSelectionChanged(assignments[0].courseId)) return@tryWeave
            val courseAssignments = getOnlineUploadAssignmentsList(requireContext(), assignments)

            // Init assignment spinner
            val adapter = FileUploadAssignmentsAdapter(requireContext(), courseAssignments)
            assignmentSpinner.adapter = adapter
            if (selectedAssignment != null) {
                // Prevent listener from firing the when selection is placed
                assignmentSpinner.onItemSelectedListener = null
                val position = adapter.getPosition(selectedAssignment)
                if (position >= 0) {
                    // Prevents the network callback from replacing what the user selected while cache was being displayed
                    assignmentSpinner.setSelection(position, false)
                }
            }
            assignmentSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
                    if (position < 0) return
                    if (position < adapter.count) {
                        selectedAssignment = adapter.getItem(position)
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        } catch {
            // Do nothing
        }
    }

    private fun setupCourseSpinners() {
        if (activity?.isFinishing != false) return
        if (studentEnrollmentsAdapter == null) {
            studentEnrollmentsAdapter = FileUploadCoursesAdapter(
                requireContext(),
                requireActivity().layoutInflater,
                getFilteredCourseList(courses, FileUploadCoursesAdapter.Type.STUDENT)
            )
            studentCourseSpinner.adapter = studentEnrollmentsAdapter
        } else {
            studentEnrollmentsAdapter?.setCourses(getFilteredCourseList(courses, FileUploadCoursesAdapter.Type.STUDENT))
        }
        studentCourseSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                // Make the allowed extensions disappear
                val (courseId) = parent.adapter.getItem(position) as Course
                // If the user is a teacher, let them know and don't let them select an assignment
                if (courseId > 0) {
                    setAssignmentsSpinnerToLoading()
                    fetchAssignments(courseId)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun courseSelectionChanged(newCourseId: Long): Boolean {
        return checkboxManager.selectedCheckBox!!.id == R.id.assignmentCheckBox && newCourseId != (studentCourseSpinner.selectedItem as Course).id
    }

    private fun setRevealContentsListener() {
        val avatarAnimation = AnimationUtils.loadAnimation(activity, R.anim.ease_in_shrink)
        val titleAnimation = AnimationUtils.loadAnimation(activity, R.anim.ease_in_bottom)
        avatar.viewTreeObserver.addOnGlobalLayoutListener(
            object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    removeGlobalLayoutListeners(avatar, this)
                    avatar.startAnimation(avatarAnimation)
                    userName.startAnimation(titleAnimation)
                    dialogTitle.startAnimation(titleAnimation)
                }
            }
        )
        dialogContents.viewTreeObserver.addOnGlobalLayoutListener(
            object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    removeGlobalLayoutListeners(dialogContents, this)
                    val revealAnimator = createRevealAnimator(dialogContents)
                    Handler().postDelayed({
                        if (!isAdded) return@postDelayed
                        dialogContents.visibility = View.VISIBLE
                        revealAnimator.addListener(
                            object : AnimatorListenerAdapter() {
                                override fun onAnimationEnd(animation: Animator) {
                                    setupCourseSpinners()
                                }
                            }
                        )
                        revealAnimator.start()
                    }, 600)
                }
            }
        )
    }

    private fun enableStudentSpinners(isEnabled: Boolean) {
        assignmentSpinner.isEnabled = isEnabled
        studentCourseSpinner.isEnabled = isEnabled
    }

    override fun onUserFilesSelected() {
        enableStudentSpinners(false)
    }

    override fun onAssignmentFilesSelected() {
        enableStudentSpinners(true)
    }

    override fun onDestroy() {
        assignmentJob?.cancel()
        super.onDestroy()
    }

    companion object {
        const val TAG = "uploadFileSourceFragment"

        fun newInstance(bundle: Bundle): ShareFileDestinationDialog {
            val uploadFileSourceFragment = ShareFileDestinationDialog()
            uploadFileSourceFragment.arguments = bundle
            return uploadFileSourceFragment
        }

        fun createBundle(uri: Uri, courses: ArrayList<Course>): Bundle {
            val bundle = Bundle()
            bundle.putParcelable(Const.URI, uri)
            bundle.putParcelableArrayList(Const.COURSES, courses)
            return bundle
        }
    }
}
