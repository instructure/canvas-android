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

import com.instructure.canvasapi.model.CanvasContext;
import com.instructure.canvasapi.model.FileFolder;
import com.instructure.canvasapi.utilities.APIHelpers;
import com.instructure.canvasapi.utilities.CanvasCallback;
import com.instructure.canvasapi.utilities.LinkHeaders;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.Path;

public class FileFolderAPI extends BuildInterfaceAPI {

    interface FilesFoldersInterface {
        @GET("/{context_id}/folders/root")
        void getRootFolderForContext(@Path("context_id") long context_id, Callback<FileFolder> callback);

        @GET("/self/folders/root")
        void getRootUserFolder(Callback<FileFolder> callback);

        @GET("/folders/{folderid}/folders")
        void getFirstPageFolders(@Path("folderid") long folder_id, Callback<FileFolder[]> callback);

        @GET("/folders/{folderid}/files")
        void getFirstPageFiles(@Path("folderid") long folder_id, Callback<FileFolder[]> callback);

        @GET("/{fileurl}")
        void getFileFolderFromURL(@Path(value = "fileurl", encode = false) String fileURL, Callback<FileFolder> callback);

        @GET("/{next}")
        void getNextPageFileFoldersList(@Path(value = "next", encode = false) String nextURL, Callback<FileFolder[]> callback);

        @DELETE("/files/{fileid}")
        void deleteFile(@Path("fileid")long fileId, Callback<Response> callback);
    }

    /////////////////////////////////////////////////////////////////////////
    // API Calls
    /////////////////////////////////////////////////////////////////////////

    public static void getFirstPageFoldersRoot(CanvasContext canvasContext, final CanvasCallback<FileFolder[]> callback) {
        if (APIHelpers.paramIsNull(callback, canvasContext)) {
            return;
        }

        // Build a callback bridge. Use a CanvasCallback instead of a regular callback so we can use caching appropriately and
        // not have to make more API calls than necessary
        Callback<FileFolder> bridgeCallback = new CanvasCallback<FileFolder>(callback.getStatusDelegate()) {

            @Override
            public void cache(FileFolder fileFolder, LinkHeaders linkHeaders, Response response) {
                buildCacheInterface(FilesFoldersInterface.class, callback, null).getFirstPageFolders(fileFolder.getId(), callback);
            }

            @Override
            public void firstPage(FileFolder fileFolder, LinkHeaders linkHeaders, Response response) {

                //Handle if the fragment becomes detached. This isn't a CanvasCallback, so it's not automatic.
                if(callback == null || callback.getContext() == null) {return;}

                buildInterface(FilesFoldersInterface.class, callback, null).getFirstPageFolders(fileFolder.getId(), callback);
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                callback.failure(retrofitError);
            }
        };

        // get from cache
        getRootFolder(canvasContext, true, callback, bridgeCallback);

        // get from network
        getRootFolder(canvasContext, false, callback, bridgeCallback);

    }


    public static void getFirstPageFilesRoot(CanvasContext canvasContext, final CanvasCallback<FileFolder[]> callback) {
        getFirstPageFilesRootChained(canvasContext, false, callback);
    }

    public static void getFirstPageFilesRootChained(CanvasContext canvasContext, boolean isCached, final CanvasCallback<FileFolder[]> callback) {
        if (APIHelpers.paramIsNull(callback, canvasContext)) {
            return;
        }

        // Build a callback bridge. Use a CanvasCallback instead of a regular callback so we can use caching appropriately and
        // not have to make more API calls than necessary
        Callback<FileFolder> bridgeCallback = new CanvasCallback<FileFolder>(callback.getStatusDelegate()) {

            @Override
            public void cache(FileFolder fileFolder, LinkHeaders linkHeaders, Response response) {
                buildCacheInterface(FilesFoldersInterface.class, callback, null).getFirstPageFiles(fileFolder.getId(), callback);
            }

            @Override
            public void firstPage(FileFolder fileFolder, LinkHeaders linkHeaders, Response response) {

                //Handle if the fragment becomes detached. This isn't a CanvasCallback, so it's not automatic.
                if(callback == null || callback.getContext() == null) {return;}

                buildInterface(FilesFoldersInterface.class, callback, null).getFirstPageFiles(fileFolder.getId(), callback);
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                callback.failure(retrofitError);
            }
        };

        getRootFolder(canvasContext, isCached, callback, bridgeCallback);
    }

    private static void getRootFolder(CanvasContext canvasContext, boolean isCached, CanvasCallback callback, Callback<FileFolder> bridgeCallback) {
        FilesFoldersInterface foldersInterface = buildInterface(FilesFoldersInterface.class, callback, canvasContext);

        if (canvasContext.getType() == CanvasContext.Type.USER) {
            if(isCached) {
                buildCacheInterface(FilesFoldersInterface.class, callback, canvasContext).getRootUserFolder(bridgeCallback);
            } else {
                foldersInterface.getRootUserFolder(bridgeCallback);
            }
        } else {
            if(isCached) {
                buildCacheInterface(FilesFoldersInterface.class, callback, canvasContext).getRootFolderForContext(canvasContext.getId(), bridgeCallback);
            } else {
                foldersInterface.getRootFolderForContext(canvasContext.getId(), bridgeCallback);
            }
        }
    }

    public static void getFirstPageFolders(long folderid, CanvasCallback<FileFolder[]> callback) {
        if (APIHelpers.paramIsNull(callback) || folderid <= 0) {
            return;
        }

        buildCacheInterface(FilesFoldersInterface.class, callback, null).getFirstPageFolders(folderid, callback);
        buildInterface(FilesFoldersInterface.class, callback, null).getFirstPageFolders(folderid, callback);
    }

    public static void getFirstPageFiles(long folderid, CanvasCallback<FileFolder[]> callback) {
        if (APIHelpers.paramIsNull(callback) || folderid <= 0) {
            return;
        }

        buildCacheInterface(FilesFoldersInterface.class, callback, null).getFirstPageFiles(folderid, callback);
        buildInterface(FilesFoldersInterface.class, callback, null).getFirstPageFiles(folderid, callback);
    }

    public static void getFirstPageFilesChained(long folderid, boolean isCached, CanvasCallback<FileFolder[]> callback) {
        if (APIHelpers.paramIsNull(callback) || folderid <= 0) {
            return;
        }

        if(isCached) {
            buildCacheInterface(FilesFoldersInterface.class, callback, null).getFirstPageFiles(folderid, callback);
        } else {
            buildInterface(FilesFoldersInterface.class, callback, null).getFirstPageFiles(folderid, callback);
        }
    }

    public static void getNextPageFileFolders(String nextURL, CanvasCallback<FileFolder[]> callback) {
        if (APIHelpers.paramIsNull(callback, nextURL)) {
            return;
        }

        callback.setIsNextPage(true);
        buildCacheInterface(FilesFoldersInterface.class, callback, false).getNextPageFileFoldersList(nextURL, callback);
        buildInterface(FilesFoldersInterface.class, callback, false).getNextPageFileFoldersList(nextURL, callback);
    }

    public static void getFileFolderFromURL(String url, CanvasCallback<FileFolder> callback) {
        if (APIHelpers.paramIsNull(callback, url)) {
            return;
        }

        buildCacheInterface(FilesFoldersInterface.class, callback, null).getFileFolderFromURL(url, callback);
        buildInterface(FilesFoldersInterface.class, callback, null).getFileFolderFromURL(url, callback);
    }

    public static void deleteFile(long fileId, CanvasCallback<Response> callback) {
        if (APIHelpers.paramIsNull(callback)) {
            return;
        }

        buildInterface(FilesFoldersInterface.class, callback, null).deleteFile(fileId,callback);
    }
}
