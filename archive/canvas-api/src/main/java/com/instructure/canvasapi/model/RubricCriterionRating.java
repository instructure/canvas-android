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

import java.io.Serializable;

public class RubricCriterionRating implements Parcelable,  Serializable , Comparable<RubricCriterionRating>{

    public static final long serialVersionUID = 1L;

	private String id;
    private String criterionId;
	private String description;
	private double points;
	private String comments;
    private boolean isGrade;
    private boolean isFreeFormComment;
    private double maxPoints;
    ///////////////////////////////////////////////////////////////////////////
    // Getters and Setters
    ///////////////////////////////////////////////////////////////////////////
	
	public String getId() {
		return id == null ? criterionId : id;
	}
	public void setId(String id) {
		this.id = id;
	}
    public String getCriterionId() {
        return criterionId;
    }
    public void setCriterionId(String criterionId) {
        this.criterionId = criterionId;
    }
    public String getRatingDescription() {
		return description;
	}
	public void setRatingDescription(String ratingDescription) {
		this.description = ratingDescription;
	}
	public double getPoints() {
		return points;
	}
	public void setPoints(double points) {
		this.points = points;
	}
    public double getMaxPoints() {
        return maxPoints;
    }
    public void setMaxPoints(double points) {
        this.maxPoints = points;
    }
	public String getComments() {
		return comments;
	}
	public void setComments(String comments) {
		this.comments = comments;
	}
    public boolean isGrade() {
        return isGrade;
    }
    public void setGrade(boolean grade) {
        isGrade = grade;
    }
    public void setIsFreeFormComment(boolean isFreeFormComment){
        this.isFreeFormComment = isFreeFormComment;
    }
    public boolean isFreeFormComment(){
        return this.isFreeFormComment;
    }
    ///////////////////////////////////////////////////////////////////////////
    // Overrides
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RubricCriterionRating rating = (RubricCriterionRating) o;

        return id != null ? id.equals(rating.id) : rating.id == null;

    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }


    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    public RubricCriterionRating() {}

    public RubricCriterionRating(String criterionId) {
        setGrade(false);
        setCriterionId(criterionId);
    }

    public RubricCriterionRating(RubricCriterion rubricCriterion) {
        setGrade(false);
        setCriterionId(rubricCriterion.getId());
    }

    ///////////////////////////////////////////////////////////////////////////
    // Helpers
    ///////////////////////////////////////////////////////////////////////////
    public boolean isComment() {
        return getComments() != null && !getComments().equals("");
    }


    ///////////////////////////////////////////////////////////////////////////
    // Overrides
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public int compareTo(RubricCriterionRating rubricCriterionRating) {
        return this.getId().compareTo(rubricCriterionRating.getId());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.criterionId);
        dest.writeString(this.description);
        dest.writeDouble(this.points);
        dest.writeString(this.comments);
        dest.writeByte(isGrade ? (byte) 1 : (byte) 0);
        dest.writeByte(isFreeFormComment ? (byte) 1 : (byte) 0);
        dest.writeDouble(this.maxPoints);
    }

    private RubricCriterionRating(Parcel in) {
        this.id = in.readString();
        this.criterionId = in.readString();
        this.description = in.readString();
        this.points = in.readDouble();
        this.comments = in.readString();
        this.isGrade = in.readByte() != 0;
        this.isFreeFormComment = in.readByte() != 0;
        this.maxPoints = in.readDouble();
    }

    public static Creator<RubricCriterionRating> CREATOR = new Creator<RubricCriterionRating>() {
        public RubricCriterionRating createFromParcel(Parcel source) {
            return new RubricCriterionRating(source);
        }

        public RubricCriterionRating[] newArray(int size) {
            return new RubricCriterionRating[size];
        }
    };
}
