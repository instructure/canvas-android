/*
 * Copyright (C) 2017 - present  Instructure, Inc.
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
 */

package com.instructure.teacher.utils;

import android.content.Context;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

import com.instructure.pandarecycler.interfaces.EmptyInterface;
import com.instructure.teacher.R;
import com.instructure.teacher.adapters.BaseListRecyclerAdapter;

import instructure.androidblueprint.ListPresenter;
import instructure.androidblueprint.ListRecyclerAdapter;
import instructure.androidblueprint.SyncExpandablePresenter;
import instructure.androidblueprint.SyncExpandableRecyclerAdapter;
import instructure.androidblueprint.SyncPresenter;
import instructure.androidblueprint.SyncRecyclerAdapter;

public class RecyclerViewUtils {

    public static RecyclerView buildRecyclerView(
            View rootView,
            final Context context,
            final SyncRecyclerAdapter recyclerAdapter,
            final SyncPresenter presenter,
            int swipeToRefreshLayoutResId,
            int recyclerViewResId,
            int emptyViewResId,
            String emptyViewText) {

        EmptyInterface emptyInterface = rootView.findViewById(emptyViewResId);
        RecyclerView recyclerView = rootView.findViewById(recyclerViewResId);
        emptyInterface.setTitleText(emptyViewText);
        emptyInterface.setNoConnectionText(context.getString(R.string.noConnection));

        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(recyclerAdapter);
        final SwipeRefreshLayout swipeRefreshLayout = rootView.findViewById(swipeToRefreshLayoutResId);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!com.instructure.pandautils.utils.Utils.isNetworkAvailable(context)) {
                    swipeRefreshLayout.setRefreshing(false);
                } else {
                    presenter.refresh(true);
                }
            }
        });

        return recyclerView;
    }

    public static RecyclerView buildRecyclerView(
            final Context context,
            final ListRecyclerAdapter recyclerAdapter,
            final ListPresenter presenter,
            final SwipeRefreshLayout swipeRefreshLayout,
            final RecyclerView recyclerView,
            final EmptyInterface emptyInterface,
            String emptyViewText) {

        emptyInterface.setTitleText(emptyViewText);
        emptyInterface.setNoConnectionText(context.getString(R.string.noConnection));

        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(recyclerAdapter);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!com.instructure.pandautils.utils.Utils.isNetworkAvailable(context)) {
                    swipeRefreshLayout.setRefreshing(false);
                } else {
                    presenter.refresh(true);
                }
            }
        });

        return recyclerView;
    }

    public static RecyclerView buildRecyclerView(
            View rootView,
            final Context context,
            final SyncExpandableRecyclerAdapter recyclerAdapter,
            final SyncExpandablePresenter presenter,
            int swipeToRefreshLayoutResId,
            int recyclerViewResId,
            int emptyViewResId,
            String emptyViewText) {

        EmptyInterface emptyInterface = rootView.findViewById(emptyViewResId);
        RecyclerView recyclerView = rootView.findViewById(recyclerViewResId);
        emptyInterface.setTitleText(emptyViewText);
        emptyInterface.setNoConnectionText(context.getString(R.string.noConnection));

        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(recyclerAdapter);
        final SwipeRefreshLayout swipeRefreshLayout = rootView.findViewById(swipeToRefreshLayoutResId);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!com.instructure.pandautils.utils.Utils.isNetworkAvailable(context)) {
                    swipeRefreshLayout.setRefreshing(false);
                } else {
                    presenter.refresh(true);
                }
            }
        });

        return recyclerView;
    }

    public static void checkIfEmpty(EmptyInterface emptyPandaView,
                                    BaseListRecyclerAdapter adapter,
                                    RecyclerView recyclerView, boolean isRefresh, boolean isEmpty) {
        if (emptyPandaView != null && adapter != null && recyclerView != null) {
            if (adapter.size() == 0 && !isRefresh) {
                recyclerView.setVisibility(View.GONE);
                emptyPandaView.setVisibility(View.VISIBLE);
                if (isEmpty) {
                    emptyPandaView.setListEmpty();
                } else {
                    emptyPandaView.setLoading();
                }
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                emptyPandaView.setVisibility(View.GONE);
            }
        }
    }

    public static void checkIfEmpty(EmptyInterface emptyPandaView,
                                    RecyclerView recyclerView,
                                    SwipeRefreshLayout swipeRefreshLayout,
                                    SyncRecyclerAdapter adapter,
                                    boolean isEmpty) {
        if (emptyPandaView != null && adapter != null && recyclerView != null) {
            if (adapter.size() == 0 && !swipeRefreshLayout.isRefreshing()) {
                recyclerView.setVisibility(View.GONE);
                emptyPandaView.setVisibility(View.VISIBLE);
                if (isEmpty) {
                    emptyPandaView.setListEmpty();
                } else {
                    emptyPandaView.setLoading();
                }
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                emptyPandaView.setVisibility(View.GONE);
            }
        }
    }

    public static void checkIfEmpty(EmptyInterface emptyPandaView,
                                    RecyclerView recyclerView,
                                    SwipeRefreshLayout swipeRefreshLayout,
                                    ListRecyclerAdapter adapter,
                                    boolean isEmpty) {
        if (emptyPandaView != null && adapter != null && recyclerView != null) {
            if (adapter.size() == 0 && !swipeRefreshLayout.isRefreshing()) {
                recyclerView.setVisibility(View.GONE);
                emptyPandaView.setVisibility(View.VISIBLE);
                if (isEmpty) {
                    emptyPandaView.setListEmpty();
                } else {
                    emptyPandaView.setLoading();
                }
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                emptyPandaView.setVisibility(View.GONE);
            }
        }
    }

    public static void checkIfEmpty(EmptyInterface emptyPandaView,
                                    RecyclerView recyclerView,
                                    SwipeRefreshLayout swipeRefreshLayout,
                                    SyncExpandableRecyclerAdapter adapter,
                                    boolean isEmpty) {
        if (emptyPandaView != null && adapter != null && recyclerView != null) {
            if (adapter.size() == 0 && !swipeRefreshLayout.isRefreshing()) {
                recyclerView.setVisibility(View.GONE);
                emptyPandaView.setVisibility(View.VISIBLE);
                if (isEmpty) {
                    emptyPandaView.setListEmpty();
                } else {
                    emptyPandaView.setLoading();
                }
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                emptyPandaView.setVisibility(View.GONE);
            }
        }
    }
}
