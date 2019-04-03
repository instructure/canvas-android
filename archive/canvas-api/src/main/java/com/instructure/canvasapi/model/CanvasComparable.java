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

import android.os.Parcelable;

import java.io.Serializable;
import java.util.Date;

public abstract class CanvasComparable<T extends CanvasComparable> implements Comparable<T>, Parcelable, Serializable {

    public static final long serialVersionUID = 1L;

    public long getId() {
        return -1;
    }
    // return null if there is no date
    public abstract Date getComparisonDate();
    public abstract String getComparisonString();

    ///////////////////////////////////////////////////////////////////////////
    // Comparisons
    ///////////////////////////////////////////////////////////////////////////
    @Override
    public int compareTo(T comparable) {
        if (getId() == comparable.getId() && getId() > 0) {
            return 0;
        }

        int dateResult = CanvasComparable.compare(getComparisonDate(), comparable.getComparisonDate());
        if (dateResult != 0) {
            return dateResult;
        }

        int stringResult = CanvasComparable.compare(getComparisonString(), comparable.getComparisonString());
        if (stringResult != 0) {
            return stringResult;
        }

        // even if they have the same date and string just compare ids
        return Long.valueOf(getId()).compareTo(comparable.getId());
    }

    public static <C extends Comparable> int compare(C a, C b) {
        if (a == null && b == null) {
            return 0;
        } else if (a == null) {
            return 1;
        } else if (b == null) {
            return -1;
        }
        return a.compareTo(b);
    }

    public static <C extends Comparable> boolean equals(C a, C b) {
        if (a == null && b == null) {
            return true;
        } else if (a == null) {
            return false;
        } else if (b == null) {
            return false;
        }
        return a.equals(b);
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
