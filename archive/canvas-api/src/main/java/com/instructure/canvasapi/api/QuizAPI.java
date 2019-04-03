/*
 * Copyright (C) 2017 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

package com.instructure.canvasapi.api;

import com.instructure.canvasapi.model.CanvasContext;
import com.instructure.canvasapi.model.Quiz;
import com.instructure.canvasapi.model.QuizQuestion;
import com.instructure.canvasapi.model.QuizSubmission;
import com.instructure.canvasapi.model.QuizSubmissionQuestionResponse;
import com.instructure.canvasapi.model.QuizSubmissionResponse;
import com.instructure.canvasapi.model.QuizSubmissionTime;
import com.instructure.canvasapi.utilities.APIHelpers;
import com.instructure.canvasapi.utilities.CanvasCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import retrofit.Callback;
import retrofit.client.Response;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;
import retrofit.http.Query;


public class QuizAPI extends BuildInterfaceAPI {

    private static final String QUIZ_SUBMISSION_SESSION_STARTED = "android_session_started";

    interface QuizzesInterface {
        @GET("/{context_id}/quizzes")
        void getFirstPageQuizzesList(@Path("context_id") long context_id, Callback<Quiz[]> callback);

        @GET("/{next}")
        void getNextPageQuizzesList(@Path(value = "next", encode = false) String nextURL, Callback<Quiz[]> callback);

        @GET("/{context_id}/quizzes/{quizid}")
        void getDetailedQuiz(@Path("context_id") long context_id, @Path("quizid") long quizid, Callback<Quiz> callback);

        @GET("/{next}")
        void getDetailedQuizFromURL(@Path(value = "next", encode = false) String quizURL, Callback<Quiz> callback);

        @GET("/{context_id}/quizzes/{quizid}/questions")
        void getFirstPageQuizQuestions(@Path("context_id") long context_id, @Path("quizid") long quizid, Callback<QuizQuestion[]> callback);

        @GET("/{next}")
        void getNextPageQuizQuestions(@Path(value = "next", encode = false) String nextURL, Callback<QuizQuestion[]> callback);

        @POST("/{context_id}/quizzes/{quizid}/submissions")
        void startQuiz(@Path("context_id") long context_id, @Path("quizid") long quizid, @Body String body, Callback<Response> callback);

        @GET("/{context_id}/quizzes/{quizid}/submissions")
        void getFirstPageQuizSubmissions(@Path("context_id") long context_id, @Path("quizid") long quizid, Callback<QuizSubmissionResponse> callback);

        @GET("/{next}")
        void getNextPageQuizSubmissions(@Path(value = "next", encode = false) String nextURL, Callback<QuizSubmissionResponse> callback);

        @GET("/quiz_submissions/{quiz_submission_id}/questions")
        void getFirstPageSubmissionQuestions(@Path("quiz_submission_id") long quizSubmissionId, Callback<QuizSubmissionQuestionResponse> callback);

        @GET("/{next}")
        void getNextPageSubmissionQuestions(@Path(value = "next", encode = false) String nextURL, Callback<QuizSubmissionQuestionResponse> callback);

        @POST("/quiz_submissions/{quiz_submission_id}/questions")
        void postQuizQuestionMultiChoice(@Path("quiz_submission_id") long quizSubmissionId, @Query("attempt") int attempt, @Query("validation_token") String token, @Query("quiz_questions[][id]") long questionId, @Query("quiz_questions[][answer]") long answer, @Body String body, Callback<QuizSubmissionQuestionResponse> callback);

        @PUT("/quiz_submissions/{quiz_submission_id}/questions/{question_id}/flag")
        void putFlagQuizQuestion(@Path("quiz_submission_id") long quizSubmissionId, @Path("question_id") long questionId, @Query("attempt") int attempt, @Query("validation_token") String token, @Body String body, CanvasCallback<Response> callback);

        @PUT("/quiz_submissions/{quiz_submission_id}/questions/{question_id}/unflag")
        void putUnflagQuizQuestion(@Path("quiz_submission_id") long quizSubmissionId, @Path("question_id") long questionId, @Query("attempt") int attempt, @Query("validation_token") String token, @Body String body, CanvasCallback<Response> callback);

        @POST("/quiz_submissions/{quiz_submission_id}/questions")
        void postQuizQuestionEssay(@Path("quiz_submission_id") long quizSubmissionId, @Query("attempt") int attempt, @Query("validation_token") String token, @Query("quiz_questions[][id]") long questionId, @Query("quiz_questions[][answer]") String answer, @Body String body, Callback<QuizSubmissionQuestionResponse> callback);

        @POST("/quiz_submissions/{quiz_submission_id}/questions")
        void postQuizQuestionFileUpload(@Path("quiz_submission_id") long quizSubmissionId, @Query("attempt") int attempt, @Query("validation_token") String token, @Query("quiz_questions[][id]") long questionId, @Query("quiz_questions[][answer]") String answer, @Body String body, Callback<QuizSubmissionQuestionResponse> callback);

        @POST("/{context_id}/quizzes/{quiz_id}/submissions/{submission_id}/complete")
        void postQuizSubmit(@Path("context_id") long context_id, @Path("quiz_id") long quizId, @Path("submission_id") long submissionId, @Query("attempt") int attempt, @Query("validation_token") String token, @Body String body, Callback<QuizSubmissionResponse> callback);

        @POST("/{context_id}/quizzes/{quiz_id}/submissions/{submission_id}/events")
        void postQuizStartedEvent(@Path("context_id") long context_id, @Path("quiz_id") long quizId, @Path("submission_id") long submissionId, @Query("quiz_submission_events[][event_type]") String sessionStartedString, @Query("quiz_submission_events[][event_data][user_agent]") String userAgentString, @Body String body, CanvasCallback<Response> callback);

        @GET("/{context_id}/quizzes/{quiz_id}/submissions/{submission_id}/time")
        void getQuizSubmissionTime(@Path("context_id") long context_id, @Path("quiz_id") long quizId, @Path("submission_id") long submissionId, CanvasCallback<QuizSubmissionTime> callback);

        @POST("/quiz_submissions/{quiz_submission_id}/questions{query_params}")
        void postQuizQuestionMultiAnswers(@Path("quiz_submission_id") long quizSubmissionId,  @Path(value = "query_params", encode = false) String queryParams, @Body String body, Callback<QuizSubmissionQuestionResponse> callback);

        @POST("/quiz_submissions/{quiz_submission_id}/questions{query_params}")
        void postQuizQuestionMatching(@Path("quiz_submission_id") long quizSubmissionId,  @Path(value = "query_params", encode = false) String queryParams, @Body String body, Callback<QuizSubmissionQuestionResponse> callback);

        @POST("/quiz_submissions/{quiz_submission_id}/questions{query_params}")
        void postQuizQuestionMultipleDropdown(@Path("quiz_submission_id") long quizSubmissionId,  @Path(value = "query_params", encode = false) String queryParams, @Body String body, Callback<QuizSubmissionQuestionResponse> callback);

    }

    /////////////////////////////////////////////////////////////////////////
    // API Calls
    /////////////////////////////////////////////////////////////////////////

    public static void getFirstPageQuizzes(CanvasContext canvasContext, CanvasCallback<Quiz[]> callback) {
        if (APIHelpers.paramIsNull(callback, canvasContext)) { return; }

        buildCacheInterface(QuizzesInterface.class, callback, canvasContext).getFirstPageQuizzesList(canvasContext.getId(), callback);
        buildInterface(QuizzesInterface.class, callback, canvasContext).getFirstPageQuizzesList(canvasContext.getId(), callback);
    }

    public static void getNextPageQuizzes(String nextURL, CanvasCallback<Quiz[]> callback){
        if (APIHelpers.paramIsNull(callback, nextURL)) { return; }

        callback.setIsNextPage(true);
        buildCacheInterface(QuizzesInterface.class, callback, false).getNextPageQuizzesList(nextURL, callback);
        buildInterface(QuizzesInterface.class, callback, false).getNextPageQuizzesList(nextURL, callback);
    }

    public static void getDetailedQuiz(CanvasContext canvasContext, long quiz_id, CanvasCallback<Quiz> callback) {
        if (APIHelpers.paramIsNull(callback, canvasContext)) { return; }

        buildCacheInterface(QuizzesInterface.class, callback, canvasContext).getDetailedQuiz(canvasContext.getId(), quiz_id, callback);
        buildInterface(QuizzesInterface.class, callback, canvasContext).getDetailedQuiz(canvasContext.getId(), quiz_id, callback);
    }

    public static void getDetailedQuizFromURL(String url, CanvasCallback<Quiz> callback) {
        if (APIHelpers.paramIsNull(callback,url)) { return; }

        buildCacheInterface(QuizzesInterface.class, callback, null).getDetailedQuizFromURL(url, callback);
        buildInterface(QuizzesInterface.class, callback, null).getDetailedQuizFromURL(url, callback);
    }

    public static void getFirstPageQuizQuestions(CanvasContext canvasContext, long quiz_id, CanvasCallback<QuizQuestion[]> callback) {
        if (APIHelpers.paramIsNull(callback, canvasContext)) { return; }

        buildCacheInterface(QuizzesInterface.class, callback, canvasContext).getFirstPageQuizQuestions(canvasContext.getId(), quiz_id, callback);
        buildInterface(QuizzesInterface.class, callback, canvasContext).getFirstPageQuizQuestions(canvasContext.getId(), quiz_id, callback);
    }

    public static void getNextPageQuizQuestions(String nextURL, CanvasCallback<QuizQuestion[]> callback){
        if (APIHelpers.paramIsNull(callback, nextURL)) { return; }

        callback.setIsNextPage(true);
        buildCacheInterface(QuizzesInterface.class, callback, false).getNextPageQuizQuestions(nextURL, callback);
        buildInterface(QuizzesInterface.class, callback, false).getNextPageQuizQuestions(nextURL, callback);
    }

    public static void startQuiz(CanvasContext canvasContext, long quiz_id, CanvasCallback<Response> callback) {
        if (APIHelpers.paramIsNull(callback, canvasContext)) { return; }

        buildInterface(QuizzesInterface.class, callback, canvasContext).startQuiz(canvasContext.getId(), quiz_id, "", callback);
    }

    public static void getFirstPageQuizSubmissions(CanvasContext canvasContext, long quiz_id, CanvasCallback<QuizSubmissionResponse> callback) {
        if (APIHelpers.paramIsNull(callback, canvasContext)) { return; }

        buildCacheInterface(QuizzesInterface.class, callback, canvasContext).getFirstPageQuizSubmissions(canvasContext.getId(), quiz_id, callback);
        buildInterface(QuizzesInterface.class, callback, canvasContext).getFirstPageQuizSubmissions(canvasContext.getId(), quiz_id, callback);
    }

    public static void getNextPageQuizSubmissions(String nextURL, CanvasCallback<QuizSubmissionResponse> callback){
        if (APIHelpers.paramIsNull(callback, nextURL)) { return; }

        callback.setIsNextPage(true);
        buildCacheInterface(QuizzesInterface.class, callback, false).getNextPageQuizSubmissions(nextURL, callback);
        buildInterface(QuizzesInterface.class, callback, false).getNextPageQuizSubmissions(nextURL, callback);
    }

    public static void getFirstPageSubmissionQuestions(long quizSubmissionId, CanvasCallback<QuizSubmissionQuestionResponse> callback) {
        if (APIHelpers.paramIsNull(callback)) { return; }

        buildInterface(QuizzesInterface.class, callback, null).getFirstPageSubmissionQuestions(quizSubmissionId, callback);
    }

    public static void getNextPageSubmissionQuestions(String nextURL, CanvasCallback<QuizSubmissionQuestionResponse> callback){
        if (APIHelpers.paramIsNull(callback, nextURL)) { return; }

        callback.setIsNextPage(true);
        buildInterface(QuizzesInterface.class, callback, false).getNextPageSubmissionQuestions(nextURL, callback);
    }

    public static void postQuizQuestionMultiChoice(QuizSubmission quizSubmission, long answerId, long questionId, CanvasCallback<QuizSubmissionQuestionResponse> callback){
        if (APIHelpers.paramIsNull(callback, quizSubmission, quizSubmission.getSubmissionId(), quizSubmission.getValidationToken())) { return; }

        buildInterface(QuizzesInterface.class, callback, null, false).postQuizQuestionMultiChoice(quizSubmission.getId(), quizSubmission.getAttempt(), quizSubmission.getValidationToken(), questionId, answerId, "", callback);
    }

    public static void putFlagQuizQuestion(QuizSubmission quizSubmission, long quizQuestionId, boolean shouldFlag, CanvasCallback<Response> callback) {
        if (APIHelpers.paramIsNull(callback, quizSubmission, quizSubmission.getSubmissionId(), quizSubmission.getValidationToken())) { return; }

        if(shouldFlag) {
            buildInterface(QuizzesInterface.class, callback, null, false).putFlagQuizQuestion(quizSubmission.getId(), quizQuestionId, quizSubmission.getAttempt(), quizSubmission.getValidationToken(), "", callback);
        } else {
            buildInterface(QuizzesInterface.class, callback, null, false).putUnflagQuizQuestion(quizSubmission.getId(), quizQuestionId, quizSubmission.getAttempt(), quizSubmission.getValidationToken(), "", callback);

        }
    }

    public static void postQuizQuestionEssay(QuizSubmission quizSubmission, String answer, long questionId, CanvasCallback<QuizSubmissionQuestionResponse> callback){
        if (APIHelpers.paramIsNull(callback, quizSubmission, quizSubmission.getSubmissionId(), quizSubmission.getValidationToken())) { return; }

        buildInterface(QuizzesInterface.class, callback, null, false).postQuizQuestionEssay(quizSubmission.getId(), quizSubmission.getAttempt(), quizSubmission.getValidationToken(), questionId, answer, "", callback);
    }

    public static void postQuizSubmit(CanvasContext canvasContext, QuizSubmission quizSubmission, CanvasCallback<QuizSubmissionResponse> callback) {
        if (APIHelpers.paramIsNull(canvasContext, callback, quizSubmission, quizSubmission.getSubmissionId(), quizSubmission.getValidationToken())) { return; }

        buildInterface(QuizzesInterface.class, callback, canvasContext, false).postQuizSubmit(canvasContext.getId(), quizSubmission.getQuizId(), quizSubmission.getId(), quizSubmission.getAttempt(), quizSubmission.getValidationToken(), "", callback);
    }

    public static void postQuizStartedEvent(CanvasContext canvasContext, QuizSubmission quizSubmission, String userAgentString, CanvasCallback<Response> callback) {
        if (APIHelpers.paramIsNull(canvasContext, callback, quizSubmission, quizSubmission.getSubmissionId())) { return; }

        buildInterface(QuizzesInterface.class, callback, canvasContext, false).postQuizStartedEvent(canvasContext.getId(), quizSubmission.getQuizId(), quizSubmission.getId(), QUIZ_SUBMISSION_SESSION_STARTED, userAgentString, "", callback);
    }

    public static void getQuizSubmissionTime(CanvasContext canvasContext, QuizSubmission quizSubmission, CanvasCallback<QuizSubmissionTime> callback) {
        if(APIHelpers.paramIsNull(canvasContext, callback, quizSubmission)) { return; }

        buildInterface(QuizzesInterface.class, callback, canvasContext).getQuizSubmissionTime(canvasContext.getId(), quizSubmission.getQuizId(), quizSubmission.getId(), callback);
    }
    public static void postQuizQuestionMultiAnswer(QuizSubmission quizSubmission, long questionId, ArrayList<Long> answers,  CanvasCallback<QuizSubmissionQuestionResponse> callback){
        if (APIHelpers.paramIsNull(callback, quizSubmission, quizSubmission.getSubmissionId(), quizSubmission.getValidationToken())) { return; }

        //we don't to append the per_page parameter because we're building the query parameters ourselves, so use the different interface
        buildInterface(QuizzesInterface.class, callback, null, false).postQuizQuestionMultiAnswers(quizSubmission.getId(), buildMultiAnswerList(quizSubmission.getAttempt(), quizSubmission.getValidationToken(), questionId, answers), "", callback);
    }

    public static void postQuizQuestionMatching(QuizSubmission quizSubmission, long questionId, HashMap<Long, Integer> answers,  CanvasCallback<QuizSubmissionQuestionResponse> callback){
        if (APIHelpers.paramIsNull(callback, quizSubmission, quizSubmission.getSubmissionId(), quizSubmission.getValidationToken())) { return; }

        //we don't to append the per_page parameter because we're building the query parameters ourselves, so use the different interface
        buildInterface(QuizzesInterface.class, callback, null, false).postQuizQuestionMatching(quizSubmission.getId(), buildMatchingList(quizSubmission.getAttempt(), quizSubmission.getValidationToken(), questionId, answers), "", callback);
    }

    public static void postQuizQuestionMultipleDropdown(QuizSubmission quizSubmission, long questionId, HashMap<String, Long> answers,  CanvasCallback<QuizSubmissionQuestionResponse> callback){
        if (APIHelpers.paramIsNull(callback, quizSubmission, quizSubmission.getSubmissionId(), quizSubmission.getValidationToken())) { return; }

        //we don't to append the per_page parameter because we're building the query parameters ourselves, so use the different interface
        buildInterface(QuizzesInterface.class, callback, null, false).postQuizQuestionMultipleDropdown(quizSubmission.getId(), buildMultipleDropdownList(quizSubmission.getAttempt(), quizSubmission.getValidationToken(), questionId, answers), "", callback);
    }

    public static void postQuizQuestionFileUpload(QuizSubmission quizSubmission, long answer, long questionId, CanvasCallback<QuizSubmissionQuestionResponse> callback){
        if (APIHelpers.paramIsNull(callback, quizSubmission, quizSubmission.getSubmissionId(), quizSubmission.getValidationToken())) { return; }

        buildInterface(QuizzesInterface.class, callback, null, false).postQuizQuestionFileUpload(quizSubmission.getId(), quizSubmission.getAttempt(), quizSubmission.getValidationToken(), questionId, (answer == -1) ? "" : Long.toString(answer), "", callback);
    }

    private static String buildMultiAnswerList(int attempt, String validationToken, long questionId, ArrayList<Long> answers) {
        // build the query params because we'll have an unknown amount of answers. It will end up looking like:
        // ?attempt={attempt}&validation_token={validation_token}&quiz_questions[][id]={question_id}&quiz_questions[][answer][]={answer_id}...
        StringBuilder builder = new StringBuilder();
        builder.append("?");
        builder.append("attempt=");
        builder.append(Integer.toString(attempt));
        builder.append("&");
        builder.append("validation_token=");
        builder.append(validationToken);
        builder.append("&");
        builder.append("quiz_questions[][id]=");
        builder.append(Long.toString(questionId));
        builder.append("&");
        for(Long answer : answers) {
            builder.append("quiz_questions[][answer][]");

            builder.append("=");
            builder.append(Long.toString(answer));
            builder.append("&");
        }

        String answerString = builder.toString();
        if(answerString.endsWith("&")) {
            answerString = answerString.substring(0, answerString.length() - 1);
        }
        return answerString;
    }

    private static String buildMatchingList(int attempt, String validationToken, long questionId, HashMap<Long, Integer> answers) {
        // build the query params. It will end up looking like:
        // ?attempt={attempt}&validation_token={validation_token}&quiz_questions[][id]={question_id}&quiz_questions[][answer][][answer_id]={answer_id}&quiz_questions[][answer][][match_id]={match_id}...
        StringBuilder builder = new StringBuilder();
        builder.append("?");
        builder.append("attempt=");
        builder.append(Integer.toString(attempt));
        builder.append("&");
        builder.append("validation_token=");
        builder.append(validationToken);
        builder.append("&");
        builder.append("quiz_questions[][id]=");
        builder.append(Long.toString(questionId));
        builder.append("&");
        //loop through the HashMap that contains the list of answers and their matches that the user selected
        for(Map.Entry<Long, Integer> answer : answers.entrySet()) {
            builder.append("quiz_questions[][answer][][answer_id]");

            builder.append("=");
            builder.append(Long.toString(answer.getKey()));
            builder.append("&");
            builder.append("quiz_questions[][answer][][match_id]");
            builder.append("=");
            builder.append(Integer.toString(answer.getValue()));
            builder.append("&");

        }

        String answerString = builder.toString();
        if(answerString.endsWith("&")) {
            answerString = answerString.substring(0, answerString.length() - 1);
        }
        return answerString;
    }

    private static String buildMultipleDropdownList(int attempt, String validationToken, long questionId, HashMap<String, Long> answers) {
        // build the query params. It will end up looking like:
        // ?attempt={attempt}&validation_token={validation_token}&quiz_questions[][id]={question_id}&quiz_questions[][answer][{answerKey}]={answerValue}...
        StringBuilder builder = new StringBuilder();
        builder.append("?");
        builder.append("attempt=");
        builder.append(Integer.toString(attempt));
        builder.append("&");
        builder.append("validation_token=");
        builder.append(validationToken);
        builder.append("&");
        builder.append("quiz_questions[][id]=");
        builder.append(Long.toString(questionId));
        builder.append("&");
        //loop through the HashMap that contains the list of answers and their matches that the user selected
        for(Map.Entry<String, Long> answer : answers.entrySet()) {
            builder.append("quiz_questions[][answer][");


            builder.append(answer.getKey());
            builder.append("]");

            builder.append("=");
            builder.append(Long.toString(answer.getValue()));
            builder.append("&");

        }

        String answerString = builder.toString();
        if(answerString.endsWith("&")) {
            answerString = answerString.substring(0, answerString.length() - 1);
        }
        return answerString;
    }
}
