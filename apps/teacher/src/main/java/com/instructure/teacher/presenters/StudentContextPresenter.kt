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
package com.instructure.teacher.presenters

import com.instructure.canvasapi2.StudentContextCardQuery
import com.instructure.canvasapi2.StudentContextCardQuery.*
import com.instructure.canvasapi2.managers.StudentContextManager
import com.instructure.canvasapi2.type.EnrollmentType
import com.instructure.canvasapi2.utils.weave.WeaveJob
import com.instructure.canvasapi2.utils.weave.awaitQLPaginated
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryWeave
import com.instructure.teacher.viewinterface.StudentContextView
import com.instructure.pandautils.blueprint.FragmentPresenter


class StudentContextPresenter(
        private val studentId: Long,
        private val courseId: Long
) : FragmentPresenter<StudentContextView>() {

    private lateinit var student: User
    private lateinit var course: AsCourse
    private lateinit var mPermissions: Permissions
    private var studentSummary: Analytics? = null
    private var isStudent = true
    private val submissions: MutableList<Submission> = mutableListOf()

    /** Whether we have loaded and displayed the data outside of the submission list (user name, avatar, grade, etc) */
    private var isBaseDataLoaded = false

    /** Whether all pages of submission data have been loaded */
    private var allPagesLoaded = false

    private var apiJob: WeaveJob? = null

    override fun loadData(forceNetwork: Boolean) {
        if (isBaseDataLoaded) {
            viewCallback?.onRefreshFinished()
            viewCallback?.setData(course, student, studentSummary, isStudent)
            viewCallback?.addSubmissions(submissions, course, student)
            return
        }
        if (apiJob?.isActive == true) return
        viewCallback?.onRefreshStarted()

        apiJob = tryWeave {
            awaitQLPaginated<Data> {
                onRequest {
                    viewCallback?.showLoadMoreIndicator(true)
                    StudentContextManager.getStudentContext(courseId, studentId, SUBMISSION_PAGE_SIZE, forceNetwork, it)
                }

                onResponse { data ->
                    // It's possible they routed here via a group, which we don't support in Teacher app yet
                    if (data.course == null) {
                        viewCallback?.onErrorLoading(false)
                        return@onResponse null
                    }
                    course = data.course as AsCourse

                    // If the user was removed from the course, we won't be able to view their information
                    if (course.users!!.edges!!.isEmpty()) {
                        viewCallback?.onErrorLoading(false)
                        return@onResponse null
                    }
                    if (!isBaseDataLoaded) {
                        student = course.users!!.edges!!.first().user!!
                        studentSummary = student.analytics
                        mPermissions = course.permissions!!
                        isStudent = student.enrollments.any { it.type == EnrollmentType.STUDENTENROLLMENT }
                        viewCallback?.onRefreshFinished()
                        viewCallback?.setData(course, student, studentSummary, isStudent)
                        isBaseDataLoaded = true
                    }
                    val newSubmissions = course.submissions!!.edges?.mapNotNull { it.submission } ?: emptyList()
                    submissions.addAll(newSubmissions)
                    viewCallback?.addSubmissions(newSubmissions, course, student)
                    viewCallback?.showLoadMoreIndicator(false)
                    val pageInfo = course.submissions!!.pageInfo
                    return@onResponse if (pageInfo.isHasNextPage) pageInfo.endCursor else null
                }

                onComplete {
                    allPagesLoaded = true
                }
            }
        } catch { error ->
            val isDesigner = this::student.isInitialized && student.enrollments.any { it.type == EnrollmentType.DESIGNERENROLLMENT }
            viewCallback?.onErrorLoading(isDesigner)
            error.printStackTrace()
        }
    }

    fun loadMoreSubmissions() {
        apiJob?.next()
    }

    override fun refresh(forceNetwork: Boolean) {
        if(forceNetwork) {
            isBaseDataLoaded = false
            allPagesLoaded = false
            submissions.clear()
            viewCallback?.clear()
            apiJob?.cancel()
        }
        loadData(forceNetwork)
    }

    override fun onDestroyed() {
        super.onDestroyed()
        apiJob?.cancel()
    }

    companion object {
        private const val SUBMISSION_PAGE_SIZE = 20
    }
}
