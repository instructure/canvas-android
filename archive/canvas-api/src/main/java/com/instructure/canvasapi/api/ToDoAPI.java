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

package com.instructure.canvasapi.api;

import android.content.Context;

import com.instructure.canvasapi.model.Assignment;
import com.instructure.canvasapi.model.CanvasContext;
import com.instructure.canvasapi.model.ToDo;
import com.instructure.canvasapi.utilities.APIHelpers;
import com.instructure.canvasapi.utilities.CanvasCallback;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import retrofit.Callback;
import retrofit.client.Response;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.Path;


public class ToDoAPI extends BuildInterfaceAPI {

    interface ToDosInterface {
        @GET("/users/self/todo")
        void getUserTodos(Callback<ToDo[]> callback);

        @GET("/{context_id}/todo")
        void getCourseTodos(@Path("context_id") long context_id, Callback<ToDo[]> callback);

        @DELETE("/{path}")
        void dismissTodo(@Path(value = "path", encode = false)String path, CanvasCallback<Response>responseCallback);

        /////////////////////////////////////////////////////////////////////////////
        // Synchronous
        /////////////////////////////////////////////////////////////////////////////

        @GET("/{context_id}/todo")
        ToDo[] getCourseTodos(@Path("context_id") long courseID);

        @GET("/users/self/todo")
        ToDo[] getUserTodos();

    }


    /////////////////////////////////////////////////////////////////////////
    // API Calls
    /////////////////////////////////////////////////////////////////////////

    public static void getUserTodos(CanvasCallback<ToDo[]> callback) {
        if (APIHelpers.paramIsNull(callback)) { return; }

        buildCacheInterface(ToDosInterface.class, callback, null).getUserTodos(callback);
        buildInterface(ToDosInterface.class, callback, null).getUserTodos(callback);
    }

    public static void getCourseTodos(CanvasContext canvasContext, CanvasCallback<ToDo[]> callback) {
        if (APIHelpers.paramIsNull(callback, canvasContext)) { return; }

        buildCacheInterface(ToDosInterface.class, callback, canvasContext).getCourseTodos(canvasContext.getId(), callback);
        buildInterface(ToDosInterface.class, callback, canvasContext).getCourseTodos(canvasContext.getId(), callback);
    }

    public static void getTodos(CanvasContext canvasContext, final CanvasCallback<ToDo[]> callback) {
        if (APIHelpers.paramIsNull(callback, canvasContext)) { return; }

        if (canvasContext.getType() == CanvasContext.Type.USER) {
            getUserTodos(callback);
        } else {
            getCourseTodos(canvasContext, callback);
        }
    }

    public static void dismissTodo(Context context, ToDo toDo, CanvasCallback<Response> callback) {
        if (APIHelpers.paramIsNull(callback, toDo)) return;

        String path = APIHelpers.removeDomainFromUrl(toDo.getIgnore());
        buildInterface(ToDosInterface.class, callback, null).dismissTodo(path, callback);
    }


    /////////////////////////////////////////////////////////////////////////////
    // Helper Methods
    /////////////////////////////////////////////////////////////////////////////

    public static ArrayList<ToDo> mergeToDoUpcoming(ArrayList<ToDo> todos, ArrayList<ToDo> upcomingEvents) {
        if (todos == null && upcomingEvents == null) {
            return null;
        }

        if (todos == null) {
            todos = new ArrayList<ToDo>();
        }

        if (upcomingEvents == null) {
            upcomingEvents = new ArrayList<ToDo>();
        }

        //Add all Assignment ids from TODO
        HashMap<Long, Boolean> assignmentIds = new HashMap<Long, Boolean>();
        for (ToDo toDo : todos) {
            if (toDo.getAssignment() != null) {
                assignmentIds.put(toDo.getAssignment().getId(), true);
            }
        }
        //If the hashmap contains any assignment ids from Upcoming, it's a duplicate
        Iterator<ToDo> iterator = upcomingEvents.iterator();
        while (iterator.hasNext()) {
            ToDo current = iterator.next();
            Assignment assignment = current.getScheduleItem().getAssignment();
            if (assignment != null && assignmentIds.containsKey(assignment.getId())) {
                //We already have it in ToDo so remove the item.
                iterator.remove();
            }
        }

        int todoIndex = 0;
        int upcomingIndex = 0;
        ArrayList<ToDo> merged = new ArrayList<ToDo>();

        while (todoIndex < todos.size() || upcomingIndex < upcomingEvents.size()) {
            //We only have upcoming left.
            if (todoIndex >= todos.size()) {
                List<ToDo> subset = upcomingEvents.subList(upcomingIndex, upcomingEvents.size());
                for (ToDo upcomming : subset) {
                    merged.add(upcomming);
                }
                return merged;
            }

            //We only have todo left.
            if (upcomingIndex >= upcomingEvents.size()) {
                List<ToDo> subset = todos.subList(todoIndex, todos.size());
                for (ToDo td : subset) {
                    merged.add(td);
                }
                return merged;
            }

            //We need to determine which one comes sooner.
            Date toDoDate;
            if (todos.get(todoIndex).getAssignment() == null) {
                toDoDate = null;
            } else {
                toDoDate = todos.get(todoIndex).getAssignment().getDueDate();
            }
            Date upcomingDate = upcomingEvents.get(upcomingIndex).getScheduleItem().getStartDate();

            //handle null cases first
            if (toDoDate == null) {
                merged.add(upcomingEvents.get(upcomingIndex));
                upcomingIndex++;
            } else if (upcomingDate == null) {
                merged.add(todos.get(todoIndex));
                todoIndex++;
            } else if (toDoDate.before(upcomingDate)) {
                merged.add(todos.get(todoIndex));
                todoIndex++;
            } else {
                merged.add(upcomingEvents.get(upcomingIndex));
                upcomingIndex++;
            }
        }
        //Should never get here.
        return merged;
    }

    /////////////////////////////////////////////////////////////////////////////
    // Synchronous
    //
    // If Retrofit is unable to parse (no network for example) Synchronous calls
    // will throw a nullPointer exception. All synchronous calls need to be in a
    // try catch block.
    /////////////////////////////////////////////////////////////////////////////

    public static ToDo[] getTodosSynchronous(Context context, CanvasContext canvasContext) {

        if(context == null || canvasContext == null) {
            return null;
        }

        if (canvasContext.getType() == CanvasContext.Type.USER) {
            return getUserTodosSynchronous(context);
        } else {
            return getCourseTodosSynchronous(context, canvasContext);
        }
    }

    public static ToDo[] getUserTodosSynchronous(Context context) {
        if(context == null){
            return null;
        }

        //If not able to parse (no network for example), this will crash. Handle that case.
        try  {
            return buildInterface(ToDosInterface.class, context).getUserTodos();
        } catch (Exception E){
            return null;
        }

    }

    public static ToDo[] getCourseTodosSynchronous(Context context, CanvasContext canvasContext) {

        if(context == null || canvasContext == null) {
            return null;
        }

        //If not able to parse (no network for example), this will crash. Handle that case.
        try {
            return buildInterface(ToDosInterface.class, context, canvasContext).getCourseTodos(canvasContext.getId());
        } catch (Exception E){
            return null;
        }

    }

}
