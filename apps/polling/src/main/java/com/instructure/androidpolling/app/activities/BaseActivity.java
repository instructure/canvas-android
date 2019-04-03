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

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.instructure.androidpolling.app.R;
import com.instructure.androidpolling.app.asynctasks.PollsLogoutTask;
import com.instructure.androidpolling.app.fragments.ParentFragment;
import com.instructure.androidpolling.app.util.ApplicationManager;
import com.instructure.canvasapi2.models.Enrollment;
import com.instructure.canvasapi2.models.Poll;
import com.instructure.canvasapi2.models.User;
import com.instructure.loginapi.login.tasks.LogoutTask;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public abstract class BaseActivity extends AppCompatActivity implements ParentFragment.OnUpdatePollListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ApplicationManager.trackActivity(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.overflow_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logout:
                new PollsLogoutTask(LogoutTask.Type.LOGOUT).execute();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void swapFragments(Fragment fragment, String fragmentTag) {
        swapFragments(fragment, fragmentTag, 0, 0, 0, 0);
    }
    public void swapFragments(Fragment fragment, String fragmentTag, int inAnimation, int outAnimation, int inPop, int outPop) {
        // Swap the fragment in the 'fragment_container' FrameLayout
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.setCustomAnimations(inAnimation, outAnimation, inPop, outPop);
        fragmentTransaction.replace(R.id.fragment_container, fragment, fragmentTag);

        fragmentTransaction.addToBackStack(fragmentTag);

        fragmentTransaction.commit();
        ApplicationManager.trackFragment(BaseActivity.this, fragment);
    }

    public void removeFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.remove(fragment);
        fragmentTransaction.commit();
        getSupportFragmentManager().popBackStack();
    }

    public void removeFragment(String tag) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(tag);
        fragmentTransaction.remove(fragment);
        fragmentTransaction.commit();
        getSupportFragmentManager().popBackStack();
    }

    //we need to know if the user is a teacher in any course
    public void checkEnrollments(User user) {
        if(user == null || user.getEnrollments() == null) {
            return;
        }
        for(Enrollment enrollment: user.getEnrollments()) {
            if(enrollment.isTeacher()) {
                ApplicationManager.hasTeacherEnrollment(this);

            }
            else {
                ApplicationManager.hasStudentEnrollment(this);
            }
        }
        invalidateOptionsMenu();
    }

    public void setActionBarTitle(String title) {
        getSupportActionBar().setTitle(title);
    }

    public void loadData(){
        if(getSupportFragmentManager().findFragmentById(R.id.fragment_container) != null) {
            ((ParentFragment) (getSupportFragmentManager().findFragmentById(R.id.fragment_container))).loadData();
        }
    }

    @Override
    public void onUpdatePoll(Poll poll, String fragmentTag) {
        ParentFragment fragment = (ParentFragment)getSupportFragmentManager().findFragmentByTag(fragmentTag);
        fragment.updatePoll(poll);
    }
}
