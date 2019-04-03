/*
 * Copyright (C) 2017 - present  Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.instructure.teacher.factory;

import com.instructure.teacher.presenters.ChooseRecipientsPresenter;

import instructure.androidblueprint.PresenterFactory;


public class ChooseRecipientsPresenterFactory implements PresenterFactory<ChooseRecipientsPresenter> {

    private String mRootContextId;

    public ChooseRecipientsPresenterFactory(String rootContextId) {
        mRootContextId = rootContextId;
    }

    @Override
    public ChooseRecipientsPresenter create() {
        return new ChooseRecipientsPresenter(mRootContextId);
    }
}
