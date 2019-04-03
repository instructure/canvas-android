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

import com.instructure.canvasapi.utilities.APIHelpers;

import java.util.Date;

public class ModuleObject extends CanvasModel<ModuleObject> {

    /**
     * {
     // the unique identifier for the module
     id: 123,

     // the position of this module in the course (1-based)
     position: 2,


     // the name of this module
     name: "Imaginary Numbers and You",

     // (Optional) the date this module will unlock
     unlock_at: "2012-12-31T06:00:00-06:00",

     // Whether module items must be unlocked in order
     require_sequential_progress: true,

     // IDs of Modules that must be completed before this one is unlocked
     prerequisite_module_ids: [121, 122],

     // The state of this Module for the calling user
     // one of 'locked', 'unlocked', 'started', 'completed'
     // (Optional; present only if the caller is a student)
     state: 'started',

     // the date the calling user completed the module
     // (Optional; present only if the caller is a student)
     completed_at: nil
     }
     */

    private long id;
    private int position;
    private String name;
    private String unlock_at;
    private boolean require_sequential_progress;
    private long[] prerequisite_module_ids;
    private String state;
    private String completed_at;

    @Override
    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }
    public int getPosition() {
        return position;
    }
    public void setPosition(int position) {
        this.position = position;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public Date getUnlock_at() {
        if(unlock_at != null) {
            return APIHelpers.stringToDate(unlock_at);
        }
        else {
            return null;
        }

    }
    public boolean isSequential_progress() {
        return require_sequential_progress;
    }
    public void setSequential_progress(boolean sequential_progress) {
        this.require_sequential_progress = sequential_progress;
    }
    public long[] getPrerequisite_ids() {
        return prerequisite_module_ids;
    }
    public void setPrerequisite_ids(long[] prerequisite_ids) {
        this.prerequisite_module_ids = prerequisite_ids;
    }
    public String getState() {
        return state;
    }
    public void setState(String state) {
        this.state = state;
    }
    public Date getCompleted_at() {
        if(completed_at != null) {
            return APIHelpers.stringToDate(completed_at);
        }
        else {
            return null;
        }
    }

    public enum STATE {completed, must_submit, must_view, must_contribute, min_score, unlock_requirements, unlocked, started, locked}

    ///////////////////////////////////////////////////////////////////////////
    // Required Overrides
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public Date getComparisonDate() {
        return null;
    }

    @Override
    public String getComparisonString() {
        return getName();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Overrides
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ModuleObject that = (ModuleObject) o;

        return id == that.id;

    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeInt(this.position);
        dest.writeString(this.name);
        dest.writeString(this.unlock_at);
        dest.writeByte(require_sequential_progress ? (byte) 1 : (byte) 0);
        dest.writeLongArray(this.prerequisite_module_ids);
        dest.writeString(this.state);
        dest.writeString(this.completed_at);
    }

    public ModuleObject() {
    }

    private ModuleObject(Parcel in) {
        this.id = in.readLong();
        this.position = in.readInt();
        this.name = in.readString();
        this.unlock_at = in.readString();
        this.require_sequential_progress = in.readByte() != 0;
        this.prerequisite_module_ids = in.createLongArray();
        this.state = in.readString();
        this.completed_at = in.readString();
    }

    public static Creator<ModuleObject> CREATOR = new Creator<ModuleObject>() {
        public ModuleObject createFromParcel(Parcel source) {
            return new ModuleObject(source);
        }

        public ModuleObject[] newArray(int size) {
            return new ModuleObject[size];
        }
    };
}

