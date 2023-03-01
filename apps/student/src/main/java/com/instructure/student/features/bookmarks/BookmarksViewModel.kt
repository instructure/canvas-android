package com.instructure.student.features.bookmarks

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.managers.BookmarkManager
import com.instructure.pandautils.mvvm.Event
import com.instructure.pandautils.mvvm.ViewState
import com.instructure.pandautils.utils.textAndIconColor
import com.instructure.student.features.bookmarks.itemviewmodels.BookmarkItemViewModel
import com.instructure.student.router.RouteMatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookmarksViewModel @Inject constructor(
    private val bookmarkManager: BookmarkManager
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

    init {
        loadData()
    }

    fun loadData(forceNetwork: Boolean = false) {
        _state.postValue(ViewState.Loading)
        viewModelScope.launch {
            try {
                val items = bookmarkManager.getBookmarksAsync(forceNetwork).await().dataOrThrow
                    .map { BookmarkViewData(it.name, it.url, RouteMatcher.getContextFromUrl(it.url).textAndIconColor) }
                    .map { BookmarkItemViewModel(it) }

                _data.postValue(BookmarksViewData(items))
                _state.postValue(ViewState.Success)
            } catch (e: Exception) {
                _state.postValue(ViewState.Error())
            }

        }
    }
}