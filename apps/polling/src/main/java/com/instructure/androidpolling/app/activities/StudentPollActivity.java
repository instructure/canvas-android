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
 *
 */

package com.instructure.androidpolling.app.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;

import com.instructure.androidpolling.app.R;
import com.instructure.androidpolling.app.fragments.StudentPollFragment;
import com.instructure.androidpolling.app.util.Constants;
import com.instructure.canvasapi2.models.Poll;
import com.instructure.canvasapi2.models.PollSession;

public class StudentPollActivity extends PollListActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Check that the activity is using the layout version with
        // the fragment_container FrameLayout
        if (findViewById(R.id.fragment_container) != null) {

            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {
                return;
            }

            // Create a new Fragment to be placed in the activity layout
            StudentPollFragment firstFragment = new StudentPollFragment();

            // In case this activity was started with special instructions from an
            // Intent, pass the Intent's extras to the fragment as arguments
            firstFragment.setArguments(getIntent().getExtras());

            // Add the fragment to the 'fragment_container' FrameLayout
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, firstFragment).commit();
        }
    }

    public static Intent createIntent(Context context, Poll poll, PollSession pollSession, boolean hasSubmitted) {
        Intent intent = new Intent(context, StudentPollActivity.class);
        intent.putExtra(Constants.POLL_DATA, (Parcelable)poll);
        intent.putExtra(Constants.POLL_SESSION, (Parcelable)pollSession);
        intent.putExtra(Constants.HAS_SUBMITTED, hasSubmitted);
        return intent;
    }
}
