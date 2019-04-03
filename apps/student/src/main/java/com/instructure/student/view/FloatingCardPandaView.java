/*
 * Copyright (C) 2016 - present Instructure, Inc.
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

package com.instructure.student.view;

import android.content.Context;
import androidx.cardview.widget.CardView;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.instructure.student.R;

public class FloatingCardPandaView extends FrameLayout {
    private View mRootView;

    public FloatingCardPandaView(Context context) {
        super(context);
        init(context);
    }

    public FloatingCardPandaView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public FloatingCardPandaView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mRootView = inflate(context, R.layout.floating_card_panda_view, null);
        addView(mRootView);
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        // Allows for subviews to be specified in xml. All subviews are added to the CardView
        if (child == mRootView) {
            super.addView(child, index, params);
        } else {
            CardView cardView = (CardView)mRootView.findViewById(R.id.cardView);
            cardView.addView(child);
        }
    }
}
