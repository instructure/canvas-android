package com.instructure.student.features.bookmarks

import android.content.res.Resources
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.managers.BookmarkManager
import com.instructure.canvasapi2.models.Bookmark
import com.instructure.pandautils.mvvm.Event
import com.instructure.pandautils.mvvm.ViewState
import com.instructure.pandautils.utils.textAndIconColor
import com.instructure.student.R
import com.instructure.student.features.bookmarks.itemviewmodels.BookmarkItemViewModel
import com.instructure.student.router.RouteMatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookmarksViewModel @Inject constructor(
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

    init {
        _state.postValue(ViewState.Loading)
        loadData()
    }

    fun loadData(forceNetwork: Boolean = false) {
        viewModelScope.launch {
            try {
                val bookmarks = bookmarkManager.getBookmarksAsync(forceNetwork).await().dataOrThrow
                bookmarkMap = bookmarks.associateBy { it.id }
                val items = bookmarks
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
            try {
                bookmarkManager.deleteBookmarkAsync(id).await().dataOrThrow
                val newItems = _data.value?.items?.filterNot { it.data.id == id} ?: emptyList()
                _data.value = BookmarksViewData(newItems)
                _events.postValue(Event(BookmarksAction.ShowSnackbar(resources.getString(R.string.bookmarkDeleted))))
            } catch (e: Exception) {
                _events.postValue(Event(BookmarksAction.ShowSnackbar(resources.getString(R.string.errorOccurred))))
            }
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
}