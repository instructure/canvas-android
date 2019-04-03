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

package com.instructure.speedgrader.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import com.instructure.canvasapi.utilities.APIStatusDelegate;
import com.instructure.canvasapi.utilities.CanvasCallback;
import com.instructure.canvasapi.utilities.Masquerading;
import com.instructure.speedgrader.R;
import com.instructure.speedgrader.fragments.ParentFragment;

public abstract class ParentActivity extends ActionBarActivity implements APIStatusDelegate{

    public CharSequence mTitle;

    /////////////////////////////////////////////////////////////////
    //                  Lifecycle methods
    /////////////////////////////////////////////////////////////////
    public static boolean isUIThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }

    public void showProgress() {
        setProgressBarIndeterminateVisibility(true);
    }

    public void hideProgress() {
        setProgressBarIndeterminateVisibility(false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getRootLayout());

        handleIntent(getIntent());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    /////////////////////////////////////////////////////////////////
    //                  Actionbar methods
    /////////////////////////////////////////////////////////////////
    public void setTitle(String title){
        getSupportActionBar().setTitle(title);
    }

    @Override
    public void onBackPressed() {
        setProgressBarIndeterminateVisibility(false);
        super.onBackPressed();
    }

    public void setActionBarColor(int color){
        if(Masquerading.isMasquerading(this)) {
            color = getResources().getColor(R.color.masqueradeRed);
        }
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(color)) ;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(color);
        }
    }

    /////////////////////////////////////////////////////////////////
    //                  Abstract methods
    /////////////////////////////////////////////////////////////////
    /**
     * @desc   define the layout file to inflate for this activity
     * @return layout file
     */
    public abstract int getRootLayout();
    public abstract void handleIntent(Intent intent);
    public void loadData(){};

    /////////////////////////////////////////////////////////////////
    //                  Fragment Manager
    /////////////////////////////////////////////////////////////////
    /**
     * @desc   Adds the current fragment inside the content_frame
     *         with that fragments classname as the tag.
     * @return layout file
     */
    public void addFragment( ParentFragment fragment ){
        getSupportFragmentManager().beginTransaction()
                .add(R.id.content_frame,  fragment, fragment.getFragmentClass().toString())
                .commit();
        getSupportFragmentManager().executePendingTransactions();
    }

    /**
     * @desc   Replaces the current fragment inside the content_frame
     *         with that fragments classname as the tag,
     *         also adds that item to the backstack
     * @return layout file
     */
    public void showFragment( ParentFragment fragment ){

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content_frame,  fragment, fragment.getFragmentClass().toString())
                .addToBackStack(fragment.getFragmentClass().toString())
                .commit();
        getSupportFragmentManager().executePendingTransactions();
    }

    /**
     * @desc   Adds the current fragment inside the content_frame
     *         with that fragments classname as the tag.
     * @return layout file
     */
    public void addFragmentToLayout( ParentFragment fragment , int layout){
        getSupportFragmentManager().beginTransaction()
                .add(layout,  fragment, fragment.getFragmentClass().toString())
                .commit();
        getSupportFragmentManager().executePendingTransactions();
    }

    /**
     * @desc   Adds the current fragment inside the content_frame
     *         with that fragments classname as the tag.
     * @return layout file
     */
    public void replaceFragmentInLayout( ParentFragment fragment , int layout){
        getSupportFragmentManager().beginTransaction()
                .replace(layout,  fragment, fragment.getFragmentClass().toString())
                .commit();
        getSupportFragmentManager().executePendingTransactions();
    }

    /////////////////////////////////////////////////////////////////
    //                  APIStatus Delegate
    /////////////////////////////////////////////////////////////////
    @Override
    public void onCallbackFinished(CanvasCallback.SOURCE source) {}
    @Override
    public void onCallbackStarted() {
        if (isUIThread()) {
            showProgress();
        }
    }


    @Override
    public void onNoNetwork() {}

    @Override
    public Context getContext() {
        return this;
    }
}
