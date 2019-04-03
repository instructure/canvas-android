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
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class Rubric implements Parcelable {

	private static final long serialVersionUID = 1L;
	
	private Assignment assignment;
	private List<RubricCriterion> criteria = new ArrayList<RubricCriterion>();
	private boolean free_form_criterion_comments;

    ///////////////////////////////////////////////////////////////////////////
    // Getters and Setters
    ///////////////////////////////////////////////////////////////////////////
	
	public Assignment getAssignment() {
		return assignment;
	}
	public void setAssignment(Assignment assignment) {
		this.assignment = assignment;
	}
	public List<RubricCriterion> getCriteria() {
		return criteria;
	}
	public void setCriteria(List<RubricCriterion> criteria) {
		this.criteria = criteria;
	}
	public boolean isFreeFormComments() {
		return free_form_criterion_comments;
	}
	public void setFreeFormComments(boolean freeFormComments) {
		this.free_form_criterion_comments = freeFormComments;
	}

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    public Rubric(Assignment assignment) {
        setAssignment(assignment);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.assignment, flags);
        dest.writeTypedList(criteria);
        dest.writeByte(free_form_criterion_comments ? (byte) 1 : (byte) 0);
    }

    private Rubric(Parcel in) {
        in.readTypedList(criteria, RubricCriterion.CREATOR);
        this.free_form_criterion_comments = in.readByte() != 0;
    }

    public static Creator<Rubric> CREATOR = new Creator<Rubric>() {
        public Rubric createFromParcel(Parcel source) {
            return new Rubric(source);
        }

        public Rubric[] newArray(int size) {
            return new Rubric[size];
        }
    };
}
