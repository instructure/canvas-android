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

package com.instructure.student.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.instructure.student.R;
import com.instructure.canvasapi2.models.CanvasContext;
import com.instructure.canvasapi2.models.Course;
import com.instructure.canvasapi2.models.QuizSubmissionQuestion;
import com.instructure.pandautils.utils.ColorKeeper;
import com.instructure.pandautils.utils.Const;

import java.util.ArrayList;

public class QuizQuestionDialog extends DialogFragment {

    public static final String TAG = "quizQuestionDialog";

    private ExpandableListView listView;
    private ExpandableListAdapter adapter;

    private RecyclerView.LayoutManager layoutManager;

    public void setLayoutManager(RecyclerView.LayoutManager layoutManager) {
        this.layoutManager = layoutManager;
    }

    public static QuizQuestionDialog newInstance(ArrayList<Long> questionIds, ArrayList<QuizSubmissionQuestion> submissionQuestions, Course course) {
        QuizQuestionDialog quizQuestionDialog = new QuizQuestionDialog();
        Bundle args = new Bundle();
        args.putSerializable(Const.QUIZ_QUESTION_IDS, questionIds);
        args.putParcelableArrayList(Const.QUIZ_QUESTIONS, submissionQuestions);
        args.putParcelable(Const.COURSE, course);
        quizQuestionDialog.setArguments(args);

        return quizQuestionDialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final FragmentActivity activity = getActivity();

        AlertDialog.Builder builder =
                new AlertDialog.Builder(activity)
                        .setTitle(activity.getString(R.string.viewQuestions))
                        .setPositiveButton(activity.getString(R.string.okay), null);

        @SuppressLint("InflateParams") // Suppress lint warning about null parent when inflating layout
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.quiz_question_dialog, null);

        listView = view.findViewById(R.id.listview);

        builder.setView(view);

        final AlertDialog dialog = builder.create();

        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface useless) {
                CanvasContext course = getArguments().getParcelable(Const.COURSE);
                if (course != null) {
                    int courseColor = ColorKeeper.getOrGenerateColor(course);
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(courseColor);
                }
            }
        });

        return dialog;
    }


    ///////////////////////////////////////////////////////////////////////////
    // LifeCycle
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ArrayList<Long> questionIds = (ArrayList<Long>)getArguments().getSerializable(Const.QUIZ_QUESTION_IDS);
        final ArrayList<QuizSubmissionQuestion> submissionQuestions = getArguments().getParcelableArrayList(Const.QUIZ_QUESTIONS);
        Course course = getArguments().getParcelable(Const.COURSE);
        ArrayList<QuizSubmissionQuestion> flaggedQuestions = new ArrayList<>();
        boolean hasFlagged;

        hasFlagged = false;
        for(QuizSubmissionQuestion quizSubmissionQuestion: submissionQuestions) {
            if(quizSubmissionQuestion.isFlagged()) {
                hasFlagged = true;
                flaggedQuestions.add(quizSubmissionQuestion);
            }
        }
        adapter = new QuestionAdapter(flaggedQuestions, submissionQuestions, questionIds, course, hasFlagged);

        listView.setAdapter(adapter);
        //expand all the groups
        for(int i = 0; i < adapter.getGroupCount(); i++) {
            listView.expandGroup(i);
        }

        listView.setGroupIndicator(null);
        listView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView expandableListView, View view, int i, long l) {
                return true;
            }
        });

        listView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView expandableListView, View view, int groupPosition, int childPosition, long l) {
                //dismiss the dialog
                dismiss();

                //scroll to the correct index (position - 1). We passed the layoutManager into the constructor so we can use it here
                int position = submissionQuestions.indexOf(adapter.getChild(groupPosition, childPosition));
                layoutManager.scrollToPosition(position);
                return true;
            }
        });
    }


    /**
     * Adapter to take care of populating the list
     *
     * flaggedQuestions is an arrayList that is populated based on which questions have been flagged by the user
     *
     * allQuestions is an arrayList of all the questions that are available on the quizResult
     *
     * questionIds is an arrayList of longs that correspond to question ids. We use this to indicate which questions
     * have been answered. This list is populated when the user selects an answer
     */
    private class QuestionAdapter extends BaseExpandableListAdapter {

        private ArrayList<Long> questionIds;
        private ArrayList<QuizSubmissionQuestion> allQuestions;
        private ArrayList<QuizSubmissionQuestion> flaggedQuestions;
        private Course course;
        private boolean hasFlagged;

        private QuestionAdapter(ArrayList<QuizSubmissionQuestion> flaggedQuestions, ArrayList<QuizSubmissionQuestion> allQuestions, ArrayList<Long> questionIds, Course course, boolean hasFlagged) {
            this.flaggedQuestions = flaggedQuestions;
            this.allQuestions = allQuestions;
            this.questionIds = questionIds;
            this.course = course;
            this.hasFlagged = hasFlagged;
        }

        @Override
        public int getGroupCount() {
            if(hasFlagged) {
                return 2;
            }
            return 1;
        }

        @Override
        public int getChildrenCount(int i) {
            if(hasFlagged && i == 0) {
                return flaggedQuestions.size();
            }
            return allQuestions.size();
        }

        @Override
        public String getGroup(int i) {
            if(i == 0 && hasFlagged) {
                return getString(R.string.flagged);
            } else {
                return getString(R.string.questions);
            }
        }

        @Override
        public QuizSubmissionQuestion getChild(int groupIndex, int childIndex) {
            if(hasFlagged && groupIndex == 0) {
                return flaggedQuestions.get(childIndex);
            }
            return allQuestions.get(childIndex);
        }

        @Override
        public long getGroupId(int i) {
            return i;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return getChild(groupPosition, childPosition).getId();
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getGroupView(int i, boolean b, View convertView, ViewGroup viewGroup) {
            GroupViewHolder holder;

            if (convertView == null) {
                LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
                convertView = layoutInflater.inflate(R.layout.expandable_list_group_two, null);

                holder = new GroupViewHolder();

                holder.type = convertView.findViewById(R.id.tvGroup);

                convertView.setTag(holder);
            } else {
                holder = (GroupViewHolder) convertView.getTag();
            }

            holder.type.setText(getGroup(i));
            return convertView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean b, View convertView, ViewGroup viewGroup) {

            QuizViewHolder holder;
            if(convertView == null) {
                LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
                convertView = layoutInflater.inflate(R.layout.quiz_question_dialog_item, null);
                holder = new QuizViewHolder();
                holder.icon = convertView.findViewById(R.id.image);
                holder.question = convertView.findViewById(R.id.question_number);
                convertView.setTag(holder);
            } else {
                holder = (QuizViewHolder) convertView.getTag();
            }

            QuizSubmissionQuestion quizSubmissionQuestion = getChild(groupPosition, childPosition);

            holder.icon.setVisibility(View.INVISIBLE);

            if(quizSubmissionQuestion.isFlagged() && groupPosition == 0 && hasFlagged) {
                holder.icon.setVisibility(View.VISIBLE);
                Drawable d = ColorKeeper.getColoredDrawable(getActivity(), R.drawable.vd_bookmark_filled, course);
                holder.icon.setImageDrawable(d);
                holder.question.setText(getString(R.string.question) + " " + (allQuestions.indexOf(quizSubmissionQuestion) + 1));
            } else if(questionIds.contains(quizSubmissionQuestion.getId())) {
                holder.icon.setVisibility(View.VISIBLE);
                Drawable d = ColorKeeper.getColoredDrawable(getActivity(), R.drawable.vd_check_white_24dp, course);
                holder.icon.setImageDrawable(d);
                holder.question.setText(getString(R.string.question) + " " + (childPosition + 1));
            } else {
                holder.question.setText(getString(R.string.question) + " " + (childPosition + 1));
            }



            return convertView;
        }

        @Override
        public boolean isChildSelectable(int i, int i2) {
            return true;
        }

    }

    private static class GroupViewHolder {
        TextView type;
    }

    private static class QuizViewHolder {
        ImageView icon;
        TextView question;
    }

}
