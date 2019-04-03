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
import android.text.TextUtils;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.instructure.canvasapi2.models.QuizSubmissionAnswer;
import com.instructure.canvasapi2.models.QuizSubmissionQuestion;
import com.instructure.pandautils.views.CanvasWebView;
import com.instructure.teacher.R;
import com.instructure.teacher.holders.QuizMultiChoiceViewHolder;


public class QuizMultiAnswerBinder {

    public static void bind(
            final QuizMultiChoiceViewHolder holder,
            final QuizSubmissionQuestion quizSubmissionQuestion,
            final int position,
            final Context context) {

        if (holder == null) {
            return;
        }

        holder.question.loadUrl("about:blank");

        holder.question.setBackgroundColor(Color.TRANSPARENT);

        holder.question.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                view.loadUrl("javascript:MyApp.resize(document.body.getBoundingClientRect().height)");
                super.onPageFinished(view, url);
            }
        });
        if (context instanceof Activity) {
            holder.question.addJavascriptInterface(new QuizMultiChoiceBinder.WebAppInterface(((Activity) context), holder.question), "MyApp");
        } else if (context instanceof ContextThemeWrapper) {
            holder.question.addJavascriptInterface(new QuizMultiChoiceBinder.WebAppInterface((Activity) (((ContextThemeWrapper) context).getBaseContext()), holder.question), "MyApp");
        }
        holder.question.loadHtml(quizSubmissionQuestion.getQuestionText(), "");

        holder.questionNumber.setText(quizSubmissionQuestion.getQuestionName() + " " + (position + 1));

        holder.questionId = quizSubmissionQuestion.getId();

        LayoutInflater inflater = LayoutInflater.from(context);

        //sometimes when we recycle views it keeps the old views in there, so clear them out if there
        //are any in there
        if (holder.answerContainer.getChildCount() > 0) {
            holder.answerContainer.removeAllViews();
        }
        //add answers to the answer container
        int index = 0;
        for (final QuizSubmissionAnswer answer : quizSubmissionQuestion.getAnswers()) {

            final LinearLayout answerContainer = (LinearLayout) inflater.inflate(R.layout.quiz_multi_choice_answer, null, false);
            final CanvasWebView webView = (CanvasWebView) answerContainer.findViewById(R.id.html_answer);
            webView.setClickable(false);
            webView.setFocusableInTouchMode(false);
            final TextView textView = (TextView) answerContainer.findViewById(R.id.text_answer);
            final CheckBox checkBox = (CheckBox) answerContainer.findViewById(R.id.answer_checkbox);

            if (!TextUtils.isEmpty(answer.getHtml())) {
                textView.setVisibility(View.GONE);

                final String html = answer.getHtml();

                webView.loadHtml(html, "");

                webView.setBackgroundColor(Color.TRANSPARENT);

            } else if (!TextUtils.isEmpty(answer.getText())) {
                webView.setVisibility(View.GONE);
                textView.setText(answer.getText());
            }

            holder.answerContainer.addView(answerContainer);

            if (index == quizSubmissionQuestion.getAnswers().length - 1) {
                //if we're on the last answer remove the bottom divider
                answerContainer.findViewById(R.id.divider).setVisibility(View.GONE);
            } else {
                answerContainer.findViewById(R.id.divider).setVisibility(View.VISIBLE);
                index++;
            }

        }
    }
}
