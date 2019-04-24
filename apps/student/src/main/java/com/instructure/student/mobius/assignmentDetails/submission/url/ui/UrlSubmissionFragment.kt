/*
 * Copyright (C) 2019 - present Instructure, Inc.
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
 */
package com.instructure.student.mobius.assignmentDetails.submission.url.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import com.instructure.student.mobius.assignmentDetails.submission.url.*
import com.instructure.student.mobius.common.ui.EffectHandler
import com.instructure.student.mobius.common.ui.MobiusFragment
import com.instructure.student.mobius.common.ui.Presenter
import com.instructure.student.mobius.common.ui.UpdateInit

class UrlSubmissionFragment : MobiusFragment<UrlSubmissionModel, UrlSubmissionEvent, UrlSubmissionEffect, UrlSubmissionView, UrlSubmissionViewState>() {
    override fun makeEffectHandler(): EffectHandler<UrlSubmissionView, UrlSubmissionEvent, UrlSubmissionEffect> = UrlSubmissionEffectHandler()

    override fun makeUpdate(): UpdateInit<UrlSubmissionModel, UrlSubmissionEvent, UrlSubmissionEffect> = UrlSubmissionUpdate()

    override fun makeView(inflater: LayoutInflater, parent: ViewGroup): UrlSubmissionView {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun makePresenter(): Presenter<UrlSubmissionModel, UrlSubmissionViewState> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun makeInitModel(): UrlSubmissionModel {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}