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

package com.instructure.canvasapi2.utils;

import android.content.Context;
import android.webkit.MimeTypeMap;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;


public class FileUtils {

    public final static String FILE_SUFFIX = ".serializable";
    public final static String FILE_DIRECTORY = "cache";

    /**
     * Converts a serializable object to the specified file.
     *
     * @param context
     * @param cacheFileName
     * @param serializable
     * @return
     */
    public static boolean SerializableToFile(Context context, String cacheFileName, Serializable serializable) {
        if (context == null || cacheFileName == null || serializable == null) {
            return false;
        }
        try {
            cacheFileName += FILE_SUFFIX;

            File f = new File(context.getFilesDir(), FILE_DIRECTORY);
            File file = new File(f, cacheFileName);

            file.getParentFile().mkdirs();
            file.createNewFile();


            //Write to file.
            OutputStream outputStream = new FileOutputStream(file);
            OutputStream buffer = new BufferedOutputStream(outputStream);
            ObjectOutput output = new ObjectOutputStream(buffer);

            output.writeObject(serializable);

            output.flush();
            output.close();
            return true;
        } catch (Exception E) {
            return false;
        }
    }

    /**
     * Deletes the cache file with the given name
     *
     * @param context
     * @param cacheFileName
     * @return
     */
    public static boolean DeleteFile(Context context, String cacheFileName) {
        if (context == null || cacheFileName == null) {
            return false;
        }

        try {
            cacheFileName += FILE_SUFFIX;

            //use buffering
            File f = new File(context.getFilesDir(), FILE_DIRECTORY);
            f.mkdirs();
            File file = new File(f, cacheFileName);

            return file.delete();
        } catch (Exception E) {
            return false;
        }
    }

    /**
     * Converts a specified file to a serializable object.
     *
     * @param context
     * @param cacheFileName
     * @return
     */
    public static Serializable FileToSerializable(Context context, String cacheFileName) {
        try {
            cacheFileName += FILE_SUFFIX;

            //use buffering
            File f = new File(context.getFilesDir(), FILE_DIRECTORY);
            f.mkdirs();
            File file = new File(f, cacheFileName);

            InputStream fileInputStream = new FileInputStream(file);
            InputStream buffer = new BufferedInputStream(fileInputStream);
            ObjectInput input = new ObjectInputStream(buffer);
            try {
                //deserialize
                return (Serializable) input.readObject();
            } finally {
                input.close();
            }
        } catch (Exception E) {
            return null;
        }
    }


    /**
     * deleteAllFilesInDirectory will RECURSIVELY delete all files/folders in a directory
     *
     * @param startFile
     * @return
     */
    public static boolean deleteAllFilesInDirectory(File startFile) {
        try {
            //If it's a directory.
            if (startFile.isDirectory()) {
                //Delete all files inside of it.
                String[] files = startFile.list();
                for (String fileName : files) {
                    File file = new File(startFile, fileName);
                    //If it's a directory. recursive.
                    if (file.isDirectory()) {
                        deleteAllFilesInDirectory(file);
                    }
                    //It's a file. Delete it.
                    else {
                        file.delete();
                    }
                }
                //Now delete the parent folder.
                startFile.delete();
            }
            //If it's not a directory, delete the file.
            else {
                startFile.delete();
            }
            return true;
        } catch (Exception E) {
            return false;
        }
    }


    /**
     * getFileExtensionFromMimetype returns what's after the /.
     * For example : image/png returns png.
     *
     * @param mimetype
     * @return
     */
    public static String getFileExtensionFromMimetype(String mimetype) {
        if (mimetype == null) {
            return "";
        } else {
            String[] split = mimetype.split("/");
            return split[split.length - 1];
        }
    }

    public static String getMimeType(String url) {
        String type = "";
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            type = mime.getMimeTypeFromExtension(extension);
            if (type == null) {
                type = "";
            }
        }
        return type;
    }

    public static String notoriousCodeFromMimeType(String mimetype) {
        if (mimetype == null) {
            return "0";
        } else {
            String[] split = mimetype.split("/");
            if (split[0].equals("video")) {
                return "1";
            } else if (split[0].equals("audio")) {
                return "5";
            } else {
                return "0";
            }
        }
    }

    public static String mediaTypeFromNotoriousCode(long notoriousCode) {
        if (notoriousCode == 1) {
            return "video";
        } else if (notoriousCode == 5) {
            return "audio";
        } else {
            return "";
        }
    }

    /**
     * GetAssetsFile allows you to open a file that exists in the Assets directory.
     *
     * @param context An Android Context
     * @param fileName The name of the asset file
     * @return the contents of the file as a String
     */
    public static String getAssetsFile(Context context, String fileName) {
        try {
            String file = "";
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(context.getAssets().open(fileName)));

            // do reading
            String line = "";
            while (line != null) {
                file += line;
                line = reader.readLine();
            }

            reader.close();
            return file;

        } catch (Exception e) {
            return "";
        }
    }
}
