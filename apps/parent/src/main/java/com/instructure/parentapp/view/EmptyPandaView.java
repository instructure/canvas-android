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
package com.instructure.parentapp.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.instructure.pandarecycler.interfaces.EmptyInterface;
import com.instructure.parentapp.R;

public class EmptyPandaView extends FrameLayout implements EmptyInterface {

    private TextView mTitle;
    private TextView mMessage;
    private TextView mNoConnection;
    private ImageView mImage;
    private ProgressBar mProgressBar;
    private View mLoadingView;

    private String mNoConnectionText;
    private String mTitleText;
    private String mMessageText;
    private boolean mIsDisplayNoConnection = false;

    public EmptyPandaView(Context context) {
        super(context);
        init();
    }

    public EmptyPandaView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public EmptyPandaView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.empty_view, this);
        mTitle = (TextView) findViewById(R.id.title);
        mMessage = (TextView) findViewById(R.id.message);
        mNoConnection = (TextView) findViewById(R.id.no_connection);
        mImage = (ImageView) findViewById(R.id.image);
        mProgressBar = (ProgressBar) findViewById(R.id.progress);
        mLoadingView = findViewById(R.id.loading);
    }

    public ProgressBar getProgressBar() {
        return mProgressBar;
    }

    @Override
    public void setLoading() {
        mTitle.setVisibility(View.GONE);
        mMessage.setVisibility(View.GONE);
        mImage.setVisibility(View.GONE);
        mLoadingView.setVisibility(View.VISIBLE);
    }

    @Override
    public void setDisplayNoConnection(boolean isNoConnection) {
        mIsDisplayNoConnection = isNoConnection;
    }

    @Override
    public void setListEmpty() {
        if (mIsDisplayNoConnection) {
            mNoConnection.setText(mNoConnectionText);
            mNoConnection.setContentDescription(mNoConnectionText);
        } else {
            mTitle.setText(mTitleText);
            mMessage.setText(mMessageText);
        }

        mTitle.setVisibility(View.VISIBLE);
        mMessage.setVisibility(View.VISIBLE);
        mLoadingView.setVisibility(View.GONE);

        if (mImage != null) {
            if (mImage.getDrawable() != null) {
                mImage.setVisibility(View.VISIBLE);
            } else {
                mImage.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void setTitleText(String s) {
        mTitleText = s;
        if (mTitle != null) {
            mTitle.setText(s);
            mTitle.setContentDescription(s);
        }
    }

    @Override
    public void setTitleText(int sResId) {
        if (mTitle != null && getContext() != null) {
            String s = getContext().getResources().getString(sResId);
            mTitle.setText(s);
            mTitle.setContentDescription(s);
            mTitleText = s;
        }
    }

    @Override
    public void setMessageText(String s) {
        mMessageText = s;
        if (mMessage != null) {
            mMessage.setText(s);
        }
    }

    @Override
    public void setMessageText(int sResId) {
        if (mMessage != null && getContext() != null) {
            String s = getContext().getResources().getString(sResId);
            mMessage.setText(s);
            mMessageText = s;
        }
    }

    @Override
    public void setNoConnectionText(String s) {
        mNoConnectionText = s;
        if (mNoConnection != null) {
            mNoConnection.setText(s);
            mNoConnection.setContentDescription(s);
        }
    }

    @Override
    public void setEmptyViewImage(Drawable drawable) {
        if (mImage != null) {
            mImage.setImageDrawable(drawable);
        }
    }

    @Override
    public ImageView getEmptyViewImage() {
        return mImage;
    }

    @Override
    public void emptyViewText(String s) {
        setTitleText(s);
    }

    @Override
    public void emptyViewText(int sResId) {
        setTitleText(sResId);
    }

    @Override
    public void emptyViewImage(Drawable drawable) {
        setEmptyViewImage(drawable);
    }
}
