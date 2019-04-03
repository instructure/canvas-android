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

package instructure.androidblueprint;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public abstract class FragmentPresenter<VIEW extends FragmentViewInterface> implements Presenter<VIEW> {

    public abstract void loadData(boolean forceNetwork);
    public abstract void refresh(boolean forceNetwork);

    private VIEW mView;

    @Override
    public Presenter onViewAttached(@NonNull VIEW view) {
        mView = view;
        return this;
    }

    @Override
    public void onViewDetached() {
        mView = null;
    }

    @Override
    public void onDestroyed() {
        mView = null;
    }

    public @Nullable
    VIEW getViewCallback() {
        return mView;
    }

    protected void onRefreshStarted() {
        if(getViewCallback() != null) {
            getViewCallback().onRefreshStarted();
        }
    }
}
