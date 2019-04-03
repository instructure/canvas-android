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

package com.instructure.canvasapi.model;

import android.os.Parcel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PollSubmissionResponse extends CanvasComparable<PollSubmissionResponse> {

    private List<PollSubmission> poll_submissions = new ArrayList<PollSubmission>();

    public List<PollSubmission> getPollSubmissions() {
        return poll_submissions;
    }

    public void setPollSubmissions(List<PollSubmission> poll_submissions) {
        this.poll_submissions = poll_submissions;
    }

    @Override
    public Date getComparisonDate() {
        return null;
    }

    @Override
    public String getComparisonString() {
        return null;
    }

    @Override
    public int compareTo(PollSubmissionResponse another) {
        return 0;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(poll_submissions);
    }

    public PollSubmissionResponse() {
    }

    private PollSubmissionResponse(Parcel in) {
        in.readTypedList(poll_submissions, PollSubmission.CREATOR);
    }

    public static Creator<PollSubmissionResponse> CREATOR = new Creator<PollSubmissionResponse>() {
        public PollSubmissionResponse createFromParcel(Parcel source) {
            return new PollSubmissionResponse(source);
        }

        public PollSubmissionResponse[] newArray(int size) {
            return new PollSubmissionResponse[size];
        }
    };
}
