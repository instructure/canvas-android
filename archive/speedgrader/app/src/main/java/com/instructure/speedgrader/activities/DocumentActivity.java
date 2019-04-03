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

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.LongSparseArray;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.BounceInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SlidingDrawer;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.instructure.canvasapi.api.GroupAPI;
import com.instructure.canvasapi.api.SectionAPI;
import com.instructure.canvasapi.api.SubmissionAPI;
import com.instructure.canvasapi.api.UserAPI;
import com.instructure.canvasapi.model.Assignment;
import com.instructure.canvasapi.model.Attachment;
import com.instructure.canvasapi.model.CanvasContext;
import com.instructure.canvasapi.model.Course;
import com.instructure.canvasapi.model.Group;
import com.instructure.canvasapi.model.RubricCriterionRating;
import com.instructure.canvasapi.model.Section;
import com.instructure.canvasapi.model.Submission;
import com.instructure.canvasapi.model.SubmissionComment;
import com.instructure.canvasapi.model.User;
import com.instructure.canvasapi.utilities.CanvasCallback;
import com.instructure.canvasapi.utilities.LinkHeaders;
import com.instructure.pandautils.utils.CanvasContextColor;
import com.instructure.speedgrader.R;
import com.instructure.speedgrader.adapters.DocumentPagerAdapter;
import com.instructure.speedgrader.adapters.UserSubmissionsListAdapter;
import com.instructure.speedgrader.dialogs.EditAssignmentDialog;
import com.instructure.speedgrader.dialogs.GenericDialogStyled;
import com.instructure.speedgrader.fragments.BaseSubmissionView;
import com.instructure.speedgrader.fragments.GradingDrawerFragment;
import com.instructure.speedgrader.fragments.MediaUploadFragment;
import com.instructure.speedgrader.fragments.OnlineURLFragment;
import com.instructure.speedgrader.fragments.ParentFragment;
import com.instructure.speedgrader.fragments.SubmissionWebViewFragment;
import com.instructure.speedgrader.interfaces.GenericDialogListener;
import com.instructure.speedgrader.interfaces.SubmissionListener;
import com.instructure.speedgrader.util.App;
import com.instructure.speedgrader.util.CanvasErrorDelegate;
import com.instructure.speedgrader.util.Const;
import com.instructure.speedgrader.util.ViewUtils;
import com.instructure.speedgrader.views.CircularProgressBar;
import com.instructure.speedgrader.views.HelveticaTextView;
import com.instructure.speedgrader.views.StaticViewPager;
import com.instructure.speedgrader.views.StudentListView;
import com.instructure.speedgrader.views.StudentListView.StudentListViewListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import retrofit.client.Response;

public class DocumentActivity extends ParentActivity implements SubmissionListener, StudentListViewListener {

    // get strings of the different submission types we are supporting to make it easier to read
    private static final String onlineURL = Assignment.submissionTypeToAPIString(Assignment.SUBMISSION_TYPE.ONLINE_URL);
    private static final String mediaUpload = Assignment.submissionTypeToAPIString(Assignment.SUBMISSION_TYPE.MEDIA_RECORDING);


    private App mApp;
    private CanvasContext mCanvasContext;
    private Assignment mAssignment;
    private boolean isGradeIndividually;
    private boolean isGroupAssignment;

    // Sections
    private ArrayList<Section> mSections;
    private long currentSectionId;

    // Callbacks
    private CanvasCallback<Submission[]> mCallbackSubmission;
    private CanvasCallback<User[]> mCallbackUsers;
    private CanvasCallback<Section[]> mCallbackSections;
    private CanvasCallback<Group[]> mCallbackGroups;
    private Submission[] mSubmissions;
    private User[] mUsers;
    private Group[] mGroups;

    // Student ListView
    private List<Submission> mSubmissionsList;

    // Viewpager
    private StaticViewPager mViewpager;
    private GradingDrawerFragment mGradingFragment;
    private DocumentPagerAdapter mAdapter;

    private TextSwitcher mNameText;
    private StudentListView mPopupWindow;
    private SlidingDrawer mSlidingDrawer;
    private TextView mNextButton;
    private TextView mPrevButton;

    //Lifecycle
    private boolean isDestroyed;
    private boolean isDrawerOpen;

    private HashMap<Long, Assignment> updatedAssignmentsMap = new HashMap<>();
    private int gradedCount = 0;
    private int currentViewPagerPosition = 0;

    private Attachment currentAttachment;
    // Edit Assignment
    private EditAssignmentDialog genericDialog;
    private HashMap<String, Long> currentAttempts; // HashMap <submission_id, attempt_number>
    // We store the submission_id's as strings, since calling hashmap.get() with a large long # can cause crashes.

    // The sliding drawer layout being used will hide the drawer contents when the drawer is closed. In order to animate the bounce effect
    // when a user first visits the activity. We need to manually open the drawer, animate it, then close it again. STATE_BOUNCE
    // prevent open/close listeners from fire when bouncing.
    private boolean STATE_BOUNCING = false;

    /////////////////////////////////////////////////////////////////
    //region                  LifeCycle
    /////////////////////////////////////////////////////////////////
    @Override
    public int getRootLayout() {
        return R.layout.activity_document;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        showLoading();
        setupCallBacks();

        setActionBarColor(CanvasContextColor.getCachedColor(getContext(), mCanvasContext));

        initActionBar();
        initViewPager();
        initRubricDrawer();

        mApp = (App) getApplication();

        if (savedInstanceState != null) {
            if (getSupportFragmentManager().findFragmentById(R.id.drawerFragment) instanceof GradingDrawerFragment) {
                mGradingFragment = (GradingDrawerFragment) getSupportFragmentManager().findFragmentById(R.id.drawerFragment);
            }

            loadSavedInstanceStateBundle(savedInstanceState);
            initRubricDrawer();

            if (mUsers == null || mSubmissions == null || mSections == null) {
                getData();
            } else {
                populateAdapters(mSubmissions, mUsers);
            }
        } else {
            getData();
        }
    }

    @Override
    protected void onDestroy() {
        isDestroyed = true;
        if (mPopupWindow != null && mPopupWindow.isShowing()) {
            mPopupWindow.dismiss();
        }
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        if (mPopupWindow != null && mPopupWindow.isShowing()) {
            mPopupWindow.dismiss();
        }
        super.onStop();
    }

    @Override
    protected void onPause() {
        if (mPopupWindow != null && mPopupWindow.isShowing()) {
            mPopupWindow.dismiss();
        }
        super.onPause();
    }

    //endregion
    /////////////////////////////////////////////////////////////////
    //  View Helpers
    /////////////////////////////////////////////////////////////////
    private void showLoading() {
        RelativeLayout loading = (RelativeLayout) findViewById(R.id.documentLoadingView);
        ((CircularProgressBar) loading.findViewById(R.id.circularProgressBar)).setColor((CanvasContextColor.getCachedColor(getContext(), mCanvasContext)));
        loading.setVisibility(View.VISIBLE);
    }

    /////////////////////////////////////////////////////////////////
    //region  Intent & Bundle
    /////////////////////////////////////////////////////////////////
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArray(Const.assignmentUsers, mUsers);
        outState.putParcelableArray(Const.assignmentSubmissions, mSubmissions);
        outState.putParcelable(Const.assignment, mAssignment);
        outState.putBoolean(Const.isDrawerOpen, mSlidingDrawer.isOpened());
        outState.putSerializable(Const.currentAttempts, currentAttempts);
        outState.putParcelableArrayList(Const.sections, mSections);
        outState.putInt(Const.currentPosition, currentViewPagerPosition);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void handleIntent(Intent intent) {
        mAssignment = intent.getExtras().getParcelable(Const.assignment);
        mCanvasContext = intent.getExtras().getParcelable(Const.canvasContext);
        currentSectionId = intent.getExtras().getLong(Const.currentSectionId);
        isGradeIndividually = mAssignment.isGradeGroupsIndividually();
        isGroupAssignment = mAssignment.getGroupCategoryId() != 0;
    }

    public static Intent createIntent(Context context, CanvasContext canvasContext, Assignment assignment, long currentSectionId) {
        final Intent intent = new Intent(context, DocumentActivity.class);
        intent.putExtra(Const.assignment, (Parcelable) assignment);
        intent.putExtra(Const.canvasContext, (Parcelable) canvasContext);
        intent.putExtra(Const.currentSectionId, currentSectionId);
        return intent;
    }

    public void loadSavedInstanceStateBundle(Bundle savedInstanceState) {
        isDrawerOpen = savedInstanceState.getBoolean(Const.isDrawerOpen);

        // get all the Submissions from the parcelable array
        Parcelable[] submissionParcels = savedInstanceState.getParcelableArray(Const.assignmentSubmissions);
        if (submissionParcels != null) {
            mSubmissions = new Submission[submissionParcels.length];
            // Casting the parcelable array into an array of submissions can cause crashes sometimes, so instead we iterate over the parcelable
            // array and cast each item into a submission
            for (int i = 0; i < submissionParcels.length; i++) {
                mSubmissions[i] = (Submission) submissionParcels[i];
            }
        }

        // get all users from parcelable array
        Parcelable[] userParcels = savedInstanceState.getParcelableArray(Const.assignmentUsers);
        if (userParcels != null) {
            mUsers = new User[userParcels.length];
            // Casting the parcelable array into an array of users can cause crashes sometimes, so instead we iterate over the parcelable
            // array and cast each item into a user
            for (int i = 0; i < userParcels.length; i++) {
                mUsers[i] = (User) userParcels[i];
            }
        }

        mAssignment = savedInstanceState.getParcelable(Const.assignment);
        currentAttempts = (HashMap<String, Long>) savedInstanceState.getSerializable(Const.currentAttempts);
        currentViewPagerPosition = savedInstanceState.getInt(Const.currentPosition);
        mSections = savedInstanceState.getParcelableArrayList(Const.sections);
    }

    public static Intent createIntent(Context context, Uri passedURI) {
        final Intent intent = new Intent(context, DocumentActivity.class);
        intent.putExtra(Const.passedURI, passedURI);
        return intent;
    }
    //endregion

    ///////////////////////////////////////////////////////////////////////////
    // Interface Methods
    ///////////////////////////////////////////////////////////////////////////
    @Override
    public void onAttachmentSelected(Attachment attachment, Submission submission) {
        if (mAdapter == null || mViewpager == null) {
            return;
        }

        currentAttachment = attachment;
        final String submissionType = submission.getSubmissionType();
        if (submissionType != null && submissionType.equals(onlineURL)) {
            final OnlineURLFragment urlFragment = (OnlineURLFragment) mAdapter.getCachedFragment(mViewpager.getCurrentItem());
            urlFragment.setUrlAttachment(attachment, submission.getUrl());
        } else if (submissionType != null && submissionType.equals(mediaUpload)) {
            final MediaUploadFragment mediaUploadFragment = (MediaUploadFragment) mAdapter.getCachedFragment(mViewpager.getCurrentItem());
            mediaUploadFragment.loadMedia(submission);
        } else {
            final SubmissionWebViewFragment webViewFragment = (SubmissionWebViewFragment) mAdapter.getCachedFragment(mViewpager.getCurrentItem());
            webViewFragment.displayAttachment(attachment);
        }
        ((BaseSubmissionView) mAdapter.getCachedFragment(mViewpager.getCurrentItem())).setAttachment(attachment);
    }

    @Override
    public void onSubmissionSelected(final Submission submission) {
        if (mAdapter == null || mViewpager == null) {
            return;
        }

        mAdapter.setSubmission(mViewpager.getCurrentItem(), submission.getAttempt());
        currentAttempts.put(String.valueOf(submission.getId()), submission.getAttempt());
    }

    @Override
    public void onSubmissionCommentsUpdated(Submission submission) {
        if (mAdapter == null || mViewpager == null) {
            return;
        }

        final ArrayList<SubmissionComment> comments = submission.getComments();

        mSubmissionsList.get(mViewpager.getCurrentItem()).setComments(comments);

        final BaseSubmissionView fragment = (BaseSubmissionView) mAdapter.getItem(mViewpager.getCurrentItem());
        fragment.submission.setComments(comments);
    }

    @Override
    public void onSubmissionRubricAssessmentUpdated(HashMap<String, RubricCriterionRating> newRatings, Double newScore, String newGrade) {
        mSubmissionsList.get(mViewpager.getCurrentItem()).setRubricAssessment(newRatings);
        mSubmissionsList.get(mViewpager.getCurrentItem()).setScore(newScore);
        mSubmissionsList.get(mViewpager.getCurrentItem()).setGrade(newGrade);
        mPopupWindow.notifyDataSetChanged();

        final BaseSubmissionView fragment = (BaseSubmissionView) mAdapter.getItem(mViewpager.getCurrentItem());
        fragment.submission.setRubricAssessment(newRatings);
        fragment.submission.setScore(newScore);
        fragment.submission.setGrade(newGrade);
    }

    public void onAssignmentUpdated(Assignment newAssignment) {
        Toast.makeText(DocumentActivity.this, R.string.assignmentSaved, Toast.LENGTH_SHORT).show();
        this.mAssignment = newAssignment;
        this.updatedAssignmentsMap.put(newAssignment.getId(), newAssignment);
        Intent updatedAssignmentsIntent = new Intent();
        updatedAssignmentsIntent.putExtra(Const.UPDATED_ASSIGNMENTS, this.updatedAssignmentsMap);
        setResult(Const.UPDATED_ASSIGNMENT_FLAGS, updatedAssignmentsIntent);
    }

    /**
     * If the rubric fragment has unsaved information, or after the rubric fragment saves information, this method will
     * enabled or disable the viewpager accordingly.
     **/
    @Override
    public void setPagingEnabled(boolean isEnabled) {
        mViewpager.setPagingEnabled(isEnabled);
    }

    @Override
    public void showUnsavedDataDialog(final boolean isSwipeRightLeft) {
        final GenericDialogStyled genericDialog = GenericDialogStyled.newInstance(true, R.string.unsavedData, R.string.unsavedDataWarning, R.string.continueUnsaved, R.string.cancel, R.drawable.ic_cv_alert_light, new GenericDialogListener() {
            @Override
            public void onPositivePressed() {
                setPagingEnabled(true);
                if (isSwipeRightLeft) {
                    mViewpager.setCurrentItem(mViewpager.getCurrentItem() + 1);
                } else {
                    mViewpager.setCurrentItem(mViewpager.getCurrentItem() - 1);
                }
            }

            @Override
            public void onNegativePressed() {
            }
        });
        genericDialog.show(getSupportFragmentManager(), Const.unsavedData);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Callback Methods
    ///////////////////////////////////////////////////////////////////////////
    private void setupCallBacks() {
        mCallbackSubmission = new CanvasCallback<Submission[]>(this, new CanvasErrorDelegate()) {
            @Override
            public void cache(Submission[] submissions, LinkHeaders linkHeaders, Response response) {
            }

            @Override
            public void firstPage(final Submission[] submissions, LinkHeaders linkHeaders, Response response) {
                // if user clicks back before firstpage is called
                if (isDestroyed) {
                    return;
                }
                mSubmissions = submissions;
                populateAdapters(mSubmissions, mUsers);
            }
        };

        mCallbackUsers = new CanvasCallback<User[]>(this) {
            @Override
            public void cache(User[] users, LinkHeaders linkHeaders, Response response) {
            }

            @Override
            public void firstPage(User[] users, LinkHeaders linkHeaders, Response response) {
                if (isDestroyed) {
                    return;
                }
                mUsers = users;
                populateAdapters(mSubmissions, mUsers);
            }
        };

        mCallbackSections = new CanvasCallback<Section[]>(this) {
            @Override
            public void cache(Section[] courseSections, LinkHeaders linkHeaders, Response response) {
            }

            @Override
            public void firstPage(Section[] sections, LinkHeaders linkHeaders, Response response) {
                if (isDestroyed) {
                    return;
                }
                mSections = new ArrayList<>(Arrays.asList(sections));
                addAllSectionsItem();
                populateAdapters(mSubmissions, mUsers);
            }
        };

        mCallbackGroups = new CanvasCallback<Group[]>(this) {
            @Override
            public void cache(Group[] groups, LinkHeaders linkHeaders, Response response) {

            }

            @Override
            public void firstPage(Group[] groups, LinkHeaders linkHeaders, Response response) {
                if (isDestroyed) {
                    return;
                }
                ArrayList<Group> matchingGroups = new ArrayList<>();
                for (Group group : groups) {
                    if (group.getGroupCategoryId() == mAssignment.getGroupCategoryId() && group.getUsers().length > 0) {
                        matchingGroups.add(group);
                    }
                }
                mGroups = matchingGroups.toArray(new Group[matchingGroups.size()]);
                populateAdapters(mSubmissions, mUsers);
            }
        };
    }

    public void updateRubricSubmissionInfo(Submission submission) {
        if (mGradingFragment == null) {
            return;
        }

        mGradingFragment.updateRubricSubmissionInfo(submission);
    }

    /////////////////////////////////////////////////////////////////
    // Load Data
    /////////////////////////////////////////////////////////////////
    public void getData() {
        SubmissionAPI.getSubmissionsWithCommentsHistoryAndRubricExhaustive(mCanvasContext, mAssignment.getId(), mCallbackSubmission);
        UserAPI.getAllUsersForCourseByEnrollmentType(mCanvasContext, UserAPI.ENROLLMENT_TYPE.STUDENT, mCallbackUsers);
        SectionAPI.getCourseSectionsWithStudents((Course) mCanvasContext, mCallbackSections);

        if (mAssignment.getGroupCategoryId() != 0) {
            GroupAPI.getAllGroupsInCourse(mCanvasContext.getId(), mCallbackGroups);
        }
    }

    public void populateAdapters(Submission[] submissions, User[] courseStudents) {
        if (submissions == null || courseStudents == null || mSections == null || (isGroupAssignment && !isGradeIndividually && mGroups == null)) {
            return;
        }

        setLoadingInvisible();
        List<Submission> submissionsToDisplay;

        if (!isGradeIndividually && isGroupAssignment) {
            submissionsToDisplay = groupSubmissionsByGroupId(submissions, courseStudents);
        } else {
            submissionsToDisplay = addEmptySubmissionsForUsersAndGetGradedCount(submissions, courseStudents);
        }
        populateSubmissions(submissionsToDisplay);
    }

    private void setLoadingInvisible() {
        RelativeLayout loading = (RelativeLayout) findViewById(R.id.documentLoadingView);
        loading.setVisibility(View.GONE);
    }

    public List<Submission> groupSubmissionsByGroupId(Submission[] submissions, User[] courseStudents) {

        LongSparseArray<Group> groupMap = new LongSparseArray<>();
        LongSparseArray<Group> studentGroupMap = new LongSparseArray<>();

        // Map students to groups
        for (Group group : mGroups) {
            groupMap.put(group.getId(), group);
            for (User user : group.getUsers()) {
                studentGroupMap.put(user.getId(), group);
            }
        }

        // Get list of unassigned students
        LongSparseArray<User> unassignedStudents = new LongSparseArray<>();
        for (User student : courseStudents) {
            if (studentGroupMap.indexOfKey(student.getId()) < 0) {
                unassignedStudents.put(student.getId(), student);
            }
        }

        LongSparseArray<Submission> groupSubmissionMap = new LongSparseArray<>();
        for (Group group : mGroups) groupSubmissionMap.put(group.getId(), null);

        List<Submission> individualSubmissions = new ArrayList<>();
        List<Submission> emptyGroupSubmissions = new ArrayList<>();
        List<Submission> emptyIndividualSubmissions = new ArrayList<>();

        for (Submission submission : submissions) {
            // Group submissions by group ID
            long groupId = submission.getGroup().getId();

            // Check for and fix missing group info
            if (groupId == 0) {
                long userId = submission.getUser_id();
                if (studentGroupMap.get(userId) != null) {
                    Group group = studentGroupMap.get(userId);
                    submission.setGroup(group);
                    groupId = group.getId();
                }
            }

            if (groupSubmissionMap.get(groupId) == null) {
                // check if we need to increment the graded count
                if (UserSubmissionsListAdapter.isGraded(submission)) {
                    gradedCount++;
                }

                if (groupId != 0) {
                    groupSubmissionMap.put(groupId, submission);
                } else {
                    // It's possible the submission belongs to a user who doesn't belong to a group
                    // in that case, we still want to add that submission to our results and display the username
                    individualSubmissions.add(submission);
                    unassignedStudents.remove(submission.getUser_id());
                }
            } else if (!UserSubmissionsListAdapter.isGraded(groupSubmissionMap.get(groupId)) && UserSubmissionsListAdapter.isGraded(submission)) {
                groupSubmissionMap.put(groupId, submission);
            }
        }

        List<Submission> result = new ArrayList<>();

        // Add group submissions to result, create empty submissions as necessary
        for (int i = 0; i < groupSubmissionMap.size(); i++) {
            if (groupSubmissionMap.valueAt(i) != null) {
                result.add(groupSubmissionMap.valueAt(i));
            } else {
                Group group = groupMap.get(groupSubmissionMap.keyAt(i));
                emptyGroupSubmissions.add(createEmptySubmissionForGroup(group));
            }
        }

        // Create empty submissions for students not assigned to a group
        for (int i = 0; i < unassignedStudents.size(); i++) {
            emptyIndividualSubmissions.add(createEmptySubmissionForUser(unassignedStudents.valueAt(i)));
        }

        result.addAll(individualSubmissions);
        result.addAll(emptyGroupSubmissions);
        result.addAll(emptyIndividualSubmissions);
        return result;
    }

    /**
     * CanvasAPI will return a submission for students who have turned in an mAssignment. Given a list of all students
     * in the course, we need to generate a dummy Submission object with a username that can be passed to our
     * submissions adapter.
     *
     * @param submissions
     * @param courseStudents
     * @return
     */
    public ArrayList<Submission> addEmptySubmissionsForUsersAndGetGradedCount(Submission[] submissions, User[] courseStudents) {
        ArrayList<Submission> tempSubmissions = new ArrayList<>(Arrays.asList(submissions));
        ArrayList<User> tempUsers = new ArrayList<>(Arrays.asList(courseStudents));
        for (Submission submission : tempSubmissions) {
            tempUsers.remove(submission.getUser());
            if (UserSubmissionsListAdapter.isGraded(submission)) {
                gradedCount++;
            }
        }

        for (User user : tempUsers) {
            tempSubmissions.add(createEmptySubmissionForUser(user));
        }
        return tempSubmissions;
    }

    public void populateSubmissions(List<Submission> submissions) {
        // Create and maintain currentAttempts
        if (currentAttempts == null) {
            currentAttempts = new HashMap<>(submissions.size());
        }

        // Set the current attempt(submission) being displayed in the viewpager. Display most current attempt by default.
        for (int i = 0; i < submissions.size(); i++) {
            if (!currentAttempts.containsKey(String.valueOf(submissions.get(i).getId()))) {
                // Don't set the currentAttempts if we already have one cached
                currentAttempts.put(String.valueOf(submissions.get(i).getId()), submissions.get(i).getAttempt());
            }
        }

        mSubmissionsList = submissions;

        createSubmissionPopupWindow();

        mAdapter = new DocumentPagerAdapter(DocumentActivity.this, getSupportFragmentManager(), mSubmissionsList, mAssignment, currentAttempts, mCanvasContext);
        mViewpager.setAdapter(mAdapter);

        setDefaultAttachment(0);

        //add the rubric if it hasn't already been added. (Orientation changes don't need to recreate it)
        if (mGradingFragment == null && mSubmissionsList.size() > 0) {
            mGradingFragment = ParentFragment.newInstance(GradingDrawerFragment.class, GradingDrawerFragment.createBundle(mCanvasContext, mSubmissionsList.get(0), mAssignment));
            addFragmentToLayout(mGradingFragment, R.id.drawerFragment);
        }

        handleNavigationArrows();

        // redraw our userName to reflect our new student data
        if (mSubmissionsList != null && mSubmissionsList.size() > 0) {
            setStudentNameByPosition(mViewpager.getCurrentItem());
        }

        bounceDrawer();
    }

    /**
     * When a submission is displayed in the viewpager, we display the first attachment by default.
     * DocumentActivity keeps a reference to the attachment currently displayed in the window, which
     * we can reference when the user requests to download or open in browser.
     *
     * @param position
     */
    private void setDefaultAttachment(int position) {
        Submission defaultSubmission = mAdapter.getSubmission(position);
        if (defaultSubmission.getAttachments() != null && defaultSubmission.getAttachments().size() > 0) {
            currentAttachment = defaultSubmission.getAttachments().get(0);
        } else {
            currentAttachment = null;
        }
    }

    // Dropdown students list needs to create an empty submission for all users so teachers can still grade them.
    public Submission createEmptySubmissionForUser(User user) {
        Submission emptyUserSubmission = new Submission();
        emptyUserSubmission.setUser(user);
        emptyUserSubmission.setId(mApp.getUniqueId());
        emptyUserSubmission.setUser_id(user.getId());
        emptyUserSubmission.setWorkflowState(Const.UNSUBMITTED);
        return emptyUserSubmission;
    }

    public Submission createEmptySubmissionForGroup(Group group) {
        Submission emptySubmission = createEmptySubmissionForUser(group.getUsers()[0]);
        emptySubmission.setGroup(group);
        return emptySubmission;
    }

    @Override
    public void onMediaOpened(String mime, String url, String filename) {
        if (mGradingFragment == null) {
            return;
        }
        mGradingFragment.openMedia(mime, url, filename);
    }

    /////////////////////////////////////////////////////////////////
    //                  ViewPager
    /////////////////////////////////////////////////////////////////
    private void initViewPager() {
        LinearLayout controlBar = (LinearLayout) findViewById(R.id.controlBar);
        controlBar.setBackgroundColor(CanvasContextColor.getCachedColor(this, mCanvasContext));
        mNextButton = (TextView) findViewById(R.id.nextButton);
        mPrevButton = (TextView) findViewById(R.id.prevButton);

        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mViewpager.isEnabled()) {
                    mViewpager.setCurrentItem(mViewpager.getCurrentItem() + 1, true);
                } else {
                    showUnsavedDataDialog(true);
                }
            }
        });
        mPrevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mViewpager.isEnabled()) {
                    mViewpager.setCurrentItem(mViewpager.getCurrentItem() - 1, true);
                } else {
                    showUnsavedDataDialog(false);
                }
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && getSupportActionBar() != null) {
            getSupportActionBar().setElevation(0);
            controlBar.setElevation(10);
        }

        mViewpager = (StaticViewPager) findViewById(R.id.pager);
        if (mViewpager != null) {
            mViewpager.setOffscreenPageLimit(3);
            mViewpager.setOnPageChangeListener(getOnPageChangeListener());
        }
    }

    /**
     * Function    : getOnPageChangeListener
     * Description : Returns a new OnPageChangeListener for our viewpager, this listner
     * is responsible for updating our rubric, as well as setting the textswitchers title,
     * animation direction, and navigation arrows.
     */
    private ViewPager.OnPageChangeListener getOnPageChangeListener() {
        return new ViewPager.OnPageChangeListener() {
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }

            public void onPageScrollStateChanged(int state) {
            }

            @Override
            public void onPageSelected(int position) {
                // Notify rubric of submission change
                mGradingFragment.onPageChanged(mAssignment, mSubmissionsList.get(position), ((BaseSubmissionView) mAdapter.getItem(position)).getAttachment(), ((BaseSubmissionView) mAdapter.getItem(position)).getCurrentSubmissionAttempt());

                // Determine textswitcher animation type
                final Animation in;
                final Animation out;
                if (currentViewPagerPosition > position) {
                    //right to left
                    in = AnimationUtils.loadAnimation(getContext(), R.anim.slide_in_left);
                    out = AnimationUtils.loadAnimation(getContext(), R.anim.slide_out_right);
                } else {
                    // left to right
                    in = AnimationUtils.loadAnimation(getContext(), R.anim.slide_in_right);
                    out = AnimationUtils.loadAnimation(getContext(), R.anim.slide_out_left);
                }
                mNameText.setInAnimation(in);
                mNameText.setOutAnimation(out);

                currentViewPagerPosition = position;

                setStudentNameByPosition(position);

                handleNavigationArrows();
                setDefaultAttachment(position);
            }
        };
    }

    public void bounceDrawer() {
        if (mSlidingDrawer != null && ((App) getApplication()).shouldShowBounce()) {
            STATE_BOUNCING = true;
            float viewWidth = ViewUtils.convertDipsToPixels(50, getContext());
            mSlidingDrawer.open();

            TranslateAnimation translation;
            translation = new TranslateAnimation(0, mSlidingDrawer.getWidth() - viewWidth, 0f, 0f);
            translation.setDuration(1000);
            translation.setFillAfter(false);
            translation.setInterpolator(new BounceInterpolator());
            translation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    mSlidingDrawer.close();
                    STATE_BOUNCING = false;
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });

            mSlidingDrawer.clearAnimation();
            mSlidingDrawer.startAnimation(translation);
        }
    }

    /////////////////////////////////////////////////////////////////
    //                  Navigation Drawer
    /////////////////////////////////////////////////////////////////
    public void initRubricDrawer() {
        final View contentOverlay = findViewById(R.id.contentOverlay);
        mSlidingDrawer = (SlidingDrawer) findViewById(R.id.drawer);

        mSlidingDrawer.setOnDrawerOpenListener(new SlidingDrawer.OnDrawerOpenListener() {
            @Override
            public void onDrawerOpened() {
                if (!STATE_BOUNCING) {
                    AlphaAnimation animation = new AlphaAnimation(0, 1);
                    animation.setFillAfter(true);
                    animation.setDuration(300);
                    contentOverlay.startAnimation(animation);
                    if (((App) getApplication()).shouldShowBounce()) {
                        ((App) getApplication()).setShowBounce(false);
                    }
                }
            }
        });

        mSlidingDrawer.setOnDrawerCloseListener(new SlidingDrawer.OnDrawerCloseListener() {
            @Override
            public void onDrawerClosed() {
                if (!STATE_BOUNCING) {
                    AlphaAnimation animation = new AlphaAnimation(1, 0);
                    animation.setFillAfter(true);
                    animation.setDuration(300);
                    contentOverlay.startAnimation(animation);
                }
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mSlidingDrawer.setElevation(12);
        }

        if (isDrawerOpen) {
            mSlidingDrawer.open();
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }
    }

    /////////////////////////////////////////////////////////////////
    //               ActionBar & SubmissionsList
    /////////////////////////////////////////////////////////////////
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        getMenuInflater().inflate(R.menu.document, menu);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Submission currentSubmission = getCurrentSubmissionInViewpager();
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.edit_assignments:
                genericDialog = new EditAssignmentDialog();
                genericDialog.setArguments(EditAssignmentDialog.createBundle(mAssignment, (Course) mCanvasContext));
                genericDialog.show(getSupportFragmentManager(), EditAssignmentDialog.TAG);
                return true;
            case R.id.download:
                if (currentAttachment != null) {
                    downloadFile(currentAttachment.getUrl(), currentAttachment.getFilename());
                } else if (currentSubmission.getSubmissionType() != null && currentSubmission.getSubmissionType().equals(mediaUpload)) {
                    downloadFile(currentSubmission.getMediaComment().getUrl(), currentSubmission.getMediaComment().getFileName());
                } else {
                    Toast.makeText(DocumentActivity.this, getString(R.string.nothingToDownload), Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.open_browser:
                if (currentAttachment != null) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(currentAttachment.getUrl())));
                } else {
                    Toast.makeText(DocumentActivity.this, getString(R.string.nothingToShowInBrowser), Toast.LENGTH_SHORT).show();
                }
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Function    : initActionBar
     * Description : initialize the actionbar by inflating our custom view and setting it's listeners
     */
    private void initActionBar() {
        final View v = LayoutInflater.from(getContext()).inflate(R.layout.actionbar_documentview, null);

        mNameText = (TextSwitcher) v.findViewById(R.id.studentName);
        mNameText.setFactory(createTextSwitcherViewFactory());

        // Set default in and out animations
        mNameText.setInAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.slide_in_right));
        mNameText.setOutAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.slide_out_left));
        mNameText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSubmissionsList != null) {
                    showSubmissionsPopUpWindow();
                }
            }
        });

        // show/hide arrows if necessary
        handleNavigationArrows();

        // set our custom view
        final ActionBar actionbar = getSupportActionBar();
        if (actionbar != null) {
            actionbar.setHomeButtonEnabled(false);
            actionbar.setDisplayHomeAsUpEnabled(false);
            actionbar.setDisplayShowCustomEnabled(true);
            actionbar.setDisplayShowTitleEnabled(false);
            actionbar.setDisplayShowHomeEnabled(true);
            actionbar.setCustomView(v);
        }
    }

    /**
     * Function    : showSubmissionsPopUpWindow
     * Description : Create a popupwindow and display it underneath our mNameText textswitcher
     * The submissionsPopUpWindow allows teachers to search and filter the submissions in
     * the viewpager, and allow users to skip to different submissions
     */
    private void showSubmissionsPopUpWindow() {
        // If we've already created the popupwindow show it again.
        if (mPopupWindow == null) {
            createSubmissionPopupWindow();
        }

        // Show the popupwindow
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mPopupWindow.showAsDropDown(mNameText, 0, 0, Gravity.CENTER_HORIZONTAL);
        } else {
            mPopupWindow.showAsDropDown(mNameText, 0, 0);
        }
    }

    private void createSubmissionPopupWindow() {
        mPopupWindow = new StudentListView(this, mAssignment, mSections, mSubmissionsList, currentSectionId, gradedCount);
    }

    private
    @Nullable
    Submission getCurrentSubmissionInViewpager() {
        if (mSubmissionsList != null && mSubmissionsList.size() > 0) {
            return mSubmissionsList.get(mViewpager.getCurrentItem());
        }
        return null;
    }

    private void addAllSectionsItem() {
        Section tempSection = getAllSectionsItem();
        if (mSections != null && !mSections.contains(tempSection)) {
            mSections.add(0, tempSection);
        }
    }

    private Section getAllSectionsItem() {
        Section tempSection = new Section();
        tempSection.setId(Integer.MIN_VALUE);
        tempSection.setName(getResources().getString(R.string.allSections));
        return tempSection;
    }

    private ViewSwitcher.ViewFactory createTextSwitcherViewFactory() {
        return new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                final HelveticaTextView textView = new HelveticaTextView(DocumentActivity.this);
                textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
                final FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
                params.gravity = Gravity.CENTER;
                textView.setLayoutParams(params);
                textView.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
                textView.setTextSize(20);
                return textView;
            }
        };
    }

    /**
     * Function    : handleNavigationArrows
     * Description : If we're at either the beginning or the end of the viewpager
     * hide the corresponding navigation arrow
     */
    private void handleNavigationArrows() {
        if (mViewpager == null) {
            return;
        }

        //if the current item is 0, don't show back
        if (mViewpager.getCurrentItem() == 0) {
            mPrevButton.setEnabled(false);
        } else {
            mPrevButton.setEnabled(true);
        }

        //if the current item is size()-1, don't show forward
        if (mViewpager.getCurrentItem() == mViewpager.getAdapter().getCount() - 1) {
            mNextButton.setEnabled(false);
        } else {
            mNextButton.setEnabled(true);
        }
    }

    private void downloadFile(String url, String filename) {
        //Download File
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setTitle(filename);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename);
        DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        manager.enqueue(request);
    }

    private void setStudentNameByPosition(int position) {
        if (mApp.showStudentNames()) {
            mNameText.setText(getDisplayNameBySubmission(mAdapter.getSubmission(mViewpager.getCurrentItem()), isGroupAssignment, isGradeIndividually));
        } else {
            mNameText.setText(getContext().getString(R.string.student) + " " + String.valueOf(position + 1));
        }
    }

    public static String getDisplayNameBySubmission(Submission submission, boolean isGroupAssignment, boolean isGradeIndividually) {
        if (submission == null) {
            return "";
        }
        if (isGroupAssignment && !isGradeIndividually && submission.getGroup() != null) {
            String name = submission.getGroup().getName();
            if (!TextUtils.isEmpty(name)) {
                return submission.getGroup().getName();
            }
        }
        return submission.getUser().getSortableName();
    }

    @Override
    public void onCurrentSubmission(int position) {
        mViewpager.setCurrentItem(position, true);
    }

    @Override
    public void onSubmissionListUpdated() {
        mAdapter.notifyDataSetChanged();
        mViewpager.invalidate();
        Submission currentSubmission = getCurrentSubmissionInViewpager();
        if (currentSubmission != null && currentSubmission.getUser() != null) {
            setStudentNameByPosition(mViewpager.getCurrentItem());
        }
    }
}
