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
import android.view.View;
import android.webkit.JavascriptInterface;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.internal.LinkedTreeMap;
import com.instructure.student.R;
import com.instructure.student.holders.QuizMultipleDropdownViewHolder;
import com.instructure.student.interfaces.QuizPostMultipleDropdown;
import com.instructure.student.interfaces.QuizToggleFlagState;
import com.instructure.canvasapi2.models.QuizSubmissionAnswer;
import com.instructure.canvasapi2.models.QuizSubmissionQuestion;
import com.instructure.pandautils.utils.ColorKeeper;
import com.instructure.pandautils.views.CanvasWebView;

import java.util.ArrayList;
import java.util.HashMap;

public class QuizMultipleDropdownBinder {

    // Use a global string to keep track of the html because we modify it in a loop to set the correct answers
    private static String finalHTML;
    private static boolean shouldLetAnswerQuestion;

    public static void bind(final QuizMultipleDropdownViewHolder holder,
                            final QuizSubmissionQuestion quizSubmissionQuestion,
                            final int courseColor,
                            final int position,
                            final boolean shouldLetAnswer,
                            final Context context,
                            final CanvasWebView.CanvasEmbeddedWebViewCallback embeddedWebViewCallback,
                            final CanvasWebView.CanvasWebViewClientCallback webViewClientCallback,
                            final QuizPostMultipleDropdown callback,
                            final QuizToggleFlagState flagStateCallback) {
        if (holder == null) {
            return;
        }

        shouldLetAnswerQuestion = shouldLetAnswer;

        setupViews(holder, quizSubmissionQuestion, position, context, embeddedWebViewCallback, webViewClientCallback, callback);

        LayoutInflater inflater = LayoutInflater.from(context);

        //use a map to store the data, each blank id will have an array list of potential answers
        HashMap<String, ArrayList<QuizSubmissionAnswer>> answerMap = new HashMap<>();
        for (QuizSubmissionAnswer answer : quizSubmissionQuestion.getAnswers()) {
            ArrayList<QuizSubmissionAnswer> potentialAnswers = answerMap.get(answer.getBlankId());
            if (potentialAnswers == null) {
                potentialAnswers = new ArrayList<>();
                QuizSubmissionAnswer selectAnswer = new QuizSubmissionAnswer();
                selectAnswer.setText(context.getString(R.string.quizMatchingDefaultDisplay));
                potentialAnswers.add(selectAnswer);
            }
            potentialAnswers.add(answer);
            answerMap.put(answer.getBlankId(), potentialAnswers);
        }

        if (answerMap.size() > 1) {
            //we have more than one, update the string to reflect that
            holder.chooseAnswer.setText(context.getString(R.string.choose_answers_below));
        } else {
            holder.chooseAnswer.setText(context.getString(R.string.choose_answer_below));
        }

        //add answers to the answer container
        for (final String blankId : answerMap.keySet()) {

            final LinearLayout answerWrapper = (LinearLayout) inflater.inflate(R.layout.quiz_multiple_dropdown_answer, null, false);

            final TextView answerTextView = answerWrapper.findViewById(R.id.text_answer);

            ArrayList<QuizSubmissionAnswer> list = new ArrayList<>(answerMap.get(blankId));

            if (!TextUtils.isEmpty(blankId)) {
                answerTextView.setText(blankId);

                setPreviouslySelectedAnswer(quizSubmissionQuestion, blankId, list, holder.question);
            }

            final Drawable courseColorFlag = ColorKeeper.getColoredDrawable(context, R.drawable.vd_bookmark_filled, courseColor);

            if (quizSubmissionQuestion.isFlagged()) {
                holder.flag.setImageDrawable(courseColorFlag);
            } else {
                holder.flag.setImageDrawable(ColorKeeper.getColoredDrawable(context, R.drawable.vd_navigation_bookmarks, context.getResources().getColor(R.color.defaultTextGray)));
            }

            if (shouldLetAnswer) {
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
        }
    }

    public static class QuizInterface {
        private QuizPostMultipleDropdown callback;
        private QuizSubmissionQuestion quizSubmissionQuestion;
        
        private QuizInterface(QuizSubmissionQuestion question, QuizPostMultipleDropdown callback) {
            quizSubmissionQuestion = question;
            this.callback = callback;
        }

        @JavascriptInterface
        public void onItemSelected(String value) {
            String blankId = "";
            Long currentAnswer = Long.parseLong(value);
            for(QuizSubmissionAnswer answer : quizSubmissionQuestion.getAnswers()) {
                if(answer.getId() == currentAnswer) {
                    blankId = answer.getBlankId();
                }

            }
            HashMap<String, Long> answerMap = new HashMap<>();

            answerMap.put(blankId, Long.parseLong(value));
            callback.postMultipleDropdown(quizSubmissionQuestion.getId(), answerMap);
        }
    }

    private static String getJavascript() {
        // if they can't answer the question, don't add the javascript that will select the item
        if (shouldLetAnswerQuestion) {
            // Javascript to call our java function when the user selects anything
            return "<script>\n" +
                    "          document.querySelectorAll('select').forEach(select => {\n" +
                    "            select.onchange = () => {\n" +
                    "               accessor.onItemSelected(select.value)\n" +
                    "            }\n" +
                    "          })\n" +
                    "        </script>";
        } else return "";
    }

    private static void setupViews(QuizMultipleDropdownViewHolder holder,
                                   QuizSubmissionQuestion quizSubmissionQuestion,
                                   int position,
                                   Context context,
                                   CanvasWebView.CanvasEmbeddedWebViewCallback embeddedWebViewCallback,
                                   CanvasWebView.CanvasWebViewClientCallback webViewClientCallback,
                                   QuizPostMultipleDropdown callback) {

        holder.question.getSettings().setJavaScriptEnabled(true);
        holder.question.addJavascriptInterface(new QuizInterface(quizSubmissionQuestion, callback), "accessor");
        holder.question.loadUrl("about:blank");

        holder.question.setBackgroundColor(Color.TRANSPARENT);

        holder.question.setCanvasWebViewClientCallback(webViewClientCallback);

        finalHTML = quizSubmissionQuestion.getQuestionText() + getJavascript();

        holder.question.formatHTML(finalHTML, "");
        holder.question.setCanvasEmbeddedWebViewCallback(embeddedWebViewCallback);

        holder.questionNumber.setText(context.getString(R.string.question) + " " + (position + 1));

        holder.questionId = quizSubmissionQuestion.getId();

        //sometimes when we recycle views it keeps the old views in there, so clear them out if there
        //are any in there
        if(holder.answerContainer.getChildCount() > 0) {
            holder.answerContainer.removeAllViews();
        }
    }

    private static void setPreviouslySelectedAnswer(QuizSubmissionQuestion quizSubmissionQuestion, String answer, ArrayList<QuizSubmissionAnswer> list, CanvasWebView questionWebView) {
        if(quizSubmissionQuestion.getAnswer() != null) {
            // set the one they selected last time
            for(String map :((LinkedTreeMap<String, String>) quizSubmissionQuestion.getAnswer()).keySet()) {
                if(!answer.equals("null") && map.equals(answer)) {

                    String matchId = ((LinkedTreeMap<String, String>) quizSubmissionQuestion.getAnswer()).get(map);
                    //now see if we have a match in the list of matches
                    for(QuizSubmissionAnswer match : list) {
                        if (Long.toString(match.getId()).equals(matchId)) {
                            // Modify the correct answer to be selected
                            finalHTML = finalHTML.replace("value=\"" + matchId + "\"", "value=\"" + matchId + "\" selected");
                            questionWebView.formatHTML(finalHTML, "");
                            break;
                        }
                    }
                }
            }
        }
    }
}
