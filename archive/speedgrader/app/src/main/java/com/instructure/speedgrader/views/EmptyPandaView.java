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

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.instructure.speedgrader.R;
import com.instructure.pandarecycler.interfaces.EmptyViewInterface;

public class EmptyPandaView extends LinearLayout implements EmptyViewInterface {

    private TextView noItemView;
    private ImageView emptyImage;
    private LinearLayout loadingView;
    private String noConnectionText;
    private String emptyViewText;
    private boolean isDisplayNoConnection = false;


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

    private void init(){
        inflate(getContext(), R.layout.empty_view, this);
        this.noItemView = (TextView)findViewById(R.id.noItems);
        this.loadingView = (LinearLayout)findViewById(R.id.loadingView);
        this.emptyImage = (ImageView) findViewById(R.id.emptyImage);
    }

    @Override
    public void setLoading() {
        noItemView.setVisibility(View.GONE);
        emptyImage.setVisibility(View.GONE);
        loadingView.setVisibility(View.VISIBLE);
    }

    @Override
    public void setDisplayNoConnection(boolean isNoConnection) {
        isDisplayNoConnection = isNoConnection;
    }

    @Override
    public void setListEmpty() {
        if (isDisplayNoConnection) {
            noItemView.setText(noConnectionText);
        } else {
            noItemView.setText(emptyViewText);
        }
        noItemView.setVisibility(View.VISIBLE);
        loadingView.setVisibility(View.GONE);

        if(emptyImage != null && emptyImage.getDrawable() != null) {
            emptyImage.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void emptyViewText(String s) {
        emptyViewText = s;
        if(noItemView != null) {
            noItemView.setText(s);
        }
    }

    @Override
    public void emptyViewText(int sResId) {
        if(noItemView != null && getContext() != null) {
            String s = getContext().getResources().getString(sResId);
            noItemView.setText(s);
            emptyViewText = s;
        }
    }

    @Override
    public void setNoConnectionText(String s) {
        noConnectionText = s;
        if(noItemView != null) {
            noItemView.setText(s);
        }
    }

    @Override
    public void emptyViewImage(Drawable drawable) {
        if(emptyImage != null) {
            emptyImage.setImageDrawable(drawable);
        }
    }

    @Override
    public ImageView getEmptyViewImage() {
        return emptyImage;
    }
}
