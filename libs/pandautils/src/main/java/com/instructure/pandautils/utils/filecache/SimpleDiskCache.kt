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
package com.instructure.pandautils.utils.filecache

import com.instructure.canvasapi2.utils.tryOrNull
import com.jakewharton.disklrucache.DiskLruCache
import java.io.*
import java.math.BigInteger
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*

/**
 * Adapted from https://github.com/fhucho/simple-disk-cache
 * License Apache 2.0
 */
class SimpleDiskCache private constructor(dir: File, private val mAppVersion: Int, maxSize: Long) {
    var cache: DiskLruCache
        private set

    init {
        cache = DiskLruCache.open(dir, mAppVersion, 2, maxSize)
    }

    /**
     * User should be sure there are no outstanding operations.
     * @throws IOException
     */
    @Throws(IOException::class)
    fun clear() {
        val dir = cache.directory
        val maxSize = cache.maxSize
        cache.delete()
        cache = DiskLruCache.open(dir, mAppVersion, 2, maxSize)
    }

    /**
     * Attempts to retrieve an existing cached file associated with the given key
     * @param rawKey The key used to cache the file, such as the originating url
     * @return The cached file, or null if it does not exist
     * @throws IOException
     */
    @Throws(IOException::class)
    fun getFile(rawKey: String): File? {
        val key = toInternalKey(rawKey)
        cache[key] ?: return null
        val file = File(cache.directory, "$key.0")
        return if (file.exists() && file.isFile) file else null
    }

    @Throws(IOException::class)
    operator fun contains(key: String): Boolean {
        val snapshot = cache[toInternalKey(key)] ?: return false
        snapshot.close()
        return true
    }

    @Throws(IOException::class)
    fun openStream(key: String, metadata: Map<String, Serializable>): OutputStream {
        val editor = cache.edit(toInternalKey(key))
        return try {
            writeMetadata(metadata, editor)
            val bos = BufferedOutputStream(editor.newOutputStream(VALUE_IDX))
            CacheOutputStream(bos, editor)
        } catch (e: IOException) {
            editor.abort()
            throw e
        }
    }

    @JvmOverloads
    @Throws(IOException::class)
    fun put(key: String, inputStream: InputStream?, annotations: Map<String, Serializable> = HashMap()) {
        openStream(key, annotations).use { os -> inputStream?.copyTo(os) }
    }

    @Throws(IOException::class)
    private fun writeMetadata(metadata: Map<String, Serializable>, editor: DiskLruCache.Editor) {
        var oos: ObjectOutputStream? = null
        try {
            oos = ObjectOutputStream(BufferedOutputStream(editor.newOutputStream(METADATA_IDX)))
            oos.writeObject(metadata)
        } finally {
            tryOrNull { oos?.close() }
        }
    }

    private fun toInternalKey(key: String): String {
        return md5(key)
    }

    private fun md5(s: String): String {
        return try {
            val m = MessageDigest.getInstance("MD5")
            m.update(s.toByteArray(StandardCharsets.UTF_8))
            val digest = m.digest()
            val bigInt = BigInteger(1, digest)
            bigInt.toString(16)
        } catch (e: NoSuchAlgorithmException) {
            throw AssertionError()
        }
    }

    private class CacheOutputStream(os: OutputStream, val editor: DiskLruCache.Editor) : FilterOutputStream(os) {
        private var failed = false

        @Throws(IOException::class)
        override fun close() {
            var closeException: IOException? = null
            try {
                super.close()
            } catch (e: IOException) {
                closeException = e
            }
            if (failed) {
                editor.abort()
            } else {
                editor.commit()
            }
            if (closeException != null) throw closeException
        }

        @Throws(IOException::class)
        override fun flush() {
            try {
                super.flush()
            } catch (e: IOException) {
                failed = true
                throw e
            }
        }

        @Throws(IOException::class)
        override fun write(oneByte: Int) {
            try {
                super.write(oneByte)
            } catch (e: IOException) {
                failed = true
                throw e
            }
        }

        @Throws(IOException::class)
        override fun write(buffer: ByteArray) {
            try {
                super.write(buffer)
            } catch (e: IOException) {
                failed = true
                throw e
            }
        }

        @Throws(IOException::class)
        override fun write(buffer: ByteArray, offset: Int, length: Int) {
            try {
                super.write(buffer, offset, length)
            } catch (e: IOException) {
                failed = true
                throw e
            }
        }

    }

    companion object {
        private const val VALUE_IDX = 0
        private const val METADATA_IDX = 1
        private val usedDirs: MutableList<File> = ArrayList()

        @Synchronized
        @Throws(IOException::class)
        fun open(dir: File, appVersion: Int, maxSize: Long): SimpleDiskCache {
            check(!usedDirs.contains(dir)) { "Cache dir " + dir.absolutePath + " was used before." }
            usedDirs.add(dir)
            return SimpleDiskCache(dir, appVersion, maxSize)
        }
    }
}
