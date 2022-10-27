package com.instructure.student.features.dashboard.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.interactions.router.Route
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.NullableParcelableArg
import com.instructure.pandautils.utils.makeBundle
import com.instructure.student.R
import com.instructure.student.databinding.FragmentNewDashboardBinding
import com.instructure.student.fragment.CourseBrowserFragment
import com.instructure.student.fragment.ParentFragment
import com.instructure.student.router.RouteMatcher
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_new_dashboard.*

@AndroidEntryPoint
class NewDashboardFragment : ParentFragment() {

    private var canvasContext: CanvasContext? by NullableParcelableArg(key = Const.CANVAS_CONTEXT)

    private val viewModel: DashboardViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentNewDashboardBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun title(): String = if (isAdded) getString(R.string.dashboard) else ""

    override fun applyTheme() {
        toolbar.title = title()
        // Styling done in attachNavigationDrawer
        navigation?.attachNavigationDrawer(this, toolbar)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.loadData()

        viewModel.events.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let {
                handleActions(it)
            }
        }
    }

    private fun handleActions(action: DashboardAction) {
        when (action) {
            is DashboardAction.OpenCourse -> {
                RouteMatcher.route(requireContext(), CourseBrowserFragment.makeRoute(action.course))
            }
        }
    }

    companion object {
        fun newInstance(route: Route) =
            NewDashboardFragment().apply {
                arguments = route.canvasContext?.makeBundle(route.arguments) ?: route.arguments
            }

        fun makeRoute(canvasContext: CanvasContext?) = Route(NewDashboardFragment::class.java, canvasContext)
    }

}