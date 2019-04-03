//
// Copyright (C) 2018-present Instructure, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//



package support.upload.apks

import com.google.cloud.storage.*
import java.io.File
import java.nio.file.Paths
import java.security.MessageDigest
import javax.xml.bind.DatatypeConverter

/**
 * Google Cloud Storage App for uploading Android APKs for Espresso testing.
 */
object CloudStorageApp {

    private val storage = StorageOptions.getDefaultInstance().service
    private val bucketLabel: Map<String, String> = mapOf(Pair("espresso", "apks"))
    private const val bucketName = "espresso-teacher"
    private const val storageLocation = "us-central1"
    private const val daysToLive = 30
    private val teacherBuildOutputsPath = File("../teacher/app/build/outputs/apk")

    /**
     * Uploads Teacher App qa/debug App Apk and androidTest Apk to GCS bucket.
     */
    fun uploadApks(): List<Apk> {
        val bucket = espressoBucket()
        val appApk = readFileBytes("qa/debug/app-qa-debug.apk")
        val testApk = readFileBytes("androidTest/qa/debug/app-qa-debug-androidTest.apk")

        bucket.upload(appApk)
        bucket.upload(testApk)

        return listOf(Apk("TEACHER_APP_APK", appApk.md5()), Apk("TEACHER_TEST_APK", testApk.md5()))
    }

    private fun readFileBytes(path: String): ByteArray {
        val file = Paths.get(teacherBuildOutputsPath.toString(), path).toFile()
        if (!file.exists()) throw RuntimeException("Apk $path Not Found!")
        return file.readBytes()
    }

    /**
     * Returns the storage bucket if it exists, otherwise
     * Creates and returns new storage bucket.
     */
    private fun espressoBucket(): Bucket {
        val bucket = storage.list().values?.find { it.name == bucketName }
        if (bucket != null) return bucket

        val deleteRule = BucketInfo.LifecycleRule(
                BucketInfo.LifecycleRule.LifecycleAction.newDeleteAction(),
                BucketInfo.LifecycleRule.LifecycleCondition.newBuilder().setAge(daysToLive).build())

        // Information on storage classes and locations:
        // https://cloud.google.com/storage/docs/storage-classes
        // https://cloud.google.com/storage/docs/bucket-locations#location-mr
        return storage.create(BucketInfo.newBuilder(bucketName)
                .setStorageClass(StorageClass.REGIONAL)
                .setLocation(storageLocation)
                .setLabels(bucketLabel)
                .setLifecycleRules(listOf(deleteRule))
                .build())
    }
}

/**
 * Provides a fully qualified blob path to be used for downloading.
 * Path can be used as <src_url> with "gsutil cp <src_url> <dst_url>.
 * https://cloud.google.com/storage/docs/gsutil/commands/cp
 *
 * Example:
 *     "gsutil cp gs://espresso-teacher/app-qa-debug.apk ."
 */
val Blob.path: String
    get() = "gs://${this.blobId.bucket}/${this.blobId.name}"

fun Bucket.missing(fileName: String): Boolean =
        this.get(fileName) == null

fun Bucket.upload(fileBytes: ByteArray) {
    val fileName = fileBytes.md5()
    if (this.missing(fileName)) {
        this.create(fileName, fileBytes)
    }
}

fun ByteArray.md5(): String {
    val md5 = MessageDigest.getInstance("MD5")
    md5.update(this)
    val md5Hex = DatatypeConverter.printHexBinary(md5.digest())
    return "$md5Hex.apk"
}

