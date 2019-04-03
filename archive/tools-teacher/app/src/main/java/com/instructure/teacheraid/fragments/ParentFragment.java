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
 */

package com.instructure.teacheraid.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.instructure.canvasapi.model.CanvasContext;
import com.instructure.canvasapi.model.User;
import com.instructure.canvasapi.utilities.APIStatusDelegate;
import com.instructure.canvasapi.utilities.CanvasCallback;
import com.instructure.teacheraid.activities.BaseActivity;
import com.instructure.teacheraid.util.Const;

abstract public class ParentFragment extends Fragment implements APIStatusDelegate {

    // action bar
    private String actionBarTitle;

    private CanvasContext canvasContext;

    private TextView titleView;

    public enum TabletType { COLUMN, FULLSCREEN }
    ///////////////////////////////////////////////////////////////////////////
    // Getters and Setters
    ///////////////////////////////////////////////////////////////////////////

    public CanvasContext getCanvasContext() {
        return canvasContext;
    }

    public void setCanvasContext(CanvasContext canvasContext) {
        this.canvasContext = canvasContext;
    }

    ///////////////////////////////////////////////////////////////////////////
    // LifeCycle
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        if(getActivity() != null && menu != null){
            getActivity().onPrepareOptionsMenu(menu);
        }
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((ActionBarActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                ((BaseActivity)getActivity()).removeFragments();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Override this
     * Add custom logic and return true to bypass the default action.
     * Return false to do the default action.
     * Don't allow them to hit the back button if there is unsaved data.
     * @return
     */
    public boolean handleBackPressed() {
        if(!hasUnSavedData()){
            return false;
        } else{
            //DataLossFragmentStyled.show(getActivity());
            return true;
        }
    }


    /**
     * Override this to tell the system when you have unsaved data.
     *
     * Used in conjunction with HandleBackPressed();
     * @return
     */
    public boolean hasUnSavedData(){
        return false;
    }



    public boolean handleKeyDown(int keyCode, KeyEvent event) {
        return false;
    }


    ///////////////////////////////////////////////////////////////////////////
    // Required Overrides for APIStatusDelegate
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public Context getContext() {
        return getActivity();
    }

    @Override
    public void onCallbackFinished(CanvasCallback.SOURCE source) {
        if(source.isAPI()) {
        }
    }

    @Override public void onNoNetwork() { }

    @Override
    public void onCallbackStarted() {

    }

    ///////////////////////////////////////////////////////////////////////////
    // Fragment
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Factory method for constructing a fragment of the specified type.
     *
     * Make sure to use the generic parameters of this method
     * in order to avoid casting.
     *
     *
     * @param fragmentClass The class of fragment to be created.
     * @param params The bundle of extras to be passed to the
     *               fragment's handleIntentExtras() method. This
     *               method is called immediately after the fragment is constructed.
     * @param <Type> The type of fragment that this method will return, in order to
     *               avoid casting.
     * @return The fragment that was constructed.
     */
    public static <Type extends ParentFragment> Type createFragment(Class<Type> fragmentClass, Bundle params) {
        ParentFragment fragment = null;
        try {
            //NewRelic.setInteractionName(fragmentClass.getSimpleName());
            fragment = fragmentClass.newInstance();
        } catch (java.lang.InstantiationException e) {
           // LoggingUtility.LogException(null, e);
        } catch (IllegalAccessException e) {
            //LoggingUtility.LogException(null, e);
        }
        fragment.handleIntentExtras(params);
        return (Type)fragment;
    }

    public final BaseActivity getParentActivity() {
        return (BaseActivity)getActivity();
    }

    public void loadData() {}

    public void reloadData(View v) {}

    public void reloadData() {}

    //Fragment-ception fix:
    //Some fragments (currently our AssigmentFragment) have children fragments.
    //In the module progression view pager these child fragments don't get 
    //destroyed when the root fragment gets destroyed. Override this function
    //in the appropriate activity to remove child fragments.  For example, in
    //the module progression class we call this function when onDestroyItem 
    //is called and it is implemented in the AssigmentFragment class.
    public void removeChildFragments() {}

    protected <I> I getModelObject() { return null; }

    public TabletType getTabletViewType() {
        return TabletType.COLUMN;
    }

    @Override
    public void startActivity(Intent intent) {
        if(getContext() == null){return;}

        super.startActivity(intent);
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        if(getContext() == null) {return;}

        //check that we are on a tablet and that we aren't trying to start an activity
        //Note: tablets can be on pages other than TabletShell. (course module progress for example).

        super.startActivityForResult(intent, requestCode);
    }
    ///////////////////////////////////////////////////////////////////////////
    // Intent
    ///////////////////////////////////////////////////////////////////////////


    public void handleIntentExtras(Bundle extras) {
    }

    public static Bundle createBundle(CanvasContext canvasContext) {
        Bundle bundle = new Bundle();
        //bundle.putParcelable(Const.CANVAS_CONTEXT, canvasContext);

        return bundle;
    }

    public static Bundle createBundle(CanvasContext canvasContext, User user) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(Const.USER, user);
        bundle.putParcelable(Const.COURSE, canvasContext);
        return bundle;
    }


    public void viewTwoFingerDoubleTapped(){}

}
