/*
 * Copyright (C) 2017 - present Instructure, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

/* This is an auto-generated file. */

package com.instructure.teacher.ui.models;

import java.util.ArrayList;
import java.util.Arrays;

public class QuizQuestion {
    public static final String CALCULATED_QUESTION = "calculated_question";
    public static final String ESSAY_QUESTION = "essay_question";
    public static final String FILE_UPLOAD_QUESTION = "file_upload_question";
    public static final String MULTIPLE_BLANKS_QUESTION = "fill_in_multiple_blanks_question";
    public static final String MATCHING_QUESTION = "matching_question";
    public static final String MULTIPLE_ANSWERS_QUESTION = "multiple_answers_question";
    public static final String MULTIPLE_CHOICE_QUESTION = "multiple_choice_question";
    public static final String MULTIPLE_DROPDOWNS_QUESTION = "multiple_dropdowns_question";
    public static final String NUMERICAL_QUESTION = "numerical_question";
    public static final String SHORT_ANSWER_QUESTION = "short_answer_question";
    public static final String TEXT_ONLY_QUESTION = "text_only_question";
    public static final String TRUE_FALSE_QUESTION = "true_false_question";
    public static final String EXACT_ANSWER = "exact_answer";
    public static final String RANGE_ANSWER = "range_answer";
    public static final String PRECISION_ANSWER = "precision_answer";

    public ArrayList<QuizQuestionAnswer> answers;
    public String correctComments;
    public int id;
    public String incorrectComments;
    public String neutralComments;
    public double pointsPossible;
    public int position;
    public String questionName;
    public String questionText;
    public String questionType;
    public int quizId;

    public QuizQuestion(QuizQuestionAnswer[] answers, String correctComments, int id, String incorrectComments, String neutralComments,
                        double pointsPossible, int position, String questionName, String questionText, String questionType, int quizId) {
        this.answers = new ArrayList<>(Arrays.asList(answers));
        this.correctComments = correctComments;
        this.id = id;
        this.incorrectComments = incorrectComments;
        this.neutralComments = neutralComments;
        this.pointsPossible = pointsPossible;
        this.position = position;
        this.questionName = questionName;
        this.questionText = questionText;
        this.questionType = questionType;
        this.quizId = quizId;
    }
}
