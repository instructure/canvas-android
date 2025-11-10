package com.instructure.pandautils.features.todolist

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.utils.pageview.PageView
import com.instructure.interactions.FragmentInteractions
import com.instructure.interactions.Navigation
import com.instructure.interactions.router.Route
import com.instructure.pandautils.R
import com.instructure.pandautils.analytics.SCREEN_VIEW_TO_DO_LIST
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.base.BaseCanvasFragment
import com.instructure.pandautils.compose.CanvasTheme
import com.instructure.pandautils.interfaces.NavigationCallbacks
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.makeBundle
import com.instructure.pandautils.utils.toLocalDate
import com.instructure.pandautils.utils.withArgs
import dagger.hilt.android.AndroidEntryPoint
import org.threeten.bp.format.DateTimeFormatter
import javax.inject.Inject

@PageView
@ScreenView(SCREEN_VIEW_TO_DO_LIST)
@AndroidEntryPoint
class ToDoListFragment : BaseCanvasFragment(), FragmentInteractions, NavigationCallbacks {

    @Inject
    lateinit var toDoListRouter: ToDoListRouter

    private var onToDoCountChanged: OnToDoCountChanged? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        onToDoCountChanged = context as? OnToDoCountChanged
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        applyTheme()

        return ComposeView(requireActivity()).apply {
            setContent {
                CanvasTheme {
                    ToDoListScreen(
                        navigationIconClick = {
                            toDoListRouter.openNavigationDrawer()
                        },
                        openToDoItem = { itemId ->
                            toDoListRouter.openToDoItem(itemId)
                        },
                        onToDoCountChanged = { count ->
                            onToDoCountChanged?.onToDoCountChanged(count)
                        },
                        onDateClick = { date ->
                            toDoListRouter.openCalendar(date.toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE))
                        }
                    )
                }
            }
        }
    }

    override val navigation: Navigation?
        get() = activity as? Navigation

    override fun title(): String = getString(R.string.Todo)

    override fun applyTheme() {
        ViewStyler.setStatusBarDark(requireActivity(), ThemePrefs.primaryColor)
        toDoListRouter.attachNavigationDrawer()
    }

    override fun getFragment(): Fragment = this

    override fun onHandleBackPressed(): Boolean {
        return false
    }

    companion object {
        fun makeRoute(canvasContext: CanvasContext): Route = Route(ToDoListFragment::class.java, canvasContext, Bundle())

        private fun validateRoute(route: Route) = route.canvasContext != null

        fun newInstance(route: Route): ToDoListFragment? {
            if (!validateRoute(route)) return null
            return ToDoListFragment().withArgs(route.canvasContext!!.makeBundle())
        }
    }
}