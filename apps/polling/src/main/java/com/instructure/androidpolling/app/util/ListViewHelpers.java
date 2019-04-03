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

package com.instructure.androidpolling.app.util;

import android.content.Context;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.TextView;

import com.instructure.androidpolling.app.R;

public class ListViewHelpers {

    private final static int REFRESH_HEIGHT = 130;

    // These aren't in the pull-to-refresh code because we update the library from time to time

    ///////////////////////////////////////////////////////////////////////////
    // General
    ///////////////////////////////////////////////////////////////////////////

    // For some reason the height is intermittently too small and part of the view gets clipped.
    // This fixes that.
    public static void setupListView(SwipeRefreshLayout swipeRefreshLayout) {

    }

    ///////////////////////////////////////////////////////////////////////////
    // Empty View
    ///////////////////////////////////////////////////////////////////////////

    public static void changeEmptyViewToLoading(View emptyView) {
        if(emptyView == null) return;

        setEmptyViewColorAndTitle(emptyView);
        emptyView.findViewById(R.id.noItems).setVisibility(View.GONE);
        //don't want loading view right now
        emptyView.findViewById(R.id.loadingView).setVisibility(View.GONE);

        //not all views will implement the retry button
        if(emptyView.findViewById(R.id.retry) != null) {
            emptyView.findViewById(R.id.retry).setVisibility(View.GONE);
        }

    }

    public static void changeEmptyViewToNoItems(View emptyView) {
        if(emptyView == null) return;

        setEmptyViewColorAndTitle(emptyView);

        emptyView.findViewById(R.id.noItems).setVisibility(View.VISIBLE);
        emptyView.findViewById(R.id.loadingView).setVisibility(View.GONE);

        //not all views will implement the retry button
        if(emptyView.findViewById(R.id.retry) != null) {
            emptyView.findViewById(R.id.retry).setVisibility(View.VISIBLE);
        }
    }

    private static void setEmptyViewColorAndTitle(View emptyView) {
        if(emptyView == null) return;

        int colorConstant = getColorConstant();

        TextView noItems = (TextView) emptyView.findViewById(R.id.noItems);
        noItems.setTextColor(colorConstant);

        TextView loading = (TextView) emptyView.findViewById(R.id.loadingText);
        loading.setTextColor(colorConstant);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Footer
    ///////////////////////////////////////////////////////////////////////////


    public static void showFooterTapToRetry(View footer) {

    }

    private static void showFooterMessage(View footer, String message) {

    }

    public static void hideFooter(View footer) {

    }

    public static boolean footerIsTapToRetry(View footer) {
       // return footerHasText(footer, R.string.tapToRetry);
        return false;
    }

    public static boolean footerIsLoading(View footer) {
        return footerHasText(footer, R.string.loading);
    }

    private static boolean footerHasText(View footer, int textId) {
        if (footer != null) {
            Context context = footer.getContext();
//           // TextView footerText = (TextView) footer.findViewById(R.id.listview_loading_text);
//            if (footerText.getText().equals(context.getString(textId))) {
//                return true;
//            }
        }
        return false;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Color Helpers
    ///////////////////////////////////////////////////////////////////////////

    private static int getColorConstant() {
        return R.color.canvasTextDark;
    }
}
