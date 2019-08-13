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

package com.instructure.student.binders;

import android.content.Context;
import android.content.DialogInterface;
import androidx.appcompat.app.AlertDialog;
import android.view.View;

import com.instructure.student.R;
import com.instructure.student.adapter.QuizSubmissionQuestionListRecyclerAdapter;
import com.instructure.student.holders.SubmitButtonViewHolder;
import com.instructure.student.interfaces.QuizSubmit;
import com.instructure.canvasapi2.models.CanvasContext;
import com.instructure.pandautils.utils.ColorKeeper;

public class SubmitButtonBinder {

    public static void bind(SubmitButtonViewHolder holder, final Context context, CanvasContext canvasContext, final QuizSubmissionQuestionListRecyclerAdapter adapter, final QuizSubmit callback) {

        if(holder == null) {
            return;
        }

        //todo implement primary/dark color here
        int color = ColorKeeper.getOrGenerateColor(canvasContext);
        holder.submitButton.setBackgroundColor(color    );

        holder.submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //check to see if there are unanswered quizResult questions
                boolean hasUnanswered = false;
                int numUnanswered = 0;
                if(adapter.getAnsweredQuestions().size() != adapter.getAnswerableQuestionCount()) {
                    hasUnanswered = true;
                    numUnanswered = (adapter.getAnswerableQuestionCount()) - adapter.getAnsweredQuestions().size();
                }

                String message;

                if(hasUnanswered) {
                    message = context.getResources().getQuantityString(R.plurals.unanswered_quiz_questions, numUnanswered, numUnanswered);
                } else {
                    message = context.getString(R.string.areYouSure);
                }

                AlertDialog dialog = new AlertDialog.Builder(context)
                        .setTitle(R.string.submitQuiz)
                        .setMessage(message)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                callback.submitQuiz();
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        })
                        .setCancelable(false)
                        .create();

                dialog.setCancelable(false);
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();
            }
        });
    }
}
