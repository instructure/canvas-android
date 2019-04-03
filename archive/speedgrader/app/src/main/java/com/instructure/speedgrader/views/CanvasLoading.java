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

package com.instructure.speedgrader.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.instructure.speedgrader.R;

public class CanvasLoading extends FrameLayout {

    private TextView noConnectionView;
    private View loadingView;

    public CanvasLoading(Context context) {
        super(context);
        setup();
    }

    public CanvasLoading(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup();
    }

    public CanvasLoading(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setup();
    }

    @TargetApi(21)
    public CanvasLoading(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setup();
    }

    private void setup() {
        View container = LayoutInflater.from(getContext()).inflate(R.layout.loading_panda , null);
        noConnectionView = (TextView) container.findViewById(R.id.noConnection);
        container.setVisibility(View.VISIBLE);
        loadingView = container.findViewById(R.id.pandaLoading );
        addView(container);
    }

    public void displayNoConnection(boolean isNoConnection) {
        noConnectionView.setVisibility(isNoConnection ? View.VISIBLE : View.GONE);
        loadingView.setVisibility(isNoConnection ? View.GONE : View.VISIBLE);
    }
}