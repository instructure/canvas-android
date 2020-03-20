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

package com.instructure.pandautils.utils;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.instructure.canvasapi2.models.CanvasContext;
import com.instructure.canvasapi2.models.postmodels.FileSubmitObject;
import com.instructure.pandautils.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import androidx.annotation.Nullable;

public class FileUploadUtils {

    public static final String FILE_SCHEME = "file";
    public static final String CONTENT_SCHEME = "content";

    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.
     * <p/>
     * http://stackoverflow.com/questions/20067508/get-real-path-from-uri-android-kitkat-new-storage-access-framework
     *
     * @param context An Android Context
     * @param uri      The Uri to query.
     * @author paulburke
     */
    @SuppressLint("NewApi")
    public static String getPath(final Context context, final Uri uri) {

        // DocumentProvider
        if (DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {

                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);

                if (id.startsWith("raw:")) {
                    return id.replaceFirst("raw:", "");
                }

                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context.getContentResolver(), contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {

                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context.getContentResolver(), contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if (CONTENT_SCHEME.equalsIgnoreCase(uri.getScheme())) {
            // Return the remote address
            return getDataColumn(context.getContentResolver(), uri, null, null);
        }
        // File
        else if (FILE_SCHEME.equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     * @param resolver ContentResolver to query our URI
     * @param uri      The Uri to query.
     * @return The value of the _data column, which is typically a file path.
     *         Empty string if column does not exist.
     */
    public static String getDataColumn(ContentResolver resolver, Uri uri){
        return getDataColumn(resolver, uri, null, null);
    }

    /**
     *
     * @param resolver
     * @param uri
     * @param selection
     * @param selectionArgs
     * @return
     */
    public static String getDataColumn(ContentResolver resolver,  Uri uri, String selection,
                                       String[] selectionArgs) {
        Log.v(Const.PANDA_UTILS_FILE_UPLOAD_UTILS_LOG, "getDataColumn uri: " + uri + " selection: " + selection + " args: " + selectionArgs);
        String filePath = "";
        Cursor cursor = null;
        final String column = MediaStore.MediaColumns.DATA;
        final String[] projection = {
                column
        };

        try {
            cursor = resolver.query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                filePath = cursor.getString(column_index);
            }
        }catch (Exception e){
            // An exception will be raised if the _data column does not exist.
            // This is mostly likely caused by new fileProvider permissions in kitkat+, in those cases, we fall back to using openFileDescriptor
            // to get access to the shared file.
            Log.e(Const.PANDA_UTILS_FILE_UPLOAD_UTILS_LOG, "cursor " + e.toString());
            return "";

        } finally {
            if (cursor != null){
                cursor.close();
            }
        }

        if(filePath == null) {
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(resolver, uri);

                // CALL THIS METHOD TO GET THE URI FROM THE BITMAP
                Uri tempUri = getImageUri(resolver, bitmap);

                // CALL THIS METHOD TO GET THE ACTUAL PATH
                filePath = getRealPathFromURI(resolver, tempUri);
            } catch(Exception e) {
                Log.e(Const.PANDA_UTILS_FILE_UPLOAD_UTILS_LOG, "filePath==null:  " + e.toString());
            }
        }
        return filePath;
    }

    public static Uri getImageUri(ContentResolver resolver, Bitmap inImage) {
        String path = MediaStore.Images.Media.insertImage(resolver, inImage, "profilePic", null);
        return Uri.parse(path);
    }

    public static String getRealPathFromURI(ContentResolver resolver, Uri uri) {
        Cursor cursor = resolver.query(uri, null, null, null, null);
        cursor.moveToFirst();
        int idx = cursor.getColumnIndex(MediaStore.MediaColumns.DATA);
        String index = cursor.getString(idx);
        cursor.close();
        return index;
    }

    @Nullable
    public static String getFileNameFromUri(ContentResolver resolver, Uri uri) {
        Cursor cursor = resolver.query(uri, null, null, null, null);
        if (cursor == null) return null;
        int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        cursor.moveToFirst();
        String name = cursor.getString(nameIndex);
        cursor.close();
        return name;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    public static FileSubmitObject getFile(Context context, Uri uri) {
        ContentResolver contentResolver = context.getContentResolver();
        String mimeType = FileUploadUtils.getFileMimeType(contentResolver, uri);
        String fileName = FileUploadUtils.getFileNameWithDefault(contentResolver, uri, mimeType);
        return FileUploadUtils.getFileSubmitObjectFromInputStream(context, uri, fileName, mimeType);
    }

    public static FileSubmitObject getFileSubmitObjectFromInputStream(Context context, Uri uri, String fileName, final String mimeType) {
        if (uri == null) return null;
        File file;
        String errorMessage = "";
        // copy file from uri into new temporary file and pass back that new file's path
        InputStream input = null;
        FileOutputStream output = null;
        try {
            ContentResolver cr = context.getContentResolver();

            input = cr.openInputStream(uri);
            // add extension to filename if needed
            int lastDot = fileName.lastIndexOf(".");
            String extension = getFileExtensionFromMimeType(cr.getType(uri));
            if (lastDot == -1 && !extension.isEmpty()) {
                fileName = fileName + "." + extension;
            }

            // create a temp file to copy the uri contents into
            String tempFilePath = getTempFilePath(context, fileName);

            output = new FileOutputStream(tempFilePath);
            int read = 0;
            byte[] bytes = new byte[4096];
            while ((read = input.read(bytes)) != -1) {
                output.write(bytes, 0, read);
            }
            // return the filepath of our copied file.
            file = new File(tempFilePath);

        } catch (FileNotFoundException e) {
            file = null;
            errorMessage = context.getString(R.string.errorOccurred);
            Log.e(Const.PANDA_UTILS_FILE_UPLOAD_UTILS_LOG, e.toString());
        } catch (Exception exception) {
            // if querying the datacolumn and the FileDescriptor both fail We can't handle the shared file.
            file = null;
            Log.e(Const.PANDA_UTILS_FILE_UPLOAD_UTILS_LOG, exception.toString());
            errorMessage = context.getString(R.string.errorLoadingFiles);
        } finally {
            if (input != null) try {
                input.close();
            } catch (Exception ignored) {
            }
            if (output != null) try {
                output.close();
            } catch (Exception ignored) {
            }
        }

        if (file != null) {
            return new FileSubmitObject(fileName, file.length(), mimeType, file.getAbsolutePath(), errorMessage, FileSubmitObject.STATE.NORMAL);
        }
        return new FileSubmitObject(fileName, 0, mimeType, "", errorMessage, FileSubmitObject.STATE.NORMAL);
    }

    public static String getFileNameWithDefault(ContentResolver resolver, Uri uri, String mimeType) {
        String fileName = "";
        String scheme = uri.getScheme();
        if (FILE_SCHEME.equalsIgnoreCase(scheme)) {
            fileName = uri.getLastPathSegment();
        } else if (CONTENT_SCHEME.equalsIgnoreCase(scheme)) {
            final String[] proj = {MediaStore.MediaColumns.DISPLAY_NAME};

            // get file name
            Cursor metaCursor = null;
            // Don't have try with resources, so we get a finally block that can close the cursor
            //noinspection TryFinallyCanBeTryWithResources
            try {
                metaCursor = resolver.query(uri, proj, null, null, null);
                if (metaCursor != null) {
                    if (metaCursor.moveToFirst()) {
                        fileName = metaCursor.getString(0);
                    }
                }
            } catch (Exception ignore) {
                if (fileName.isEmpty()) {
                    fileName = uri.getLastPathSegment();
                }
            } finally {
                if (metaCursor != null) {
                    metaCursor.close();
                }
            }
        }

        return getTempFilename(fileName);
    }


    public static String getFileMimeType(ContentResolver resolver, Uri uri) {
        String scheme = uri.getScheme();
        String mimeType = null;
        if (FILE_SCHEME.equalsIgnoreCase(scheme)) {
            if (uri.getLastPathSegment() != null) {
                mimeType = getMimeTypeFromFileNameWithExtension(uri.getLastPathSegment());
            }
        } else if (CONTENT_SCHEME.equalsIgnoreCase(scheme)) {
            mimeType = resolver.getType(uri);
        }
        if (mimeType == null) {
            return "*/*";
        }
        return mimeType;
    }

    public static String getMimeTypeFromFileNameWithExtension(String fileNameWithExtension) {
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        int index = fileNameWithExtension.indexOf(".");
        String ext = "";
        if (index != -1) {
            ext = fileNameWithExtension.substring(index + 1).toLowerCase(); // Add one so the dot isn't included
        }
        return mime.getMimeTypeFromExtension(ext);
    }

    public static String getTempFilename(String fileName) {
        if (fileName == null || "".equals(fileName)) {
            fileName = "File_Upload";
        } else if (fileName.equals("image.jpg")) {
            fileName = "Image_Upload";
        } else if (fileName.equals("video.mpg") || fileName.equals("video.mpeg")) {
            // image doesn't have a name.
            fileName = "Video_Upload";
        }

        return fileName;
    }

    public static String getFileExtensionFromMimeType(String mimeType) {
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        String extension = mime.getExtensionFromMimeType(mimeType);
        if (extension == null) {
            return "";
        }
        return extension;
    }

    private static String getTempFilePath(Context context, String fileName) throws IOException {

        fileName = fileName.replace("/", "_");
        File outputDir = getCacheDir(context);
        File outputFile = new File(outputDir, fileName);
        return outputFile.getAbsolutePath();
    }


    public static File getCacheDir(Context context) {
        File canvasFolder = new File(context.getCacheDir(), "file_upload");
        if (!canvasFolder.exists()) {
            canvasFolder.mkdirs();
        }
        return canvasFolder;
    }

    public static boolean deleteTempFile(String filename) {
        File file = new File(filename);
        return file.delete();
    }

    public static boolean deleteTempDirectory(Context context){
        return deleteDirectory(getCacheDir(context)) && deleteDirectory(getExternalCacheDir(context));
    }

    public static boolean deleteDirectory(File fileFolder){
        if (fileFolder.isDirectory()){
            String[] children = fileFolder.list();
            for (String aChildren : children) {
                boolean success = deleteDirectory(new File(fileFolder, aChildren));
                if (!success) {
                    return false;
                }
            }
        }
        return fileFolder.delete();
    }

    public static File getExternalCacheDir(Context context) {
        File cacheDir = new File(context.getExternalCacheDir(), "file_upload");
        if (!cacheDir.exists()) cacheDir.mkdirs();
        return cacheDir;
    }

    public static Bundle createTaskLoaderBundle(CanvasContext canvasContext, String url, String title, boolean authenticate) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(Const.CANVAS_CONTEXT, canvasContext);
        bundle.putString(Const.INTERNAL_URL, url);
        bundle.putBoolean(Const.AUTHENTICATE, authenticate);
        bundle.putString(Const.ACTION_BAR_TITLE, title);
        return bundle;
    }


}
