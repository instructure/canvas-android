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

package com.instructure.androidpolling.app.fragments;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;

import com.instructure.androidpolling.app.R;
import com.instructure.androidpolling.app.activities.BaseActivity;
import com.instructure.androidpolling.app.activities.StudentPollActivity;
import com.instructure.androidpolling.app.rowfactories.PollRowFactory;
import com.instructure.androidpolling.app.util.Constants;
import com.instructure.canvasapi2.StatusCallback;
import com.instructure.canvasapi2.managers.PollsManager;
import com.instructure.canvasapi2.models.Course;
import com.instructure.canvasapi2.models.Poll;
import com.instructure.canvasapi2.models.PollResponse;
import com.instructure.canvasapi2.models.PollSession;
import com.instructure.canvasapi2.models.PollSessionResponse;
import com.instructure.canvasapi2.utils.ApiType;
import com.instructure.canvasapi2.utils.LinkHeaders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClosedPollListFragment extends PaginatedListFragment<PollSession> {

    //callback
    private StatusCallback<PollSessionResponse> pollSessionCallback;
    private StatusCallback<PollResponse> pollCallback;

    private Map<Long, Course> courseMap;
    private Map<Long, Poll> pollMap;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == Constants.SUBMIT_POLL_SUCCESS) {
            //success! we just submitted a poll
            //refresh everything so we have up to date info
            reloadData();
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((BaseActivity)getActivity()).setActionBarTitle(getString(R.string.studentView));
    }

    @Override
    public void configureViews(View rootView) {

        if(getArguments() != null) {
            ArrayList<Course> courseList = (ArrayList<Course>)getArguments().getSerializable(Constants.COURSES_LIST);
            if(courseList != null) {
                courseMap = createCourseMap(courseList);
            }
            else {
                courseMap = new HashMap<>();
            }
        } else {
            courseMap = new HashMap<>();
        }

        //set an animation for adding list items
        LayoutAnimationController controller
                = AnimationUtils.loadLayoutAnimation(
                getActivity(), R.anim.list_layout_controller);

        getListView().setLayoutAnimation(controller);

        pollMap = new HashMap<>();
    }

    @Override
    public View getRowViewForItem(PollSession item, View convertView, int childPosition) {
        Poll poll = pollMap.get(item.getPollId());
        String pollName = "";
        if(poll != null) {
            pollName = poll.getQuestion();
        }
        String courseName = "";
        if(courseMap.containsKey(item.getCourseId())) {
            courseName = courseMap.get(item.getCourseId()).getName();
        }
        return PollRowFactory.INSTANCE.buildRowView(layoutInflater(), courseName, pollName, convertView, getActivity(), item.getCreatedAt());
    }

    @Override
    public int getEmptyViewLayoutCode() {
        return R.layout.empty_view_student_polls;
    }

    @Override
    public int getFooterLayoutCode() {
        return 0;
    }

    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public boolean onRowClick(PollSession item, int position) {
        //we haven't gotten the poll data yet, don't let it go any farther
        if(!pollMap.containsKey(item.getPollId())) {
            return true;
        }

        startActivityForResult(StudentPollActivity.createIntent(getActivity(), pollMap.get(item.getPollId()), item, item.getHasSubmitted()), Constants.SUBMIT_POLL_REQUEST);
        return true;
    }

    @Override
    public boolean areItemsSorted() {
        return true;
    }

    @Override
    public void loadFirstPage() {
        PollsManager.getClosedSessions(pollSessionCallback, true);
    }

    @Override
    public void loadNextPage(String nextURL) {
    }

    @Override
    public String getNextURL() {
        return null;
    }

    @Override
    public void setNextURLNull() {
    }

    @Override
    public void resetData() {

    }

    @Override
    public void setupCallbacks() {

        pollCallback = new StatusCallback<PollResponse>() {
            @Override
            public void onResponse(@NonNull retrofit2.Response<PollResponse> response, @NonNull LinkHeaders linkHeaders, @NonNull ApiType type) {
                if(getActivity() == null || type.isCache()) return;

                List<Poll> polls = response.body().getPolls();
                if(polls != null) {
                    for (Poll poll : polls) {
                        pollMap.put(poll.getId(), poll);
                    }
                    //now we have the poll question data, so update the list
                    notifyDataSetChanged();
                }
            }

            @Override
            public void onFinished(ApiType type) {
                if(swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
        };

        pollSessionCallback = new StatusCallback<PollSessionResponse>() {
            @Override
            public void onResponse(@NonNull retrofit2.Response<PollSessionResponse> response, @NonNull LinkHeaders linkHeaders, @NonNull ApiType type) {
                if(getActivity() == null || type.isCache()) return;

                List<PollSession> pollSessions = response.body().getPollSessions();
                if(pollSessions != null) {
                    for (PollSession pollSession : pollSessions) {
                        PollsManager.getSinglePoll(pollSession.getPollId(), pollCallback, true);
                        addItem(pollSession);
                    }
                }
            }
        };
    }

    public static Map<Long, Course> createCourseMap(List<Course> courses) {
        Map<Long, Course> courseMap = new HashMap<>();
        if(courses == null) {
            return courseMap;
        }
        for (Course course : courses) {
            courseMap.put(course.getId(), course);
        }
        return courseMap;
    }
}
