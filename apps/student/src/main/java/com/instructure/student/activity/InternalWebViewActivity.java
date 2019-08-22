/*
 * Copyright (C) 2016 - present Instructure, Inc.
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

package com.instructure.student.activity;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.instructure.student.R;
import com.instructure.student.fragment.InternalWebviewFragment;

import com.instructure.canvasapi2.models.CanvasContext;
import com.instructure.interactions.router.Route;
import com.instructure.pandautils.activities.BaseActionBarActivity;
import com.instructure.pandautils.utils.ColorKeeper;
import com.instructure.pandautils.utils.Const;
import com.instructure.pandautils.utils.ViewStyler;

public class InternalWebViewActivity extends BaseActionBarActivity {

    public static final String HIDE_TOOLBAR = "hide_toolbar";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getToolbar() != null) {
            ViewStyler.themeToolbar(this, getToolbar(), Color.WHITE, Color.BLACK, false);
        }

        if(savedInstanceState == null) {
            Bundle bundle = getIntent().getBundleExtra(Const.EXTRAS);

            if(bundle.containsKey(Const.ACTION_BAR_TITLE)) {
                getToolbar().setTitle(bundle.getString(Const.ACTION_BAR_TITLE));
            }
            if(bundle.containsKey(Const.CANVAS_CONTEXT)) {
                CanvasContext canvasContext = bundle.getParcelable(Const.CANVAS_CONTEXT);
                if(canvasContext != null) {
                    // Currently we use an empty context when showing the EULA, privacy policy, etc., in which case we
                    // want the internalWebViewFragment to hide its toolbar
                    if(canvasContext.getId() == 0) {
                        bundle.putBoolean(HIDE_TOOLBAR, true);
                    } else {
                        //todo fix actionbar setting with viewstyler
                        final int color = ColorKeeper.getOrGenerateColor(canvasContext);
                        setActionBarStatusBarColors(color, color);
                        getSupportActionBar().setTitle(canvasContext.getName());
                    }
                }
            }

            InternalWebviewFragment fragment = InternalWebviewFragment.Companion.newInstance(InternalWebviewFragment.Companion.makeRoute(bundle));
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(R.id.container, fragment, InternalWebviewFragment.class.getName());
            ft.commitAllowingStateLoss();
        }
    }

    @Override
    public int contentResId() {
        return R.layout.base_layout;
    }

    @Override
    public boolean showHomeAsUp() {
        return true;
    }

    @Override
    public boolean showTitleEnabled() {
        return false;
    }

    @Override
    public void onUpPressed() {
        finish();
    }

    public static Intent createIntent(Context context, String url, String title, boolean authenticate) {
        // Assumes no CanvasContext
        Bundle extras = InternalWebViewActivity.createBundle(CanvasContext.Companion.emptyCourseContext(), url, title, authenticate);

        Intent intent = new Intent(context, InternalWebViewActivity.class);
        intent.putExtra(Const.EXTRAS, extras);

        return intent;
    }

    public static Intent createIntent(Context context, Route route, String title, Boolean authenticate) {
        return createIntent(context, route.getUri().toString(), title, authenticate);
    }

    public static Intent createIntent(Context context, String url, String html, String title, boolean authenticate) {
        // Assumes no CanvasContext
        Bundle extras = InternalWebViewActivity.createBundle(CanvasContext.Companion.emptyCourseContext(), url, title, authenticate, html);

        Intent intent = new Intent(context, InternalWebViewActivity.class);
        intent.putExtra(Const.EXTRAS, extras);

        return intent;
    }

    public static Intent createIntent(Context context, CanvasContext canvasContext, String url, boolean authenticate) {
        Bundle extras = InternalWebViewActivity.createBundle(canvasContext, url, authenticate);
        Intent intent = new Intent(context, InternalWebViewActivity.class);
        intent.putExtra(Const.EXTRAS, extras);
        return intent;
    }

    @Override
    public void onBackPressed() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.container);
        if(fragment instanceof InternalWebviewFragment) {
            InternalWebviewFragment webviewFragment = (InternalWebviewFragment)fragment;
            if(!webviewFragment.handleBackPressed()) {
                super.onBackPressed();
            }
        } else {
            super.onBackPressed();
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // ACTION BAR STATUS BAR COLORS
    ///////////////////////////////////////////////////////////////////////////

    public void setActionBarStatusBarColors(int actionBarColor, int statusBarColor) {
        setActionbarColor(actionBarColor);
        setStatusBarColor(statusBarColor);
    }

    public void setActionbarColor(int actionBarColor) {
        ColorDrawable colorDrawable = new ColorDrawable(actionBarColor);
        getSupportActionBar().setBackgroundDrawable(colorDrawable);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void setStatusBarColor(int statusBarColor) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && statusBarColor != Integer.MAX_VALUE) {
            getWindow().setStatusBarColor(statusBarColor);
        }
    }

    private static Bundle createBundle(CanvasContext canvasContext) {
        Bundle extras = new Bundle();
        extras.putParcelable(Const.CANVAS_CONTEXT, canvasContext);
        return extras;
    }

    public static Bundle createBundle(CanvasContext canvasContext, String url, String title, Boolean authenticate) {
        Bundle extras = createBundle(canvasContext);
        extras.putString(Const.INTERNAL_URL, url);
        extras.putBoolean(Const.AUTHENTICATE, authenticate);
        extras.putString(Const.ACTION_BAR_TITLE, title);
        return extras;
    }

    public static Bundle createBundle(CanvasContext canvasContext, String url, String title, Boolean authenticate, String html) {
        Bundle extras = createBundle(canvasContext);
        extras.putString(Const.INTERNAL_URL, url);
        extras.putBoolean(Const.AUTHENTICATE, authenticate);
        extras.putString(Const.ACTION_BAR_TITLE, title);
        extras.putString(Const.HTML, html);
        return extras;
    }

    public static Bundle createBundle(CanvasContext canvasContext, String url, Boolean authenticate) {
        Bundle extras = createBundle(canvasContext);
        extras.putString(Const.INTERNAL_URL, url);
        extras.putBoolean(Const.AUTHENTICATE, authenticate);
        return extras;
    }
}
