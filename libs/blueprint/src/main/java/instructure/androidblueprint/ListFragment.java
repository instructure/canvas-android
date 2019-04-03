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
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.instructure.canvasapi2.utils.ApiPrefs;
import com.instructure.pandarecycler.PaginatedScrollListener;
import com.instructure.pandarecycler.util.UpdatableSortedList;


public abstract class ListFragment<
        MODEL,
        PRESENTER extends ListPresenter<MODEL, VIEW>,
        VIEW extends ListManager<MODEL>,
        HOLDER extends RecyclerView.ViewHolder,
        ADAPTER extends ListRecyclerAdapter<MODEL, HOLDER>> extends Fragment {

    protected abstract void onReadySetGo(PRESENTER presenter);
    protected abstract PresenterFactory<PRESENTER> getPresenterFactory();
    protected abstract void onPresenterPrepared(PRESENTER presenter);
    protected abstract ADAPTER getAdapter();
    protected abstract @NonNull RecyclerView getRecyclerView();
    protected void hitRockBottom(){}

    private static final int LOADER_ID = 1002;

    // boolean flag to avoid delivering the result twice. Calling initLoader in onActivityCreated makes
    // onLoadFinished will be called twice during configuration change.
    private boolean mDelivered = false;
    private Presenter<VIEW> mPresenter;
    protected ADAPTER mAdapter;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // LoaderCallbacks as an object, so no hint regarding loader will be leak to the subclasses.
        getLoaderManager().initLoader(LOADER_ID, null, new LoaderManager.LoaderCallbacks<PRESENTER>() {
            @Override
            public final Loader<PRESENTER> onCreateLoader(int id, Bundle args) {
                return new PresenterLoader<>(getContext(), getPresenterFactory());
            }

            @Override
            public final void onLoadFinished(Loader<PRESENTER> loader, PRESENTER presenter) {
                if (!mDelivered) {
                    ListFragment.this.mPresenter = presenter;
                    mDelivered = true;
                    onPresenterPrepared(presenter);
                }
            }

            @Override
            public final void onLoaderReset(Loader<PRESENTER> loader) {
                ListFragment.this.mPresenter = null;
                onPresenterDestroyed();
            }
        });
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onResume() {
        super.onResume();
        onReadySetGo((PRESENTER)mPresenter.onViewAttached(getPresenterView()));
    }

    @Override
    public void onPause() {
        mPresenter.onViewDetached();
        super.onPause();
    }

    protected void onPresenterDestroyed() {
        // hook for subclasses
    }

    public boolean withPagination() {
        return true;
    }

    public void clearAdapter() {
        getAdapter().clear();
    }

    // Override in case of fragment not implementing Presenter<View> interface
    @SuppressWarnings("unchecked")
    protected VIEW getPresenterView() {
        return (VIEW) this;
    }

    @SuppressWarnings("unchecked")
    protected PRESENTER getPresenter() {
        return (PRESENTER)mPresenter;
    }

    protected void addPagination() {
        if(withPagination()) {
            getRecyclerView().clearOnScrollListeners();
            getRecyclerView().addOnScrollListener(new PaginatedScrollListener(new PaginatedScrollListener.PaginatedScrollCallback() {
                @Override
                public void loadData() {
                    hitRockBottom();
                }
            }, perPageCount()));
        }
    }

    protected void addSwipeToRefresh(@NonNull SwipeRefreshLayout swipeRefreshLayout) {
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                addPagination();
                getPresenter().refresh(true);
            }
        });
    }

    public UpdatableSortedList getList() {
        return getPresenter().getData();
    }

    protected int perPageCount() {
        return ApiPrefs.getPerPageCount();
    }
}
