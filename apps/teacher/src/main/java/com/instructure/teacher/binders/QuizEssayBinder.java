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

package com.instructure.teacher.binders;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.ContextThemeWrapper;

import com.instructure.canvasapi2.models.QuizSubmissionQuestion;
import com.instructure.pandautils.views.CanvasWebView;
import com.instructure.teacher.R;
import com.instructure.teacher.holders.QuizEssayViewHolder;


public class QuizEssayBinder {

    public static void bind(final QuizEssayViewHolder holder,
                            final QuizSubmissionQuestion quizSubmissionQuestion,
                            int position,
                            final Context context,
                            CanvasWebView.CanvasEmbeddedWebViewCallback callback,
                            CanvasWebView.CanvasWebViewClientCallback webViewClientCallback) {

        if(holder == null) {
            return;
        }
        holder.question.loadUrl("about:blank");
        holder.question.setCanvasWebViewClientCallback(webViewClientCallback);
        if(context instanceof Activity) {
            holder.question.addJavascriptInterface(new QuizMultiChoiceBinder.WebAppInterface(((Activity) context), holder.question), "MyApp");
        } else if(context instanceof ContextThemeWrapper) {
            holder.question.addJavascriptInterface(new QuizMultiChoiceBinder.WebAppInterface((Activity)(((ContextThemeWrapper) context).getBaseContext()), holder.question), "MyApp");
        }
        holder.question.loadHtml(quizSubmissionQuestion.getQuestionText(), "");
        holder.question.setBackgroundColor(Color.TRANSPARENT);
        holder.question.setCanvasEmbeddedWebViewCallback(callback);
        holder.questionNumber.setText(context.getString(R.string.question) + " " + (position + 1));

        holder.questionId = quizSubmissionQuestion.getId();
    }

    
}
