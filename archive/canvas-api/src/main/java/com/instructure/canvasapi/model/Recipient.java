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

import java.util.Date;
import java.util.HashMap;



public class Recipient extends CanvasComparable<Recipient>{

    public enum Type {group, metagroup, person}

    private String id;
    private int user_count;
    private int item_count;
    private String name;
    private String avatar_url;

    @SerializedName("common_courses")
    private HashMap<String, String[]> commonCourses;

    @SerializedName("common_groups")
    private HashMap<String, String[]> commonGroups;
    ///////////////////////////////////////////////////////////////////////////
    // Getters
    ///////////////////////////////////////////////////////////////////////////
    public String getStringId(){
        return id;
    }

    public long getIdAsLong(){
        try{
            if(id.startsWith("group_") || id.startsWith("course_")){
                int indexUnder = id.indexOf("_");
                return Long.parseLong(id.substring(indexUnder+1, id.length()));
            }
            return Long.parseLong(id);
        }catch(NumberFormatException ex){
            return 0;
        }
    }
    public HashMap<String, String[]> getCommonCourses() {
        return commonCourses;
    }

    public void setCommonCourses(HashMap<String, String[]> commonCourses) {
        this.commonCourses = commonCourses;
    }

    public HashMap<String, String[]> getCommonGroups() {
        return commonGroups;
    }

    public void setCommonGroups(HashMap<String, String[]> commonGroups) {
        this.commonGroups = commonGroups;
    }

    @Override
    public Date getComparisonDate() {
        return null;
    }

    @Override
    public String getComparisonString() {
        return id;
    }

    public int getUser_count() {
		return user_count;
	}
	public String getName() {
		return name;
	}
	public Type getRecipientType() {

        try{
            long tempId = Long.parseLong(id);
            return Type.person;
        }
        catch(Exception E){}

        if(user_count > 0){
           return Type.group;
        }

		return Type.metagroup;
	}
	public int getItemCount() {
		return item_count;
	}

    public String getAvatarURL() {
        return avatar_url;
    }
    public void setAvatarURL(String avatar) {
        this.avatar_url = avatar;
    }

	///////////////////////////////////////////////////////////////////////////
	// Parcelable
	///////////////////////////////////////////////////////////////////////////

    public Recipient(Parcel p)
    {
        readFromParcel(p);
    }

    public Recipient(String _id, String _name, int _userCount, int _itemCount, int _enum) {
        id = _id;
        name = _name;

        user_count = _userCount;
        item_count = _itemCount;

    }
	
	public static final Parcelable.Creator<Recipient> CREATOR
	        = new Parcelable.Creator<Recipient>() {

		public Recipient createFromParcel(Parcel in) {
			return new Recipient(in);
		}

		public Recipient[] newArray(int size) {
			return new Recipient[size];
		}
	};

	public static int recipientTypeToInt(Type t)
	{
		if(t == Type.group)
			return 0;
		else if (t == Type.metagroup)
			return 1;
		else if (t == Type.person)
			return 2;
		else
			return -1;
	}

	public static Type intToRecipientType(int i)
	{
		if(i == 0)
			return Type.group;
		else if (i == 1)
			return Type.metagroup;
		else if (i == 2)
			return Type.person;
		else
			return null;
	}

	public void readFromParcel(Parcel in){
		id = in.readString();
		user_count = in.readInt();
		item_count = in.readInt();
		name = in.readString();
        commonCourses = (HashMap<String, String[]>) in.readSerializable();
        commonGroups = (HashMap<String, String[]>) in.readSerializable();

	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(id);
		dest.writeInt(user_count);
		dest.writeInt(item_count);
		dest.writeString(name);
        dest.writeSerializable(commonCourses);
        dest.writeSerializable(commonGroups);
	}

    ///////////////////////////////////////////////////////////////////////////
    // Overrides
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }


    @Override
    public boolean equals(Object obj) {
        if (getClass() != obj.getClass()) {
            return false;
        }
        Recipient other = (Recipient) obj;

        return compareTo(other) == 0;
    }

    @Override
    public String toString(){
        return name;
    }

}
