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

package com.instructure.speedgrader.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.instructure.canvasapi.model.Assignment;
import com.instructure.canvasapi.model.CanvasContext;
import com.instructure.canvasapi.utilities.APIStatusDelegate;
import com.instructure.canvasapi.utilities.CanvasCallback;
import com.instructure.pandarecycler.BaseRecyclerAdapter;
import com.instructure.pandarecycler.PaginatedRecyclerAdapter;
import com.instructure.pandarecycler.PandaRecyclerView;
import com.instructure.pandarecycler.interfaces.EmptyViewInterface;
import com.instructure.pandarecycler.util.Types;
import com.instructure.pandautils.loaders.OpenMediaAsyncTaskLoader;
import com.instructure.pandautils.utils.LoaderUtils;
import com.instructure.speedgrader.R;
import com.instructure.speedgrader.activities.HomeActivity;
import com.instructure.speedgrader.activities.InternalWebviewActivity;
import com.instructure.speedgrader.activities.ParentActivity;
import com.instructure.speedgrader.adapters.ExpandableRecyclerAdapter;
import com.instructure.speedgrader.animators.ExpandCollapseItemAnimator;
import com.instructure.speedgrader.decorations.DividerItemDecoration;
import com.instructure.speedgrader.decorations.ExpandableGridSpacingDecorator;
import com.instructure.speedgrader.decorations.GridSpacingDecorator;
import com.instructure.speedgrader.util.Const;

import java.lang.reflect.Constructor;

public abstract class ParentFragment extends Fragment  implements APIStatusDelegate {

    private CanvasContext canvasContext;

    public void setRootView(View rootView) {
        this.mRootView = rootView;
    }

    public View mRootView;

    // OpenMediaAsyncTaskLoader
    private Bundle openMediaBundle;
    private LoaderManager.LoaderCallbacks<OpenMediaAsyncTaskLoader.LoadedMedia> openMediaCallbacks;
    private ProgressDialog progressDialog;
    protected SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView.ItemDecoration mSpacingDecoration;

    ///////////////////////////////////////////////////////////////////////////
    // Getters and Setters
    ///////////////////////////////////////////////////////////////////////////

    public CanvasContext getCanvasContext() {
        return canvasContext;
    }

    public void setCanvasContext(CanvasContext canvasContext) {
        this.canvasContext = canvasContext;
    }

    public void setTitle(String title){
        if(isAdded()){
            getActivity().setTitle(title);
        }
    }

    public void setActionBarColor(int color){
        ((ParentActivity)getActivity()).setActionBarColor(color);
    }
    ///////////////////////////////////////////////////////////////////////////
    // New Instance
    ///////////////////////////////////////////////////////////////////////////

    public static <Type extends ParentFragment> Type newInstance(Class<Type> fragmentClass, Bundle params) {
        ParentFragment fragment = null;
        try {
            Constructor<? extends ParentFragment> constructor = fragmentClass.getConstructor();
            fragment = constructor.newInstance();

            if(params != null && params.containsKey(Const.canvasContext)){
                fragment.setCanvasContext((CanvasContext) params.getParcelable(Const.canvasContext));
            }

            fragment.setArguments(params);
            fragment.getBundleData(fragment.getArguments());
        } catch (Exception e) {
            Log.v("canvas-speed-grader", e.toString());
        }
        return (Type)fragment;
    }

    ///////////////////////////////////////////////////////////////////////////
    // LifeCycle
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Restore our loader if the user rotated their device while downloading media.
        LoaderUtils.restoreLoaderFromBundle(getActivity().getSupportLoaderManager(), savedInstanceState, getLoaderCallbacks(), R.id.openMediaLoaderID, com.instructure.pandautils.utils.Const.OPEN_MEDIA_LOADER_BUNDLE);
        if (savedInstanceState != null && savedInstanceState.getBundle(com.instructure.pandautils.utils.Const.OPEN_MEDIA_LOADER_BUNDLE) != null) {
            showProgressDialog();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = inflateLayout(inflater, container);
        return mRootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null){
            canvasContext = (CanvasContext) savedInstanceState.getSerializable(Const.canvasContext);
        }
        setupCallbacks();
        setHasOptionsMenu(true);
        setRetainInstance(retainInstanceState());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(Const.canvasContext, canvasContext);

        dismissProgressDialog(); // Always close.
        LoaderUtils.saveLoaderBundle(outState, openMediaBundle, com.instructure.pandautils.utils.Const.OPEN_MEDIA_LOADER_BUNDLE);

        super.onSaveInstanceState(outState);
    }

    //Fragment-ception fix:
    //Some fragments (currently our AssigmentFragment) have children fragments.
    //In the module progression view pager these child fragments don't get
    //destroyed when the root fragment gets destroyed. Override this function
    //in the appropriate activity to remove child fragments.  For example, in
    //the module progression class we call this function when onDestroyItem
    //is called and it is implemented in the AssigmentFragment class.
    public void removeChildFragments() {}

    //show the progress bar and move the title so it doesn't jump around
    public void showProgress() {
        getActivity().setProgressBarIndeterminateVisibility(true);
    }

    //hide the progress bar and move the title so it doesn't jump around
    public void hideProgress() {
        getActivity().setProgressBarIndeterminateVisibility(false);
    }

    /////////////////////////////////////////////////////////////////
    //                  Interface methods
    /////////////////////////////////////////////////////////////////
    public abstract void setupCallbacks();
    public abstract int getRootLayout();
    public abstract boolean retainInstanceState();

    public void loadData() {}

    public void reloadData(View v) {}


    public void reloadData() {}

    @Override
    public void onNoNetwork() {

    }

    /*
       Hide or show the indeterminate progress bar
    */
    public void showProgressBar() {
        // check to see if user exited activity
        final Activity activity = getActivity();
        if (activity == null) {
            return;
        }

        if(activity instanceof ParentActivity){
            ((ParentActivity)activity).showProgress();
        } else {
            getActivity().setProgressBarIndeterminateVisibility(true);
        }
    }

    public void hideProgressBar() {
        // check to see if user exited activity
        final Activity activity = getActivity();
        if (activity == null) {
            return;
        }

        //if we're removing the fragment or have already started the fragment
        //we don't want to worry about changing the margins or removing the indeterminate progress bar.
        //There was a problem sometimes when an fragment that you could no longer see finished an
        //async task and would hide the progress bar and throw off the margins
        if(this.isRemoving() && !this.isResumed()) {
            return;
        }

        if(activity instanceof ParentActivity){
            ((ParentActivity)activity).hideProgress();
        } else {
            getActivity().setProgressBarIndeterminateVisibility(false);
        }
    }


    @Override
    public void onCallbackStarted() {
        if (ParentActivity.isUIThread()) {
            showProgressBar();
        }
    }
    @Override
    public void onCallbackFinished(CanvasCallback.SOURCE source) {
        if(source.isAPI() && ParentActivity.isUIThread()) {
            hideProgressBar();
        }
    }
    @Override
    public Context getContext() {
        if(getActivity() != null){
            return getActivity().getApplicationContext();
        }
        return null;
    }

    /////////////////////////////////////////////////////////////////
    //                  Helpers
    /////////////////////////////////////////////////////////////////
    public PandaRecyclerView configureRecyclerView(
            View rootView,
            Context context,
            final BaseRecyclerAdapter baseRecyclerAdapter,
            int swipeRefreshLayoutResId,
            int emptyViewResId,
            int recyclerViewResId) {
        return configureRecyclerView(rootView, context, baseRecyclerAdapter, swipeRefreshLayoutResId, emptyViewResId, recyclerViewResId, getResources().getString(R.string.noItemsToDisplayShort), false);
    }

    public PandaRecyclerView configureRecyclerView(
            View rootView,
            Context context,
            final BaseRecyclerAdapter baseRecyclerAdapter,
            int swipeRefreshLayoutResId,
            int emptyViewResId,
            int recyclerViewResId,
            String emptyViewString,
            boolean withDivider) {
        EmptyViewInterface emptyViewInterface = (EmptyViewInterface)rootView.findViewById(emptyViewResId);
        PandaRecyclerView recyclerView = (PandaRecyclerView)rootView.findViewById(recyclerViewResId);

        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setEmptyView(emptyViewInterface);
        emptyViewInterface.emptyViewText(emptyViewString);
        emptyViewInterface.setNoConnectionText(getString(R.string.noConnection));
        recyclerView.setSelectionEnabled(true);
        recyclerView.setAdapter(baseRecyclerAdapter);
        if(withDivider) {
            recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL_LIST));
        }

        mSwipeRefreshLayout = (SwipeRefreshLayout)rootView.findViewById(swipeRefreshLayoutResId);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!com.instructure.pandautils.utils.Utils.isNetworkAvailable(getContext())) {
                    mSwipeRefreshLayout.setRefreshing(false);
                } else {
                    baseRecyclerAdapter.refresh();
                }
            }
        });

        return recyclerView;
    }

    public void configureRecyclerViewAsGrid(
            View rootView,
            final BaseRecyclerAdapter baseRecyclerAdapter,
            int swipeRefreshLayoutResId,
            int emptyViewResId,
            int recyclerViewResId) {
        configureRecyclerViewAsGrid(rootView, baseRecyclerAdapter, swipeRefreshLayoutResId, emptyViewResId, recyclerViewResId, R.string.noItemsToDisplayShort);
    }

    public void configureRecyclerViewAsGrid(View rootView, BaseRecyclerAdapter baseRecyclerAdapter, int swipeRefreshLayoutResId, int emptyViewResId, int recyclerViewResId, int emptyViewStringResId, int span) {
        configureRecyclerViewAsGrid(rootView, baseRecyclerAdapter, swipeRefreshLayoutResId, emptyViewResId, recyclerViewResId, emptyViewStringResId, span, null);
    }

    public void configureRecyclerViewAsGrid(
            View rootView,
            final BaseRecyclerAdapter baseRecyclerAdapter,
            int swipeRefreshLayoutResId,
            int emptyViewResId,
            int recyclerViewResId,
            int emptyViewStringResId,
            Drawable...emptyImage) {
        configureRecyclerViewAsGrid(rootView, baseRecyclerAdapter, swipeRefreshLayoutResId, emptyViewResId, recyclerViewResId, emptyViewStringResId, null, emptyImage);
    }

    public void configureRecyclerViewAsGrid(View rootView, BaseRecyclerAdapter baseRecyclerAdapter, int swipeRefreshLayoutResId, int emptyViewResId, int recyclerViewResId, int emptyViewStringResId, View.OnClickListener emptyImageClickListener, Drawable... emptyImage) {
        final int minCardWidth = getResources().getDimensionPixelOffset(R.dimen.course_card_min_width);
        final Display display = getActivity().getWindowManager().getDefaultDisplay();
        final Point size = new Point();
        display.getSize(size);
        final int width = size.x;
        final int cardPadding = getResources().getDimensionPixelOffset(R.dimen.card_outer_margin);

        //Sets a dynamic span size based on the min card width we need to display the color chooser.
        final int span;
        if(width != 0) {
            span = width / (minCardWidth + cardPadding);
        } else {
            span = 1;
        }
        configureRecyclerViewAsGrid(rootView, baseRecyclerAdapter, swipeRefreshLayoutResId, emptyViewResId, recyclerViewResId, emptyViewStringResId, span, emptyImageClickListener, emptyImage);
    }

    public void configureRecyclerViewAsGrid(
            View rootView,
            final BaseRecyclerAdapter baseRecyclerAdapter,
            int swipeRefreshLayoutResId,
            int emptyViewResId,
            int recyclerViewResId,
            int emptyViewStringResId,
            final int span,
            View.OnClickListener emptyImageListener,
            Drawable...emptyImage) {

        final int cardPadding = getResources().getDimensionPixelOffset(R.dimen.card_outer_margin);
        EmptyViewInterface emptyViewInterface = (EmptyViewInterface)rootView.findViewById(emptyViewResId);
        final PandaRecyclerView recyclerView = (PandaRecyclerView)rootView.findViewById(recyclerViewResId);
        emptyViewInterface.emptyViewText(emptyViewStringResId);
        emptyViewInterface.setNoConnectionText(getString(R.string.noConnection));

        if(emptyImage != null && emptyImage.length > 0) {
            emptyViewInterface.emptyViewImage(emptyImage[0]);
            if(emptyImageListener != null && emptyViewInterface.getEmptyViewImage() != null) {
                emptyViewInterface.getEmptyViewImage().setOnClickListener(emptyImageListener);
            }
        }

        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), span, GridLayoutManager.VERTICAL, false);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if(position < recyclerView.getAdapter().getItemCount()) {
                    int viewType = recyclerView.getAdapter().getItemViewType(position);
                    if (Types.TYPE_HEADER == viewType || PaginatedRecyclerAdapter.LOADING_FOOTER_TYPE == viewType) {
                        return span;
                    }
                } else {
                    //if something goes wrong it will take up the entire space, but at least it won't crash
                    return span;
                }
                return 1;
            }
        });

        if(mSpacingDecoration != null) {
            recyclerView.removeItemDecoration(mSpacingDecoration);
        }
        if(baseRecyclerAdapter instanceof ExpandableRecyclerAdapter) {
            mSpacingDecoration = new ExpandableGridSpacingDecorator(cardPadding);
        } else {
            mSpacingDecoration = new GridSpacingDecorator(cardPadding);
        }
        recyclerView.addItemDecoration(mSpacingDecoration);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new ExpandCollapseItemAnimator());
        recyclerView.setEmptyView(emptyViewInterface);
        recyclerView.setAdapter(baseRecyclerAdapter);
        mSwipeRefreshLayout = (SwipeRefreshLayout)rootView.findViewById(swipeRefreshLayoutResId);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!com.instructure.pandautils.utils.Utils.isNetworkAvailable(getContext())) {
                    mSwipeRefreshLayout.setRefreshing(false);
                } else {
                    baseRecyclerAdapter.refresh();
                }
            }
        });

    }

    /**
     * Memory pressure. Launch dashboard.
     */
    public void launchDashboard(){
        Activity activity = getActivity();
        activity.startActivity(new Intent(activity, HomeActivity.class));
    }

    private LoaderManager.LoaderCallbacks<OpenMediaAsyncTaskLoader.LoadedMedia> getLoaderCallbacks() {
        if (openMediaCallbacks == null) {
            openMediaCallbacks = new LoaderManager.LoaderCallbacks<OpenMediaAsyncTaskLoader.LoadedMedia>() {
                @Override
                public Loader<OpenMediaAsyncTaskLoader.LoadedMedia> onCreateLoader(int id, Bundle args) {
                    showProgressDialog();
                    return new OpenMediaAsyncTaskLoader(getContext(), args);
                }

                @Override
                public void onLoadFinished(Loader<OpenMediaAsyncTaskLoader.LoadedMedia> loader, OpenMediaAsyncTaskLoader.LoadedMedia loadedMedia) {
                    dismissProgressDialog();
                    openMediaBundle = null; // set to null, otherwise the progressDialog will appear again

                    if (loadedMedia.isError()) {
                        Toast.makeText(getActivity(), getActivity().getResources().getString(loadedMedia.getErrorMessage()), Toast.LENGTH_SHORT).show();
                    }
                    else if (loadedMedia.isHtmlFile()) {
                        startActivity(InternalWebviewActivity.createIntent(getContext(), loadedMedia.getBundle().getString(com.instructure.pandautils.utils.Const.INTERNAL_URL), false));
                    }
                    else if (loadedMedia.getIntent() != null) {
                        getActivity().startActivity(loadedMedia.getIntent());
                    }
                }

                @Override
                public void onLoaderReset(Loader<OpenMediaAsyncTaskLoader.LoadedMedia> loader) {}
            };
        }
        return openMediaCallbacks;
    }

    public void openMedia(String mime, String url, String filename) {
        openMediaBundle = OpenMediaAsyncTaskLoader.createBundle(getCanvasContext(), mime, url, filename);
        LoaderUtils.restartLoaderWithBundle(getActivity().getSupportLoaderManager(), openMediaBundle, getLoaderCallbacks(), R.id.openMediaLoaderID);
    }

    public void getBundleData(Bundle bundle){
        canvasContext = bundle.getParcelable(Const.canvasContext);
    }

    public static Bundle createBundle(CanvasContext canvasContext) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(Const.canvasContext, canvasContext);
        return bundle;
    }

    public static Bundle createBundle(CanvasContext canvasContext, Assignment assignment) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(Const.canvasContext, canvasContext);
        bundle.putParcelable(Const.assignmentId, assignment);
        return bundle;
    }

    public Class getFragmentClass(){
        return ((Object) this).getClass();
    }

    public View inflateLayout(LayoutInflater inflater,ViewGroup container){
        return inflater.inflate(getRootLayout(), container, false);
    }

    // ProgressDialog
    private void initProgressDialog() {
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setCancelable(true);
        progressDialog.setMessage(getString(R.string.opening));
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                dismissProgressDialog();
                openMediaBundle = null; // set to null, otherwise the progressDialog will appear again
                getActivity().getSupportLoaderManager().getLoader(R.id.openMediaLoaderID).abandon();
            }
        });
        progressDialog.setCanceledOnTouchOutside(true);
    }

    public void showProgressDialog() {
        if (progressDialog == null) {
            initProgressDialog();
        }
        progressDialog.show();
    }

    public void dismissProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    public void setRefreshing(boolean isRefreshing) {
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setRefreshing(isRefreshing);
        }
    }
}
