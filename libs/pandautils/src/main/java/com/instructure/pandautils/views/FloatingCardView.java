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

package com.instructure.pandautils.views;

import android.annotation.TargetApi;
import android.content.Context;
import androidx.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.instructure.pandautils.R;

public abstract class FloatingCardView extends FrameLayout {

    public abstract int backgroundResId();

    private View mRootView;

    public FloatingCardView(Context context) {
        super(context);
        init(context);
    }

    public FloatingCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public FloatingCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @TargetApi(21)
    public FloatingCardView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        mRootView = inflate(context, R.layout.floating_card_view, null);
        mRootView.setBackgroundResource(backgroundResId());
        addView(mRootView);
    }

    @Override
    public void addView(@NonNull View child, int index, ViewGroup.LayoutParams params) {
        // Allows for subviews to be specified in xml. All subviews are added to the CardView
        if (child == mRootView) {
            super.addView(child, index, params);
        } else {
            ViewGroup cardView = (ViewGroup)mRootView.findViewById(R.id.cardView);
            cardView.addView(child);
        }
    }
}
