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

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;

import com.instructure.student.R;
import com.instructure.student.fragment.NotificationListFragment;
import com.instructure.canvasapi2.models.StreamItem;
import com.instructure.pandautils.utils.Const;

public class NotificationWidgetRouter extends ParentActivity {

    private StreamItem streamItem;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handleIntent();
        if (streamItem != null) NotificationListFragment.addFragmentForStreamItem(streamItem, getContext(), true);
        finish();
    }

    protected void handleIntent() {
        Intent intent = getIntent();
        if (intent.hasExtra(Const.STREAM_ITEM)) {
            streamItem = intent.getParcelableExtra(Const.STREAM_ITEM);
        }
    }

    public static Intent createIntent(Context context, StreamItem streamItem) {
        Intent intent = Companion.createIntent(context, NotificationWidgetRouter.class, R.layout.notification_widget_router_empty);
        intent.putExtra(Const.STREAM_ITEM,  (Parcelable)streamItem);
        return intent;
    }

    @Override
    public int contentResId() {
        return 0;
    }

    @Override
    public boolean showHomeAsUp() {
        return false;
    }

    @Override
    public boolean showTitleEnabled() {
        return false;
    }

    @Override
    public void onUpPressed() {}
}
