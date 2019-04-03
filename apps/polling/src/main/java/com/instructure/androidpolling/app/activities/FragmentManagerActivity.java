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
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.instructure.androidpolling.app.R;
import com.instructure.androidpolling.app.asynctasks.PollsLogoutTask;
import com.instructure.androidpolling.app.fragments.QuestionListFragment;
import com.instructure.androidpolling.app.util.ApplicationManager;
import com.instructure.canvasapi2.utils.ApiPrefs;
import com.instructure.loginapi.login.tasks.LogoutTask;

import androidx.fragment.app.FragmentTransaction;

import static com.instructure.androidpolling.app.R.menu.switch_to_student_view;

public class FragmentManagerActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Remember that the first view should be the teacher view
        ApplicationManager.setFirstView(this, true);

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
            QuestionListFragment firstFragment = new QuestionListFragment();

            // In case this activity was started with special instructions from an
            // Intent, pass the Intent's extras to the fragment as arguments
            firstFragment.setArguments(getIntent().getExtras());

            // Add the fragment to the 'fragment_container' FrameLayout
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.add(R.id.fragment_container, firstFragment, QuestionListFragment.class.getSimpleName());

            fragmentTransaction.commit();
        }

        checkEnrollments(ApiPrefs.getUser());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(switch_to_student_view, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logout:
                new PollsLogoutTask(LogoutTask.Type.LOGOUT).execute();
                break;
            case R.id.action_switch_to_student:
                startActivity(PollListActivity.Companion.createIntent(this));
        }
        return super.onOptionsItemSelected(item);
    }

    public static Intent createIntent(Context context) {
        Intent intent = new Intent(context, FragmentManagerActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        return intent;
    }

    public static Intent createIntent(Context context, Uri passedURI) {
        Intent intent = new Intent(context, FragmentManagerActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        intent.putExtra("passedURI", passedURI);
        return intent;
    }
}
