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

package com.instructure.speedgrader.activities;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;
import android.support.v7.widget.SwitchCompat;
import com.instructure.canvasapi.api.UserAPI;
import com.instructure.canvasapi.model.Assignment;
import com.instructure.canvasapi.model.CanvasColor;
import com.instructure.canvasapi.model.CanvasContext;
import com.instructure.canvasapi.model.User;
import com.instructure.canvasapi.utilities.APIHelpers;
import com.instructure.canvasapi.utilities.CanvasCallback;
import com.instructure.canvasapi.utilities.LinkHeaders;
import com.instructure.canvasapi.utilities.Masquerading;
import com.instructure.canvasapi.utilities.UserCallback;
import com.instructure.loginapi.login.api.zendesk.utilities.ZendeskDialogStyled;
import com.instructure.loginapi.login.util.Utils;
import com.instructure.pandautils.utils.CanvasContextColor;
import com.instructure.pandautils.utils.PermissionUtils;
import com.instructure.pandautils.utils.TutorialUtils;
import com.instructure.speedgrader.R;
import com.instructure.speedgrader.asynctasks.LogoutAsyncTask;
import com.instructure.speedgrader.fragments.AssignmentListFragment;
import com.instructure.speedgrader.fragments.CourseGridFragment;
import com.instructure.speedgrader.fragments.ParentFragment;
import com.instructure.speedgrader.interfaces.OnSettingsChangedListener;
import com.instructure.speedgrader.util.App;
import com.instructure.speedgrader.util.Const;
import com.instructure.speedgrader.views.HelveticaTextView;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import java.io.File;
import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class HomeActivity extends ParentActivity implements ZendeskDialogStyled.ZendeskDialogResultListener{

    public ActionBarDrawerToggle mDrawerToggle;
    public DrawerLayout mDrawerLayout;
    private ScrollView mDrawer;
    private CircleImageView avatar;
    private HelveticaTextView userName;
    private UserCallback userCallback;
    private Target avatarTarget;
    private RelativeLayout aboutButton;
    private RelativeLayout helpButton;
    private RelativeLayout reportProblemButton;
    private LinearLayout navigationHeader;
    private RelativeLayout logoutButton;
    private SharedPreferences preferences;
    private LinearLayout   subHeader;
    private boolean showingAbout = false;

    //Masquerading
    private RelativeLayout masquerade;
    private ImageButton btnMasquerade;
    private GestureDetector gesture;
    private View.OnTouchListener gestureListener;
    private long first = 0;
    private long second = 0;
    private boolean firstFree = true;
    private EditText masqueradeId;

    protected CanvasCallback<CanvasColor> courseColorsCallback;

    /////////////////////////////////////////////////////////////////
    //                  LifeCycle
    /////////////////////////////////////////////////////////////////
    @Override
    public int getRootLayout() {
        return R.layout.activity_home;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            addFragment(ParentFragment.newInstance(CourseGridFragment.class, new Bundle()));
        }

        route(getIntent());
        setupCallbacks();
        UserAPI.getColors(getApplicationContext(), courseColorsCallback);
        initNavigationDrawer();
        initDrawerToggle();
        doPermissionChecks();
    }

    @TargetApi(23)
    private void doPermissionChecks() {
        if(!PermissionUtils.hasPermissions(HomeActivity.this, PermissionUtils.WRITE_EXTERNAL_STORAGE)) {
            requestPermissions(PermissionUtils.makeArray(PermissionUtils.WRITE_EXTERNAL_STORAGE), PermissionUtils.PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == PermissionUtils.PERMISSION_REQUEST_CODE) {
            if(!PermissionUtils.permissionGranted(permissions, grantResults, PermissionUtils.WRITE_EXTERNAL_STORAGE)) {
                finish();
            }
        }
    }

    @Override
    public void loadData() {
        ((ParentFragment)getSupportFragmentManager().findFragmentById(R.id.content_frame)).loadData();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    private void setupCallbacks(){
        avatarTarget = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom loadedFrom) {
                avatar.setImageBitmap(bitmap);
            }

            @Override
            public void onBitmapFailed(Drawable drawable) {
                avatar.setImageResource(R.drawable.ic_cv_student);
            }

            @Override
            public void onPrepareLoad(Drawable drawable) {
            }
        };

        userCallback = new UserCallback(this) {
            @Override
            public void cachedUser(User user) {
                if (user != null && user.getAvatarURL() != null) {
                    Picasso.with(getContext()).load(user.getAvatarURL()).into(avatarTarget);
                    userName.setText(user.getName());
                }
            }

            @Override
            public void user(User user, Response response) {
                Picasso.with(getContext()).load(user.getAvatarURL()).into(avatarTarget);
                userName.setText(user.getName());
            }
        };

        courseColorsCallback = new CanvasCallback<CanvasColor>(this) {
            @Override
            public void cache(CanvasColor canvasColor) {
            }

            @Override
            public void firstPage(CanvasColor canvasColor, LinkHeaders linkHeaders, Response response) {
                if(response.getStatus() == 200) {
                    //Replaces the current cache with the updated fresh one from the api.
                    CanvasContextColor.addToCache(canvasColor);
                    //Sends a broadcast so the course grid can refresh it's colors if needed.
                    //When first logging in this will probably get called/return after the courses.
                    Intent intent = new Intent(com.instructure.pandautils.utils.Const.COURSE_THING_CHANGED);
                    Bundle extras = new Bundle();
                    extras.putBoolean(com.instructure.pandautils.utils.Const.COURSE_COLOR, true);
                    intent.putExtras(extras);
                    LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
                }
            }
        };
    }

    /////////////////////////////////////////////////////////////////
    //                  ActionBar
    /////////////////////////////////////////////////////////////////
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the HomeActivity/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getSupportActionBar().setTitle(mTitle);
    }

    public void hideDrawer(){
        mDrawerToggle.setDrawerIndicatorEnabled(false);
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }

    public void showDrawer(){
        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
    }

    /////////////////////////////////////////////////////////////////
    //              Intent
    /////////////////////////////////////////////////////////////////
    @Override
    public void handleIntent(Intent intent){}

    public static Intent createIntent(Context context, Uri passedURI) {
        Intent intent = new Intent(context, HomeActivity.class);
        intent.putExtra("passedURI", passedURI);
        return intent;
    }

    public static Intent createIntent(Context context) {
        Intent intent = new Intent(context, HomeActivity.class);
        return intent;
    }

    public static Intent createIntent(Context context, Bundle bundle){
        Intent intent = new Intent(context, HomeActivity.class);
        intent.putExtras(bundle);
        return intent;
    }

    /////////////////////////////////////////////////////////////////
    //                  Zendesk
    /////////////////////////////////////////////////////////////////
    @Override
    public void onTicketPost() {
        Toast.makeText(HomeActivity.this, R.string.zendesk_feedbackThankyou, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onTicketError() {
        Toast.makeText(HomeActivity.this, R.string.errorOccurred, Toast.LENGTH_SHORT).show();
    }

    /////////////////////////////////////////////////////////////////
    //                  Navigation Drawer
    /////////////////////////////////////////////////////////////////
    //TODO : This Settings drawer is getting too big. Should at some point be moved into a separate fragment.

    private void initDrawerToggle(){
        //Navigation Drawer Toggle
        mDrawerLayout       = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawer             = (ScrollView) findViewById(R.id.left_drawer);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getSupportActionBar().setTitle(getCourseTitle());
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getSupportActionBar().setTitle(getString(R.string.settings));
                invalidateOptionsMenu();
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    public void initNavigationDrawer(){
        // User Data
        avatar              = (CircleImageView) findViewById(R.id.profile_image);
        userName            = (HelveticaTextView) findViewById(R.id.userName);
        aboutButton         = (RelativeLayout) findViewById(R.id.aboutButton);
        helpButton          = (RelativeLayout) findViewById(R.id.helpButton);
        reportProblemButton = (RelativeLayout) findViewById(R.id.reportProblemButton);
        navigationHeader    = (LinearLayout)   findViewById(R.id.navigationHeader);

        UserAPI.getSelf(userCallback);

        intAboutPage();
        initSettingsListeners();
        initLogoutButton();
        initMasquerading();
        // Help Button
        helpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(InternalWebviewActivity.createIntent(getContext(), com.instructure.pandautils.utils.Const.CANVAS_USER_GUIDES, false));
                overridePendingTransition(R.anim.slide_down, 0);
            }
        });

        reportProblemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ZendeskDialogStyled dialog = new ZendeskDialogStyled();
                dialog.show(getSupportFragmentManager(), ZendeskDialogStyled.TAG);
            }
        });
    }

    private String getCourseTitle() {
        if (preferences == null) {
            preferences = getSharedPreferences(App.PREF_FILE_NAME, Context.MODE_PRIVATE);
        }
        CourseGridFragment.COURSE_VIEW course_view = CourseGridFragment.COURSE_VIEW.values()[preferences.getInt("course_view", 0)];
        if (course_view == CourseGridFragment.COURSE_VIEW.FAVORITE_COURSES) {
            return getString(R.string.favorites);
        }
        return getString(R.string.allCourses);
    }

    public void intAboutPage(){

        final ImageView helpButtonArrow  = (ImageView) findViewById(R.id.helpButtonArrow);
        final ImageView reportProblemButtonArrow  = (ImageView) findViewById(R.id.reportProblemButtonArrow);
        final ImageView aboutArrow       = (ImageView) findViewById(R.id.aboutButtonArrow);
        final ImageView aboutArrowBack   = (ImageView) findViewById(R.id.aboutButtonArrowBack);

        // Set the colors for our arrows
        final Drawable arrow      = CanvasContextColor.getColoredDrawable(getContext(), R.drawable.ic_cv_arrow_right, getContext().getResources().getColor(R.color.sg_lightGrayText));
        final Drawable arrowBack  = CanvasContextColor.getColoredDrawable(getContext(), R.drawable.ic_cv_arrow_left, getContext().getResources().getColor(R.color.sg_lightGrayText));
        helpButtonArrow.setImageDrawable(arrow);
        reportProblemButtonArrow.setImageDrawable(arrow);
        aboutArrowBack.setImageDrawable(arrowBack);
        aboutArrow.setImageDrawable(arrow);

        final ViewSwitcher aboutViewSwitcher  = (ViewSwitcher) findViewById(R.id.aboutViewSwitcher);
        final HelveticaTextView versionNumber = (HelveticaTextView) findViewById(R.id.sg_about_version_text);
        final HelveticaTextView accountName   = (HelveticaTextView) findViewById(R.id.sg_about_account_text);
        // Set Version Number
        try{
            versionNumber.setText( getPackageManager().getPackageInfo(getPackageName(),0).versionName + " (" + getPackageManager().getPackageInfo(getPackageName(), 0).versionCode + ")");
        }catch(Exception e){
            versionNumber.setText("");
        }

        // Copyright Text
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        HelveticaTextView copyrightText = (HelveticaTextView) findViewById(R.id.sg_about_copyright_text);
        String yearText = String.format(getString(R.string.copyrightText,String.valueOf(year)));
        copyrightText.setText(yearText);

        accountName.setText(APIHelpers.getCacheUser(getContext()).getEmail());

        aboutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!showingAbout){
                    showingAbout = !showingAbout;

                    aboutViewSwitcher.setInAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.slide_in_left));
                    aboutViewSwitcher.setOutAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.slide_out_right));

                    aboutViewSwitcher.setDisplayedChild(1);
                    aboutArrowBack.setVisibility(View.VISIBLE);
                    aboutArrow.setVisibility(View.INVISIBLE);
                }else{
                    showingAbout = !showingAbout;
                    // left to right
                    aboutViewSwitcher.setInAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.slide_in_right));
                    aboutViewSwitcher.setOutAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.slide_out_left));

                    aboutViewSwitcher.setDisplayedChild(0);
                    aboutArrowBack.setVisibility(View.GONE);
                    aboutArrow.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    public void initSettingsListeners() {
        final SwitchCompat hideNamesSwitch = (SwitchCompat) findViewById(R.id.sg_options_hideNames_switch);
        final SwitchCompat showUngradedFirstSwitch = (SwitchCompat) findViewById(R.id.sg_options_showUngradedFirst_switch);
        final SwitchCompat showUngradedCountSwitch = (SwitchCompat) findViewById(R.id.sg_options_viewUngradedCount_switch);

        final HelveticaTextView hideNamesLabel = (HelveticaTextView) findViewById(R.id.sg_options_hideNames);
        final HelveticaTextView showUngradedFirstLabel = (HelveticaTextView) findViewById(R.id.sg_options_showUngradedFirst);
        final HelveticaTextView viewUngradedCountLabel = (HelveticaTextView) findViewById(R.id.sg_options_viewUngradedCount);
        final ImageView pulse = (ImageView) findViewById(R.id.pulse);

        new TutorialUtils(HomeActivity.this, App.getPrefs(), pulse, TutorialUtils.TYPE.COLOR_CHANGING_DIALOG)
                .setContent(getString(R.string.tutorial_tipHideStudentNames), getString(R.string.tutorial_tipHideStudentNamesMessage))
                .build();
        hideNamesLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideNamesSwitch.toggle();
            }
        });

        showUngradedFirstLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showUngradedFirstSwitch.toggle();
            }
        });

        viewUngradedCountLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showUngradedCountSwitch.toggle();
            }
        });

        // Set Switch defaults
        final App applicationManager = (App) getApplication();

        // Set Default values for our switches
        hideNamesSwitch        .setChecked(applicationManager.showStudentNames());
        showUngradedFirstSwitch.setChecked(applicationManager.showUngradedStudentsFirst());
        showUngradedCountSwitch.setChecked(applicationManager.showUngradedCount());

        // set On check changed listeners
        hideNamesSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                applicationManager.setShowStudentNames(isChecked);
            }
        });

        showUngradedFirstSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                applicationManager.setShowUngradedStudentsFirst(isChecked);
            }
        });

        showUngradedCountSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                applicationManager.setShowUngradedCount(isChecked);
                if (getSupportFragmentManager().findFragmentById(R.id.content_frame) instanceof OnSettingsChangedListener) {
                    ((OnSettingsChangedListener) getSupportFragmentManager().findFragmentById(R.id.content_frame)).onShowUngradedCountChanged(isChecked);
                }
            }
        });
    }

    public void initLogoutButton(){
        // Inflate Navigation Menu Items
        ImageView logoutIcon = (ImageView) findViewById(R.id.logoutIcon);
        Drawable logout = CanvasContextColor.getColoredDrawable(getContext(),R.drawable.ic_cv_import, getContext().getResources().getColor(R.color.white));
        logoutIcon.setImageDrawable(logout);
        // Logout Button
        RelativeLayout logoutButton = (RelativeLayout) findViewById(R.id.navigationFooter);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new LogoutAsyncTask(HomeActivity.this, null ).execute();
            }
        });
    }

    private void route(Intent intent){
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            if (bundle != null && bundle.containsKey(com.instructure.loginapi.login.util.Const.ROUTING_CANVAS_CONEXT)
                    && bundle.containsKey(com.instructure.loginapi.login.util.Const.ROUTING_ASSINGMENT)) {
                addFragment(ParentFragment.newInstance(AssignmentListFragment.class,
                        AssignmentListFragment.createBundle((CanvasContext) bundle.getParcelable(com.instructure.loginapi.login.util.Const.ROUTING_CANVAS_CONEXT),
                                (Assignment)bundle.getParcelable(com.instructure.loginapi.login.util.Const.ROUTING_ASSINGMENT))));
            }
        }
    }

    public void reinitViews(){
        initNavigationDrawer();
        ((ParentFragment)getSupportFragmentManager().findFragmentById(R.id.content_frame)).loadData();
    }

    public void initMasquerading(){
        btnMasquerade = (ImageButton)    findViewById(R.id.btn_masquerade);
        masquerade    = (RelativeLayout) findViewById(R.id.masquerade);
        masqueradeId  = (EditText)       findViewById(R.id.masqueradeId);
        logoutButton  = (RelativeLayout) findViewById(R.id.navigationFooter);
        subHeader     = (LinearLayout)   findViewById(R.id.subHeader);

        int red  = getResources().getColor(R.color.masqueradeRed);
        int blue = getResources().getColor(R.color.speedgrader_aqua);

        // Setup masquerading views
        if(Masquerading.isMasquerading(this)) {
            masquerade.setVisibility(View.VISIBLE);
            masqueradeId.setText(String.valueOf(Masquerading.getMasqueradingId(this)));
            btnMasquerade.setImageDrawable(CanvasContextColor.getColoredDrawable(this, R.drawable.ic_cv_login_x, red));
            subHeader.setBackgroundColor(getResources().getColor(R.color.masqueradeRed));
            navigationHeader.setBackgroundColor(red);
            logoutButton.setBackgroundColor(red);
            setActionBarColor(getResources().getColor(R.color.masqueradeRed));

        }else{
            btnMasquerade.setImageDrawable(CanvasContextColor.getColoredDrawable(this, R.drawable.ic_cv_arrow_right, blue));
            logoutButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.logout_bg));
            navigationHeader.setBackgroundColor(blue);
            setActionBarColor(getResources().getColor(R.color.sg_defaultPrimary));
            subHeader.setBackgroundColor(blue);
        }

        //Set up gesture for the two finger double tap to show the masquerading option
        gesture = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener(){
            public boolean onDown(MotionEvent event) {
                return true;
            }
        });

        gestureListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gesture.onTouchEvent(event);
            }
        };

        findViewById(R.id.aboutContent).setOnTouchListener(gestureListener);

        masqueradeId.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_GO) {
                    btnMasquerade.performClick();
                    return true;
                }
                return false;
            }
        });

        btnMasquerade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Masquerading.isMasquerading(HomeActivity.this)) {
                    masquerade.setVisibility(View.GONE);
                    masqueradeId.setText("");
                    Masquerading.stopMasquerading(HomeActivity.this);

                    //delete the cache for the masqueraded user
                    File cacheDir = new File(getFilesDir(), "cache_masquerade");
                    //need to delete the contents of the internal cache folder so previous user's results don't show up on incorrect user
                    com.instructure.canvasapi.utilities.FileUtilities.deleteAllFilesInDirectory(cacheDir);

                    //clear any shared preferences for the masqueraded user
                    SharedPreferences masq_settings = getSharedPreferences(App.MASQ_PREF_NAME, 0);
                    SharedPreferences.Editor masq_editor = masq_settings.edit();
                    masq_editor.clear();
                    masq_editor.commit();
                    reinitViews();
                }
                else {
                    if(masqueradeId.getText().toString().trim().length() == 0) {
                        Toast.makeText(HomeActivity.this, R.string.emptyId, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    //Actually try to masquerade.
                    long id = 0;
                    if(masqueradeId.getText().toString().trim().length() > 0) {
                        try {
                            id = Long.parseLong(masqueradeId.getText().toString());
                        }
                        catch(NumberFormatException e) {
                            id = 0;
                        }
                    }

                    Masquerading.startMasquerading(id, HomeActivity.this, new CanvasCallback<User>(APIHelpers.statusDelegateWithContext(HomeActivity.this)) {
                        @Override
                        public void cache(User user) {}

                        @Override
                        public void firstPage(User user, LinkHeaders linkHeaders, Response response) {

//                            Make sure we got a valid user back.
                            if(user != null && user.getId() > 0) {

                                APIHelpers.setCacheUser(HomeActivity.this, user);

                                reinitViews();
                            }
                            else{
                                onFailure(null);
                            }
                        }
//
                        @Override
                        public boolean onFailure(RetrofitError retrofitError) {
                            Masquerading.stopMasquerading(HomeActivity.this);
                            Toast.makeText(HomeActivity.this, R.string.masqueradeFail, Toast.LENGTH_SHORT).show();
                            return true;
                        }
                    }, APIHelpers.getDomain(getContext()));
                }
            }
        });
    }

    public boolean onTouchEvent(MotionEvent event) {
        try {
            int action = event.getAction() & MotionEvent.ACTION_MASK;
            //capture the event when the user lifts their fingers, not on the down press
            //to make sure they're not long pressing
            if (action == MotionEvent.ACTION_POINTER_UP) {
                //timer to get difference between clicks
                Calendar now = Calendar.getInstance();
                //detect number of fingers, change to 1 for a single-finger double-click, 3 for a triple-finger double-click!
                if (event.getPointerCount() == 2) {
                    firstFree = !firstFree;

                    if (firstFree) {
                        //if this is the first click, then there hasn't been a second
                        //click yet, also record the time
                        first = now.getTimeInMillis();
                    } else  {
                        //if this is the second click, record its time
                        second = now.getTimeInMillis();
                    }

                    //if the difference between the 2 clicks is less than 500 ms (1/2 second)
                    //Math.abs() is used because you need to be able to detect any sequence of clicks, rather than just in pairs of two
                    //(e.g. click1 could be registered as a second click if the difference between click1 and click2 > 500 but
                    //click2 and the next click1 is < 500)
                    if (Math.abs(second-first) < 500) {
                        if(masquerade.getVisibility() == View.GONE) {
                            masquerade.setVisibility(View.VISIBLE);
                            //put the focus on the edit text for masquerading
                            masqueradeId.requestFocus();
                        }
                    }
                }
            }
        } catch (Exception e){
            Utils.e("Error: " + e);
        }
        return true;
    }
}
