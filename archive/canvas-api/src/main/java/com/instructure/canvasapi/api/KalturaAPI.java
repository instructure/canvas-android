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
import com.instructure.canvasapi.model.KalturaConfig;
import com.instructure.canvasapi.model.KalturaSession;
import com.instructure.canvasapi.model.kaltura.xml;
import com.instructure.canvasapi.utilities.APIHelpers;
import com.instructure.canvasapi.utilities.CanvasCallback;
import com.instructure.canvasapi.utilities.FileUtilities;
import com.instructure.canvasapi.utilities.KalturaRestAdapter;
import retrofit.client.Response;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.Part;
import retrofit.http.Query;
import retrofit.mime.TypedFile;
import retrofit.mime.TypedString;


//Make caching work
public class KalturaAPI extends BuildInterfaceAPI {
    private static String getKalturaConfigCache() {
        return "/services/kaltura";
    }

    //Interface talking to Canvas servers
    public interface KalturaConfigurationInterface {
        @GET("/services/kaltura")
        void getKalturaConfigaration(Callback<KalturaConfig> callback);

        @POST("/services/kaltura_session")
        void startKalturaSession(@Body String body, Callback<KalturaSession> callback);

    }
    
    //Interface talking to Kaltura servers
    public interface KalturaAPIInterface {

        @POST("/index.php?service=uploadtoken&action=add")
        void getKalturaUploadToken(@Query(value = "ks", encodeValue = true) String ks, @Body String body, Callback<xml> callback);


        @POST("/index.php?service=media&action=addFromUploadedFile")
        xml getMediaIdForUploadedFileTokenSynchronous(@Query("ks") String ks, @Query("uploadTokenId") String uploadToken, @Query("mediaEntry:name") String name, @Query("mediaEntry:mediaType") String mediaType, @Body String body);

        @Multipart
        @POST("/index.php?service=uploadtoken&action=upload")
        Response uploadFileAtPath(@Part("ks") TypedString kalturaToken, @Part("uploadTokenId") TypedString uploadToken, @Part("fileData") TypedFile fileData);

    }

    /////////////////////////////////////////////////////////////////////////
    // Build Interface Helpers
    /////////////////////////////////////////////////////////////////////////

    private static KalturaAPIInterface buildKalturaAPIInterface(CanvasCallback<?> callback) {
        RestAdapter restAdapter = KalturaRestAdapter.buildAdapter(callback);
        return restAdapter.create(KalturaAPIInterface.class);
    }

    /////////////////////////////////////////////////////////////////////////
    // API Calls
    /////////////////////////////////////////////////////////////////////////
    public static void getKalturaConfiguration(final CanvasCallback<KalturaConfig> callback) {
        if (APIHelpers.paramIsNull(callback)) {
            return;
        }

        buildInterface(KalturaConfigurationInterface.class, callback, null).getKalturaConfigaration(callback);
    }

    public static void startKalturaSession(final CanvasCallback<KalturaSession> callback) {
        if (APIHelpers.paramIsNull(callback)) {
            return;
        }

        buildInterface(KalturaConfigurationInterface.class, callback, null).startKalturaSession("", callback);
    }

    public static void getKalturaUploadToken(final CanvasCallback<xml> callback) {
        if (APIHelpers.paramIsNull(callback)) {
            return;
        }

        String kalturaToken = APIHelpers.getKalturaToken(callback.getContext());

        buildKalturaAPIInterface(callback).getKalturaUploadToken(kalturaToken, "", callback);
    }

    public static Response uploadKalturaFile(Context context, String kalturaToken, String uploadToken, TypedFile fileData, String hostUrl) {
        return buildUploadInterfaceXML(KalturaAPIInterface.class, context, hostUrl).uploadFileAtPath(new TypedString(kalturaToken), new TypedString(uploadToken), fileData);
    }

    public static xml getMediaIdForUploadedFileTokenSynchronous(Context context, String ks, String uploadToken, String fileName, String mimetype) {
        try {
            RestAdapter restAdapter = KalturaRestAdapter.buildAdapter(context);
            String mediaTypeConverted = FileUtilities.kalturaCodeFromMimeType(mimetype); 
            return restAdapter.create(KalturaAPIInterface.class).getMediaIdForUploadedFileTokenSynchronous(ks, uploadToken, fileName, mediaTypeConverted, "");
        } catch (Exception E) {
            Log.e(APIHelpers.LOG_TAG, E.toString());
            return null;
        }
    }

}
