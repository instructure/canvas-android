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

import android.content.Context;
import android.os.Parcel;

import com.google.gson.annotations.SerializedName;
import com.instructure.canvasapi.R;
import com.instructure.canvasapi.utilities.APIHelpers;
import com.instructure.canvasapi.utilities.DateHelpers;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;



//could be a calendar event or an assignment
public class ScheduleItem extends CanvasModel<ScheduleItem> {

    public enum Type { TYPE_ASSIGNMENT, TYPE_CALENDAR, TYPE_SYLLABUS }

    // from api
	private String id;
	private String title;
    private String description;
	private String start_at;
    private String end_at;
    private boolean all_day;
    private String all_day_date;
    private String location_address;
    private String location_name;
    private String html_url;
    private String context_code;
    private String effective_context_code;
    private boolean hidden;
    @SerializedName("assignment_overrides")
    private List<AssignmentOverride> assignmentOverrides;

    // helper variables
    private CanvasContext.Type contextType;
    private long userId = -1;
    private String userName;
    private long courseId = -1;
    private long groupId = -1;
	private Type itemType = Type.TYPE_CALENDAR;

	private List<Assignment.SUBMISSION_TYPE> submissionTypes = new ArrayList<Assignment.SUBMISSION_TYPE>();
	private double pointsPossible;
	private long quizId = 0;
	private DiscussionTopicHeader discussionTopicHeader;
	private String lockedModuleName;
    private Assignment assignment;

    Date startDate;

 
    ///////////////////////////////////////////////////////////////////////////
    // Getters and Setters
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public long getId() {
        //id can either be a regular long, or it could be prefixed by "assignment_".
        //for more info check out the upcoming_events api documentation
        try {
            return Long.parseLong(id);
        }
        catch(NumberFormatException e) {
            if(assignmentOverrides != null && !assignmentOverrides.isEmpty()) {
                long id = assignmentOverrides.get(0).id;
                setId(id);
                return id;
            } else {
                //it's a string with assignment_ as a prefix...hopefully
                try {
                    String stringId = id;
                    String tempId = stringId.replace("assignment_", "");
                    long assignmentId = Long.parseLong(tempId);
                    setId(assignmentId);
                    return assignmentId;
                } catch (Exception e1) {
                    setId(-1L);
                    return -1L;
                }
            }
        }
        catch(Exception e) {
            setId(-1L);
            return -1L;
        }
    }
    public void setId(long id) {
        this.id = Long.toString(id);
    }
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public boolean isAllDay() {
        return all_day;
    }
    public void setAllDay(boolean allDay) {
        this.all_day = allDay;
    }
    public Date getAllDayDate() {
        if(all_day_date == null) {
            return null;
        }
        return APIHelpers.stringToDate(all_day_date);
    }
    public void setAllDayDate(String allDayDate) {
        this.all_day_date = allDayDate;
    }
    public String getLocationAddress() {
        return location_address;
    }
    public void setLocationAddress(String locationAddress) {
        this.location_address = locationAddress;
    }
    public String getLocationName() {
        return location_name;
    }
    public void setLocationName(String locationName) {
        this.location_name = locationName;
    }
    public CanvasContext.Type getContextType() {
        if(context_code == null) {
            contextType = CanvasContext.Type.USER;
        } else if (contextType == null) {
            parseContextCode();
        }

        return contextType;
    }
    public void setContextType(CanvasContext.Type contextType) {
        this.contextType = contextType;
    }
    public long getUserId() {
        if (userId < 0) {
            parseContextCode();
        }
        return userId;
    }
    public void setUserId(long userId) {
        this.userId = userId;
    }
    public String getUserName() {
        return userName;
    }
    public void setUserName(String userName) {
        this.userName = userName;
    }
    public long getCourseId() {
        if (courseId < 0) {
            parseContextCode();
        }
        return courseId;
    }
    public void setCourseId(long courseId) {
        this.courseId = courseId;
    }
    public long getGroupId() {
        if (groupId < 0) {
            parseContextCode();
        }
        return groupId;
    }
    public void setGroupId(long groupId) {
        this.groupId = groupId;
    }
    public Type getType() {
		return itemType;
	}
	public void setType(Type type) {
		this.itemType = type;
	}
    public Date getStartDate() {
        if(start_at == null) {
            return null;
        }

        if (startDate == null) {
            startDate = APIHelpers.stringToDate(start_at);
        }
        return startDate;
    }
    public void setStartDate(Date startDate) {
        this.startDate = startDate;
        start_at = APIHelpers.dateToString(startDate);
    }
    public Date getEndDate() {
        if(end_at == null) {
            return null;
        }
        return APIHelpers.stringToDate(end_at);
    }
    public void setEndDate(String endDate) {
        this.end_at = endDate;
    }
	public List<Assignment.SUBMISSION_TYPE> getSubmissionTypes() {
		return submissionTypes;
	}
	public void setSubmissionTypes(List<Assignment.SUBMISSION_TYPE> submissionTypes) {
		this.submissionTypes = submissionTypes;
	}
	public double getPointsPossible() {
		return pointsPossible;
	}
	public void setPointsPossible(double pointsPossible) {
		this.pointsPossible = pointsPossible;
	}
	public void setDiscussionTopicHeader(DiscussionTopicHeader header) {
		discussionTopicHeader = header;
	}
	public DiscussionTopicHeader getDiscussionTopicHeader() {
		return discussionTopicHeader;
	}
	public String getHtmlUrl() {
		return html_url;
	}
	public void setHtmlUrl(String htmlUrl) {
		this.html_url = htmlUrl;
	}
	public long getQuizId() {
		return quizId;
	}
	public void setQuizId(long id) {
		quizId = id;
	}
	public String getLockedModuleName() {
        return lockedModuleName;
    }
    public void setLockedModuleName(String lockedModuleName) {
        this.lockedModuleName = lockedModuleName;
    }
    public Assignment getAssignment() {
        return assignment;
    }
    public void setAssignment(Assignment assignment) {
        this.assignment = assignment;
    }

    public boolean isHidden() {
        return hidden;
    }

    public List<AssignmentOverride> getAssignmentOverrides() {
        if(assignmentOverrides == null) {
            assignmentOverrides = new ArrayList<>();
        }
        return assignmentOverrides;
    }

    public boolean hasAssignmentOverrides() {
        return assignmentOverrides != null && !assignmentOverrides.isEmpty();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Required Overrides
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public Date getComparisonDate() {
        return getStartDate();
    }

    @Override
    public String getComparisonString() {
        return getTitle();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    public ScheduleItem() {}

    public ScheduleItem(CanvasContext.Type type) {
        setContextType(type);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Helpers
    ///////////////////////////////////////////////////////////////////////////

    public long getContextId() {
        switch (getContextType()) {
            case COURSE:
                return getCourseId();
            case GROUP:
                return getGroupId();
            case USER:
                return getUserId();
            default:
                return -1;
        }
    }

    private void parseContextCode() {
        if (effective_context_code != null) {
            parseContextCode(effective_context_code);
        } else {
            parseContextCode(context_code);
        }
    }

    private void parseContextCode(String contextCode) {
        if (contextCode.startsWith("user_")) {
            setContextType(CanvasContext.Type.USER);
            String userId = contextCode.replace("user_", "");
            setUserId(Long.parseLong(userId));
        } else if (contextCode.startsWith("course_")) {
            setContextType(CanvasContext.Type.COURSE);
            String courseId = contextCode.replace("course_", "");
            setCourseId(Long.parseLong(courseId));
        } else if (contextCode.startsWith("group_")) {
            setContextType(CanvasContext.Type.GROUP);
            String groupId = contextCode.replace("group_", "");
            setGroupId(Long.parseLong(groupId));
        }
    }

    public String getStartString(Context context) {
        if (isAllDay()) {
            return context.getString(R.string.allDayEvent);
        }
        if (getStartDate() != null) {
            return DateHelpers.createPrefixedDateString(context, R.string.Starts, getStartDate());
        }
        return "";
    }

    public String getStartDateString(Context context) {
        if (isAllDay() && getAllDayDate() != null) {
            return DateHelpers.getFormattedDate(context, getAllDayDate());
        }
        if (getStartDate() != null) {
            return DateHelpers.getFormattedDate(context, getStartDate());
        }
        return "";
    }

    public String getStartToEndString(Context context) {
        if (isAllDay()) {
            return context.getString(R.string.allDayEvent);
        }
        if (getStartDate() != null) {
            if (getEndDate() != null && !getStartDate().equals(getEndDate())) {
                return DateHelpers.getFormattedTime(context, getStartDate()) + " " + context.getResources().getString(R.string.to) + " " + DateHelpers.getFormattedTime(context, getEndDate());
            }
            return DateHelpers.getFormattedTime(context, getStartDate());
        }
        return "";
    }

    public String getShortStartString(Context context) {
        if (isAllDay() && getAllDayDate() != null) {
            return DateHelpers.getFormattedDate(context, getAllDayDate());
        }
        if (getStartDate() != null) {
            return DateHelpers.getFormattedDate(context, getStartDate());
        }
        return "";
    }

    public String getEndString(Context context) {
        if (isAllDay()) {
            if (getAllDayDate() != null) {
                return DateHelpers.getFormattedDate(context, getAllDayDate());
            } else if (getStartDate() != null) {
                return DateHelpers.getFormattedDate(context, getStartDate());
            }
        }
        if (getEndDate() != null) {
            return DateHelpers.createPrefixedDateTimeString(context, R.string.Ends, getEndDate());
        }
        return "";
    }

    public int getStartDayOfMonth() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(getStartDate());

        return calendar.get(Calendar.DAY_OF_MONTH);
    }

    public static ScheduleItem createSyllabus(String title, String description) {
        ScheduleItem syllabus = new ScheduleItem();
        syllabus.setType(Type.TYPE_SYLLABUS);
        syllabus.setTitle(title);
        syllabus.setDescription(description);
        syllabus.setId(Long.MIN_VALUE);

        return syllabus;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Overrides
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public int compareTo(ScheduleItem scheduleItem) {
        if (getStartDate() == null && scheduleItem.getStartDate() == null) {
            return getTitle().compareTo(scheduleItem.getTitle());
        } else if (getStartDate() == null) {
            return 1;
        } else if (scheduleItem.getStartDate() == null) {
            return -1;
        } else if (getStartDate().equals(scheduleItem.getStartDate())) {
            return getTitle().compareTo(scheduleItem.getTitle());
        }
        return getStartDate().compareTo(scheduleItem.getStartDate());
    }

    ///////////////////////////////////////////////////////////////////////////
    // Parcelable Overrides
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.title);
        dest.writeString(this.description);
        dest.writeString(this.start_at);
        dest.writeString(this.end_at);
        dest.writeByte(all_day ? (byte) 1 : (byte) 0);
        dest.writeString(this.all_day_date);
        dest.writeString(this.location_address);
        dest.writeString(this.location_name);
        dest.writeString(this.html_url);
        dest.writeString(this.context_code);
        dest.writeString(this.effective_context_code);
        dest.writeInt(this.contextType == null ? -1 : this.contextType.ordinal());
        dest.writeLong(this.userId);
        dest.writeString(this.userName);
        dest.writeLong(this.courseId);
        dest.writeLong(this.groupId);
        dest.writeInt(this.itemType == null ? -1 : this.itemType.ordinal());

        //Hack to make ENUMS list parcelable
        //http://stackoverflow.com/questions/15016259/how-to-make-a-listenum-parcelable
        List<String> submissionTypeStrings = new ArrayList<String>();
        for(Assignment.SUBMISSION_TYPE submissionType: this.submissionTypes){
            submissionTypeStrings.add(submissionType.name());
        }
        dest.writeList(submissionTypeStrings);

        dest.writeDouble(this.pointsPossible);
        dest.writeLong(this.quizId);
        dest.writeParcelable(this.discussionTopicHeader, 0);
        dest.writeString(this.lockedModuleName);
        dest.writeParcelable(this.assignment, flags);
        dest.writeLong(startDate != null ? startDate.getTime() : -1);
        dest.writeByte(hidden ? (byte) 1 : (byte) 0);
        dest.writeTypedList(this.assignmentOverrides);

    }

    private ScheduleItem(Parcel in) {
        this.id = in.readString();
        this.title = in.readString();
        this.description = in.readString();
        this.start_at = in.readString();
        this.end_at = in.readString();
        this.all_day = in.readByte() != 0;
        this.all_day_date = in.readString();
        this.location_address = in.readString();
        this.location_name = in.readString();
        this.html_url = in.readString();
        this.context_code = in.readString();
        this.effective_context_code = in.readString();
        int tmpContextType = in.readInt();
        this.contextType = tmpContextType == -1 ? null : CanvasContext.Type.values()[tmpContextType];
        this.userId = in.readLong();
        this.userName = in.readString();
        this.courseId = in.readLong();
        this.groupId = in.readLong();
        int tmpType = in.readInt();
        this.itemType = tmpType == -1 ? null : Type.values()[tmpType];

        //Hack to make ENUMS list parcelable
        //http://stackoverflow.com/questions/15016259/how-to-make-a-listenum-parcelable
        List<String> submissionTypeStrings = new ArrayList<String>();
        in.readList(submissionTypeStrings, null);
        for(String submissionTypeString: submissionTypeStrings){
            this.submissionTypes.add(Assignment.SUBMISSION_TYPE.valueOf(submissionTypeString));
        }

        this.pointsPossible = in.readDouble();
        this.quizId = in.readLong();
        this.discussionTopicHeader = in.readParcelable(DiscussionTopicHeader.class.getClassLoader());
        this.lockedModuleName = in.readString();
        this.assignment = in.readParcelable(Assignment.class.getClassLoader());
        long tmpStartDate = in.readLong();
        this.startDate = tmpStartDate == -1 ? null : new Date(tmpStartDate);
        this.hidden = in.readByte() != 0;
        this.assignmentOverrides = in.createTypedArrayList(AssignmentOverride.CREATOR);

    }

    public static Creator<ScheduleItem> CREATOR = new Creator<ScheduleItem>() {
        public ScheduleItem createFromParcel(Parcel source) {
            return new ScheduleItem(source);
        }

        public ScheduleItem[] newArray(int size) {
            return new ScheduleItem[size];
        }
    };
}
