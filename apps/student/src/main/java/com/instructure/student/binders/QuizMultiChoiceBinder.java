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
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.instructure.student.R;
import com.instructure.student.holders.QuizMultiChoiceViewHolder;
import com.instructure.student.interfaces.QuizPostMultiChoice;
import com.instructure.student.interfaces.QuizToggleFlagState;
import com.instructure.student.util.StringUtilities;
import com.instructure.canvasapi2.models.QuizSubmissionAnswer;
import com.instructure.canvasapi2.models.QuizSubmissionQuestion;
import com.instructure.pandautils.utils.ColorKeeper;
import com.instructure.pandautils.views.CanvasWebView;

public class QuizMultiChoiceBinder {


    public static void bind(final QuizMultiChoiceViewHolder holder,
                            final QuizSubmissionQuestion quizSubmissionQuestion,
                            final int courseColor,
                            final int position,
                            final boolean shouldLetAnswer,
                            final Context context,
                            final CanvasWebView.CanvasEmbeddedWebViewCallback embeddedWebViewCallback,
                            final CanvasWebView.CanvasWebViewClientCallback webViewClientCallback,
                            final QuizPostMultiChoice callback,
                            final QuizToggleFlagState flagStateCallback) {

        if(holder == null) {
            return;
        }

        holder.question.loadUrl("about:blank");

        holder.question.setBackgroundColor(Color.TRANSPARENT);

        holder.question.setCanvasWebViewClientCallback(webViewClientCallback);

        holder.question.loadHtml(quizSubmissionQuestion.getQuestionText(), "");
        holder.question.setCanvasEmbeddedWebViewCallback(embeddedWebViewCallback);

        holder.questionNumber.setText(context.getString(R.string.question) + " " + (position + 1));

        holder.questionId = quizSubmissionQuestion.getId();

        LayoutInflater inflater = LayoutInflater.from(context);

        // Sometimes when we recycle views it keeps the old views in there, so clear them out if there
        // are any in there
        if(holder.answerContainer.getChildCount() > 0) {
            holder.answerContainer.removeAllViews();
        }
        // Add answers to the answer container
        int index = 0;
        for(final QuizSubmissionAnswer answer : quizSubmissionQuestion.getAnswers()) {

            final LinearLayout answerWrapper = (LinearLayout)inflater.inflate(R.layout.quiz_multi_choice_answer, null, false);
            final CanvasWebView webView = answerWrapper.findViewById(R.id.html_answer);
            webView.setClickable(false);
            webView.setFocusableInTouchMode(false);
            final TextView textView = answerWrapper.findViewById(R.id.text_answer);
            final CheckBox checkBox = answerWrapper.findViewById(R.id.answer_checkbox);

            if(!TextUtils.isEmpty(answer.getHtml())) {
                textView.setVisibility(View.GONE);

                final String html = StringUtilities.trimTrailingWhitespace(answer.getHtml()).toString();

                webView.loadHtml(html, "");

                webView.setBackgroundColor(Color.TRANSPARENT);
                // We only care about marking the answers if they can actually answer
                if(shouldLetAnswer) {
                    webView.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View view, MotionEvent motionEvent) {
                            //this may or may not be how we do the selection of things when we get the final UI
                            if (motionEvent.getAction() == MotionEvent.ACTION_UP) {

                                answerWrapper.performClick();

                                return true;
                            }
                            return true;
                       }
                    });
                }

                if(quizSubmissionQuestion.getAnswer() != null) {
                    if(Long.parseLong((String)quizSubmissionQuestion.getAnswer()) == answer.getId()) {
                        answerWrapper.setBackgroundColor(context.getResources().getColor(R.color.canvasBackgroundMedium));

                        // Mark this one as selected
                        checkBox.setChecked(true);
                    }
                }

            } else if(!TextUtils.isEmpty(answer.getText())) {
                webView.setVisibility(View.GONE);
                textView.setText(answer.getText());

                if(quizSubmissionQuestion.getAnswer() != null) {
                    if(!TextUtils.isEmpty((String)quizSubmissionQuestion.getAnswer()) && Long.parseLong((String)quizSubmissionQuestion.getAnswer()) == answer.getId()) {
                        //mark this one as selected
                        checkBox.setChecked(true);
                        answerWrapper.setBackgroundColor(context.getResources().getColor(R.color.canvasBackgroundMedium));

                    }
                }
            }

            if(shouldLetAnswer) {
                answerWrapper.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        for (int i = 0; i < holder.answerContainer.getChildCount(); i++) {
                            if(holder.answerContainer.getId() != view.getId()) {
                                resetViews(i, holder, context);
                            }
                        }
                        checkBox.setChecked(true);

                        answerWrapper.setBackgroundColor(context.getResources().getColor(R.color.canvasBackgroundMedium));

                        // Post the answer to the api
                        callback.postAnswer(holder.questionId, answer.getId());

                        // Set the answer on the quizSubmissionQuestion so we'll remember which question was answered during row recycling
                        quizSubmissionQuestion.setAnswer(Long.toString(answer.getId()));

                    }
                });
            }

            final Drawable courseColorFlag = ColorKeeper.getColoredDrawable(context, R.drawable.vd_bookmark_filled, courseColor);

            if(quizSubmissionQuestion.isFlagged()) {
                holder.flag.setImageDrawable(courseColorFlag);
            } else {
                holder.flag.setImageDrawable(ColorKeeper.getColoredDrawable(context, R.drawable.vd_navigation_bookmarks, context.getResources().getColor(R.color.defaultTextGray)));
            }

            if(shouldLetAnswer) {

                holder.flag.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (quizSubmissionQuestion.isFlagged()) {
                            // Unflag it
                            holder.flag.setImageDrawable(ColorKeeper.getColoredDrawable(context, R.drawable.vd_navigation_bookmarks, context.getResources().getColor(R.color.defaultTextGray)));
                            flagStateCallback.toggleFlagged(false, quizSubmissionQuestion.getId());
                            quizSubmissionQuestion.setFlagged(false);
                        } else {
                            // Flag it
                            holder.flag.setImageDrawable(courseColorFlag);
                            flagStateCallback.toggleFlagged(true, quizSubmissionQuestion.getId());
                            quizSubmissionQuestion.setFlagged(true);
                        }
                    }
                });
            } else {
                holder.flag.setEnabled(false);
            }

            holder.answerContainer.addView(answerWrapper);

            if(index == quizSubmissionQuestion.getAnswers().length - 1) {
                // If we're on the last answer remove the bottom divider
                answerWrapper.findViewById(R.id.divider).setVisibility(View.GONE);
            } else {
                answerWrapper.findViewById(R.id.divider).setVisibility(View.VISIBLE);
                index++;
            }
        }
    }

    private static void resetViews(int i, QuizMultiChoiceViewHolder holder, Context context) {
        // Make all the layouts the normal color
        LinearLayout layout = (LinearLayout) holder.answerContainer.getChildAt(i);

        layout.setBackgroundColor(context.getResources().getColor(R.color.canvasBackgroundLight));

        ((CheckBox)layout.findViewById(R.id.answer_checkbox)).setChecked(false);
    }
}
