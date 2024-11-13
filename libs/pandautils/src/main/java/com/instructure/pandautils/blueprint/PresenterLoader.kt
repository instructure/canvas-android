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

import android.content.Context
import androidx.loader.content.Loader

class PresenterLoader<VIEW, PRESENTER : Presenter<VIEW>>(
    context: Context,
    private val factory: PresenterFactory<VIEW, PRESENTER>
) : Loader<PRESENTER>(context) {

    private var presenter: PRESENTER? = null

    override fun onStartLoading() {
        // if we already own a presenter instance, simply deliver it.
        if (presenter != null) {
            deliverResult(presenter)
            return
        }

        // Otherwise, force a load
        forceLoad()
    }

    override fun onForceLoad() {
        // Create the Presenter using the Factory
        presenter = factory.create()

        // Deliver the result
        deliverResult(presenter)
    }

    override fun onStopLoading() {}

    override fun onReset() {
        presenter?.onDestroyed()
        presenter = null
    }

}
