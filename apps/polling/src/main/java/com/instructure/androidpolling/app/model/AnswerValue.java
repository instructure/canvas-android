/*
 * Copyright (C) 2017 - present  Instructure, Inc.
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

package com.instructure.androidpolling.app.model;

import android.os.Parcel;

import com.instructure.canvasapi2.models.CanvasComparable;

import java.util.Date;

public class AnswerValue extends CanvasComparable<AnswerValue> implements android.os.Parcelable {

    private String value;
    //this id is set locally to track which view has been selected and to update the textwatcher
    private int id;
    private boolean selected;
    //this id is the id of the poll choice that this answerValue represents
    private long pollChoiceId;
    //is the item the correct answer as marked by the teacher
    private boolean isCorrect;

    private int position;

    public long getPollChoiceId() {
        return pollChoiceId;
    }

    public void setPollChoiceId(long pollChoiceId) {
        this.pollChoiceId = pollChoiceId;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getAnswerId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isCorrect() {
        return isCorrect;
    }

    public void setCorrect(boolean isCorrect) {
        this.isCorrect = isCorrect;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
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
    public int compareTo(AnswerValue another) {
        return CanvasComparable.Companion.compare(this.getPosition(), another.getPosition());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.value);
        dest.writeInt(this.id);
        dest.writeByte(selected ? (byte) 1 : (byte) 0);
        dest.writeLong(this.pollChoiceId);
        dest.writeByte(isCorrect ? (byte) 1 : (byte) 0);
    }

    public AnswerValue() {
    }

    private AnswerValue(Parcel in) {
        this.value = in.readString();
        this.id = in.readInt();
        this.selected = in.readByte() != 0;
        this.pollChoiceId = in.readLong();
        this.isCorrect = in.readByte() != 0;
    }

    public static Creator<AnswerValue> CREATOR = new Creator<AnswerValue>() {
        public AnswerValue createFromParcel(Parcel source) {
            return new AnswerValue(source);
        }

        public AnswerValue[] newArray(int size) {
            return new AnswerValue[size];
        }
    };
}
