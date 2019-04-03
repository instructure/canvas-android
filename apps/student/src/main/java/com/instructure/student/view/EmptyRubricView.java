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
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.instructure.student.R;
import com.instructure.student.binders.BaseBinder;
import com.instructure.pandarecycler.interfaces.EmptyViewInterface;

public class EmptyRubricView extends RelativeLayout implements EmptyViewInterface {
    private TextView mCurrentPoints;
    private TextView mCurrentGrade;
    private TextView mNoRubricText;
    private RelativeLayout mLoadingView;
    private LinearLayout mContentView;
    private String noConnectionText;
    private String emptyViewText;
    private boolean isDisplayNoConnection = false;

    public EmptyRubricView(Context context) {
        super(context);
        init();
    }

    public EmptyRubricView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public EmptyRubricView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        inflate(getContext(), R.layout.view_rubric_empty, this);
        mCurrentGrade = (TextView)findViewById(R.id.currentGrade);
        mCurrentPoints = (TextView)findViewById(R.id.currentPoints);
        mNoRubricText = (TextView)findViewById(R.id.noRubricText);
        mLoadingView = (RelativeLayout)findViewById(R.id.loadingContainer);
        mContentView = (LinearLayout) findViewById(R.id.topHeader);
    }

    @Override
    public void setLoading() {
        mContentView.setVisibility(View.GONE);
        mNoRubricText.setVisibility(View.GONE);
        mLoadingView.setVisibility(View.VISIBLE);
    }

    @Override
    public void setDisplayNoConnection(boolean isNoConnection) {
        isDisplayNoConnection = isNoConnection;
    }

    @Override
    public void setListEmpty() {
        if (isDisplayNoConnection) {
            mNoRubricText.setText(noConnectionText);
        } else {
            mNoRubricText.setText(emptyViewText);
        }
        mContentView.setVisibility(View.VISIBLE);
        mNoRubricText.setVisibility(View.VISIBLE);
        mLoadingView.setVisibility(View.GONE);
    }

    public void setColoredDrawable(Drawable d) {
        mNoRubricText.setCompoundDrawablesWithIntrinsicBounds(d, null, null, null);
        mNoRubricText.setCompoundDrawablePadding(10);
    }

    public void setTextViews(String currentPoints, String currentGrade) {
        mCurrentPoints.setText(currentPoints);
        mCurrentGrade.setText(currentGrade);
        BaseBinder.Companion.ifHasTextSetVisibleElseGone(mCurrentPoints);
        BaseBinder.Companion.ifHasTextSetVisibleElseGone(mCurrentGrade);

    }

    @Override
    public void emptyViewText(String s) {
        emptyViewText = s;
        if(mNoRubricText != null) {
            mNoRubricText.setText(s);
        }
    }

    @Override
    public void emptyViewText(int sResId) {
        if(mNoRubricText != null && getContext() != null) {
            mNoRubricText.setText(sResId);
            String s = getContext().getResources().getString(sResId);
            emptyViewText = s;
        }
    }

    @Override
    public void setNoConnectionText(String s) {
        noConnectionText = s;
        if(mNoRubricText != null) {
            mNoRubricText.setText(s);
        }
    }

    @Override
    public void emptyViewImage(Drawable drawable) {

    }

    @Override
    public ImageView getEmptyViewImage() {
        return null;
    }
}
