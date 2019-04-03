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

package com.instructure.teacheraid.util;

import android.graphics.Color;
import android.view.View;
import android.widget.TextView;

import com.instructure.teacheraid.R;

public class ListViewHelpers {

    ///////////////////////////////////////////////////////////////////////////
    // Empty View
    ///////////////////////////////////////////////////////////////////////////

    public static void changeEmptyViewToLoading(View emptyView) {
        if(emptyView == null) return;

        setEmptyViewColorAndTitle(emptyView);
        emptyView.findViewById(R.id.noItems).setVisibility(View.GONE);
        emptyView.findViewById(R.id.loadingView).setVisibility(View.VISIBLE);
    }

    public static void changeEmptyViewToNoItems(View emptyView) {
        if(emptyView == null) return;

        setEmptyViewColorAndTitle(emptyView);

        emptyView.findViewById(R.id.noItems).setVisibility(View.VISIBLE);
        emptyView.findViewById(R.id.loadingView).setVisibility(View.GONE);
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

    public static void showFooterLoading(View footer) {
        showFooterMessage(footer, footer.getResources().getString(R.string.loading));
        footer.setBackgroundColor(Color.TRANSPARENT);
        footer.findViewById(R.id.listview_loading).setVisibility(View.VISIBLE);
    }

    public static void showFooterTapToRetry(View footer) {
        showFooterMessage(footer, footer.getResources().getString(R.string.tapToRetry));
        footer.setBackgroundColor(footer.getResources().getColor(R.color.canvasBackgroundLight));
        footer.getBackground().setAlpha(50);
        footer.findViewById(R.id.listview_loading).setVisibility(View.GONE);
    }

    private static void showFooterMessage(View footer, String message) {
        //footer.findViewById(R.id.listview_loading_text).setVisibility(View.VISIBLE);

//        TextView textView = (TextView)footer.findViewById(R.id.listview_loading_text);
//        textView.setText(message);
//        textView.setTextColor(getColorConstant());
    }

    public static void hideFooter(View footer) {
        //footer.findViewById(R.id.listview_loading_text).setVisibility(View.GONE);
        footer.findViewById(R.id.listview_loading).setVisibility(View.GONE);
    }

    public static boolean footerIsTapToRetry(View footer) {
        return footerHasText(footer, R.string.tapToRetry);
    }

    public static boolean footerIsLoading(View footer) {
        return footerHasText(footer, R.string.loading);
    }

    private static boolean footerHasText(View footer, int textId) {
//        if (footer != null) {
//            Context context = footer.getContext();
//            TextView footerText = (TextView) footer.findViewById(R.id.listview_loading_text);
//            if (footerText.getText().equals(context.getString(textId))) {
//                return true;
//            }
//        }
        return false;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Color Helpers
    ///////////////////////////////////////////////////////////////////////////

    private static int getColorConstant() {
        return R.color.canvasTextDark;
    }
}
