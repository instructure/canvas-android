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

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.internal.LinkedTreeMap;
import com.instructure.student.R;
import com.instructure.student.adapter.QuizMatchSpinnerAdapter;
import com.instructure.student.holders.QuizMatchingViewHolder;
import com.instructure.student.interfaces.QuizPostMatching;
import com.instructure.student.interfaces.QuizToggleFlagState;
import com.instructure.canvasapi2.models.QuizSubmissionAnswer;
import com.instructure.canvasapi2.models.QuizSubmissionMatch;
import com.instructure.canvasapi2.models.QuizSubmissionQuestion;
import com.instructure.pandautils.utils.ColorKeeper;
import com.instructure.pandautils.utils.Const;
import com.instructure.pandautils.views.CanvasWebView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;

public class QuizMatchingBinder {

    public static void bind(final QuizMatchingViewHolder holder,
                            final QuizSubmissionQuestion quizSubmissionQuestion,
                            final int courseColor,
                            final int position,
                            final boolean shouldLetAnswer,
                            final Context context,
                            final CanvasWebView.CanvasEmbeddedWebViewCallback embeddedWebViewCallback,
                            final CanvasWebView.CanvasWebViewClientCallback webViewClientCallback,
                            final QuizPostMatching callback,
                            final QuizToggleFlagState flagStateCallback) {
        if(holder == null) {
            return;
        }

        setupViews(holder, quizSubmissionQuestion, position, context, embeddedWebViewCallback, webViewClientCallback);

        LayoutInflater inflater = LayoutInflater.from(context);

        //add answers to the answer container
        int index = 0;
        for(final QuizSubmissionAnswer answer : quizSubmissionQuestion.getAnswers()) {

            final LinearLayout answerWrapper = (LinearLayout)inflater.inflate(R.layout.quiz_matching_answer, null, false);

            final TextView answerTextView = (TextView) answerWrapper.findViewById(R.id.text_answer);
            final Spinner spinner = (Spinner) answerWrapper.findViewById(R.id.answer_spinner);

            ArrayList<QuizSubmissionMatch> list = new ArrayList<>();
            QuizSubmissionMatch firstMatch = new QuizSubmissionMatch();
            firstMatch.setText(context.getString(R.string.quizMatchingDefaultDisplay));
            list.add(firstMatch);

            for(QuizSubmissionMatch match : quizSubmissionQuestion.getMatches()) {
                list.add(match);
            }
            QuizMatchSpinnerAdapter adapter = new QuizMatchSpinnerAdapter(context, android.R.layout.simple_spinner_item, list);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);

            if(!TextUtils.isEmpty(answer.getHtml())) {
                answerTextView.setVisibility(View.GONE);

                setPreviouslySelectedAnswer(quizSubmissionQuestion, answer, spinner, list);

            } else if(!TextUtils.isEmpty(answer.getText())) {
                answerTextView.setText(answer.getText());

                setPreviouslySelectedAnswer(quizSubmissionQuestion, answer, spinner, list);
            }

            final Drawable courseColorFlag = ColorKeeper.getColoredDrawable(context, R.drawable.vd_bookmark_filled, courseColor);

            if(quizSubmissionQuestion.isFlagged()) {
                holder.flag.setImageDrawable(courseColorFlag);
            } else {
                holder.flag.setImageDrawable(ColorKeeper.getColoredDrawable(context, R.drawable.vd_navigation_bookmarks, context.getResources().getColor(R.color.defaultTextGray)));
            }

            if(shouldLetAnswer) {
                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        //post the answer to the api
                        HashMap<Long, Integer> answerMap = new HashMap<>();
                        if (((QuizSubmissionMatch) adapterView.getSelectedItem()).getMatchId() != 0) {
                            answerMap.put(answer.getId(), ((QuizSubmissionMatch) adapterView.getSelectedItem()).getMatchId());
                            callback.postMatching(holder.questionId, answerMap);
                        }
                    }

                    @Override
                    public void onNothingSelected (AdapterView < ? > adapterView){

                    }
                });

                holder.flag.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (quizSubmissionQuestion.isFlagged()) {
                            //unflag it
                            holder.flag.setImageDrawable(ColorKeeper.getColoredDrawable(context, R.drawable.vd_navigation_bookmarks, context.getResources().getColor(R.color.defaultTextGray)));
                            flagStateCallback.toggleFlagged(false, quizSubmissionQuestion.getId());
                            quizSubmissionQuestion.setFlagged(false);
                        } else {
                            //flag it
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
                //if we're on the last answer remove the bottom divider
                answerWrapper.findViewById(R.id.divider).setVisibility(View.GONE);
            } else {
                answerWrapper.findViewById(R.id.divider).setVisibility(View.VISIBLE);
                index++;
            }

        }
    }

    private static void setupViews(QuizMatchingViewHolder holder, QuizSubmissionQuestion quizSubmissionQuestion, int position, Context context, CanvasWebView.CanvasEmbeddedWebViewCallback embeddedWebViewCallback, CanvasWebView.CanvasWebViewClientCallback webViewClientCallback) {
        holder.question.loadUrl("about:blank");

        holder.question.setBackgroundColor(Color.TRANSPARENT);

        holder.question.setCanvasWebViewClientCallback(webViewClientCallback);

        if(context instanceof Activity) {
            holder.question.addJavascriptInterface(new WebAppInterface(((Activity) context), holder.question), "MyApp");
        } else if(context instanceof ContextThemeWrapper) {
            holder.question.addJavascriptInterface(new WebAppInterface((Activity)(((ContextThemeWrapper) context).getBaseContext()), holder.question), "MyApp");
        }
        holder.question.loadHtml(quizSubmissionQuestion.getQuestionText(), "");
        holder.question.setCanvasEmbeddedWebViewCallback(embeddedWebViewCallback);

        holder.questionNumber.setText(context.getString(R.string.question) + " " + (position + 1));

        holder.questionId = quizSubmissionQuestion.getId();

        //sometimes when we recycle views it keeps the old views in there, so clear them out if there
        //are any in there
        if(holder.answerContainer.getChildCount() > 0) {
            holder.answerContainer.removeAllViews();
        }
    }

    private static void setPreviouslySelectedAnswer(QuizSubmissionQuestion quizSubmissionQuestion, QuizSubmissionAnswer answer, Spinner spinner, ArrayList<QuizSubmissionMatch> list) {
        if(quizSubmissionQuestion.getAnswer() != null) {
            // set the one they selected last time
            // the api returns an ArrayList of LinkedTreeMaps
            for(LinkedTreeMap<String, String> map :((ArrayList<LinkedTreeMap<String, String>>) quizSubmissionQuestion.getAnswer())) {
                int answerId = Integer.parseInt(map.get(Const.QUIZ_ANSWER_ID));
                if(answerId == answer.getId()) {
                    if (map.get(Const.QUIZ_MATCH_ID) != null && !map.get(Const.QUIZ_MATCH_ID).equals("null")) {
                        int matchId = Integer.parseInt(map.get(Const.QUIZ_MATCH_ID));
                        //now see if we have a match in the list of matches
                        int listIndex = 0;
                        for(QuizSubmissionMatch match : list) {
                            if (match.getMatchId() == matchId) {
                                spinner.setSelection(listIndex);
                                break;
                            }
                            listIndex++;
                        }
                    }
                }
            }
        }
    }


    //Interface to resize the webviews that are in a recyclerview
    public static class WebAppInterface {
        private WeakReference<Activity> activity;
        private WebView webView;

        /**
         * Instantiate the interface and set the context
         */
        WebAppInterface(Activity activity, WebView webView) {
            this.activity = new WeakReference<>(activity);
            this.webView = webView;
        }


        @JavascriptInterface
        public void resize(final float height) {
            activity.get().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ViewGroup.LayoutParams params = webView.getLayoutParams();
                    params.width = webView.getWidth();
                    params.height = (int) (height * activity.get().getResources().getDisplayMetrics().density);
                    webView.setLayoutParams(params);
                }
            });
        }
    }

}
