/*
 * Copyright (C) 2016 - present  Instructure, Inc.
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
 *
 */

package com.instructure.speedgrader.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.instructure.canvasapi.model.CanvasContext;
import com.instructure.canvasapi.model.Submission;
import com.instructure.speedgrader.R;
import com.instructure.speedgrader.util.Const;
import com.instructure.speedgrader.views.HelveticaTextView;

public class EmptyViewFragment extends BaseSubmissionView {

    String messageToUser;

    @Override
    public int getRootLayout() {
        return R.layout.fragment_empty_view;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getBundleData(getArguments());
        View rootView = inflateLayout(inflater, container);

        final HelveticaTextView message = (HelveticaTextView) rootView.findViewById(R.id.emptyMessage);
        message.setText(messageToUser);
        return rootView;
    }

    @Override
    public void setupCallbacks() {}

    @Override
    public void getBundleData(Bundle bundle) {
        super.getBundleData(bundle);
        messageToUser = getString(bundle.getInt(Const.emptyMessage, R.string.noSubmission));
        this.submission = bundle.getParcelable(Const.submission);
    }

    public static Bundle createBundle(CanvasContext canvasContext, Submission submission, int emptyMessageResId){
        Bundle bundle = new Bundle();
        bundle.putParcelable(Const.submission, submission);
        bundle.putParcelable(Const.canvasContext, canvasContext);
        bundle.putInt(Const.emptyMessage, emptyMessageResId);
        return bundle;
    }
}
