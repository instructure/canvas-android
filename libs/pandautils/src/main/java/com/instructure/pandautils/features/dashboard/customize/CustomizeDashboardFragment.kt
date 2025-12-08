package com.instructure.pandautils.features.dashboard.customize

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import com.instructure.interactions.router.Route
import com.instructure.pandautils.compose.CanvasTheme
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.ViewStyler
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CustomizeDashboardFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        ViewStyler.setStatusBarDark(requireActivity(), ThemePrefs.primaryColor)
        return ComposeView(requireContext()).apply {
            setContent {
                CanvasTheme {
                    CustomizeDashboardScreen(
                        onNavigateBack = { requireActivity().onBackPressed() }
                    )
                }
            }
        }
    }

    companion object {
        fun makeRoute() = Route(CustomizeDashboardFragment::class.java, null)

        fun newInstance(route: Route): CustomizeDashboardFragment {
            val fragment = CustomizeDashboardFragment()
            fragment.arguments = route.arguments
            return fragment
        }
    }
}