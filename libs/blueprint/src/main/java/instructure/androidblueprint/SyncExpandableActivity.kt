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
import com.instructure.pandarecycler.util.GroupSortedList

abstract class SyncExpandableActivity<
        GROUP,
        MODEL : CanvasComparable<*>,
        VIEW : SyncExpandableManager<GROUP, MODEL>,
        PRESENTER : SyncExpandablePresenter<GROUP, MODEL, VIEW>,
        HOLDER : RecyclerView.ViewHolder,
        ADAPTER : SyncExpandableRecyclerAdapter<GROUP, MODEL, HOLDER, VIEW>> : AppCompatActivity() {

    protected abstract fun onReadySetGo(presenter: PRESENTER)

    protected abstract fun getPresenterFactory(): PresenterFactory<VIEW, PRESENTER>

    protected abstract fun onPresenterPrepared(presenter: PRESENTER)

    protected abstract fun createAdapter(): ADAPTER

    protected open val adapter: ADAPTER by lazy { createAdapter() }

    protected abstract fun perPageCount(): Int

    private fun hitRockBottom() {}

    protected abstract val recyclerView: RecyclerView

    // boolean flag to avoid delivering the result twice.
    private var delivered = false

    var presenter: PRESENTER? = null
        private set

    @Suppress("DEPRECATION")
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // LoaderCallbacks as an object, so no hint regarding loader will be leak to the subclasses.
        supportLoaderManager.initLoader(
            LOADER_ID,
            null,
            object : LoaderManager.LoaderCallbacks<PRESENTER> {
                override fun onCreateLoader(
                    id: Int,
                    args: Bundle?
                ): Loader<PRESENTER> {
                    return PresenterLoader(this@SyncExpandableActivity, getPresenterFactory())
                }

                override fun onLoadFinished(
                    loader: Loader<PRESENTER>,
                    presenter: PRESENTER
                ) {
                    if (!delivered) {
                        this@SyncExpandableActivity.presenter = presenter
                        delivered = true
                        onPresenterPrepared(presenter)
                    }
                }

                override fun onLoaderReset(loader: Loader<PRESENTER>) {
                    presenter = null
                    onPresenterDestroyed()
                }
            })
    }

    @Suppress("UNCHECKED_CAST")
    public override fun onStart() {
        super.onStart()
        onReadySetGo(presenter!!.onViewAttached(presenterView) as PRESENTER)
    }

    public override fun onStop() {
        presenter!!.onViewDetached()
        super.onStop()
    }

    protected fun onPresenterDestroyed() {
        // hook for subclasses
    }

    fun withPagination(): Boolean {
        return true
    }

    // Override in case of fragment not implementing Presenter<View> interface
    @Suppress("UNCHECKED_CAST")
    private val presenterView: VIEW
        get() = this as VIEW

    private fun addPagination() {
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

    val list: GroupSortedList<GROUP, MODEL>
        get() = presenter!!.data

    companion object {
        private const val LOADER_ID = 1002
    }
}
