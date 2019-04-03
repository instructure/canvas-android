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

import java.util.Date;


public class Group extends CanvasContext{

    private long id;

    private String name;
    private String description;
    private String avatar_url;

    private boolean is_public;
    private boolean followed_by_user;

    private int members_count;
    private User[] users;

    // * If "parent_context_auto_join", anyone can join and will be
    //   automatically accepted.
    // * If "parent_context_request", anyone  can request to join, which
    //   must be approved by a group moderator.
    // * If "invitation_only", only those how have received an
    //   invitation my join the group, by accepting that invitation.
    private String join_level;

    //TODO:
    private String context_type;

    //At most, ONE of these will be set.
    private long course_id;
    private long account_id;

    // Certain types of groups have special role designations. Currently,
    // these include: "communities", "student_organized", and "imported".
    // Regular course/account groups have a role of null.
    private String role;

    private long group_category_id;

    private long storage_quota_mb;
    private boolean is_favorite;

    public enum JoinLevel {Automatic, Request, Invitation, Unknown}
    public enum GroupRole {Community, Student, Imported, Course}
    public enum GroupContext {Course,  Account, Other}


    ///////////////////////////////////////////////////////////////////////////
    // Getters and Setters
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    @Override
    public Date getComparisonDate() {
        return null;
    }

    @Override
    public String getComparisonString() {
        return name;
    }

    @Override
    public Type getType() {return Type.GROUP;}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public String getAvatarUrl() {
        return avatar_url;
    }

    public boolean isPublic() {
        return is_public;
    }

    public boolean isFollowedByUser() {
        return followed_by_user;
    }

    public int getMembersCount() {
        return members_count;
    }

    public User[] getUsers() {
        return users;
    }

    public void setUsers(User[] users) {
        this.users = users;
    }

    public JoinLevel getJoinLevel() {

        // * If "parent_context_auto_join", anyone can join and will be
        //   automatically accepted.
        // * If "parent_context_request", anyone  can request to join, which
        //   must be approved by a group moderator.
        // * If "invitation_only", only those how have received an
        //   invitation my join the group, by accepting that invitation.

        if("parent_context_auto_join".equalsIgnoreCase(join_level)){
            return JoinLevel.Automatic;
        } else if ("parent_context_request".equalsIgnoreCase(join_level)){
            return JoinLevel.Request;
        } else if ("invitation_only".equalsIgnoreCase(join_level)){
            return JoinLevel.Invitation;
        }

        return JoinLevel.Unknown;
    }

    public GroupContext getContextType() {

        if("course".equalsIgnoreCase(context_type)){
            return GroupContext.Course;
        } else if ("account".equalsIgnoreCase(context_type)){
            return GroupContext.Account;
        }
        return GroupContext.Other;
    }

    public long getCourseId() {
        return course_id;
    }

    public long getAccountId() {
        return account_id;
    }

    public GroupRole getRole() {
        // Certain types of groups have special role designations. Currently,
        // these include: "communities", "student_organized", and "imported".
        // Regular course/account groups have a role of null.

        if("communities".equalsIgnoreCase(role)){
            return GroupRole.Community;
        } else if ("student_organized".equals(role)){
            return GroupRole.Student;
        } else if ("imported".equals(role)){
            return GroupRole.Imported;
        }

        return GroupRole.Course;
    }

    public long getGroupCategoryId() {
        return group_category_id;
    }

    public long getStorageQuotaMB() {
        return storage_quota_mb;
    }

    public boolean isFavorite() {
        return is_favorite;
    }

    public void setFavorite(boolean isFavorite) {
        this.is_favorite = isFavorite;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeString(this.name);
        dest.writeString(this.description);
        dest.writeString(this.avatar_url);
        dest.writeByte(is_public ? (byte) 1 : (byte) 0);
        dest.writeByte(followed_by_user ? (byte) 1 : (byte) 0);
        dest.writeInt(this.members_count);
        dest.writeParcelableArray(this.users, flags);
        dest.writeString(this.join_level);
        dest.writeString(this.context_type);
        dest.writeLong(this.course_id);
        dest.writeLong(this.account_id);
        dest.writeString(this.role);
        dest.writeLong(this.group_category_id);
        dest.writeLong(this.storage_quota_mb);
        dest.writeString(this.default_view);
        dest.writeParcelable(this.permissions, flags);
    }

    public Group() {
    }

    private Group(Parcel in) {
        this.id = in.readLong();
        this.name = in.readString();
        this.description = in.readString();
        this.avatar_url = in.readString();
        this.is_public = in.readByte() != 0;
        this.followed_by_user = in.readByte() != 0;
        this.members_count = in.readInt();
        this.users = (User[]) in.readParcelableArray(User.class.getClassLoader());
        this.join_level = in.readString();
        this.context_type = in.readString();
        this.course_id = in.readLong();
        this.account_id = in.readLong();
        this.role = in.readString();
        this.group_category_id = in.readLong();
        this.storage_quota_mb = in.readLong();
        this.default_view = in.readString();
        this.permissions = in.readParcelable(CanvasContextPermission.class.getClassLoader());
    }

    public static Creator<Group> CREATOR = new Creator<Group>() {
        public Group createFromParcel(Parcel source) {
            return new Group(source);
        }

        public Group[] newArray(int size) {
            return new Group[size];
        }
    };
}