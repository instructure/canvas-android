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

package com.instructure.annotations.FileCaching;


import com.jakewharton.disklrucache.DiskLruCache;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Adapted from https://github.com/fhucho/simple-disk-cache
 * License Apache 2.0
 */
public class SimpleDiskCache {

    private static final int VALUE_IDX = 0;
    private static final int METADATA_IDX = 1;
    private static final List<File> usedDirs = new ArrayList<>();

    private DiskLruCache diskLruCache;
    private int mAppVersion;

    private SimpleDiskCache(File dir, int appVersion, long maxSize) throws IOException {
        mAppVersion = appVersion;
        diskLruCache = DiskLruCache.open(dir, appVersion, 2, maxSize);
    }

    public static synchronized SimpleDiskCache open(File dir, int appVersion, long maxSize)
            throws IOException {
        if (usedDirs.contains(dir)) {
            throw new IllegalStateException("Cache dir " + dir.getAbsolutePath() + " was used before.");
        }

        usedDirs.add(dir);

        return new SimpleDiskCache(dir, appVersion, maxSize);
    }

    /**
     * User should be sure there are no outstanding operations.
     * @throws IOException
     */
    public void clear() throws IOException {
        File dir = diskLruCache.getDirectory();
        long maxSize = diskLruCache.getMaxSize();
        diskLruCache.delete();
        diskLruCache = DiskLruCache.open(dir, mAppVersion, 2, maxSize);
    }

    public DiskLruCache getCache() {
        return diskLruCache;
    }

    public InputStreamEntry getInputStream(String key) throws IOException {
        DiskLruCache.Snapshot snapshot = diskLruCache.get(toInternalKey(key));
        if (snapshot == null) return null;
        return new InputStreamEntry(snapshot, readMetadata(snapshot));
    }

    /**
     * Attempts to retrieve an existing cached file associated with the given key
     * @param rawKey The key used to cache the file, such as the originating url
     * @return The cached file, or null if it does not exist
     * @throws IOException
     */
    public File getFile(String rawKey) throws IOException {
        String key = toInternalKey(rawKey);
        DiskLruCache.Snapshot snapshot = diskLruCache.get(key);
        if (snapshot == null) return null;
        File file = new File(diskLruCache.getDirectory(), key + ".0");
        return file.exists() && file.isFile() ? file : null;
    }

    public boolean contains(String key) throws IOException {
        DiskLruCache.Snapshot snapshot = diskLruCache.get(toInternalKey(key));
        if(snapshot==null) return false;

        snapshot.close();
        return true;
    }

    public OutputStream openStream(String key, Map<String, ? extends Serializable> metadata)
            throws IOException {
        DiskLruCache.Editor editor = diskLruCache.edit(toInternalKey(key));
        try {
            writeMetadata(metadata, editor);
            BufferedOutputStream bos = new BufferedOutputStream(editor.newOutputStream(VALUE_IDX));
            return new CacheOutputStream(bos, editor);
        } catch (IOException e) {
            editor.abort();
            throw e;
        }
    }

    public void put(String key, InputStream is) throws IOException {
        put(key, is, new HashMap<String, Serializable>());
    }

    public void put(String key, InputStream is, Map<String, Serializable> annotations)
            throws IOException {
        OutputStream os = null;
        try {
            os = openStream(key, annotations);
            IOUtil.copy(is, os);
        } finally {
            if (os != null) os.close();
        }
    }

    private void writeMetadata(Map<String, ? extends Serializable> metadata,
                               DiskLruCache.Editor editor) throws IOException {
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(new BufferedOutputStream(
                    editor.newOutputStream(METADATA_IDX)));
            oos.writeObject(metadata);
        } finally {
            IOUtil.closeStream(oos);
        }
    }

    private Map<String, Serializable> readMetadata(DiskLruCache.Snapshot snapshot)
            throws IOException {
        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(new BufferedInputStream(
                    snapshot.getInputStream(METADATA_IDX)));
            @SuppressWarnings("unchecked")
            Map<String, Serializable> annotations = (Map<String, Serializable>) ois.readObject();
            return annotations;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } finally {
            IOUtil.closeStream(ois);
        }
    }

    public String toInternalKey(String key) {
        return md5(key);
    }

    private String md5(String s) {
        try {
            MessageDigest m = MessageDigest.getInstance("MD5");
            m.update(s.getBytes("UTF-8"));
            byte[] digest = m.digest();
            BigInteger bigInt = new BigInteger(1, digest);
            return bigInt.toString(16);
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError();
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError();
        }
    }

    private class CacheOutputStream extends FilterOutputStream {

        private final DiskLruCache.Editor editor;
        private boolean failed = false;

        private CacheOutputStream(OutputStream os, DiskLruCache.Editor editor) {
            super(os);
            this.editor = editor;
        }

        @Override
        public void close() throws IOException {
            IOException closeException = null;
            try {
                super.close();
            } catch (IOException e) {
                closeException = e;
            }

            if (failed) {
                editor.abort();
            } else {
                editor.commit();
            }

            if (closeException != null) throw closeException;
        }

        @Override
        public void flush() throws IOException {
            try {
                super.flush();
            } catch (IOException e) {
                failed = true;
                throw e;
            }
        }

        @Override
        public void write(int oneByte) throws IOException {
            try {
                super.write(oneByte);
            } catch (IOException e) {
                failed = true;
                throw e;
            }
        }

        @Override
        public void write(byte[] buffer) throws IOException {
            try {
                super.write(buffer);
            } catch (IOException e) {
                failed = true;
                throw e;
            }
        }

        @Override
        public void write(byte[] buffer, int offset, int length) throws IOException {
            try {
                super.write(buffer, offset, length);
            } catch (IOException e) {
                failed = true;
                throw e;
            }
        }
    }

    public static class InputStreamEntry {
        private final DiskLruCache.Snapshot snapshot;
        private final Map<String, Serializable> metadata;

        public InputStreamEntry(DiskLruCache.Snapshot snapshot, Map<String, Serializable> metadata) {
            this.metadata = metadata;
            this.snapshot = snapshot;
        }

        public InputStream getInputStream() {
            return snapshot.getInputStream(VALUE_IDX);
        }

        public Map<String, Serializable> getMetadata() {
            return metadata;
        }

        public void close() {
            snapshot.close();

        }

    }
}