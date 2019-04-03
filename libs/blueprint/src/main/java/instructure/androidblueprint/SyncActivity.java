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

import com.instructure.canvasapi2.models.CanvasComparable;
import com.instructure.pandarecycler.PaginatedScrollListener;
import com.instructure.pandarecycler.util.UpdatableSortedList;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;


public abstract class SyncActivity<
        MODEL extends CanvasComparable<?>,
        PRESENTER extends SyncPresenter<MODEL, VIEW>,
        VIEW extends SyncManager<MODEL>,
        HOLDER extends RecyclerView.ViewHolder,
        ADAPTER extends SyncRecyclerAdapter<MODEL, HOLDER>> extends AppCompatActivity {

    protected abstract void onReadySetGo(PRESENTER presenter);
    protected abstract PresenterFactory<PRESENTER> getPresenterFactory();
    protected abstract void onPresenterPrepared(PRESENTER presenter);
    protected abstract ADAPTER getAdapter();
    protected abstract int perPageCount();

    @NonNull
    protected abstract RecyclerView getRecyclerView();

    protected void hitRockBottom() {
    }

    private static final int LOADER_ID = 1002;

    // boolean flag to avoid delivering the result twice.
    private boolean mDelivered = false;
    private Presenter<VIEW> mPresenter;
    protected ADAPTER mAdapter;

    /* We normally invoke onReadySetGo() as part of onStart(), but this requires a valid presenter which not available
    until LoaderCallback.onLoadFinished() is called. Because the order of onStart() and onLoadFinished() is not
    guaranteed, we need to track the case where onStart() is called first so we can ensure that onReadySetGo()
    is invoked as soon as the presenter is ready. */
    private boolean readySetGoSkipped = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // LoaderCallbacks as an object, so no hint regarding loader will be leak to the subclasses.
        getSupportLoaderManager().initLoader(LOADER_ID, null, new LoaderManager.LoaderCallbacks<PRESENTER>() {
            @Override
            public final Loader<PRESENTER> onCreateLoader(int id, Bundle args) {
                return new PresenterLoader<>(SyncActivity.this, getPresenterFactory());
            }

            @SuppressWarnings("unchecked")
            @Override
            public final void onLoadFinished(Loader<PRESENTER> loader, PRESENTER presenter) {
                if (!mDelivered) {
                    SyncActivity.this.mPresenter = presenter;
                    mDelivered = true;
                    onPresenterPrepared(presenter);
                }
                if (readySetGoSkipped) {
                    // Make sure to invoke onReadySetGo() if it had to be skipped in onStart()
                    onReadySetGo((PRESENTER) mPresenter.onViewAttached(getPresenterView()));
                    readySetGoSkipped = false;
                }
            }

            @Override
            public final void onLoaderReset(Loader<PRESENTER> loader) {
                SyncActivity.this.mPresenter = null;
                onPresenterDestroyed();
            }
        });
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onStart() {
        super.onStart();
        if (mPresenter != null) {
            onReadySetGo((PRESENTER) mPresenter.onViewAttached(getPresenterView()));
        } else {
            readySetGoSkipped = true;
        }
    }

    @Override
    public void onStop() {
        mPresenter.onViewDetached();
        super.onStop();
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
        return (PRESENTER) mPresenter;
    }

    protected void addPagination() {
        if (withPagination()) {
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
}
