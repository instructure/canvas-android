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


import android.os.Bundle;
import com.instructure.canvasapi2.models.Section;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public abstract class PresenterFragment<
        PRESENTER extends FragmentPresenter<VIEW>,
        VIEW extends FragmentViewInterface> extends Fragment {

    protected abstract void onReadySetGo(PRESENTER presenter);

    protected abstract PresenterFactory<PRESENTER> getPresenterFactory();

    protected abstract void onPresenterPrepared(PRESENTER presenter);

    private static final int LOADER_ID = 1003;

    private boolean mDelivered = false;
    private Presenter<VIEW> mPresenter;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getLoaderManager().initLoader(LOADER_ID, null, new LoaderManager.LoaderCallbacks<PRESENTER>() {
            @Override
            public Loader<PRESENTER> onCreateLoader(int id, Bundle args) {
                return new PresenterLoader<>(getContext(), getPresenterFactory());
            }

            @Override
            public void onLoadFinished(Loader<PRESENTER> loader, PRESENTER presenter) {
                if (!mDelivered) {
                    PresenterFragment.this.mPresenter = presenter;
                    mDelivered = true;
                    onPresenterPrepared(presenter);
                }
            }

            @Override
            public void onLoaderReset(Loader<PRESENTER> loader) {
                PresenterFragment.this.mPresenter = null;
                onPresenterDestroyed();
            }
        });
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onResume() {
        super.onResume();
        onReadySetGo((PRESENTER) mPresenter.onViewAttached(getPresenterView()));
    }

    @Override
    public void onPause() {
        mPresenter.onViewDetached();
        super.onPause();
    }

    protected void onPresenterDestroyed() {
        // hook for subclasses
    }

    // Override in case of fragment no implementing Presenter<VIEW> interface
    @SuppressWarnings("unchecked")
    protected VIEW getPresenterView() {
        return (VIEW) this;
    }

    @SuppressWarnings("unchecked")
    protected PRESENTER getPresenter() {
        return (PRESENTER) mPresenter;
    }

    protected void addSwipeToRefresh(@NonNull SwipeRefreshLayout swipeRefreshLayout) {
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getPresenter().refresh(true);
            }
        });
    }
}
