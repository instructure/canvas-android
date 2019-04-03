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
import androidx.annotation.NonNull;

import com.instructure.androidpolling.app.R;
import com.instructure.androidpolling.app.util.ApplicationManager;
import com.instructure.androidpolling.app.util.Constants;
import com.instructure.canvasapi2.StatusCallback;
import com.instructure.canvasapi2.managers.CourseManager;
import com.instructure.canvasapi2.models.Course;
import com.instructure.canvasapi2.utils.ApiType;
import com.instructure.canvasapi2.utils.LinkHeaders;

import java.util.List;

public class StartingActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CourseManager.getAllFavoriteCourses(true, new StatusCallback<List<Course>>() {
            @Override
            public void onResponse(@NonNull retrofit2.Response<List<Course>> response, @NonNull LinkHeaders linkHeaders, @NonNull ApiType type) {
                ApplicationManager.saveCourses(StartingActivity.this, response.body());
                checkEnrollments(response.body());
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }

    //we need to know if the user is a teacher in any course
    //track the enrollments of the user as well
    private void checkEnrollments(List<Course> courses) {

        int teacherCount = 0;
        int studentCount = 0;

        for(Course course: courses) {
            if(course.isTeacher()) {
                teacherCount++;
                ApplicationManager.setHasTeacherEnrollment(getApplicationContext());
            }
            else {
                studentCount++;
                ApplicationManager.setHasStudentEnrollment(getApplicationContext());
            }
        }

        // Send it

        if(ApplicationManager.hasViewPreference(this)) {
            if(ApplicationManager.shouldShowTeacherView(this)) {
                startActivity(FragmentManagerActivity.createIntent(getApplicationContext()));
            }
            else {
                startActivity(PollListActivity.Companion.createIntent(getApplicationContext()));
            }
            finish();
            return;
        }
        else if(ApplicationManager.hasTeacherEnrollment(getApplicationContext())) {
            startActivity(FragmentManagerActivity.createIntent(getApplicationContext()));
        }
        else {
            startActivity(PollListActivity.Companion.createIntent(getApplicationContext()));
        }
        finish();
    }

    public static Intent createIntent(Context context) {
        Intent intent = new Intent(context, StartingActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        return intent;
    }

    public static Intent createIntent(Context context, Uri passedURI) {
        Intent intent = new Intent(context, StartingActivity.class);
        intent.putExtra(Constants.PASSED_URI, passedURI);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        return intent;
    }
}
