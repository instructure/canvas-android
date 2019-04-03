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

package com.instructure.pandautils.activities;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.instructure.canvasapi2.models.CanvasComparable;
import com.instructure.canvasapi2.models.CanvasContext;
import com.instructure.pandautils.R;
import com.instructure.pandautils.interfaces.NavigationCallbacks;
import com.instructure.pandautils.utils.Const;

import java.util.List;

import instructure.androidblueprint.SyncExpandableActivity;
import instructure.androidblueprint.SyncExpandableManager;
import instructure.androidblueprint.SyncExpandablePresenter;
import instructure.androidblueprint.SyncExpandableRecyclerAdapter;

public abstract class BaseExpandableSyncActivity<
        GROUP,
        MODEL extends CanvasComparable,
        VIEW extends SyncExpandableManager<GROUP, MODEL>,
        PRESENTER extends SyncExpandablePresenter<GROUP, MODEL, VIEW>,
        HOLDER extends RecyclerView.ViewHolder,
        ADAPTER extends SyncExpandableRecyclerAdapter<GROUP, MODEL, HOLDER>> extends SyncExpandableActivity<GROUP, MODEL, VIEW, PRESENTER, HOLDER, ADAPTER> {

    private CanvasContext mCanvasContext;

    //Only gets called if not null
    public abstract void unBundle(@NonNull Bundle extras);
    public boolean tryUnBundle(){
        return true;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(tryUnBundle() && getIntent().getExtras() != null) {
            unBundle(getIntent().getExtras());
        }
    }

    public static Bundle createBundle(CanvasContext canvasContext) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(Const.CANVAS_CONTEXT, canvasContext);
        return bundle;
    }

    @Nullable
    public final CanvasContext getCanvasContext() {
        if(mCanvasContext == null && getIntent().getExtras() != null && getIntent().getExtras().containsKey(Const.CANVAS_CONTEXT)) {
            mCanvasContext = getIntent().getExtras().getParcelable(Const.CANVAS_CONTEXT);
        }
        return mCanvasContext;
    }

    public final void setCanvasContext(CanvasContext canvasContext) {
        mCanvasContext = canvasContext;
    }

    protected void addUpIndicatorToExit(Toolbar toolbar) {
        if(toolbar != null) {
            toolbar.setNavigationIcon(R.drawable.vd_close_white);
            toolbar.setNavigationContentDescription(R.string.close);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Fragment topFragment = getTopFragment();
                    if(topFragment instanceof NavigationCallbacks) {
                        if(((NavigationCallbacks) topFragment).onHandleBackPressed()) {
                            return;
                        }
                    }
                    finish();
                }
            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        Fragment topFragment = getTopFragment();
        if(topFragment instanceof NavigationCallbacks) {
            if(((NavigationCallbacks) topFragment).onHandleBackPressed()) {
                return;
            }
        }

        super.onBackPressed();
    }

    /**
     * Only returns a fragment if one exists and if it was added to the backStack
     * FragmentManager.addToBackStack()
     * @return The top fragment in the backStack or null
     */
    @Nullable
    public Fragment getTopFragment() {
        if(getSupportFragmentManager().getBackStackEntryCount() > 0) {
            final List<Fragment> fragments = getSupportFragmentManager().getFragments();
            if(!fragments.isEmpty()) {
                return fragments.get(getSupportFragmentManager().getBackStackEntryCount() - 1);
            }
        }
        return null;
    }
}
