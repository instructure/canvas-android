package com.instructure.student.mobius.syllabus.ui

import com.instructure.canvasapi2.models.Course
import com.instructure.interactions.router.Route
import com.instructure.pandautils.utils.makeBundle
import com.instructure.pandautils.utils.withArgs
import com.instructure.student.mobius.syllabus.SyllabusRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SyllabusRepositoryFragment : SyllabusFragment() {

    @Inject
    lateinit var syllabusRepository: SyllabusRepository

    companion object {

        fun makeRoute(course: Course): Route {
            return Route(null, SyllabusRepositoryFragment::class.java, course, course.makeBundle())
        }

        fun validRoute(route: Route): Boolean {
            return route.canvasContext is Course
        }

        fun newInstance(route: Route): SyllabusRepositoryFragment? {
            if (!validRoute(route)) return null

            return SyllabusRepositoryFragment().withArgs(route.arguments)
        }
    }

    override fun getRepository(): SyllabusRepository = syllabusRepository
}