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

public class PollSessionResponse extends CanvasComparable<PollSessionResponse> implements android.os.Parcelable {

    private List<PollSession> poll_sessions = new ArrayList<PollSession>();

    public List<PollSession> getPollSessions() {
        return poll_sessions;
    }

    public void setPollSessions(List<PollSession> pollSessions) {
        this.poll_sessions = pollSessions;
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
    public int compareTo(PollSessionResponse another) {
        return 0;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(poll_sessions);
    }

    public PollSessionResponse() {
    }

    private PollSessionResponse(Parcel in) {
        in.readTypedList(poll_sessions, PollSession.CREATOR);
    }

    public static Creator<PollSessionResponse> CREATOR = new Creator<PollSessionResponse>() {
        public PollSessionResponse createFromParcel(Parcel source) {
            return new PollSessionResponse(source);
        }

        public PollSessionResponse[] newArray(int size) {
            return new PollSessionResponse[size];
        }
    };
}
