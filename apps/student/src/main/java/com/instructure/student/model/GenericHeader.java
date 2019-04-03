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

package com.instructure.student.model;

import android.os.Parcel;

import com.instructure.canvasapi2.models.CanvasModel;

import java.util.Date;

public class GenericHeader extends CanvasModel<GenericHeader> {

    private String headerText;

    public GenericHeader(String headerText) {
        this.headerText = headerText;
    }

    @Override
    public long getId() {
        return headerText != null ? headerText.hashCode() : -1;
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
    public int describeContents() {
        return 0;
    }

    public String getHeaderText() {
        return headerText;
    }

    public void setHeaderText(String headerText) {
        this.headerText = headerText;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.headerText);
    }

    public GenericHeader() {
    }

    private GenericHeader(Parcel in) {
        this.headerText = in.readString();
    }

    public static final Creator<GenericHeader> CREATOR = new Creator<GenericHeader>() {
        public GenericHeader createFromParcel(Parcel source) {
            return new GenericHeader(source);
        }

        public GenericHeader[] newArray(int size) {
            return new GenericHeader[size];
        }
    };
}
