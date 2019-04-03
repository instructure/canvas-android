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

package com.instructure.pandautils.fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.instructure.canvasapi2.models.CanvasContext;
import com.instructure.pandautils.R;
import com.instructure.pandautils.interfaces.NavigationCallbacks;
import com.instructure.pandautils.utils.Const;

import instructure.androidblueprint.ListFragment;
import instructure.androidblueprint.ListManager;
import instructure.androidblueprint.ListPresenter;
import instructure.androidblueprint.ListRecyclerAdapter;

public abstract class BaseListFragment<
        MODEL,
        PRESENTER extends ListPresenter<MODEL, VIEW>,
        VIEW extends ListManager<MODEL>,
        HOLDER extends RecyclerView.ViewHolder,
        ADAPTER extends ListRecyclerAdapter<MODEL, HOLDER>> extends ListFragment<MODEL, PRESENTER, VIEW, HOLDER, ADAPTER>
        implements NavigationCallbacks {

    private CanvasContext mCanvasContext;
    protected View mRootView;

    //Only gets called if not null
    public void unBundle(@SuppressWarnings("UnusedParameters") @NonNull Bundle extras){}
    public abstract int layoutResId();
    public abstract void onCreateView(View view);

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null) {
            unBundle(getArguments());
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRootView = inflater.inflate(layoutResId(), container, false);
        onCreateView(mRootView);
        return mRootView;
    }

    @SuppressWarnings("unused")
    public boolean apiCheck(){
        return isAdded();
    }

    @Nullable
    public final CanvasContext getCanvasContext() {
        if(mCanvasContext == null && getArguments() != null && getArguments().containsKey(Const.CANVAS_CONTEXT)) {
            mCanvasContext = getArguments().getParcelable(Const.CANVAS_CONTEXT);
        }
        return mCanvasContext;
    }

    public final void setCanvasContext(CanvasContext canvasContext) {
        mCanvasContext = canvasContext;
    }

    public static Bundle createBundle(CanvasContext canvasContext) {
        Bundle extras = new Bundle();
        extras.putParcelable(Const.CANVAS_CONTEXT, canvasContext);
        return extras;
    }

    @Override
    public boolean onHandleBackPressed() {
        return false;
    }

    @Override
    public boolean onHandleClose() {
        return false;
    }

    protected void addUpIndicatorToExit(Toolbar toolbar) {
        if(toolbar != null) {
            toolbar.setNavigationIcon(R.drawable.vd_close_white);
            toolbar.setNavigationContentDescription(R.string.close);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!onHandleClose()) {
                        getActivity().finish();
                    }
                }
            });
        }
    }
}
