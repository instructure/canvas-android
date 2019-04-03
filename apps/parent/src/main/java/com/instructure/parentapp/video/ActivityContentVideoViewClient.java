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
package com.instructure.parentapp.video;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.os.Build;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.instructure.parentapp.R;
import com.instructure.parentapp.activity.DetailViewActivity;

/**
 * Uses an existing Activity to handle displaying video in full screen.
 */
public class ActivityContentVideoViewClient implements ContentVideoViewClient {
    private Activity mActivity;
    private View mView;
    private boolean mIsFullScreen;

    public ActivityContentVideoViewClient(Activity activity)  {
        this.mActivity = activity;
    }

    @Override
    public void onShowCustomView(View view) {
        view.setBackgroundColor(Color.BLACK);
        view.setClickable(true);
        Window window = getWindow();
        window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        view.setId(R.id.videoFullScreenView);

        if (isSuperDumb()) {
            performWorkAroundForDumbDevices(false);
        }

        window.addContentView(view,
                new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        Gravity.CENTER));
        mIsFullScreen = true;
        mView = view;
    }

    @Override
    public void onDestroyContentVideoView() {
        mIsFullScreen = false;
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        if (isSuperDumb()) {
            performWorkAroundForDumbDevices(true);
        }

        ViewGroup rootView = (ViewGroup) window.getDecorView();
        if (rootView != null && rootView.findViewById(R.id.videoFullScreenView) != null) {
            ViewGroup container = (ViewGroup) rootView.findViewById(R.id.videoFullScreenView).getParent();
            container.removeView(mView);
            mView = null;
        }
    }

    private void performWorkAroundForDumbDevices(boolean isShow) {
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setWindowAnimations(R.style.DetailsDialogAnimationVideo);
            if (isShow) {
                dialog.show();
            } else {
                dialog.hide();
            }
        }
    }

    private boolean isSuperDumb() {
        return Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR2;
    }

    @Nullable
    private Dialog getDialog() {
        if(mActivity instanceof DetailViewActivity) {
            Fragment fragment = ((DetailViewActivity)mActivity).getTopFragment();
            if (fragment instanceof DialogFragment) {
                return ((DialogFragment)fragment).getDialog();
            }
        }
        return null;
    }

    private Window getWindow() {
        Window window = mActivity.getWindow();
        Dialog dialog = getDialog();

        return window;
    }

    public boolean isFullscreen() {
        return mIsFullScreen;
    }

    @Override
    public View getVideoLoadingProgressView() {

        return null;
    }

    public void toggleHideyBar() {

        // BEGIN_INCLUDE (get_current_ui_flags)
        // The UI options currently enabled are represented by a bitfield.
        // getSystemUiVisibility() gives us that bitfield.
        int uiOptions = mActivity.getWindow().getDecorView().getSystemUiVisibility();
        int newUiOptions = uiOptions;
        // END_INCLUDE (get_current_ui_flags)
        // BEGIN_INCLUDE (toggle_ui_flags)
        boolean isImmersiveModeEnabled =
                ((uiOptions | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY) == uiOptions);

        // Navigation bar hiding:  Backwards compatible to ICS.
        if (Build.VERSION.SDK_INT >= 14) {
            newUiOptions ^= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        }

        // Status bar hiding: Backwards compatible to Jellybean
        if (Build.VERSION.SDK_INT >= 16) {
            newUiOptions ^= View.SYSTEM_UI_FLAG_FULLSCREEN;
        }

        // Immersive mode: Backward compatible to KitKat.
        // Note that this flag doesn't do anything by itself, it only augments the behavior
        // of HIDE_NAVIGATION and FLAG_FULLSCREEN.  For the purposes of this sample
        // all three flags are being toggled together.
        // Note that there are two immersive mode UI flags, one of which is referred to as "sticky".
        // Sticky immersive mode differs in that it makes the navigation and status bars
        // semi-transparent, and the UI flag does not get cleared when the user interacts with
        // the screen.
        if (Build.VERSION.SDK_INT >= 18) {
            newUiOptions ^= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        }

        mActivity.getWindow().getDecorView().setSystemUiVisibility(newUiOptions);
        //END_INCLUDE (set_ui_flags)
    }
}
