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
import instructure.androidblueprint.BaseCanvasActivity
import androidx.loader.app.LoaderManager
import androidx.loader.content.Loader

/**
 * The Presenter will decide what to do for every incoming event,
 * retrieving or updating data from the Model and preparing the data so the View can display it.
 */
abstract class PresenterActivity<PRESENTER : Presenter<VIEW>, VIEW> : BaseCanvasActivity() {

    /**
     * This point in time we have a presenter prepared and the view interface has been initialized
     * We are ready to go go go
     * The same as setting views in onStart() but also provides a Presenter
     * @param presenter An implementation of the Presenter interface
     */
    protected abstract fun onReadySetGo(presenter: PRESENTER)

    protected abstract fun getPresenterFactory(): PresenterFactory<VIEW, PRESENTER>

    protected abstract fun onPresenterPrepared(presenter: PRESENTER)

    var presenter: PRESENTER? = null
        private set

    /* We normally invoke onReadySetGo() as part of onStart(), but this requires a valid presenter which not available
    until LoaderCallback.onLoadFinished() is called. Because the order of onStart() and onLoadFinished() is not
    guaranteed, we need to track the case where onStart() is called first so we can ensure that onReadySetGo()
    is invoked as soon as the presenter is ready. */
    private var readySetGoSkipped = false
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LoaderManager.getInstance(this).initLoader(
            LOADER_ID,
            null,
            object : LoaderManager.LoaderCallbacks<PRESENTER> {
                override fun onCreateLoader(id: Int, args: Bundle?): Loader<PRESENTER> {
                    return PresenterLoader(this@PresenterActivity, getPresenterFactory())
                }

                @Suppress("UNCHECKED_CAST")
                override fun onLoadFinished(loader: Loader<PRESENTER>, presenter: PRESENTER) {
                    this@PresenterActivity.presenter = presenter
                    intent?.extras?.let { unBundle(it) }
                    onPresenterPrepared(presenter)
                    if (readySetGoSkipped) {
                        // Make sure to invoke onReadySetGo() if it had to be skipped in onStart()
                        onReadySetGo(presenter.onViewAttached(presenterView) as PRESENTER)
                        readySetGoSkipped = false
                    }
                }

                override fun onLoaderReset(loader: Loader<PRESENTER>) {
                    presenter = null
                    onPresenterDestroyed()
                }
            })
    }

    @Suppress("UNCHECKED_CAST")
    override fun onStart() {
        super.onStart()
        if (presenter != null) {
            onReadySetGo(presenter!!.onViewAttached(presenterView) as PRESENTER)
        } else {
            readySetGoSkipped = true
        }
    }

    override fun onStop() {
        presenter?.onViewDetached()
        super.onStop()
    }

    protected fun onPresenterDestroyed() {
        // hook for subclasses
    }

    // Override in case of Activity not implementing Presenter<View> interface
    @Suppress("UNCHECKED_CAST")
    protected val presenterView: VIEW
        get() = this as VIEW

    /**
     * Only gets called if extras is not null
     * @param extras A non-null group of extras in Bundle form
     */
    protected open fun unBundle(extras: Bundle) {}

    companion object {
        private const val LOADER_ID = 1001
    }
}
