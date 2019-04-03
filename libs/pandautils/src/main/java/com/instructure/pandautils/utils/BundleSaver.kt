/*
 * Copyright (C) 2018 - present Instructure, Inc.
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
 */
package com.instructure.pandautils.utils

import android.os.Bundle
import android.os.Parcel
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.canvasapi2.utils.Logger
import java.io.File
import java.util.*

object BundleSaver {

    private const val UUID_KEY = "saved_bundle_uuid"

    private val storageDir by lazy {
        val dir = File(ContextKeeper.appContext.cacheDir, "tmp-bundles").apply { mkdirs() }
        val ageThreshold = System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000)
        dir.listFiles()?.forEach {
            try {
                if (it.lastModified() < ageThreshold) it.delete()
            } catch (e: Throwable) {
                Logger.w("Unable to delete bundle file at ${it.absolutePath}")
            }
        }
        dir
    }

    fun saveBundleToDisk(bundle: Bundle?) {
        if (bundle == null) return
        try {
            val uuid = UUID.randomUUID().toString()
            Parcel.obtain().apply {
                writeBundle(bundle)
                File(storageDir, uuid).writeBytes(marshall())
                recycle()
            }
            bundle.clear()
            bundle.putString(UUID_KEY, uuid)
        } catch (e: Throwable) {
            Logger.w("Error saving bundle to disk")
            Logger.logBundle(bundle)
        }
    }

    fun restoreBundleFromDisk(bundle: Bundle?) {
        val uuid: String = bundle?.getString(UUID_KEY) ?: return
        val bundleFile = File(storageDir, uuid).takeUnless { !it.exists() || !it.isFile } ?: return
        try {
            val bytes =  bundleFile.readBytes()
            Parcel.obtain().apply {
                unmarshall(bytes, 0, bytes.size)
                setDataPosition(0)
                bundle.putAll(readBundle(BundleSaver::class.java.classLoader))
                recycle()
            }
            bundleFile.delete()
        } catch (e: Exception) {
            Logger.w("Unable to restore bundle from disk")
            Logger.logBundle(bundle)
        }
    }

}
