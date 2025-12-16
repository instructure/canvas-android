//
// Copyright (C) 2018-present Instructure, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//

package com.instructure.dataseeding.api

import com.instructure.dataseeding.model.CanvasUserApiModel
import com.instructure.dataseeding.model.CourseApiModel
import com.instructure.dataseeding.model.DiscussionApiModel
import com.instructure.dataseeding.model.EnrollmentApiModel
import com.instructure.dataseeding.model.FavoriteApiModel
import com.instructure.dataseeding.model.ModuleApiModel

// Data-seeding API
object SeedApi {

    // Seed data request
    data class SeedDataRequest (
        val teachers: Int = 0,
        val students: Int = 0,
        val parents: Int = 0,
        val courses: Int = 0,
        val pastCourses: Int = 0,
        val favoriteCourses: Int = 0,
        val homeroomCourses: Int = 0,
        val accountId: Long? = null,
        val gradingPeriods: Boolean = false,
        val discussions: Int = 0,
        val announcements: Int = 0,
        val locked: Boolean = false,
        val publishCourses: Boolean = true,
        val TAs: Int = 0,
        val syllabusBody: String? = null,
        val modules: Int = 0
    )

    // Seed data object/model, made to look very much like the old proto-generated SeededData class
    class SeededDataApiModel {
        // lists of students, teachers, courses, etc...
        val teachersList = mutableListOf<CanvasUserApiModel>()
        val taList = mutableListOf<CanvasUserApiModel>()
        val studentsList = mutableListOf<CanvasUserApiModel>()
        val parentsList = mutableListOf<CanvasUserApiModel>()
        val coursesList = mutableListOf<CourseApiModel>()
        val enrollmentsList = mutableListOf<EnrollmentApiModel>()
        val favoriteCoursesList = mutableListOf<FavoriteApiModel>()
        val discussionsList = mutableListOf<DiscussionApiModel>()
        val announcementsList = mutableListOf<DiscussionApiModel>()
        val modulesList = mutableListOf<ModuleApiModel>()

        // Methods to add elements to lists
        fun addTeachers(teacher: CanvasUserApiModel) {
            teachersList.add(teacher)
        }
        fun addTAs(ta: CanvasUserApiModel) {
            taList.add(ta)
        }
        fun addStudents(student: CanvasUserApiModel) {
            studentsList.add(student)
        }
        fun addParents(parent: CanvasUserApiModel) {
            parentsList.add(parent)
        }
        fun addCourses(course: CourseApiModel) {
            coursesList.add(course)
        }
        fun addEnrollments(enrollment: EnrollmentApiModel) {
            enrollmentsList.add(enrollment)
        }
        fun addFavoriteCourses(favoriteCourse: FavoriteApiModel) {
            favoriteCoursesList.add(favoriteCourse)
        }
        fun addDiscussions(discussion: DiscussionApiModel) {
            discussionsList.add(discussion)
        }
        fun addAnnouncements(announcement: DiscussionApiModel) {
            announcementsList.add(announcement)
        }

        // Methods to import collections into our lists
        fun addAllFavorites(favorites: Iterable<FavoriteApiModel>) {
            favoriteCoursesList.addAll(favorites)
        }
        fun addAllDiscussions(discussions: Iterable<DiscussionApiModel>) {
            discussionsList.addAll(discussions)
        }
        fun addAllAnnouncements(announcements: Iterable<DiscussionApiModel>) {
            announcementsList.addAll(announcements)
        }
        fun addAllModules(modules: Iterable<ModuleApiModel>) {
            modulesList.addAll(modules)
        }

    }

    // Seed data from a SeedDataRequest, return SeededDataApiModel
    fun seedDataForSubAccount(request: SeedDataRequest) : SeededDataApiModel {
        val seededData = SeededDataApiModel()

        with(seededData) {
            var homeroomCourses = request.homeroomCourses
            var homeroomCoursesCountForAnnouncements = request.homeroomCourses
            for (c in 0 until maxOf(request.courses + request.pastCourses, request.favoriteCourses, request.homeroomCourses)) {
                // Seed course
                if(homeroomCourses > 0) addCourses(createCourse(request.gradingPeriods, request.publishCourses, true, request.accountId, request.syllabusBody))
                else addCourses(createCourse(request.gradingPeriods, request.publishCourses, false, request.accountId, request.syllabusBody))

                // Seed users
                for (t in 0 until request.teachers) {
                    addTeachers(UserApi.createCanvasUser())
                    addEnrollments(EnrollmentsApi.enrollUserAsTeacher(coursesList[c].id, teachersList[t].id))
                }

                for (s in 0 until request.students) {
                    addStudents(UserApi.createCanvasUser())
                    addEnrollments(EnrollmentsApi.enrollUserAsStudent(coursesList[c].id, studentsList[s].id))
                }

                for (ta in 0 until request.TAs) {
                    addTAs(UserApi.createCanvasUser())
                    addEnrollments(EnrollmentsApi.enrollUserAsTA(coursesList[c].id, taList[ta].id))
                }
                homeroomCourses--
            }

            // Make the last x courses concluded to keep the first ones as favorites
            for (c in coursesList.size - request.pastCourses until coursesList.size) {
                CoursesApi.concludeCourse(coursesList[c].id)
            }

            // Seed favorite courses for all students
            (0 until minOf(request.favoriteCourses, coursesList.size)).forEach { courseIndex ->
                studentsList.forEach { student ->
                    {
                        coursesList[courseIndex].isFavorite = true
                        addFavoriteCourses(
                            CoursesApi.addCourseToFavorites(
                                coursesList[courseIndex].id,
                                student.token
                            )
                        )
                    }
                }
            }

            // Seed discussions
            addAllDiscussions(
                (0 until request.discussions).map {
                    DiscussionTopicsApi.createDiscussion(coursesList[0].id, teachersList[0].token)
                }
            )

            // Seed announcements
            if(homeroomCoursesCountForAnnouncements > 0) {
                addAllAnnouncements(
                    (0 until homeroomCoursesCountForAnnouncements).map {
                        DiscussionTopicsApi.createAnnouncement(coursesList[it].id, teachersList[0].token)
                    }
                )
            }
            addAllAnnouncements(
                (homeroomCoursesCountForAnnouncements until request.announcements).map {
                    DiscussionTopicsApi.createAnnouncement(coursesList[it].id, teachersList[0].token)
                }
            )
        }

        return seededData
    }


    // Seed data from a SeedDataRequest, return SeededDataApiModel
    fun seedData(request: SeedDataRequest) : SeededDataApiModel {
        val seededData = SeededDataApiModel()

        with(seededData) {
            for (c in 0 until maxOf(request.courses + request.pastCourses, request.favoriteCourses)) {
                // Seed course
                addCourses(createCourse(request.gradingPeriods, request.publishCourses, syllabusBody = request.syllabusBody))

                // Seed users
                for (t in 0 until request.teachers) {
                    addTeachers(UserApi.createCanvasUser())
                    addEnrollments(EnrollmentsApi.enrollUserAsTeacher(coursesList[c].id, teachersList[t].id))
                }

                for (s in 0 until request.students) {
                    addStudents(UserApi.createCanvasUser())
                    addEnrollments(EnrollmentsApi.enrollUserAsStudent(coursesList[c].id, studentsList[s].id))
                }

                for (p in 0 until request.parents) {
                    addParents(UserApi.createCanvasUser())
                    studentsList.forEach { student ->
                        addEnrollments(EnrollmentsApi.enrollUserAsObserver(coursesList[c].id, parentsList[p].id, student.id))
                    }
                }

                for (ta in 0 until request.TAs) {
                    addTAs(UserApi.createCanvasUser())
                    addEnrollments(EnrollmentsApi.enrollUserAsTA(coursesList[c].id, taList[ta].id))
                }
            }

            // Make the last x courses concluded to keep the first ones as favorites
            for (c in coursesList.size - request.pastCourses until coursesList.size) {
                CoursesApi.concludeCourse(coursesList[c].id)
            }

            // Seed favorite courses for all students
            (0 until minOf(request.favoriteCourses, coursesList.size)).forEach { courseIndex ->
                studentsList.forEach { student ->
                    addFavoriteCourses(CoursesApi.addCourseToFavorites(coursesList[courseIndex].id, student.token))
                }
            }

            // Seed discussions
            addAllDiscussions(
                    (0 until request.discussions).map {
                        DiscussionTopicsApi.createDiscussion(coursesList[0].id, teachersList[0].token)
                    }
            )

            // Seed announcements
            if(request.locked) {
                addAllAnnouncements(
                    (0 until request.announcements).map {
                        DiscussionTopicsApi.createAnnouncement(coursesList[0].id, teachersList[0].token, locked = true)
                    }
                )
            }
            else {
                addAllAnnouncements(
                    (0 until request.announcements).map {
                        DiscussionTopicsApi.createAnnouncement(
                            coursesList[0].id,
                            teachersList[0].token
                        )
                    }
                )
            }

            // Seed modules
            addAllModules(
                (0 until request.modules).map {
                    ModulesApi.createModule(coursesList[0].id, teachersList[0].token)
                }
            )
        }

        return seededData
    }

    // Seed parent data request
    data class SeedParentDataRequest (
            val courses: Int = 0,
            val students: Int = 0,
            val parents: Int = 0
    )

    // Seeded parent data object/model, made to look very much like the old SeededParentData proto-generated model.
    class SeededParentDataApiModel {
        val coursesList = mutableListOf<CourseApiModel>()
        val studentsList = mutableListOf<CanvasUserApiModel>()
        val parentsList = mutableListOf<CanvasUserApiModel>()
        val enrollmentsList = mutableListOf<EnrollmentApiModel>()

        fun addCourses(course: CourseApiModel) {
            coursesList.add(course)
        }
        fun addStudents(student: CanvasUserApiModel) {
            studentsList.add(student)
        }
        fun addParents(parent: CanvasUserApiModel) {
            parentsList.add(parent)
        }
        fun addEnrollments(enrollment: EnrollmentApiModel) {
            enrollmentsList.add(enrollment)
        }
    }

    // Seed parent data from a SeedParentDataRequest, return a SeededParentDataApiModel
    fun seedParentData(request: SeedParentDataRequest ) : SeededParentDataApiModel {
        val seededData = SeededParentDataApiModel()

        with(seededData) {
            for (c in 0 until request.courses) {
                // Seed course
                addCourses(createCourse())

                // Seed users
                for (s in 0 until request.students) {
                    addStudents(UserApi.createCanvasUser())
                    addEnrollments(EnrollmentsApi.enrollUserAsStudent(coursesList[c].id, studentsList[s].id) )
                }

                // Seed parents
                for (t in 0 until request.parents) {
                    addParents(UserApi.createCanvasUser())
                    studentsList.forEach { student ->
                        addEnrollments(EnrollmentsApi.enrollUserAsObserver(coursesList[c].id, parentsList[t].id, student.id))
                    }
                }
            }

        }

        return seededData
    }

    // Private course-creation method that does some special handling for grading periods
    private fun createCourse(gradingPeriods: Boolean = false, publishCourses: Boolean = true, isHomeroomCourse: Boolean = false, accountId: Long? = null, syllabusBody: String? = null) : CourseApiModel {
        return if(gradingPeriods) {
            val enrollmentTerm = EnrollmentTermsApi.createEnrollmentTerm()
            val gradingPeriodSetWrapper = GradingPeriodsApi.createGradingPeriodSet(enrollmentTerm.id)
            val gradingPeriodSet = GradingPeriodsApi.createGradingPeriod(gradingPeriodSetWrapper.gradingPeriodSet.id)
            val courseWithTerm = createCourseWithSubAccountCondition(publishCourses, isHomeroomCourse, accountId, enrollmentTerm.id, syllabusBody)
            courseWithTerm
        } else {
            val course = createCourseWithSubAccountCondition(publishCourses, isHomeroomCourse, accountId, syllabusBody = syllabusBody)
            course
        }
    }

    private fun createCourseWithSubAccountCondition(publishCourses: Boolean = false, isHomeroomCourse: Boolean = false, accountId: Long? = null, enrollmentTermId: Long? = null, syllabusBody: String? = null): CourseApiModel {
        return if (accountId != null) {
            CoursesApi.createCourseInSubAccount(accountId = accountId, homeroomCourse = isHomeroomCourse, enrollmentTermId = enrollmentTermId, publish = publishCourses, syllabusBody = syllabusBody)
        } else {
            CoursesApi.createCourse(enrollmentTermId, publishCourses, syllabusBody = syllabusBody)
        }
    }
}