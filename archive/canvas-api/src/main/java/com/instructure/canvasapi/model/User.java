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

import com.google.gson.annotations.SerializedName;
import com.instructure.canvasapi.utilities.APIHelpers;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



public class User extends CanvasContext{

    private long id;
    private String name;
    private String short_name;
    private String login_id;
    private String avatar_url;
    private String primary_email;
    private String sortable_name;
    private String bio;
    @SerializedName("last_login")
    private String lastLogin;

    private List<Enrollment> enrollments = new ArrayList<Enrollment>();

    //Helper variable for the "specified" enrollment.
    private int enrollmentIndex;

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
    // Getters and Setters
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }

    @Override
    public Type getType() {
        return Type.USER;
    }

    public void setName(String name) {
        this.name = name;
    }
    public String getShortName() {
        return short_name;
    }
    public void setShortName(String shortName) {
        this.short_name = shortName;
    }
    public String getLoginId() {
        return login_id;
    }
    public void setLoginId(String loginId) {
        this.login_id = loginId;
    }
    public String getAvatarURL() {
        return avatar_url;
    }
    public void setAvatarURL(String avatar) {
        this.avatar_url = avatar;
    }
    public String getEmail() {
        return primary_email;
    }
    public void setEmail(String email) {
        this.primary_email = email;
    }
    public List<Enrollment> getEnrollments() {
        return enrollments;
    }
    public void setEnrollments(ArrayList<Enrollment> enrollments) {
        this.enrollments = enrollments;
    }
    public int getEnrollmentIndex(){
        return enrollmentIndex;
    }
    public void setEnrollmentIndex(int index){
        enrollmentIndex = index;
    }
    public String getSortableName(){
        return sortable_name;
    }
    public void setSortableName(String sortable_name){
        this.sortable_name = sortable_name;
    }

    public String getBio() { return bio; }
    public void setBio(String bio) {
        this.bio = bio;
    }

    // User Permissions - defaults to false, returned with UserAPI.getSelfWithPermissions()
    public boolean canUpdateAvatar(){
        return getPermissions() != null && getPermissions().canUpdateAvatar();
    }
    public boolean canUpdateName(){
        return getPermissions() != null && getPermissions().canUpdateName();
    }

    // Matches recipents common_courses or common_groups format
    public HashMap<String, String[]> getEnrollmentsHash() {
        HashMap<String, List<String>> enrollments = new HashMap<>();
        for (Enrollment enrollment: getEnrollments()) {
            String key = enrollment.getCourseId() + "";
            if (enrollments.containsKey(key)) {
                enrollments.get(key).add(enrollment.getRole());
            } else {
                List<String> newList = new ArrayList<>();
                newList.add(enrollment.getRole());
                enrollments.put(key, newList);
            }
        }

        HashMap<String, String[]> stringArrayEnrollments = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : enrollments.entrySet()) {
            stringArrayEnrollments.put(entry.getKey(), entry.getValue().toArray(new String[entry.getValue().size()]));
        }
        return stringArrayEnrollments;
    }

    public String getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(String lastLogin) {
        this.lastLogin = lastLogin;
    }

    public void setLastLogin(Date date) {
        lastLogin = APIHelpers.dateToString(date);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    public User() {}

    public User(long id) {
        this.id = id;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Overrides
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (id ^ (id >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }

        User other = (User) obj;
        return id == other.id;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeString(this.name);
        dest.writeString(this.short_name);
        dest.writeString(this.login_id);
        dest.writeString(this.avatar_url);
        dest.writeString(this.primary_email);
        dest.writeList(this.enrollments);
        dest.writeInt(this.enrollmentIndex);
        dest.writeString(this.sortable_name);
        dest.writeString(this.bio);
        dest.writeParcelable(this.permissions, flags);
    }

    private User(Parcel in) {
        this.id = in.readLong();
        this.name = in.readString();
        this.short_name = in.readString();
        this.login_id = in.readString();
        this.avatar_url = in.readString();
        this.primary_email = in.readString();
        in.readList(this.enrollments, Enrollment.class.getClassLoader());
        this.enrollmentIndex = in.readInt();
        this.sortable_name = in.readString();
        this.bio = in.readString();
        this.permissions = in.readParcelable(CanvasContextPermission.class.getClassLoader());
    }

    public static Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {
        public User createFromParcel(Parcel source) {
            return new User(source);
        }

        public User[] newArray(int size) {
            return new User[size];
        }
    };

    @Override
    public int compareTo(CanvasContext canvasContext) {
        return 0;
    }
}
