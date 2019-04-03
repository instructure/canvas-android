/*
 * Copyright (C) 2016 - present  Instructure, Inc.
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

package com.instructure.speedgrader.interfaces;

import com.instructure.canvasapi.model.Attachment;
import com.instructure.canvasapi.model.RubricCriterionRating;
import com.instructure.canvasapi.model.Submission;
import java.util.HashMap;

public interface SubmissionListener {
    void onAttachmentSelected(Attachment attachment, Submission submission);
    void onSubmissionSelected(Submission submission);
    void onSubmissionCommentsUpdated(Submission submission);
    void onSubmissionRubricAssessmentUpdated(HashMap<String, RubricCriterionRating> newRatings, Double newScore, String newGrade);
    void setPagingEnabled(boolean isEnabled);
    void showUnsavedDataDialog(boolean isSwipeRightLeft);
    void onMediaOpened(String mime, String url, String filename);
}