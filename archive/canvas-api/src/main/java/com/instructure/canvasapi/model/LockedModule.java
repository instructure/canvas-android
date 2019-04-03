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
import android.util.Log;

import com.instructure.canvasapi.utilities.APIHelpers;
import com.instructure.canvasapi.utilities.TestHelpers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class LockedModule extends CanvasComparable<LockedModule> implements Serializable{
    private static final long serialVersionUID = 1L;

    private long id;
    private long context_id;
    private String context_type;
    private String name;
    private String unlock_at;
    private boolean require_sequential_progress;

    private List<ModuleName> prerequisites = new ArrayList<>();
    private List<ModuleCompletionRequirement> completion_requirements = new ArrayList<>();

    ///////////////////////////////////////////////////////////////////////////
    // Getters and Setters
    ///////////////////////////////////////////////////////////////////////////
    @Override
    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }
    public String getContextType() {
        return context_type;
    }
    public void setContextType(String context_type) {
        this.context_type = context_type;
    }
    public String getName() {
        return name;
    }
    public void setName(String name){
        this.name = name;
    }
    public boolean isRequireSequentialProgress() {
        return require_sequential_progress;
    }
    public void setRequireSequentialProgress(boolean require_sequential_progress) {
        this.require_sequential_progress = require_sequential_progress;
    }
    public List<ModuleName> getPrerequisites() {
        return prerequisites;
    }
    public Date getUnlock_at() {
        return APIHelpers.stringToDate(unlock_at);
    }
    public long getContext_id() {
        return context_id;
    }
    public List<ModuleCompletionRequirement> getCompletionRequirements() {
        return completion_requirements;
    }
    public void setCompletionRequirements(List<ModuleCompletionRequirement> completion_requirements) {
        this.completion_requirements = completion_requirements;
    }

    //Module Name
    private class ModuleName implements Serializable {
        private static final long serialVersionUID = 1L;

        private String getName() {
            return name;
        }

        private void setName(String name) {
            this.name = name;
        }

        private String name;
    }
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
    // Unit Tests
    ///////////////////////////////////////////////////////////////////////////
    public static boolean isLockedModuleValid(LockedModule lockedModule) {
        if(lockedModule.getContext_id() <= 0) {
            Log.d(TestHelpers.UNIT_TEST_TAG, "Invalid LockedModule id");
            return false;
        }
        if(lockedModule.getName() == null) {
            Log.d(TestHelpers.UNIT_TEST_TAG, "Invalid LockedModule name");
            return false;
        }
        if(lockedModule.getUnlock_at() == null) {
            Log.d(TestHelpers.UNIT_TEST_TAG, "Invalid LockedModule unlock date");
            return false;
        }
        if(lockedModule.getPrerequisites() == null) {
            Log.d(TestHelpers.UNIT_TEST_TAG, "Invalid LockedModule prerequisites");
            return false;
        }
        for(int i = 0; i < lockedModule.getPrerequisites().size(); i++) {
            if(lockedModule.getPrerequisites().get(i).getName() == null) {
                Log.d(TestHelpers.UNIT_TEST_TAG, "Invalid LockedModule prereq name");
                return false;
            }
        }
        return true;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////
    public LockedModule() {}

    private LockedModule(Parcel in) {
        in.readList(this.getPrerequisites(), ModuleName.class.getClassLoader());
        this.unlock_at = in.readString();
        this.name = in.readString();
        this.context_id = in.readLong();
        this.id = in.readLong();
        this.context_type = in.readString();
        this.require_sequential_progress = in.readByte() != 0;
        in.readList(this.getCompletionRequirements(), ModuleCompletionRequirement.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeList(this.prerequisites);
        dest.writeString(this.unlock_at);
        dest.writeString(this.name);
        dest.writeLong(this.context_id);
        dest.writeLong(this.id);
        dest.writeString(this.context_type);
        dest.writeByte(this.require_sequential_progress ? (byte) 1 : (byte) 0);
        dest.writeList(this.completion_requirements);
    }

    public static Creator<LockedModule> CREATOR = new Creator<LockedModule>() {
        public LockedModule createFromParcel(Parcel source) {
            return new LockedModule(source);
        }

        public LockedModule[] newArray(int size) {
            return new LockedModule[size];
        }
    };
}
