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

package com.instructure.teacheraid.activities;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.instructure.canvasapi.api.UserAPI;
import com.instructure.canvasapi.model.User;
import com.instructure.canvasapi.utilities.APIStatusDelegate;
import com.instructure.canvasapi.utilities.CanvasCallback;
import com.instructure.canvasapi.utilities.UserCallback;
import com.instructure.teacheraid.R;
import com.instructure.teacheraid.asynctask.LogoutAsyncTask;
import com.instructure.teacheraid.fragments.CourseListFragment;
import com.instructure.teacheraid.util.Const;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit.client.Response;


public class BaseActivity extends ActionBarActivity implements APIStatusDelegate {

    private LinearLayout hotSeat;
    private LinearLayout guessWho;
    private LinearLayout studentNotes;

    private UserCallback getUserCallback;
    private CircleImageView avatar;
    private TextView userName;

    private int mLastActionbarColor = Integer.MAX_VALUE;
    private int mLastStatusBarColor = Integer.MAX_VALUE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        //If it's a phone only allow portrait
        if(!getResources().getBoolean(R.bool.isTablet)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        Toolbar toolbar = (Toolbar) findViewById(com.instructure.pandautils.R.id.toolbar);
        setSupportActionBar(toolbar);

        if(savedInstanceState != null) {
            findViewById(R.id.container).setVisibility(View.VISIBLE);
        }

        setupViews();
        setupListeners();
        setupCallbacks();
        UserAPI.getSelf(getUserCallback);

        mLastActionbarColor = getResources().getColor(R.color.mainBlue);
        mLastStatusBarColor = getResources().getColor(R.color.mainDarkBlue);
        setActionBarStatusBarColors(mLastActionbarColor, mLastStatusBarColor);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getSupportActionBar().setElevation(20);
        }
    }

    public void setActionBarTitle(String title) {
        getSupportActionBar().setTitle(title);
    }

    private void setupViews() {
        hotSeat = (LinearLayout) findViewById(R.id.hotSeat);
        guessWho = (LinearLayout) findViewById(R.id.guessWho);
        studentNotes = (LinearLayout) findViewById(R.id.studentNotes);

        avatar = (CircleImageView) findViewById(R.id.avatar);
        userName = (TextView) findViewById(R.id.username);
    }

    private void setupListeners() {
        hotSeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onItemSelected(0);
            }
        });

        guessWho.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onItemSelected(1);
            }
        });

        studentNotes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onItemSelected(2);
            }
        });
    }

    private void setupCallbacks() {
        getUserCallback = new UserCallback(this) {
            @Override
            public void cachedUser(User user) {
                user(user, null);
            }

            @Override
            public void user(User user, Response response) {
                if(user != null) {
                    userName.setText(user.getName());
                    Picasso.with(BaseActivity.this).load(user.getAvatarURL()).into(avatar);
                }
            }
        };
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(getSupportFragmentManager().getBackStackEntryCount() == 0) {
            findViewById(R.id.container).setVisibility(View.GONE);
            setActionBarTitle(getString(R.string.app_name));
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
    }

    public void onItemSelected(int position) {
        findViewById(R.id.container).setVisibility(View.VISIBLE);
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = getFragmentAtPosition(position);
        fragmentManager.beginTransaction()
                .replace(R.id.container, fragment)
                .addToBackStack(fragment.getTag())
                .commit();
    }

    public void swapFragment(Fragment fragment, String tag) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, fragment)
                .addToBackStack(tag)
                .commit();
    }

    public void removeFragments() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        findViewById(R.id.container).setVisibility(View.GONE);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        setActionBarTitle(getString(R.string.app_name));
    }

    private Fragment getFragmentAtPosition(int position) {
        switch (position) {
            case 0:
                //hot seat
                Bundle bundle = CourseListFragment.createBundle(Const.HOT_SEAT);
                CourseListFragment courseListFragment = new CourseListFragment();
                courseListFragment.setArguments(bundle);
                return courseListFragment;
            case 1:
                //guess who
                Bundle guessBundle = CourseListFragment.createBundle(Const.GUESS_WHO);
                CourseListFragment courseListFragmentGuessWho = new CourseListFragment();
                courseListFragmentGuessWho.setArguments(guessBundle);
                return courseListFragmentGuessWho;
            case 2:
                //Student Notes
                Bundle notesBundle = CourseListFragment.createBundle(Const.STUDENT_NOTES);
                CourseListFragment courseListFragmentNotes = new CourseListFragment();
                courseListFragmentNotes.setArguments(notesBundle);
                return courseListFragmentNotes;
            default:
                return null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.logout, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_logout) {
            new LogoutAsyncTask(BaseActivity.this, "").execute();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCallbackStarted() {

    }

    @Override
    public void onCallbackFinished(CanvasCallback.SOURCE source) {

    }

    @Override
    public void onNoNetwork() {

    }

    @Override
    public Context getContext() {
        return BaseActivity.this;
    }


    public void loadData(){};

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

    ///////////////////////////////////////////////////////////////////////////
    // Intents
    ///////////////////////////////////////////////////////////////////////////

    public static Intent createIntent(Context context) {
        Intent intent = new Intent(context, BaseActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        return intent;
    }

    public static Intent createIntent(Context context, Uri passedURI) {
        Intent intent = new Intent(context, BaseActivity.class);
        intent.putExtra(Const.PASSED_URI, passedURI);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        return intent;
    }
}
