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

package com.instructure.student.holders;

import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.instructure.student.R;
import com.instructure.pandautils.views.CanvasWebView;


public class QuizMultiChoiceViewHolder extends RecyclerView.ViewHolder {

    public TextView questionNumber;
    public CanvasWebView question;
    public LinearLayout answerContainer;
    public FloatingActionButton flag;
    public long questionId;

    public QuizMultiChoiceViewHolder(View v) {
        super(v);
        questionNumber = (TextView) v.findViewById(R.id.question_number);
        question = (CanvasWebView) v.findViewById(R.id.quiz_question);
        answerContainer = (LinearLayout) v.findViewById(R.id.answer_container);
        flag = (FloatingActionButton) v.findViewById(R.id.flag_question);
    }

    public static int adapterResId() {
        return R.layout.quiz_multi_choice;
    }

}
