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
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.instructure.canvasapi2.models.CanvasComparable;
import com.instructure.pandarecycler.PaginatedScrollListener;
import com.instructure.pandarecycler.util.GroupSortedList;
import com.instructure.pandarecycler.util.Types;


public abstract class SyncExpandableActivity<
        GROUP,
        MODEL extends CanvasComparable,
        VIEW extends SyncExpandableManager<GROUP, MODEL>,
        PRESENTER extends SyncExpandablePresenter<GROUP, MODEL, VIEW>,
        HOLDER extends RecyclerView.ViewHolder,
        ADAPTER extends SyncExpandableRecyclerAdapter<GROUP, MODEL, HOLDER>> extends AppCompatActivity {

    protected abstract void onReadySetGo(PRESENTER presenter);
    protected abstract PresenterFactory<PRESENTER> getPresenterFactory();
    protected abstract void onPresenterPrepared(PRESENTER presenter);
    protected abstract ADAPTER getAdapter();
    protected abstract int perPageCount();
    protected void hitRockBottom() {}

    @NonNull
    protected abstract RecyclerView getRecyclerView();

    private static final int LOADER_ID = 1002;

    // boolean flag to avoid delivering the result twice.
    private boolean mDelivered = false;
    private Presenter<VIEW> mPresenter;
    protected ADAPTER mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // LoaderCallbacks as an object, so no hint regarding loader will be leak to the subclasses.
        getSupportLoaderManager().initLoader(LOADER_ID, null, new LoaderManager.LoaderCallbacks<PRESENTER>() {
            @Override
            public final Loader<PRESENTER> onCreateLoader(int id, Bundle args) {
                return new PresenterLoader<>(SyncExpandableActivity.this, getPresenterFactory());
            }

            @Override
            public final void onLoadFinished(Loader<PRESENTER> loader, PRESENTER presenter) {
                if (!mDelivered) {
                    SyncExpandableActivity.this.mPresenter = presenter;
                    mDelivered = true;
                    onPresenterPrepared(presenter);
                }
            }

            @Override
            public final void onLoaderReset(Loader<PRESENTER> loader) {
                SyncExpandableActivity.this.mPresenter = null;
                onPresenterDestroyed();
            }
        });
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onStart() {
        super.onStart();
        onReadySetGo((PRESENTER) mPresenter.onViewAttached(getPresenterView()));
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

    public int childItemType() {
        return Types.TYPE_ITEM;
    }

    public int groupItemType() {
        return Types.TYPE_HEADER;
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

    public void notifyItemRangeInserted(int position, int count) {
        getAdapter().notifyItemRangeInserted(position, count);
    }

    public void notifyItemRangeRemoved(int position, int count) {
        getAdapter().notifyItemRangeRemoved(position, count);
    }

    public void notifyItemMoved(int fromPosition, int toPosition) {
        getAdapter().notifyItemMoved(fromPosition, toPosition);
    }

    public void notifyItemRangeChanged(int position, int count) {
        getAdapter().notifyItemRangeChanged(position, count);
    }

    public GroupSortedList<GROUP, MODEL> getList() {
        return getPresenter().getData();
    }

    @SuppressWarnings("unchecked")
    public int compare(MODEL o1, MODEL o2) {
        return o1.compareTo(o2);
    }

    public boolean areContentsTheSame(MODEL oldItem, MODEL newItem) {
        return false;
    }

    public boolean areItemsTheSame(MODEL item1, MODEL item2) {
        return false;
    }

    public int compare(GROUP group, MODEL item1, MODEL item2) {
        return 0;
    }

    public int compare(GROUP group1, GROUP group2) {
        return 0;
    }

    public boolean areContentsTheSame(GROUP group1, GROUP group2) {
        return false;
    }

    public boolean areItemsTheSame(GROUP group1, GROUP group2) {
        return false;
    }
}
