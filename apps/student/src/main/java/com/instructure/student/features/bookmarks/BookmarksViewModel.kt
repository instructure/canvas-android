package com.instructure.student.features.bookmarks

import android.content.res.Resources
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.managers.BookmarkManager
import com.instructure.canvasapi2.managers.CourseManager
import com.instructure.canvasapi2.managers.GroupManager
import com.instructure.canvasapi2.models.Bookmark
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Group
import com.instructure.pandautils.mvvm.Event
import com.instructure.pandautils.mvvm.ItemViewModel
import com.instructure.pandautils.mvvm.ViewState
import com.instructure.pandautils.utils.isCourse
import com.instructure.pandautils.utils.isGroup
import com.instructure.pandautils.utils.textAndIconColor
import com.instructure.student.R
import com.instructure.student.features.bookmarks.itemviewmodels.BookmarkGroupItemViewModel
import com.instructure.student.features.bookmarks.itemviewmodels.BookmarkItemViewModel
import com.instructure.student.router.RouteMatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookmarksViewModel @Inject constructor(
    private val courseManager: CourseManager,
    private val groupManager: GroupManager,
    private val bookmarkManager: BookmarkManager,
    private val resources: Resources
) : ViewModel() {

    val state: LiveData<ViewState>
        get() = _state
    private val _state = MutableLiveData<ViewState>()

    val data: LiveData<BookmarksViewData>
        get() = _data
    private val _data = MutableLiveData<BookmarksViewData>()

    val events: LiveData<Event<BookmarksAction>>
        get() = _events
    private val _events = MutableLiveData<Event<BookmarksAction>>()

    private var bookmarkMap: Map<Long, Bookmark> = emptyMap()
    private var courseMap: Map<Long, Course> = emptyMap()
    private var groupMap: Map<Long, Group> = emptyMap()

    private var grouping = BookmarkGrouping.CONTEXT

    init {
        _state.postValue(ViewState.Loading)
        loadData()
    }

    fun loadData(forceNetwork: Boolean = false) {
        viewModelScope.launch {
            try {
                val bookmarks = bookmarkManager.getBookmarksAsync(forceNetwork).await().dataOrThrow
                bookmarkMap = bookmarks.associateBy { it.id }

                courseMap = courseManager.getCoursesAsync(forceNetwork).await().dataOrThrow
                    .associateBy { it.id }

                groupMap = groupManager.getAllGroupsAsync(forceNetwork).await().dataOrThrow
                    .associateBy { it.id }

                val items = createContextGroups()

                _data.postValue(BookmarksViewData(items))
                _state.postValue(ViewState.Success)
            } catch (e: Exception) {
                _state.postValue(ViewState.Error())
            }

        }
    }

    fun editBookmarkClicked(id: Long) {
        TODO("Not yet implemented")
    }

    fun deleteBookmarkClicked(id: Long) {
        bookmarkMap[id]?.let {
            _events.postValue(Event(BookmarksAction.ShowDeleteConfirmation(it)))
        } ?: _events.postValue(Event(BookmarksAction.ShowSnackbar(resources.getString(R.string.errorOccurred))))
    }

    fun deleteBookmark(id: Long) {
        viewModelScope.launch {
//            try {
//                bookmarkManager.deleteBookmarkAsync(id).await().dataOrThrow
//                val newItems = _data.value?.items?.filterNot { it.data.id == id } ?: emptyList()
//                _data.value = BookmarksViewData(newItems)
//                _events.postValue(Event(BookmarksAction.ShowSnackbar(resources.getString(R.string.bookmarkDeleted))))
//            } catch (e: Exception) {
//                _events.postValue(Event(BookmarksAction.ShowSnackbar(resources.getString(R.string.errorOccurred))))
//            }
        }
    }

    fun createShortcut(id: Long) {
        bookmarkMap[id]?.let {
            _events.postValue(Event(BookmarksAction.CreateShortcut(it)))
        } ?: _events.postValue(Event(BookmarksAction.ShowSnackbar(resources.getString(R.string.errorOccurred))))
    }

    fun refresh() {
        _state.postValue(ViewState.Refresh)
        loadData(true)
    }

    fun changeGrouping(grouping: BookmarkGrouping) {
        this.grouping = grouping
        when (this.grouping) {
            BookmarkGrouping.CONTEXT -> {
                createContextGroups()
            }
            BookmarkGrouping.LABELS -> {
                createLabelGroups()
            }
        }
    }

    private fun createLabelGroups() {
        TODO("Not yet implemented")
    }

    private fun createContextGroups(): List<ItemViewModel> {
        val contextMap = mutableMapOf<String?, MutableList<Bookmark>>()
        bookmarkMap.values.forEach {
            val context = RouteMatcher.getContextFromUrl(it.url)
            val contextName = if (context?.isCourse == true) {
                courseMap[context.id]?.name ?: resources.getString(R.string.other)
            } else if (context?.isGroup == true) {
                groupMap[context.id]?.name ?: resources.getString(R.string.other)
            } else {
                resources.getString(R.string.other)
            }

            if (contextMap.containsKey(contextName)) {
                contextMap[contextName]?.add(it)
            } else {
                contextMap[contextName] = mutableListOf(it)
            }
        }

        return contextMap.map {
            createContextGroup(it.key, it.value)
        }
    }

    private fun createContextGroup(groupName: String?, items: List<Bookmark>): BookmarkGroupItemViewModel {
        return BookmarkGroupItemViewModel(
            BookmarkGroupViewData(groupName ?: resources.getString(R.string.other)),
            createBookmarkItems(items)
        )
    }

    private fun createBookmarkItems(items: List<Bookmark>): List<BookmarkItemViewModel> {
        return items
            .map {
                BookmarkViewData(
                    it.id,
                    it.name,
                    it.url,
                    RouteMatcher.getContextFromUrl(it.url).textAndIconColor
                )
            }
            .map {
                BookmarkItemViewModel(it, openPopupHandler = { view ->
                    _events.postValue(Event(BookmarksAction.OpenPopup(view, it.id)))
                })
            }
    }
}