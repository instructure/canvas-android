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
 */

package com.instructure.teacher.holders;

import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.instructure.pandautils.views.CanvasWebView;
import com.instructure.teacher.R;


public class QuizMatchingViewHolder extends RecyclerView.ViewHolder {

    public TextView questionNumber;
    public CanvasWebView question;
    public LinearLayout answerContainer;
    public long questionId;

    public QuizMatchingViewHolder(View v) {
        super(v);
        questionNumber = (TextView) v.findViewById(R.id.question_number);
        question = (CanvasWebView) v.findViewById(R.id.quiz_question);
        answerContainer = (LinearLayout) v.findViewById(R.id.answer_container);
    }

    public static int adapterResId() {
        return R.layout.quiz_multi_choice;
    }

}
