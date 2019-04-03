package com.instructure.canvasapi.model;

/*
 * Copyright (C) 2017 - present Instructure, Inc.
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

import android.os.Parcel;

import java.util.Date;

public class ModuleItemSequence extends CanvasModel<ModuleItemSequence> {


    private ModuleItemWrapper[] items;
    private ModuleObject[] modules;


    public ModuleItemWrapper[] getItems() {
        return items;
    }

    public void setItems(ModuleItemWrapper[] items) {
        this.items = items;
    }

    public ModuleObject[] getModules() {
        return modules;
    }

    public void setModules(ModuleObject[] modules) {
        this.modules = modules;
    }

    @Override
    public String getComparisonString() {
        return null;
    }

    @Override
    public Date getComparisonDate() {
        return null;
    }

    @Override
    public long getId() {
        return 0;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedArray(this.items, flags);
        dest.writeTypedArray(this.modules, flags);
    }

    public ModuleItemSequence() {
    }

    protected ModuleItemSequence(Parcel in) {
        this.items = in.createTypedArray(ModuleItemWrapper.CREATOR);
        this.modules = in.createTypedArray(ModuleObject.CREATOR);
    }

    public static final Creator<ModuleItemSequence> CREATOR = new Creator<ModuleItemSequence>() {
        @Override
        public ModuleItemSequence createFromParcel(Parcel source) {
            return new ModuleItemSequence(source);
        }

        @Override
        public ModuleItemSequence[] newArray(int size) {
            return new ModuleItemSequence[size];
        }
    };
}
