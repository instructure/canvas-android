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

package com.instructure.speedgrader.fragments;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.support.v7.widget.SwitchCompat;
import android.widget.RelativeLayout;
import android.widget.ViewFlipper;
import java.util.ArrayList;
import com.instructure.canvasapi.api.CourseAPI;
import com.instructure.canvasapi.model.CanvasContext;
import com.instructure.canvasapi.model.Course;
import com.instructure.canvasapi.model.Enrollment;
import com.instructure.canvasapi.utilities.CanvasCallback;
import com.instructure.canvasapi.utilities.LinkHeaders;
import com.instructure.pandautils.utils.CanvasContextColor;
import com.instructure.pandautils.utils.Const;
import com.instructure.pandautils.utils.TutorialUtils;
import com.instructure.speedgrader.R;
import com.instructure.speedgrader.activities.HomeActivity;
import com.instructure.speedgrader.activities.ParentActivity;
import com.instructure.speedgrader.adapters.CourseColorAdapter;
import com.instructure.speedgrader.interfaces.OnSettingsChangedListener;
import com.instructure.speedgrader.util.AnimationFactory;
import com.instructure.speedgrader.util.App;
import com.instructure.speedgrader.views.CircularTextView;
import com.instructure.speedgrader.views.CourseColorGridView;
import com.instructure.speedgrader.views.HelveticaTextView;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import retrofit.client.Response;

public class CourseGridFragment extends ParentFragment implements CompoundButton.OnCheckedChangeListener, OnSettingsChangedListener {

    //Display Options
    public static enum COURSE_VIEW {FAVORITE_COURSES, ALL_COURSES}
    private COURSE_VIEW course_view = COURSE_VIEW.FAVORITE_COURSES;

    //callback
    private CanvasCallback<Course[]> favoriteCoursesCallback;
    private CanvasCallback<Course[]> allCoursesCallback;

    //track them so we can toggle between them
    private Course[] allCourses;
    private Course[] favoriteCourses;

    //We have to make sure courses is called.
    private Map<Long, Course> savedCourses;
    private GridView coursesGrid;
    private CourseGridAdapter courseGridAdapter;
    private int contentWidth = 0;
    SwitchCompat coursesSwitch;

    SharedPreferences preferences;

    ///////////////////////////////////////////////////////////////////////////
    // LifeCycle & Parent Fragment Overrides
    ///////////////////////////////////////////////////////////////////////////
    @Override
    public boolean retainInstanceState() {
        return true;
    }

    @Override
    public int getRootLayout() {
        return R.layout.fragment_coursegrid;
    }

    @Override
    public void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(somethingChangedReceiver, new IntentFilter(Const.COURSE_THING_CHANGED));
    }

    @Override
    public void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(somethingChangedReceiver);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getCoursePreference();
        loadData();
        courseGridAdapter = new CourseGridAdapter(getContext(), new ArrayList<Course>(),(App) getActivity().getApplication());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        ((ParentActivity)getActivity()).getSupportActionBar().setTitle(getCourseTitle());
        coursesGrid = (GridView)mRootView.findViewById(R.id.courses_grid);
        setGridViewPadding(getNumColumns());

        coursesGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ParentFragment fragment = ParentFragment.newInstance(AssignmentListFragment.class, AssignmentListFragment.createBundle((CanvasContext) courseGridAdapter.getItem(position)));
                ((HomeActivity) getActivity()).showFragment(fragment);
            }
        });


        coursesGrid.setAdapter(courseGridAdapter);

        if(getActivity() instanceof HomeActivity){
            ((HomeActivity)getActivity()).showDrawer();
        }

        handleActionBarColor();
        return mRootView;
    }

    public void loadData(){
        CourseAPI.getAllFavoriteCourses(favoriteCoursesCallback);
        CourseAPI.getAllCourses(allCoursesCallback);

    }
    public void handleActionBarColor(){
        ((ParentActivity)getActivity()).setActionBarColor(getResources().getColor(R.color.sg_defaultPrimary));

    }

    private int getNumColumns(){
        return getResources().getInteger(R.integer.courseColumns);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getCoursePreference();
    }

    @Override
    public void onPause() {
        super.onPause();
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("course_view", course_view.ordinal());
        editor.commit();
    }

    @Override
    public void onResume() {
        super.onResume();
        //set the course view to be what they viewed last time (all courses or favorite courses)
        getCoursePreference();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void setupCallbacks() {
        favoriteCoursesCallback = new CanvasCallback<Course[]>(this) {
            @Override
            public void cache(Course[] courses, LinkHeaders linkHeaders, Response response) {
                //We use get resources, so check for null.
                if(getActivity() == null || coursesGrid == null) return;

                favoriteCourses = getGraderEnrollments(courses);
                if(course_view == COURSE_VIEW.FAVORITE_COURSES){
                    savedCourses = CourseAPI.createCourseMap(favoriteCourses);
                    courseGridAdapter.addAll(favoriteCourses);
                    coursesGrid.setAdapter(courseGridAdapter);
                }
            }

            @Override
            public void firstPage(Course[] courses, LinkHeaders linkHeaders, Response response) {
                //We use get resources, so check for null.
                if(getActivity() == null || coursesGrid == null) return;

                favoriteCourses = getGraderEnrollments(courses);
                if(course_view == COURSE_VIEW.FAVORITE_COURSES){
                    savedCourses = CourseAPI.createCourseMap(favoriteCourses);
                    courseGridAdapter.addAll(favoriteCourses);
                    coursesGrid.setAdapter(courseGridAdapter);
                }
            }
        };

        allCoursesCallback = new CanvasCallback<Course[]>(this) {
            @Override
            public void cache(Course[] courses, LinkHeaders linkHeaders, Response response) {
                if(!isAdded()){return;}

                allCourses = getGraderEnrollments(courses);
                if(course_view == COURSE_VIEW.ALL_COURSES) {
                    savedCourses = CourseAPI.createCourseMap(allCourses);
                    courseGridAdapter.addAll(allCourses);
                    coursesGrid.setAdapter(courseGridAdapter);
                }
            }

            @Override
            public void firstPage(Course[] courses, LinkHeaders linkHeaders, Response response) {
                if(!isAdded()) return;

                allCourses = getGraderEnrollments(courses);
                if(course_view == COURSE_VIEW.ALL_COURSES) {
                    savedCourses = CourseAPI.createCourseMap(allCourses);
                    courseGridAdapter.addAll(allCourses);
                    coursesGrid.setAdapter(courseGridAdapter);
                }
            }
        };
    }

    private String getCourseTitle() {
        if (preferences == null) {
            preferences = getActivity().getSharedPreferences(App.PREF_FILE_NAME, Context.MODE_PRIVATE);
        }
        CourseGridFragment.COURSE_VIEW course_view = CourseGridFragment.COURSE_VIEW.values()[preferences.getInt("course_view", 0)];
        if (course_view == CourseGridFragment.COURSE_VIEW.FAVORITE_COURSES) {
            return getString(R.string.favorites);
        }
        return getString(R.string.allCourses);
    }
    ///////////////////////////////////////////////////////////////////////////
    // Settings Changed
    ///////////////////////////////////////////////////////////////////////////
    @Override
    public void onHideNamesChanged(boolean shouldShowNames) {}

    @Override
    public void onShowUngradedFirstChanged(boolean showUngradedFirst) {}

    @Override
    public void onShowHTMLChanged(boolean showHtml) {}

    @Override
    public void onShowUngradedCountChanged(boolean showUngradedCount) {
        courseGridAdapter.notifyDataSetChanged();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Favorite / All Courses Switcher
    ///////////////////////////////////////////////////////////////////////////
    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        getActivity().getMenuInflater().inflate(R.menu.home, menu);

        // Setup favorite courses switch
        coursesSwitch = (SwitchCompat) menu.findItem(R.id.favoriteSwitch).getActionView().findViewById(R.id.courseSwitcher);
        ImageView pulse = (ImageView) menu.findItem(R.id.favoriteSwitch).getActionView().findViewById(R.id.pulse);

        new TutorialUtils(getActivity(), App.getPrefs(), pulse, TutorialUtils.TYPE.SORTING_FAVORITES)
                .setContent(getString(R.string.tutorial_tipMyCoursesTitle), getString(R.string.tutorial_tipMyCoursesMessage))
                .build();
        if(course_view == COURSE_VIEW.ALL_COURSES){
            coursesSwitch.setChecked(true);
        }else{
            coursesSwitch.setChecked(false);
        }
        coursesSwitch.setOnCheckedChangeListener(this);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if(allCourses == null || favoriteCourses == null){return;}

        toggleCourseView();

        if(course_view == COURSE_VIEW.FAVORITE_COURSES){
            if(favoriteCourses == null) {
                setupCallbacks();
                CourseAPI.getAllFavoriteCourses(favoriteCoursesCallback);
            }
            courseGridAdapter.addAll(favoriteCourses);
            ((ParentActivity)getActivity()).getSupportActionBar().setTitle(getString(R.string.favorites));
        }else{
            if(allCourses != null){
                setupCallbacks();
                CourseAPI.getAllCourses(allCoursesCallback);
            }
            courseGridAdapter.addAll(allCourses);
            ((ParentActivity)getActivity()).getSupportActionBar().setTitle(getString(R.string.allCourses));
        }
    }

    public void toggleCourseView(){
        if(course_view == COURSE_VIEW.ALL_COURSES){
            course_view = COURSE_VIEW.FAVORITE_COURSES;
        }else{
            course_view = COURSE_VIEW.ALL_COURSES;
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Helpers
    ///////////////////////////////////////////////////////////////////////////
    public Course[] getGraderEnrollments(Course[] courses){
        List result = new LinkedList();
        for(Course course : courses){
            for(Enrollment enrollment : course.getEnrollments()){
                if(enrollment.isTA() || enrollment.isTeacher()){
                    result.add(course);
                    break;
                }
            }
        }
        return (Course[]) result.toArray(new Course[result.size()]);
    }

    public void getCoursePreference(){
        if(preferences == null){
            preferences =  getActivity().getSharedPreferences(App.PREF_FILE_NAME, Context.MODE_PRIVATE);
        }
        course_view = COURSE_VIEW.values()[preferences.getInt("course_view", 0)];
    }

    public void setGridViewPadding(int numColumns) {
        // Convert DIPs to pixels
        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int mSizePx =  Math.round(getActivity().getResources().getDimension(R.dimen.sg_grid_column_width));
        int mSpacingPx = (int) Math.floor(getActivity().getResources().getDimension(R.dimen.sg_grid_horizontal_spacing)*metrics.scaledDensity);

        // Find out the extra space gridview uses for selector on its sides.
        Rect p = new Rect();
        coursesGrid.getSelector().getPadding(p);
        int selectorPadding = p.left + p.right;

        contentWidth = numColumns * mSizePx; // Width of items
        contentWidth += (numColumns - 1) * mSpacingPx; // Plus spaces between items
        contentWidth += selectorPadding; // Plus extra space for selector on sides

        /*      Now calculate amount of left and right margin so the grid gets
                centered. This is what we
                unfortunately cannot do with layout_width="wrap_content"
                and layout_gravity="center_horizontal"
        */
        int slack = metrics.widthPixels - contentWidth;

        coursesGrid.setNumColumns(numColumns);
        coursesGrid.setPadding(slack / 2, mSpacingPx, slack / 2, mSpacingPx);
        //not sure why 14 works, but it lines up the divider line with the gridview items
        contentWidth -= 14;
    }

    private BroadcastReceiver somethingChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if(intent != null && courseGridAdapter != null) {
                Bundle extras = intent.getExtras();
                if (extras != null) {
                    boolean courseColorChanged = extras.getBoolean(Const.COURSE_COLOR, false);
                    boolean courseFavoriteChanged = extras.getBoolean(Const.COURSE_FAVORITES, false);
                    boolean needToNotifyDataSetChanged = courseColorChanged | courseFavoriteChanged;

                    courseGridAdapter.notifyDataSetChanged();
                }
            }
        }
    };

    ///////////////////////////////////////////////////////////////////////////
    // Grid Adapter
    ///////////////////////////////////////////////////////////////////////////
    private class CourseGridAdapter extends BaseAdapter{

        private Context context;
        private ArrayList<Course> courses;
        private boolean showUnGradedCount;
        private App applicationManager;

        public CourseGridAdapter(Context context, ArrayList<Course> courses, App applicationManager){
            this.courses = courses;
            this.context = context;
            this.showUnGradedCount = applicationManager.showUngradedCount();
            this.applicationManager = applicationManager;
        }

        @Override
        public void notifyDataSetChanged() {
            this.showUnGradedCount = applicationManager.showUngradedCount();
            super.notifyDataSetChanged();
        }

        public void addItem(Course course){
            courses.add(course);
            notifyDataSetChanged();
        }

        public void addAll(Course[] courseList){

            ArrayList<Course> allCourses =  new ArrayList<Course>(Arrays.asList(courseList));
            this.courses = allCourses;
            notifyDataSetChanged();
        }

        //flip the gridview item to view the other side
        public void flipView(ViewFlipper v, AnimationFactory.FlipDirection direction) {
            AnimationFactory.flipTransition(v, direction);
            v.invalidate();
        }
        @Override
        public int getCount() {
            return courses.size();
        }

        @Override
        public Object getItem(int position) {
            return courses.get(position);
        }

        @Override
        public long getItemId(int position) {
            return courses.get(position).getId();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder viewHolder;
            final Course currentCourse = courses.get(position);
            if(convertView == null){
                viewHolder = new ViewHolder();
                LayoutInflater li = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                convertView = li.inflate(R.layout.grid_item_course, parent, false);
                viewHolder.viewFlipper  = (ViewFlipper) convertView.findViewById(R.id.viewFlipper);
                viewHolder.courseName   = (HelveticaTextView) convertView.findViewById(R.id.courseName);
                viewHolder.courseCode   = (HelveticaTextView) convertView.findViewById(R.id.courseCode);
                viewHolder.courseHeader = (RelativeLayout) convertView.findViewById(R.id.courseHeader);
                viewHolder.badge        = (CircularTextView) convertView.findViewById(R.id.badge);
                viewHolder.colorLayout  = (CourseColorGridView) convertView.findViewById(R.id.colorLayout);
                viewHolder.backButton   = (HelveticaTextView) convertView.findViewById(R.id.backButton);
                viewHolder.backButtonContainer = (LinearLayout) convertView.findViewById(R.id.backButtonWrapper);
                viewHolder.backArrow    = (ImageView) convertView.findViewById(R.id.backArrow);
                viewHolder.pulse        = (ImageView) convertView.findViewById(R.id.pulse);
                convertView.setTag(viewHolder);
            }else{
                viewHolder = (ViewHolder) convertView.getTag();
            }

            // Add Tutorial if needed
            final int firstVisiblePosition = coursesGrid.getFirstVisiblePosition();
            if(firstVisiblePosition == position){
                new TutorialUtils(getActivity(), App.getPrefs(), viewHolder.pulse, TutorialUtils.TYPE.COLOR_CHANGING_DIALOG)
                        .setContent(getString(R.string.tutorial_tipColorPickerTitle), getString(R.string.tutorial_tipColorPickerMessage))
                        .build();
            }else{
                viewHolder.pulse.setVisibility(View.GONE);
            }

            viewHolder.courseName.setText(courses.get(position).getName());
            viewHolder.courseCode.setText(courses.get(position).getCourseCode());
            final int[] currentColors = CanvasContextColor.getCachedColors(getContext(), currentCourse);
            GradientDrawable bgShape = (GradientDrawable)viewHolder.courseHeader.getBackground();
            bgShape.setColor(currentColors[0]);

            Drawable backArrowDrawable = CanvasContextColor.getColoredDrawable(getContext(),R.drawable.ic_cv_arrow_left, getContext().getResources().getColor(R.color.white));
            viewHolder.backArrow.setImageDrawable(backArrowDrawable);

            long needsGrading = courses.get(position).getNeedsGradingCount();
            if(courses.get(position).getNeedsGradingCount() > 0 && showUnGradedCount){
                viewHolder.badge.setVisibility(View.VISIBLE);
                viewHolder.badge.changeColor(context.getResources().getColor(R.color.speedgrader_red));
                if(needsGrading > 99){
                    viewHolder.badge.setText(context.getResources().getString(R.string.ninetyNinePlus));
                }else{
                    viewHolder.badge.setText(String.valueOf(needsGrading));
                }
            }else{
                viewHolder.badge.setVisibility(View.INVISIBLE);
            }

            viewHolder.courseCode.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    flipView(viewHolder.viewFlipper, AnimationFactory.FlipDirection.RIGHT_LEFT);
                }
            });

            // Back Button
            viewHolder.backButtonContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    flipView(viewHolder.viewFlipper, AnimationFactory.FlipDirection.LEFT_RIGHT);
                }
            });
            GradientDrawable backButtonShape = (GradientDrawable)viewHolder.backButtonContainer.getBackground();
            backButtonShape.setColor(currentColors[0]);


            final CourseColorAdapter colorAdapter = new CourseColorAdapter(getContext(), courses.get(position));
            colorAdapter.setOldColor(currentColors[0]);
            viewHolder.colorLayout.setAdapter(colorAdapter);
            viewHolder.colorLayout.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {

                    final int newColor = getResources().getColor(colorAdapter.getItem(position));
                    CanvasContextColor.setNewColor(CourseGridFragment.this, getContext(), currentCourse, newColor);
                    ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorAdapter.getOldColor(), newColor);
                    colorAdapter.setOldColor(newColor);
                    colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            GradientDrawable backButtonShape = (GradientDrawable)viewHolder.backButtonContainer.getBackground();
                            backButtonShape.setColor((Integer) animation.getAnimatedValue());
                            GradientDrawable bgShape = (GradientDrawable)viewHolder.courseHeader.getBackground();
                            bgShape.setColor((Integer) animation.getAnimatedValue());
                        }
                    });
                    colorAnimation.setDuration(500);
                    colorAnimation.start();
                }
            });
            return convertView;
        }
    }

    private class ViewHolder{
        private HelveticaTextView courseName;
        private HelveticaTextView courseCode;
        private ImageView courseIcon;
        private RelativeLayout courseHeader;
        private CircularTextView badge;
        private CourseColorGridView colorLayout;
        private ViewFlipper viewFlipper;
        private HelveticaTextView backButton;
        private LinearLayout backButtonContainer;
        private ImageView backArrow;
        private ImageView pulse;
    }
}
