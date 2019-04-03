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


package sheets

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.HttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import com.google.common.base.Charsets
import com.google.common.io.Files
import java.io.File
import java.io.IOException

// Code from: https://developers.google.com/sheets/api/quickstart/java
object SheetsV4 {

    /**
     * Home directory for the current user
     */
    private val HOME = System.getProperty("user.home")

    /**
     * Global instance of the JSON factory.
     */
    private val JSON_FACTORY = JacksonFactory.getDefaultInstance()

    /**
     * Global instance of the HTTP transport.
     */
    private var HTTP_TRANSPORT: HttpTransport? = null

    /**
     * Global instance of the [FileDataStoreFactory].
     */
    private var DATA_STORE_FACTORY: FileDataStoreFactory? = null

    init {
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport()
            DATA_STORE_FACTORY = FileDataStoreFactory(File(
                    HOME, ".credentials/sheets.googleapis.com-cloud-build-metrics"))
        } catch (t: Throwable) {
            t.printStackTrace()
            System.exit(1)
        }

    }

    /**
     * Build and return an authorized Sheets API client service.
     *
     * @return an authorized Sheets API client service
     * @throws IOException
     */
    val sheetsService: Sheets
        @Throws(IOException::class)
        get() {
            val credential = authorize()
            return Sheets.Builder(HTTP_TRANSPORT!!, JSON_FACTORY, credential)
                    .setApplicationName("Cloud Build Metrics")
                    .build()
        }

    /**
     * Creates an authorized Credential object.
     *
     * @return an authorized Credential object.
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun authorize(): Credential {
        // Load client secrets.
        val clientSecretsReader = Files.newReader(File(HOME, ".config/google/client_secrets.json"), Charsets.UTF_8)

        val clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, clientSecretsReader)
        clientSecretsReader.close()

        // Build flow and trigger user authorization request.
        val flow = GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, listOf(SheetsScopes.SPREADSHEETS))
                .setDataStoreFactory(DATA_STORE_FACTORY!!)
                .setAccessType("offline")
                .build()

        // Save credential to DATA_STORE_DIR
        return AuthorizationCodeInstalledApp(
                flow, LocalServerReceiver()).authorize("default")
    }
}
