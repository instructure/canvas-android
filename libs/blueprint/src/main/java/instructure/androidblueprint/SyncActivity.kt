/*
 * Copyright (C) 2017 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */
package instructure.androidblueprint

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.loader.app.LoaderManager
import androidx.loader.content.Loader
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.instructure.canvasapi2.models.CanvasComparable
import com.instructure.pandarecycler.PaginatedScrollListener
import com.instructure.pandarecycler.PaginatedScrollListener.PaginatedScrollCallback
import com.instructure.pandarecycler.util.UpdatableSortedList

abstract class SyncActivity<
        MODEL : CanvasComparable<*>,
        PRESENTER : SyncPresenter<MODEL, VIEW>,
        VIEW : SyncManager<MODEL>,
        HOLDER : RecyclerView.ViewHolder,
        ADAPTER : SyncRecyclerAdapter<MODEL, HOLDER, VIEW>> : AppCompatActivity() {

    protected abstract fun onReadySetGo(presenter: PRESENTER)

    protected abstract fun getPresenterFactory(): PresenterFactory<VIEW, PRESENTER>

    protected abstract fun onPresenterPrepared(presenter: PRESENTER)

    protected abstract fun createAdapter(): ADAPTER

    protected open val adapter: ADAPTER by lazy { createAdapter() }

    protected abstract fun perPageCount(): Int

    protected abstract val recyclerView: RecyclerView

    protected fun hitRockBottom() {}

    // boolean flag to avoid delivering the result twice.
    private var delivered = false

    var presenter: PRESENTER? = null
        private set

    /* We normally invoke onReadySetGo() as part of onStart(), but this requires a valid presenter which not available
    until LoaderCallback.onLoadFinished() is called. Because the order of onStart() and onLoadFinished() is not
    guaranteed, we need to track the case where onStart() is called first so we can ensure that onReadySetGo()
    is invoked as soon as the presenter is ready. */
    private var readySetGoSkipped = false

    @Suppress("DEPRECATION")
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // LoaderCallbacks as an object, so no hint regarding loader will be leak to the subclasses.
        supportLoaderManager.initLoader(
            LOADER_ID,
            null,
            object : LoaderManager.LoaderCallbacks<PRESENTER> {
                override fun onCreateLoader(id: Int, args: Bundle?): Loader<PRESENTER> {
                    return PresenterLoader(this@SyncActivity, getPresenterFactory())
                }

                @Suppress("UNCHECKED_CAST")
                override fun onLoadFinished(loader: Loader<PRESENTER>, presenter: PRESENTER) {
                    if (!delivered) {
                        this@SyncActivity.presenter = presenter
                        delivered = true
                        onPresenterPrepared(presenter)
                    }
                    if (readySetGoSkipped) {
                        // Make sure to invoke onReadySetGo() if it had to be skipped in onStart()
                        onReadySetGo(presenter.onViewAttached(presenterView) as PRESENTER)
                        readySetGoSkipped = false
                    }
                }

                override fun onLoaderReset(loader: Loader<PRESENTER>) {
                    this@SyncActivity.presenter = null
                    onPresenterDestroyed()
                }
            })
    }

    @Suppress("UNCHECKED_CAST")
    public override fun onStart() {
        super.onStart()
        if (this.presenter != null) {
            onReadySetGo(presenter!!.onViewAttached(presenterView) as PRESENTER)
        } else {
            readySetGoSkipped = true
        }
    }

    public override fun onStop() {
        this.presenter!!.onViewDetached()
        super.onStop()
    }

    protected fun onPresenterDestroyed() {
        // hook for subclasses
    }

    fun withPagination(): Boolean {
        return true
    }

    fun clearAdapter() {
        adapter.clear()
    }

    // Override in case of fragment not implementing Presenter<View> interface
    @Suppress("UNCHECKED_CAST")
    protected val presenterView: VIEW
        get() = this as VIEW

    protected fun addPagination() {
        if (withPagination()) {
            recyclerView.clearOnScrollListeners()
            recyclerView.addOnScrollListener(
                PaginatedScrollListener(
                    PaginatedScrollCallback { hitRockBottom() },
                    perPageCount()
                )
            )
        }
    }

    protected fun addSwipeToRefresh(swipeRefreshLayout: SwipeRefreshLayout) {
        swipeRefreshLayout.setOnRefreshListener {
            addPagination()
            presenter?.refresh(true)
        }
    }

    val list: UpdatableSortedList<MODEL>
        get() = presenter!!.data

    companion object {
        private const val LOADER_ID = 1002
    }
}
