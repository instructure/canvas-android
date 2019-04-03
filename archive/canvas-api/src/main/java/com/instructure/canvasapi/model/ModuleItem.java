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

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Date;


public class ModuleItem extends CanvasModel<ModuleItem> {

    /**
     * {
     // the unique identifier for the module item
     id: 768,

     // the position of this item in the module (1-based)
     position: 1,

     // the title of this item
     title: "Square Roots: Irrational numbers or boxy vegetables?",

     // 0-based indent level; module items may be indented to show a hierarchy
     indent: 0,

     // the type of object referred to
     // one of "File", "Page", "Discussion", "Assignment", "Quiz", "SubHeader",
     // "ExternalUrl", "ExternalTool"
     type: "Assignment",

     // link to the item in Canvas
     html_url: "https://canvas.example.edu/courses/222/modules/items/768",

     // (Optional) link to the Canvas API object, if applicable
     url: "https://canvas.example.edu/api/v1/courses/222/assignments/987",

     // Completion requirement for this module item
     completion_requirement: {
     // one of "must_view", "must_submit", "must_contribute", "min_score", "must_mark_done"
     type: "min_score",

     // minimum score required to complete (only present when type == 'min_score')
     min_score: 10,

     // whether the calling user has met this requirement
     // (Optional; present only if the caller is a student)
     completed: true
     }
     }
     */

    public static final String MUST_VIEW = "must_view";
    public static final String MUST_SUBMIT = "must_submit";
    public static final String MUST_CONTRIBUTE = "must_contribute";
    public static final String MIN_SCORE = "min_score";
    public static final String MUST_MARK_DONE = "must_mark_done";


    private long id;
    private long module_id;
    private int position;
    private String title;
    private int indent;
    private String type;
    private String html_url;
    private String url;
    private CompletionRequirement completion_requirement;
    private ModuleContentDetails content_details;

    @SerializedName("mastery_paths")
    private MasteryPath masteryPaths;

    //when we display the "Choose Assignment Group" when an assignment uses Mastery Paths we create a new row to display.
    //We still need the module item id to select the assignment group that we want, but if we use the same id as the root
    //module item both items wouldn't display (because they would have the same id at that point).
    private long masteryPathsItemId;

    public class CompletionRequirement implements Serializable {

        private static final long serialVersionUID = 1L;
        private String type;
        private double min_score;
        private boolean completed;

        public double getMin_score() {
            return min_score;
        }
        public void setMin_score(double min_score) {
            this.min_score = min_score;
        }
        public boolean isCompleted() {
            return completed;
        }
        public void setCompleted(boolean completed) {
            this.completed = completed;
        }
        public String getType() {
            return type;
        }
        public void setType(String type) {
            this.type = type;
        }
        }

    @Override
    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }

    public long getModuleId() {
        return module_id;
    }

    public int getPosition() {
        return position;
    }
    public void setPosition(int position) {
        this.position = position;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public int getIndent() {
        return indent;
    }
    public void setIndent(int indent) {
        this.indent = indent;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public String getHtml_url() {
        return html_url;
    }
    public void setHtml_url(String html_url) {
        this.html_url = html_url;
    }
    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }
    public CompletionRequirement getCompletionRequirement() {
        return completion_requirement;
    }
    public void setCompletionRequirement(CompletionRequirement completionRequirement) {
        this.completion_requirement = completionRequirement;
    }

    public ModuleContentDetails getModuleDetails() {
        return content_details;
    }

    public MasteryPath getMasteryPaths() {
        return masteryPaths;
    }

    public void setMasteryPaths(MasteryPath masteryPaths) {
        this.masteryPaths = masteryPaths;
    }

    public long getMasteryPathsItemId() {
        return masteryPathsItemId;
    }

    public void setMasteryPathsItemId(long masteryPathsItemId) {
        this.masteryPathsItemId = masteryPathsItemId;
    }

    public enum TYPE {Assignment, Discussion, File, Page, SubHeader, Quiz, ExternalUrl, ExternalTool, Locked, ChooseAssignmentGroup}

    public ModuleItem() {}

    ///////////////////////////////////////////////////////////////////////////
    // Required Overrides
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public Date getComparisonDate() {
        return null;
    }

    @Override
    public String getComparisonString() {
        return title;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeLong(this.module_id);
        dest.writeInt(this.position);
        dest.writeString(this.title);
        dest.writeInt(this.indent);
        dest.writeString(this.type);
        dest.writeString(this.html_url);
        dest.writeString(this.url);
        dest.writeSerializable(this.completion_requirement);
        dest.writeSerializable(this.masteryPaths);
    }

    private ModuleItem(Parcel in) {
        this.id = in.readLong();
        this.module_id = in.readLong();
        this.position = in.readInt();
        this.title = in.readString();
        this.indent = in.readInt();
        this.type = in.readString();
        this.html_url = in.readString();
        this.url = in.readString();
        this.completion_requirement = (CompletionRequirement) in.readSerializable();
        this.masteryPaths = (MasteryPath) in.readSerializable();
    }

    public static Creator<ModuleItem> CREATOR = new Creator<ModuleItem>() {
        public ModuleItem createFromParcel(Parcel source) {
            return new ModuleItem(source);
        }

        public ModuleItem[] newArray(int size) {
            return new ModuleItem[size];
        }
    };
}
