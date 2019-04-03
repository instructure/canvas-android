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
import android.util.Log;

import com.instructure.canvasapi.model.Attachment;
import com.instructure.canvasapi.model.CanvasColor;
import com.instructure.canvasapi.model.CanvasContext;
import com.instructure.canvasapi.model.Domain;
import com.instructure.canvasapi.model.Enrollment;
import com.instructure.canvasapi.model.FileUploadParams;
import com.instructure.canvasapi.model.Parent;
import com.instructure.canvasapi.model.ParentResponse;
import com.instructure.canvasapi.model.ParentWrapper;
import com.instructure.canvasapi.model.ResetParent;
import com.instructure.canvasapi.model.Student;
import com.instructure.canvasapi.model.User;
import com.instructure.canvasapi.utilities.APIHelpers;
import com.instructure.canvasapi.utilities.CanvasCallback;
import com.instructure.canvasapi.utilities.ExhaustiveBridgeCallback;
import com.instructure.canvasapi.utilities.Masquerading;
import com.instructure.canvasapi.utilities.UserCallback;

import org.json.JSONObject;

import java.io.File;
import java.util.LinkedHashMap;

import retrofit.Callback;
import retrofit.client.Response;
import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Part;
import retrofit.http.PartMap;
import retrofit.http.Path;
import retrofit.http.Query;
import retrofit.mime.TypedFile;


public class UserAPI extends BuildInterfaceAPI {

    public enum ENROLLMENT_TYPE {STUDENT, TEACHER, TA, OBSERVER, DESIGNER}


    interface UsersInterface {
        @GET("/users/self/profile")
        void getSelf(Callback<User> callback);

        // TODO: We probably need to create a helper that does each of these individually
        @GET("/users/self/enrollments?state[]=active&state[]=invited&state[]=completed")
        void getSelfEnrollments(Callback<Enrollment[]> callback);

        @GET("/users/self")
        void getSelfWithPermission(CanvasCallback<User> callback);

        @PUT("/users/self")
        void updateShortName(@Query("user[short_name]") String shortName, @Body String body, Callback<User> callback);

        @GET("/users/{userid}/profile")
        void getUserById(@Path("userid")long userId, Callback<User> userCallback);

        @GET("/{context_id}/users/{userId}?include[]=avatar_url&include[]=user_id&include[]=email&include[]=bio")
        void getUserById(@Path("context_id") long context_id, @Path("userId")long userId, Callback<User> userCallback);

        @GET("/{context_id}/users?include[]=enrollments&include[]=avatar_url&include[]=user_id&include[]=email&include[]=bio")
        void getFirstPagePeopleList(@Path("context_id") long context_id, Callback<User[]> callback);

        @GET("/{context_id}/users?include[]=enrollments&include[]=avatar_url&include[]=user_id&include[]=email")
        void getFirstPagePeopleListWithEnrollmentType(@Path("context_id") long context_id, @Query("enrollment_type") String enrollmentType, Callback<User[]> callback);

        @GET("/{next}")
        void getNextPagePeopleList(@Path(value = "next", encode = false) String nextURL, Callback<User[]> callback);

        @POST("/users/self/file")
        void uploadUserFileURL( @Query("url") String fileURL, @Query("name") String fileName, @Query("size") long size, @Query("content_type") String content_type, @Query("parent_folder_path") String parentFolderPath, @Body String body, Callback<String> callback);

        @POST("/users/self/files")
        FileUploadParams getFileUploadParams( @Query("size") long size, @Query("name") String fileName, @Query("content_type") String content_type, @Query("parent_folder_id") Long parentFolderId, @Body String body);

        @POST("/users/self/files")
        FileUploadParams getFileUploadParams( @Query("size") long size, @Query("name") String fileName, @Query("content_type") String content_type, @Query("parent_folder_path") String parentFolderPath, @Body String body);

        @Multipart
        @POST("/")
        Attachment uploadUserFile(@PartMap LinkedHashMap<String, String> params, @Part("file") TypedFile file);

        //Colors
        @GET("/users/self/colors")
        void getColors(CanvasCallback<CanvasColor> callback);

        @PUT("/users/self/colors/{context_id}")
        void setColor(@Path("context_id") String context_id, @Query(value = "hexcode", encodeValue = false) String color, @Body String body, CanvasCallback<CanvasColor> callback);

        @POST("/accounts/{account_id}/self_registration")
        void createSelfRegistrationUser(@Path("account_id") long account_id, @Query("user[name]") String userName, @Query("pseudonym[unique_id]") String emailAddress, @Query("user[terms_of_use]") int acceptsTerms, @Body String body, Callback<User> callback);

        @POST("/users/self/observees")
        void addObserveeWithToken(@Query("access_token") String token, @Body String body, CanvasCallback<User> callback);

        @GET("/users/self/observees?include[]=avatar_url")
        void getObservees(CanvasCallback<User[]> callback);

        @DELETE("/users/self/observees/{observee_id}")
        void removeObservee(@Path("observee_id") long observee_id, Callback<User> callback);

        @DELETE("/student/{observer_id}/{student_id}")
        void removeStudent(@Path("observer_id") String observer_id, @Path("student_id") String student_id, Callback<Response> callback);

        @PUT("/newparent")
        void addParent(@Body ParentWrapper body, Callback<ParentResponse> callback);

        @GET("/account/{observer_username}")
        void getParentUserDomain(@Path("observer_username") String email, Callback<Domain> callback);

        @POST("/authenticate")
        void authenticateParent(@Body Parent body, Callback<ParentResponse> callback);

        @GET("/students/{observer_id}")
        void getObserveesForParent(@Path("observer_id") String observerId, CanvasCallback<Student[]> callback);

        @GET("/add_student/{observer_id}")
        void addStudentToParent(@Path("observer_id") String observerId, @Query(value = "student_domain", encodeValue = false) String studentDomain, CanvasCallback<Response> callback);

        @POST("/send_password_reset/{userName}")
        void sendPasswordResetForParent(@Path(value = "userName", encode = false) String userName, @Body JSONObject body, CanvasCallback<Response> callback);

        @POST("/reset_password")
        void resetParentPassword(@Body Parent parent, CanvasCallback<ResetParent> callback);
    }

    /////////////////////////////////////////////////////////////////////////
    // API Calls
    /////////////////////////////////////////////////////////////////////////

    public static void getSelf(UserCallback callback) {
        if (APIHelpers.paramIsNull(callback)) { return; }

        //Read cache
        callback.cache(APIHelpers.getCacheUser(callback.getContext()), null, null);

        //Don't allow this API call to be made while masquerading.
        //It causes the current user to be overridden with the masqueraded one.
        if (Masquerading.isMasquerading(callback.getContext())) {
            Log.w(APIHelpers.LOG_TAG,"No API call for /users/self/profile can be made while masquerading.");
            return;
        }

        buildInterface(UsersInterface.class, callback, null).getSelf(callback);
    }

    public static void getSelfWithPermissions(CanvasCallback<User> callback) {
        if (APIHelpers.paramIsNull(callback)) { return; }

        //Don't allow this API call to be made while masquerading.
        //It causes the current user to be overriden with the masqueraded one.
        if (Masquerading.isMasquerading(callback.getContext())) {
            Log.w(APIHelpers.LOG_TAG,"No API call for /users/self can be made while masquerading.");
            return;
        }

        buildCacheInterface(UsersInterface.class, callback, null).getSelfWithPermission(callback);

        buildInterface(UsersInterface.class, callback, null).getSelfWithPermission(callback);
    }

    public static void getSelfEnrollments(CanvasCallback<Enrollment[]> callback) {
        if(APIHelpers.paramIsNull(callback)) return;

        buildCacheInterface(UsersInterface.class, callback, null).getSelfEnrollments(callback);
        buildInterface(UsersInterface.class, callback, null).getSelfEnrollments(callback);
    }


    public static void updateShortName(String shortName, CanvasCallback<User> callback) {
        if (APIHelpers.paramIsNull(callback, shortName)) { return; }

        buildInterface(UsersInterface.class, callback, null).updateShortName(shortName, "", callback);
    }

    public static void getUserById(long userId, CanvasCallback<User> userCanvasCallback){
        if(APIHelpers.paramIsNull(userCanvasCallback)){return;}

        buildCacheInterface(UsersInterface.class, userCanvasCallback, null).getUserById(userId, userCanvasCallback);
        //Passing UserCallback here will break OUR cache.
        if(userCanvasCallback instanceof UserCallback){
            Log.e(APIHelpers.LOG_TAG, "You cannot pass a User Call back here. It'll break cache for users/self..");
            return;
        }

        buildInterface(UsersInterface.class, userCanvasCallback, null).getUserById(userId, userCanvasCallback);
    }

    public static void getUserByIdNoCache(long userId, CanvasCallback<User> userCanvasCallback){
        if(APIHelpers.paramIsNull(userCanvasCallback)){return;}

        //Passing UserCallback here will break OUR cache.
        if(userCanvasCallback instanceof UserCallback){
            Log.e(APIHelpers.LOG_TAG, "You cannot pass a User Call back here. It'll break cache for users/self..");
            return;
        }

        buildInterface(UsersInterface.class, userCanvasCallback, null).getUserById(userId, userCanvasCallback);
    }

    public static void getCourseUserById(CanvasContext canvasContext, long userId, CanvasCallback<User> userCanvasCallback){
        if(APIHelpers.paramIsNull(userCanvasCallback)){return;}

        buildCacheInterface(UsersInterface.class, userCanvasCallback, canvasContext).getUserById(canvasContext.getId(), userId, userCanvasCallback);

        //Passing UserCallback here will break OUR cache.
        if(userCanvasCallback instanceof UserCallback){
            Log.e(APIHelpers.LOG_TAG, "You cannot pass a User Call back here. It'll break cache for users/self..");
            return;
        }

        buildInterface(UsersInterface.class, userCanvasCallback, canvasContext).getUserById(canvasContext.getId(), userId, userCanvasCallback);
    }

    public static void getFirstPagePeople(CanvasContext canvasContext, CanvasCallback<User[]> callback) {
        if (APIHelpers.paramIsNull(callback, canvasContext)) { return; }

        buildCacheInterface(UsersInterface.class, callback, canvasContext).getFirstPagePeopleList(canvasContext.getId(), callback);
        buildInterface(UsersInterface.class, callback, canvasContext).getFirstPagePeopleList(canvasContext.getId(), callback);
    }

    public static void getFirstPagePeopleChained(CanvasContext canvasContext, boolean isCached, CanvasCallback<User[]> callback) {
        if (APIHelpers.paramIsNull(callback, canvasContext)) { return; }

        if(isCached) {
            buildCacheInterface(UsersInterface.class, callback, canvasContext).getFirstPagePeopleList(canvasContext.getId(), callback);
        } else {
            buildInterface(UsersInterface.class, callback, canvasContext).getFirstPagePeopleList(canvasContext.getId(), callback);
        }
    }

    public static void getNextPagePeople(String nextURL, CanvasCallback<User[]> callback){
        if (APIHelpers.paramIsNull(callback, nextURL)) { return; }

        callback.setIsNextPage(true);
        buildCacheInterface(UsersInterface.class, callback, false).getNextPagePeopleList(nextURL, callback);
        buildInterface(UsersInterface.class, callback, false).getNextPagePeopleList(nextURL, callback);
    }

    public static void getNextPagePeopleChained(String nextURL, CanvasCallback<User[]> callback, boolean isCached){
        if (APIHelpers.paramIsNull(callback, nextURL)) { return; }

        callback.setIsNextPage(true);
        if (isCached) {
            buildCacheInterface(UsersInterface.class, callback, false).getNextPagePeopleList(nextURL, callback);
        } else {
            buildInterface(UsersInterface.class, callback, false).getNextPagePeopleList(nextURL, callback);
        }
    }

    public static void getAllUsersForCourseByEnrollmentType(CanvasContext canvasContext, ENROLLMENT_TYPE enrollment_type, final CanvasCallback<User[]> callback){
        if(APIHelpers.paramIsNull(callback, canvasContext)){return;}

        CanvasCallback<User[]> bridge = new ExhaustiveBridgeCallback<>(User.class, callback, new ExhaustiveBridgeCallback.ExhaustiveBridgeEvents() {
            @Override
            public void performApiCallWithExhaustiveCallback(CanvasCallback bridgeCallback, String nextURL, boolean isCached) {
                if(callback.isCancelled()) { return; }

                UserAPI.getNextPagePeopleChained(nextURL, bridgeCallback, isCached);
            }
        });
        buildCacheInterface(UsersInterface.class, callback, canvasContext).getFirstPagePeopleListWithEnrollmentType(canvasContext.getId(), getEnrollmentTypeString(enrollment_type), bridge);
        buildInterface(UsersInterface.class, callback, canvasContext).getFirstPagePeopleListWithEnrollmentType(canvasContext.getId(), getEnrollmentTypeString(enrollment_type), bridge);
    }

    public static void getFirstPagePeople(CanvasContext canvasContext, ENROLLMENT_TYPE enrollment_type, CanvasCallback<User[]> callback) {
        if (APIHelpers.paramIsNull(callback, canvasContext)) { return; }

        buildCacheInterface(UsersInterface.class, callback, canvasContext).getFirstPagePeopleListWithEnrollmentType(canvasContext.getId(), getEnrollmentTypeString(enrollment_type), callback);
        buildInterface(UsersInterface.class, callback, canvasContext).getFirstPagePeopleListWithEnrollmentType(canvasContext.getId(), getEnrollmentTypeString(enrollment_type), callback);
    }

    public static void getFirstPagePeopleChained(CanvasContext canvasContext, ENROLLMENT_TYPE enrollment_type, boolean isCached, CanvasCallback<User[]> callback) {
        if (APIHelpers.paramIsNull(callback, canvasContext)) { return; }

        if(isCached) {
            buildCacheInterface(UsersInterface.class, callback, canvasContext).getFirstPagePeopleListWithEnrollmentType(canvasContext.getId(), getEnrollmentTypeString(enrollment_type), callback);
        } else {
            buildInterface(UsersInterface.class, callback, canvasContext).getFirstPagePeopleListWithEnrollmentType(canvasContext.getId(), getEnrollmentTypeString(enrollment_type), callback);
        }
    }

    public static void getColors(Context context, CanvasCallback<CanvasColor> callback) {
        buildCacheInterface(UsersInterface.class, context, false).getColors(callback);
        buildInterface(UsersInterface.class, context, false).getColors(callback);
    }

    public static void setColor(Context context, CanvasContext canvasContext, int color, CanvasCallback<CanvasColor> callback) {
        if (APIHelpers.paramIsNull(context, canvasContext, callback)) { return; }

        setColor(context, canvasContext.getContextId(), color, callback);
    }

    public static void setColor(Context context, String context_id, int color, CanvasCallback<CanvasColor> callback) {
        if (APIHelpers.paramIsNull(context, context_id, callback)) { return; }

        //Modifies a color into a RRGGBB color string with no #.
        String hexColor = Integer.toHexString(color);
        hexColor = hexColor.substring(hexColor.length() - 6);

        if(hexColor.contains("#")) {
            hexColor = hexColor.replaceAll("#", "");
        }

        buildInterface(UsersInterface.class, context, false).setColor(context_id, hexColor, "", callback);
    }

    public static void createSelfRegistrationUser(long accountId, String userName, String emailAddress, CanvasCallback<User> callback) {
        if (APIHelpers.paramIsNull(userName, emailAddress, callback)) { return; }

        buildInterface(UsersInterface.class, callback, false).createSelfRegistrationUser(accountId, userName, emailAddress, 1, "", callback);
    }

    public static void addObserveeByToken(String token, CanvasCallback<User> callback) {
        if(APIHelpers.paramIsNull(token, callback)) { return; }

        buildInterface(UsersInterface.class, callback, false).addObserveeWithToken(token, "", callback);
    }

    public static void getObservees(CanvasCallback<User[]> callback) {
        if(APIHelpers.paramIsNull(callback)) { return; }

        buildCacheInterface(UsersInterface.class, callback).getObservees(callback);
        buildInterface(UsersInterface.class, callback).getObservees(callback);
    }

    public static void removeObservee(long observeeId, CanvasCallback<User> callback) {
        if(APIHelpers.paramIsNull(callback)) { return; }

        buildInterface(UsersInterface.class, callback).removeObservee(observeeId, callback);
    }

    /**
     * Remove student from Airwolf. Currently only used in the Parent App
     * 
     * @param observerId
     * @param studentId
     * @param callback - 200 if successful
     */
    public static void removeStudent(String observerId, String studentId, CanvasCallback<Response> callback) {
        if(APIHelpers.paramIsNull(callback)) { return; }

        buildInterface(UsersInterface.class, APIHelpers.getAirwolfDomain(callback.getContext()), callback).removeStudent(observerId, studentId, callback);
    }

    /**
     * Add parent to Airwolf/Canvas. Currently only used in the Parent App.
     * @param body
     * @param callback
     */
    public static void addParent(Parent body, CanvasCallback<ParentResponse> callback) {
        if(APIHelpers.paramIsNull(body, callback)) { return; }
        ParentWrapper parentWrapper = new ParentWrapper();
        parentWrapper.setParent(body);

        buildInterface(UsersInterface.class, APIHelpers.getAirwolfDomain(callback.getContext()), callback, false).addParent(parentWrapper, callback);
    }

    /**
     * Get the parent's domain based on their email from Airwolf. Currently only used in the Parent App.
     * @param email - Parent's username
     * @param callback
     */
    public static void getParentUserDomain(String email, CanvasCallback<Domain> callback) {
        if(APIHelpers.paramIsNull(email, callback)) { return; }

        buildInterface(UsersInterface.class, APIHelpers.getAirwolfDomain(callback.getContext()), callback).getParentUserDomain(email, callback);
    }

    public static void authenticateParent(String email, String password, CanvasCallback<ParentResponse> callback) {
        if(APIHelpers.paramIsNull(email, password, callback)) { return; }

        Parent parent = new Parent();
        parent.setUsername(email);
        parent.setPassword(password);
        buildInterface(UsersInterface.class, APIHelpers.getAirwolfDomain(callback.getContext()), callback, false).authenticateParent(parent, callback);
    }

    public static void getObserveesForParent(String parentId, CanvasCallback<Student[]> callback) {
        if(APIHelpers.paramIsNull(parentId, callback)) { return; }

        buildCacheInterface(UsersInterface.class, APIHelpers.getAirwolfDomain(callback.getContext()), callback).getObserveesForParent(parentId, callback);
        buildInterface(UsersInterface.class, APIHelpers.getAirwolfDomain(callback.getContext()), callback).getObserveesForParent(parentId, callback);
    }

    /**
     * Add a student to a parent's account so the parent can observe the student
     *
     * @param parentId - ID of the parent
     * @param studentDomain - Domain of the student
     * @param callback
     */
    public static void addStudentToParent(String parentId, String studentDomain, CanvasCallback<Response> callback) {
        if(APIHelpers.paramIsNull(parentId, studentDomain, callback)) { return; }

        buildInterfaceNoRedirects(UsersInterface.class, APIHelpers.getAirwolfDomain(callback.getContext()), callback, false).addStudentToParent(parentId, studentDomain, callback);
    }


    /**
     * Let the user request a password if they forgot it.
     *
     * Will return a 404 if there is no record of the e-mail address.
     *
     * @param userName The user's email address
     * @param callback
     */
    public static void sendPasswordResetForParent(String userName, CanvasCallback<Response> callback) {
        if(APIHelpers.paramIsNull(userName, callback)) { return; }

        //include an empty json object to pass the parsing on airwolf. It doesn't like an empty string.
        JSONObject object = new JSONObject();
        buildInterface(UsersInterface.class, APIHelpers.getAirwolfDomain(callback.getContext()), callback, false).sendPasswordResetForParent(userName, object, callback);
    }

    /**
     * The API call to actually reset the parent's password to the one they just created.
     *
     * @param userName
     * @param password
     * @param callback
     */
    public static void resetParentPassword(String userName, String password, CanvasCallback<ResetParent> callback) {
        if(APIHelpers.paramIsNull(userName, password, callback)) { return; }

        Parent parent = new Parent();
        parent.setUsername(userName);
        parent.setPassword(password);
        buildInterface(UsersInterface.class, APIHelpers.getAirwolfDomain(callback.getContext()), callback, false).resetParentPassword(parent, callback);
    }
    /////////////////////////////////////////////////////////////////////////
    // Synchronous Calls
    /////////////////////////////////////////////////////////////////////////
    public static FileUploadParams getFileUploadParams(Context context, String fileName, long size, String contentType, Long parentFolderId){
        return buildInterface(UsersInterface.class, context).getFileUploadParams(size, fileName, contentType, parentFolderId, "");
    }

    public static FileUploadParams getFileUploadParams(Context context, String fileName, long size, String contentType, String parentFolderPath){
        return buildInterface(UsersInterface.class, context).getFileUploadParams(size, fileName, contentType, parentFolderPath, "");
    }

    public static Attachment uploadUserFile(String uploadUrl, LinkedHashMap<String,String> uploadParams, String mimeType, File file){
        return buildUploadInterface(UsersInterface.class, uploadUrl).uploadUserFile(uploadParams, new TypedFile(mimeType, file));
    }

    /////////////////////////////////////////////////////////////////////////
    // Helpers
    /////////////////////////////////////////////////////////////////////////
    private static String getEnrollmentTypeString(ENROLLMENT_TYPE enrollment_type){
        String enrollmentType = "";
        switch (enrollment_type){
            case DESIGNER:
                enrollmentType = "designer";
                break;
            case OBSERVER:
                enrollmentType = "observer";
                break;
            case STUDENT:
                enrollmentType = "student";
                break;
            case TA:
                enrollmentType = "ta";
                break;
            case TEACHER:
                enrollmentType = "teacher";
                break;
        }
        return enrollmentType;
    }
}
