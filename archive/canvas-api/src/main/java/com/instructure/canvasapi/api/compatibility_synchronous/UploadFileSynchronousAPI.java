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

package com.instructure.canvasapi.api.compatibility_synchronous;

import android.app.Activity;
import android.content.Context;
import android.net.http.AndroidHttpClient;

import com.google.gson.Gson;
import com.instructure.canvasapi.model.Attachment;
import com.instructure.canvasapi.model.Avatar;
import com.instructure.canvasapi.utilities.CanvasRestAdapter;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;

public class UploadFileSynchronousAPI {

    /**
     * These methods are all GUARANTEED to be called on the UI thread.
     */
    public interface UploadFilesErrorHandler {
        void onFileQuotaExceeded();
        void onUnexpectedError(Exception exception);
    }


    //wrapper classes for the avatar so we can let the view know what type of error (if any) happened
    public static class AvatarError {
        public static int NONE = 0;
        public static int QUOTA_EXCEEDED = 1;
        public static int UNKNOWN = 2;
    }
    public static class AvatarWrapper {
        public int error;
        public Avatar avatar;

        public AvatarWrapper(){}
    }


    public static AvatarWrapper postAvatar(String imageName, long size, String contentType, String path, Context context) {
        String url = String.format(Locale.US, "/api/v1/users/self/files?name=%s&size=%d&content_type=%s",  imageName, size, contentType);
        //set the parent folder
        String parentFolder = "&parent_folder_path=profile+pictures";
        url += parentFolder;
        //don't overwrite
        url += "&on_duplicate=rename";
        APIHttpResponse response = HttpHelpers.httpPost(url, null, context);

        try
        {
			/*{
				  "upload_url": "https://some-bucket.s3.amazonaws.com/",
				  "upload_params": {
				    "key": "/users/1234/files/profile_pic.jpg",
				    "acl": "private",
				    "Filename": "profile_pic.jpg",
				    "AWSAccessKeyId": "some_id",
				    "Policy": "some_opaque_string",
				    "Signature": "another_opaque_string",
				    "Content-Type": "image/jpeg"
				  }
				}
			 */
            ArrayList<BasicNameValuePair> pairs = new ArrayList<BasicNameValuePair>();
            JSONObject json = new JSONObject(response.responseBody);

            //See if we've exceeded the quota.
            if(json.has("message") && json.getString("message").equals("file size exceeds quota")){
                //let the user know
                AvatarWrapper avatarWrapper = new AvatarWrapper();
                avatarWrapper.avatar = null;
                avatarWrapper.error = AvatarError.QUOTA_EXCEEDED;
                return avatarWrapper;
            }

            String uploadUrl = json.getString("upload_url");
            JSONObject params = json.getJSONObject("upload_params");

            Iterator<?> keys = params.keys();

            //get all the keys
            while(keys.hasNext()) {
                // loop to get the dynamic key
                String currentDynamicKey = (String)keys.next();

                //TODO: what to do if the parameter isn't a string??
                pairs.add(new BasicNameValuePair(currentDynamicKey , params.getString(currentDynamicKey)));

            }

            File file = new File(path);
            String postResponse = UploadFileSynchronousAPI.uploadFile(file, uploadUrl, contentType, pairs, context);

            JSONObject object = new JSONObject(postResponse);
            String avatarUrl = object.getString("url");

            //set up a new avatar to return
            Avatar avatar = new Avatar();
            avatar.setUrl(avatarUrl);
            avatar.setDisplayName(object.getString("display_name"));
            avatar.setType(object.getString("content-type"));

            AvatarWrapper avatarWrapper = new AvatarWrapper();
            avatarWrapper.avatar = avatar;
            avatarWrapper.error = AvatarError.NONE;

            return avatarWrapper;
        }
        catch(Exception E)
        {
            AvatarWrapper avatarWrapper = new AvatarWrapper();
            avatarWrapper.avatar = null;
            avatarWrapper.error = AvatarError.UNKNOWN;
            return avatarWrapper;
        }
    }

    public static String postBackDrop(String imageName, long size, String contentType, String path, Context context){
        String url = String.format(Locale.US, "/api/v1/users/self/files?name=%s&size=%d&content_type=%s",  imageName, size, contentType);
        //set parent folder
        String parentFolder = "&parent_folder_path=profile+pictures";
        url += parentFolder;
        //dont overwrite
        url += "&on_duplicate=rename";
        APIHttpResponse response = HttpHelpers.httpPost(url, null, context);

        try{
            ArrayList<BasicNameValuePair> pairs = new ArrayList<BasicNameValuePair>();
            JSONObject json = new JSONObject(response.responseBody);
            String uploadUrl = json.getString("upload_url");
            JSONObject params = json.getJSONObject("upload_params");

            Iterator<?> keys = params.keys();

            while(keys.hasNext()){
                String currenDynamicKey = (String)keys.next();

                pairs.add(new BasicNameValuePair(currenDynamicKey, params.getString(currenDynamicKey)));
            }

            File file = new File(path);
            String postResponse = UploadFileSynchronousAPI.uploadFile(file, uploadUrl, contentType, pairs, context);

            JSONObject object = new JSONObject(postResponse);
            return object.getString("url");
        }catch(Exception E){
            return null;
        }

    }

    public static Attachment uploadSubmissionFile(long courseId, long assignmentId, String name, long size, String path, String contentType, Activity activity, UploadFilesErrorHandler uploadFilesErrorHandler){
        String url = String.format(Locale.US, "/api/v1/courses/%d/assignments/%d/submissions/self/files?name=%s&size=%d&on_duplicate=rename",  courseId, assignmentId, name, size);
        return uploadFile(url, null, path, contentType, activity, uploadFilesErrorHandler);
    }

    public static Attachment uploadPersonalFile(String name, Long parentFolderID, String path, long size, String contentType, Activity activity, UploadFilesErrorHandler uploadFilesErrorHandler){
        String url = String.format(Locale.US, "/api/v1/users/self/files?name=%s&size=%d&on_duplicate=rename",name, size);
        return uploadFile(url, parentFolderID, path, contentType, activity, uploadFilesErrorHandler);
    }

    public static Attachment uploadCourseFile(long courseId, String name, Long parentFolderID, long size, String path, String contentType, Activity activity, UploadFilesErrorHandler uploadFilesErrorHandler){
        String url = String.format(Locale.US, "/api/v1/courses/"+courseId+"/files?name=%s&size=%d&on_duplicate=rename",name, size);
        return uploadFile(url, parentFolderID, path, contentType, activity, uploadFilesErrorHandler);
    }


    private static Attachment uploadFile(String url, Long parentFolderID, String path, String contentType, Activity activity, final UploadFilesErrorHandler uploadFilesErrorHandler) {


        if(parentFolderID != null){
            url += "&parent_folder_id="+parentFolderID;
        }

        APIHttpResponse response = HttpHelpers.httpPost(url, null, activity);

        try
        {
            /*{
                  "upload_url": "https://some-bucket.s3.amazonaws.com/",
                  "upload_params": {
                    "key": "/users/1234/files/profile_pic.jpg",
                    "acl": "private",
                    "Filename": "profile_pic.jpg",
                    "AWSAccessKeyId": "some_id",
                    "Policy": "some_opaque_string",
                    "Signature": "another_opaque_string",
                    "Content-Type": "image/jpeg"
                  }
                }
             */
            ArrayList<BasicNameValuePair> pairs = new ArrayList<BasicNameValuePair>();
            JSONObject json = new JSONObject(response.responseBody);

            //See if we've exceeded the quota.
            if(json.has("message") && json.getString("message").equals("file size exceeds quota")){
                if(uploadFilesErrorHandler != null && activity != null){
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            uploadFilesErrorHandler.onFileQuotaExceeded();
                        }
                    });
                }
                return null;
            }

            String uploadUrl = json.getString("upload_url");
            JSONObject params = json.getJSONObject("upload_params");

            Iterator<?> keys = params.keys();

            //get all the keys
            while(keys.hasNext()) {
                // loop to get the dynamic key
                String currentDynamicKey = (String)keys.next();

                //TODO: what to do if the parameter isn't a string??
                pairs.add(new BasicNameValuePair(currentDynamicKey , params.getString(currentDynamicKey)));

            }

            File file = new File(path);
            String postResponse = UploadFileSynchronousAPI.uploadFile(file, uploadUrl, contentType, pairs, activity);

            //Do the JSON parsing.
            Gson gson = CanvasRestAdapter.getGSONParser();
            return gson.fromJson(postResponse,Attachment.class);
        } catch (final Exception exception) {
            if (uploadFilesErrorHandler != null && activity != null) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        uploadFilesErrorHandler.onUnexpectedError(exception);
                    }
                });
            }
            return null;
        }
    }

    public static boolean submitFiles(long courseId, long assignmentId, ArrayList<Attachment> attachments, Context context) {
        String url = String.format(Locale.US, "/api/v1/courses/%d/assignments/%d/submissions",  courseId, assignmentId);

        ArrayList<BasicNameValuePair> postVars = new ArrayList<BasicNameValuePair>();
        postVars.add(new BasicNameValuePair("submission[submission_type]", "online_upload"));
        //takes an array of file_ids, this is how to add an array for post variables
        for(int i = 0; i < attachments.size(); i++) {
            postVars.add(new BasicNameValuePair("submission[file_ids][]", Long.toString(attachments.get(i).getId())));
        }

        APIHttpResponse response = HttpHelpers.httpPost(url, postVars, context);

        try {
            JSONObject json = new JSONObject(response.responseBody);
            return json.has("id");
        }
        catch(Exception E) {

            return false;
        }
    }

    //needs to be AndroidHttpClient to work for some reason. Not really clear why.
    private static AndroidHttpClient mHttpClient;


    //STEPS 2 AND 3 OF API

    public static String uploadFile(File image, String url, String contentType, ArrayList<BasicNameValuePair> pairs, Context context) {

        try {
            //STEP 2 of API
            HttpPost httppost = new HttpPost(url);
            MultipartEntity multipartEntity = new MultipartEntity();

            for(BasicNameValuePair pair : pairs) {
                String value = pair.getValue();
                multipartEntity.addPart(pair.getName(), new StringBody(pair.getValue()));
            }

            multipartEntity.addPart("file", new FileBody(image));
            httppost.setEntity(multipartEntity);

            HttpParams params = new BasicHttpParams();
            params.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
            if(mHttpClient == null) {
                mHttpClient = AndroidHttpClient.newInstance("agent");
                mHttpClient.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.RFC_2109);
                mHttpClient.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);

            }
            HttpResponse httpResponse = mHttpClient.execute(httppost);
            String response = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");

            //STEP 3 of API
            Header[] headerLocation = httpResponse.getHeaders("Location");
            if(headerLocation.length <= 0) {
                return "Header error";
            }
            String location = headerLocation[0].getValue();
            Header[] headerContent = httpResponse.getHeaders("Content-Length");
            if(headerContent.length <= 0) {
                return "Header content error";
            }

            BasicNameValuePair pair = new BasicNameValuePair(headerContent[0].getName(), headerContent[0].getValue());
            ArrayList<BasicNameValuePair> paramPairs = new ArrayList<BasicNameValuePair>();
            pairs.add(pair);
            APIHttpResponse postResponse = HttpHelpers.httpPost(location, paramPairs, context);

            return postResponse.responseBody;
        } catch (Exception e) {
            return null;
        }
    }
}
