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
@file:Suppress("DEPRECATION")

package com.instructure.student.dialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatEditText
import com.instructure.pandautils.blueprint.BaseCanvasDialogFragment
import com.instructure.canvasapi2.apis.UserAPI
import com.instructure.canvasapi2.managers.CourseManager.getAllFavoriteCourses
import com.instructure.canvasapi2.managers.InboxManager.createConversation
import com.instructure.canvasapi2.managers.UserManager.getFirstPagePeopleList
import com.instructure.canvasapi2.managers.UserManager.getNextPagePeopleList
import com.instructure.canvasapi2.models.Conversation
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.isValid
import com.instructure.canvasapi2.utils.weave.awaitApi
import com.instructure.canvasapi2.utils.weave.awaitPaginated
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryWeave
import com.instructure.pandautils.analytics.SCREEN_VIEW_ASK_INSTRUCTOR
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.utils.ThemePrefs.brandColor
import com.instructure.pandautils.utils.ViewStyler.themeEditText
import com.instructure.student.R
import com.instructure.student.dialog.FatalErrorDialogStyled.Companion.newInstance

@ScreenView(SCREEN_VIEW_ASK_INSTRUCTOR)
class AskInstructorDialogStyled : BaseCanvasDialogFragment() {

    // Data
    private var courseList: List<Course> = emptyList()
    private var course: Course? = null

    // Views
    private lateinit var courseSpinner: Spinner
    private lateinit var message: EditText

    // Adapter
    private var courseAdapter: CourseSpinnerAdapter? = null

    // Inflater
    private var inflater: LayoutInflater? = null

    private var canClickSend = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    override fun onDestroyView() {
        if (retainInstance) dialog?.setDismissMessage(null)
        super.onDestroyView()
    }

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.instructor_question))
            .setPositiveButton(getString(R.string.send)) { _, _ ->
                if (!canClickSend) return@setPositiveButton
                if (message.text?.toString().isValid()) {
                    sendMessage()
                } else {
                    Toast.makeText(activity, getString(R.string.emptyMessage), Toast.LENGTH_SHORT).show()
                }
            }

        val view = LayoutInflater.from(activity).inflate(R.layout.ask_instructor, null)
        courseSpinner = view.findViewById(R.id.courseSpinner)
        message = view.findViewById(R.id.message)
        builder.setView(view)

        val loading = listOf(Course(name = getString(R.string.loading)))
        courseAdapter = CourseSpinnerAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, loading)
        courseSpinner.adapter = courseAdapter

        val dialog = builder.create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(brandColor)
            themeEditText(requireContext(), (message as AppCompatEditText?)!!, brandColor)
        }
        dialog.setCanceledOnTouchOutside(true)
        return dialog
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // Anything that relies on intent data belongs here
        inflater = requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        // Get courses
        fetchCourses()
    }

    inner class CourseSpinnerAdapter(context: Context, textViewResourceId: Int, private val courses: List<Course>) :
        ArrayAdapter<Course>(context, textViewResourceId, courses) {

        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
            return getCustomView(position, convertView)
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            return getCustomView(position, convertView)
        }

        @SuppressLint("InflateParams")
        fun getCustomView(position: Int, convertView: View?): View {
            val holder: CourseViewHolder
            val view: View
            if (convertView == null) {
                view = inflater!!.inflate(R.layout.spinner_row_courses, null)
                holder = CourseViewHolder(view.findViewById(R.id.courseName))
                view.tag = holder
            } else {
                view = convertView
                holder = view.tag as CourseViewHolder
            }
            holder.courseName.text = courses[position].name
            return view
        }

    }

    private data class CourseViewHolder(val courseName: TextView)

    private fun fetchCourses() {
        tryWeave {
            courseList = awaitApi { getAllFavoriteCourses(true, it) }
            courseAdapter = CourseSpinnerAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, courseList)
            courseSpinner.adapter = courseAdapter
            courseSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                    course = parent.adapter.getItem(position) as Course
                    canClickSend = true
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        } catch {
            // Do Nothing
        }
    }

    private fun sendMessage() {
        val progressDialog = ProgressDialog.show(activity, "", getString(R.string.sending))
        tryWeave {
            val text = message.text.toString()
            val contextId = course?.contextId.orEmpty()

            // Fetch recipient IDs
            val recipients = mutableSetOf<User>()
            awaitPaginated<List<User>> { // Fetch teachers
                onRequestFirst { getFirstPagePeopleList(course!!, UserAPI.EnrollmentType.TEACHER, true, it) }
                onRequestNext { nextUrl, callback -> getNextPagePeopleList(true, nextUrl, callback) }
                onResponse { recipients += it }
            }
            awaitPaginated<List<User>> { // Fetch TAs
                onRequestFirst { getFirstPagePeopleList(course!!, UserAPI.EnrollmentType.TA, true, it) }
                onRequestNext { nextUrl, callback -> getNextPagePeopleList(true, nextUrl, callback) }
                onResponse { recipients += it }
            }
            val ids = recipients.map { it.id.toString() }

            // Send message
            awaitApi<List<Conversation>> { createConversation(ids, text, "", contextId, longArrayOf(), true, it) }

            // Dismiss dialogs
            progressDialog.dismiss()
            dismiss()
        } catch {
            progressDialog.dismiss()
            val fatalErrorDialog = newInstance(R.string.error, R.string.errorSendingMessage, true)
            fatalErrorDialog.show(requireActivity().supportFragmentManager, FatalErrorDialogStyled.TAG)
        }
    }

    companion object {
        const val TAG = "askInstructorDialog"
    }
}
