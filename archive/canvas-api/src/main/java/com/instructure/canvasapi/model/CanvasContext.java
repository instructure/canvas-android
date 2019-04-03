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

import java.util.Date;

public abstract class CanvasContext extends CanvasModel<CanvasContext> implements android.os.Parcelable {

    public static final String HOME_FEED = "feed";
    public static final String HOME_WIKI = "wiki";
    public static final String HOME_MODULES = "modules";
    public static final String HOME_ASSIGNMENTS = "assignments";
    public static final String HOME_SYLLABUS = "syllabus";

    public enum Type {
        GROUP, COURSE, USER, SECTION, UNKNOWN;

        public static boolean isGroup(CanvasContext canvasContext) {
            if (canvasContext == null) {
                return false;
            }
            return canvasContext.getType() == GROUP;
        }

        public static boolean isCourse(CanvasContext canvasContext) {
            if (canvasContext == null) {
                return false;
            }
            return canvasContext.getType() == COURSE;
        }

        public static boolean isUser(CanvasContext canvasContext) {
            if (canvasContext == null) {
                return false;
            }
            return canvasContext.getType() == USER;
        }

        public static boolean isUnknown(CanvasContext canvasContext) {
            if (canvasContext == null) {
                return false;
            }
            return canvasContext.getType() == UNKNOWN;
        }

        public static boolean isSection(CanvasContext canvasContext) {
            if (canvasContext == null) {
                return false;
            }
            return canvasContext.getType() == SECTION;
        }
    }


    public abstract String getName();

    public abstract Type getType();

    public abstract long getId();

    protected String default_view;

    protected CanvasContextPermission permissions;

    public void setPermissions(CanvasContextPermission permissions) {
        this.permissions = permissions;
    }

    public CanvasContextPermission getPermissions() {
        return permissions;
    }

    public boolean canCreateDiscussion() {
        return (permissions != null && permissions.canCreateDiscussionTopic());
    }

    ///////////////////////////////////////////////////////////////////////////
    // Overrides
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public Date getComparisonDate() {
        return null;
    }

    @Override
    public String getComparisonString() {
        return getName();
    }

    /**
     * Make sure they have the same type and the same ID.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        CanvasContext that = (CanvasContext) o;

        return !(getType() != that.getType() || getId() != that.getId());

    }

    ///////////////////////////////////////////////////////////////////////////
    // Helpers
    ///////////////////////////////////////////////////////////////////////////

    /**
     * For courses, returns the course code.
     * For everything else, returns the Name;
     */

    public String getSecondaryName() {
        String secondaryName = getName();
        if (getType() == CanvasContext.Type.COURSE) {
            secondaryName = ((Course) this).getCourseCode();
        }
        return secondaryName;
    }


    /**
     * Used for Cache Filenames in the API.
     */
    public String toAPIString() {
        String typeString;
        if (getType().equals(Type.GROUP)) {
            typeString = "groups";
        } else if (getType().equals(Type.COURSE)) {
            typeString = "courses";
        } else if (getType().equals(Type.SECTION)) {
            typeString = "sections";
        } else {
            typeString = "users";
        }

        String idString = Long.toString(getId());
        if (getType() == Type.USER && getId() == 0) {
            idString = "self";
        }

        return "/" + typeString + "/" + idString;
    }

    /**
     * @returns group_:id or course_:id
     */
    public String getContextId() {

        String prefix = "";
        if (getType() == Type.COURSE) {
            prefix = "course";
        } else if (getType() == Type.GROUP) {
            prefix = "group";
        } else if(getType() == Type.USER) {
            prefix = "user";
        }

        return prefix + "_" + getId();
    }

    public static String makeContextId(Type type, long id) {
        String prefix = "";
        if (type == Type.COURSE) {
            prefix = "course";
        } else if (type == Type.GROUP) {
            prefix = "group";
        } else if(type == Type.USER) {
            prefix = "user";
        } else {
            return null;
        }

        return prefix + "_" + id;
    }

    /**
     * Get home page label returns the fragment identifier.
     *
     * @return
     */
    public String getHomePageID() {
        if (default_view == null) {
            //notifications can't be hidden, so if for some reason we don't have the home page
            //send them to notifications instead
            return Tab.NOTIFICATIONS_ID;
        }
        if (default_view.equals(HOME_FEED)) {
            return Tab.NOTIFICATIONS_ID;
        }
        if (default_view.equals(HOME_SYLLABUS)) {
            return Tab.SYLLABUS_ID;
        }
        if (default_view.equals(HOME_WIKI)) {
            return Tab.PAGES_ID;
        }
        if (default_view.equals(HOME_ASSIGNMENTS)) {
            return Tab.ASSIGNMENTS_ID;
        }
        if (default_view.equals(HOME_MODULES)) {
            return Tab.MODULES_ID;
        }
        return Tab.NOTIFICATIONS_ID; //send them to notifications if we don't know what to do
    }

    public static CanvasContext getGenericContext(final Type type, final long id, final String name) {
        CanvasContext canvasContext;
        if(type == Type.USER){
          User  user = new User(id);
            user.setName(name);
            canvasContext = user;
        } else if (type == Type.COURSE){
            Course course = new Course();
            course.setId(id);
            course.setName(name);

            canvasContext = course;
        } else if (type == Type.GROUP){
            Group group = new Group();
            group.setId(id);
            group.setName(name);

            canvasContext = group;
        } else if (type == Type.SECTION){
            Section section = new Section();
            section.setId(id);
            section.setName(name);

            canvasContext = section;
        } else {
            return null;
        }

        return canvasContext;
    }

    public static CanvasContext emptyCourseContext() {
        return getGenericContext(Type.COURSE, 0, "");
    }

    public static CanvasContext emptyUserContext() {
        return getGenericContext(Type.USER, 0, "");
    }


}
