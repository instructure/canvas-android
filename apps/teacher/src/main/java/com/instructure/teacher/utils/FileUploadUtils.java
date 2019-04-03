/*
 * Copyright (C) 2017 - present  Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.instructure.teacher.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.instructure.pandautils.models.FileSubmitObject;
import com.instructure.pandautils.utils.Const;
import com.instructure.teacher.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


public class FileUploadUtils {

    public static final String FILE_SCHEME = "file";
    public static final String CONTENT_SCHEME = "content";

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
            if (lastDot == -1) {
                fileName = fileName + "." + getFileExtensionFromMimeType(cr.getType(uri));
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
            errorMessage = context.getString(R.string.error_occurred);
            Log.e(Const.PANDA_UTILS_FILE_UPLOAD_UTILS_LOG, e.toString());
        } catch (Exception exception) {
            // if querying the datacolumn and the FileDescriptor both fail We can't handle the shared file.
            file = null;
            Log.e(Const.PANDA_UTILS_FILE_UPLOAD_UTILS_LOG, exception.toString());
            errorMessage = context.getString(R.string.error_loading_files);
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
            return new FileSubmitObject(fileName, file.length(), mimeType, file.getAbsolutePath(), errorMessage);
        }
        return new FileSubmitObject(fileName, 0, mimeType, "", errorMessage);
    }

    public static String getFileNameWithDefault(ContentResolver resolver, Uri uri, String mimeType) {
        String fileName = "";
        String scheme = uri.getScheme();
        if (FILE_SCHEME.equalsIgnoreCase(scheme)) {
            fileName = uri.getLastPathSegment();
        } else if (CONTENT_SCHEME.equalsIgnoreCase(scheme)) {
            final String[] proj = {MediaStore.MediaColumns.DISPLAY_NAME};

            // get file name
            Cursor metaCursor = resolver.query(uri, proj, null, null, null);
            if (metaCursor != null) {
                try {
                    if (metaCursor.moveToFirst()) {
                        fileName = metaCursor.getString(0);
                    }
                } catch (Exception ignore) {

                } finally {
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
}
