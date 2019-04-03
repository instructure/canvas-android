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
import java.util.ArrayList;
import java.util.List;

public class RubricCriterion implements Serializable , Comparable<RubricCriterion>, Parcelable {

	private static final long serialVersionUID = 1L;
	
	private String id;
	private Rubric rubric;
	private String description;
	private String long_description;
	private double points;
	private List<RubricCriterionRating> ratings = new ArrayList<RubricCriterionRating>();

    ///////////////////////////////////////////////////////////////////////////
    // Getters and Setters
    ///////////////////////////////////////////////////////////////////////////
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public Rubric getRubric() {
		return rubric;
	}
	public void setRubric(Rubric rubric) {
		this.rubric = rubric;
	}
	public String getCriterionDescription() {
		return description;
	}
	public void setCriterionDescription(String criterionDescription) {
		this.description = criterionDescription;
	}
	public String getLongDescription() {
		return long_description;
	}
	public void setLongDescription(String longDescription) {
		this.long_description = longDescription;
	}
	public double getPoints() {
		return points;
	}
	public void setPoints(double points) {
		this.points = points;
	}

	public List<RubricCriterionRating> getRatings() {
		return ratings;
	}

    public List<RubricCriterionRating> getRatingsWithCriterionIds(){
        for(RubricCriterionRating rating : ratings){
            rating.setCriterionId(this.id);
        }
        return ratings;
    }

	public void setRatings(List<RubricCriterionRating> ratings) {
		this.ratings = ratings;
	}

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    public RubricCriterion(Rubric rubric) {
        setRubric(rubric);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Helpers
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RubricCriterion that = (RubricCriterion) o;

        return id != null ? id.equals(that.id) : that.id == null;

    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Helpers
    ///////////////////////////////////////////////////////////////////////////
    public RubricCriterionRating getGradedCriterionRating(){
        for(RubricCriterionRating rating : ratings){
            if(rating.isGrade()){
                return rating;
            }
        }
        return null;
    }

    /**
     *  Freeform rubric comments in canvas may contain RubricCriterionRatings not included in the assignment rubric.
     *  @return true if the rubric assessment contains a rating for the provided rubric criterion
     */
    public  boolean containsRubricCriterionRating(String ratingId, List<RubricCriterionRating> criterionRatings){
        for(RubricCriterionRating rating : criterionRatings){
            if(rating.getId().equals(ratingId)){
                return true;
            }
        }
        return false;
    }

    public void markGradeByPoints(double points){
        for (RubricCriterionRating criterionRating : ratings) {
            if (criterionRating.getPoints() == points) {
                criterionRating.setGrade(true);
            }else{
                criterionRating.setGrade(false);
            }
        }
    }

    public void handleComments(RubricCriterionRating rating){
        if (rating.isComment() && !ratings.contains(rating)) {
            rating.setRatingDescription(rating.getComments());
            ratings.add(rating);
        }
    }

    public void markGrade(RubricCriterionRating rating) {
        markGradeByPoints(rating.getPoints());
        handleComments(rating);
    }


    public void markFreeformGrade(RubricCriterionRating rating, RubricCriterion criterion) {
        if(containsRubricCriterionRating(rating.getCriterionId(), criterion.getRatings())){
            markGradeByPoints(rating.getPoints());
        }
        else{
            rating.setGrade(true);
            ratings.add(rating);
        }

        handleComments(rating);
    }

    public void markGrades(RubricAssessment rubricAssessment, List<RubricCriterion> criteria) {
        if (rubricAssessment == null) { return; }

        for (RubricCriterionRating rating : rubricAssessment.getRatings()) {
            for (RubricCriterion criterion : criteria) {
                if (criterion.getId().equals(rating.getCriterionId())) {
                    criterion.markGrade(rating);
                    break;
                }
            }
        }
    }

    public void markGrades(RubricAssessment rubricAssessment, List<RubricCriterion> criteria, boolean isFreeFormComment) {
        if (rubricAssessment == null) { return; }

        for (RubricCriterionRating rating : rubricAssessment.getRatings()) {
            for (RubricCriterion criterion : criteria) {
                if (criterion.getId().equals(rating.getCriterionId())) {
                    if(isFreeFormComment){
                        criterion.markFreeformGrade(rating, criterion);
                    }
                    else{
                        criterion.markGrade(rating);
                    }
                    break;
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Overrides
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public int compareTo(RubricCriterion rubricCriterion) {
        return this.getId().compareTo(rubricCriterion.getId());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeParcelable(this.rubric, flags);
        dest.writeString(this.description);
        dest.writeString(this.long_description);
        dest.writeDouble(this.points);
        dest.writeList(ratings);
    }

    private RubricCriterion(Parcel in) {
        this.id = in.readString();
        this.rubric =  in.readParcelable(Rubric.class.getClassLoader());
        this.description = in.readString();
        this.long_description = in.readString();
        this.points = in.readDouble();
        in.readList(ratings, RubricCriterionRating.class.getClassLoader());
    }

    public static Parcelable.Creator<RubricCriterion> CREATOR = new Parcelable.Creator<RubricCriterion>() {
        public RubricCriterion createFromParcel(Parcel source) {
            return new RubricCriterion(source);
        }

        public RubricCriterion[] newArray(int size) {
            return new RubricCriterion[size];
        }
    };
}
