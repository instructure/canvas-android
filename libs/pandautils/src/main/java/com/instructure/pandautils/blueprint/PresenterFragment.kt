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
package com.instructure.pandautils.blueprint

import android.os.Bundle
import com.instructure.pandautils.base.BaseCanvasFragment
import androidx.loader.app.LoaderManager
import androidx.loader.content.Loader
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

abstract class PresenterFragment<PRESENTER : FragmentPresenter<VIEW>, VIEW : FragmentViewInterface> : BaseCanvasFragment() {

    protected abstract fun onReadySetGo(presenter: PRESENTER)

    protected abstract fun getPresenterFactory(): PresenterFactory<VIEW, PRESENTER>

    protected abstract fun onPresenterPrepared(presenter: PRESENTER)

    private var delivered = false

    lateinit var presenter: PRESENTER
        private set

    @Suppress("DEPRECATION")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        loaderManager.initLoader(
            LOADER_ID,
            null,
            object : LoaderManager.LoaderCallbacks<PRESENTER> {
                override fun onCreateLoader(id: Int, args: Bundle?): Loader<PRESENTER> {
                    return PresenterLoader(requireContext(), getPresenterFactory())
                }

                override fun onLoadFinished(
                    loader: Loader<PRESENTER>,
                    presenter: PRESENTER
                ) {
                    if (!delivered) {
                        this@PresenterFragment.presenter = presenter
                        delivered = true
                        onPresenterPrepared(presenter)
                    }
                }

                override fun onLoaderReset(loader: Loader<PRESENTER>) {
                    //presenter = null
                    onPresenterDestroyed()
                }
            })
    }


    @Suppress("UNCHECKED_CAST")
    override fun onResume() {
        super.onResume()
        onReadySetGo(presenter.onViewAttached(presenterView) as PRESENTER)
    }

    override fun onPause() {
        presenter.onViewDetached()
        super.onPause()
    }

    protected fun onPresenterDestroyed() {
        // hook for subclasses
    }

    // Override in case of fragment no implementing Presenter<VIEW> interface
    @Suppress("UNCHECKED_CAST")
    private val presenterView: VIEW
        get() = this as VIEW


    protected fun addSwipeToRefresh(swipeRefreshLayout: SwipeRefreshLayout) {
        swipeRefreshLayout.setOnRefreshListener { presenter.refresh(true) }
    }

    companion object {
        private const val LOADER_ID = 1003
    }
}
