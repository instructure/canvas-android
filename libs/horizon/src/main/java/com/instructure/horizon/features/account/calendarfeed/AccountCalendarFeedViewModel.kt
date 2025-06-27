/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
 *
 */
package com.instructure.horizon.features.account.calendarfeed

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.CalendarContract
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.horizon.R
import com.instructure.horizon.horizonui.platform.LoadingState
import com.instructure.pandautils.utils.Const
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import javax.inject.Inject


@HiltViewModel
class AccountCalendarFeedViewModel @Inject constructor(
    private val repository: AccountCalendarFeedRepository,
    private val apiPrefs: ApiPrefs,
    @ApplicationContext private val context: Context
): ViewModel() {
    private val _uiState = MutableStateFlow(AccountCalendarUiState(
        screenState = LoadingState(isPullToRefreshEnabled = false, onSnackbarDismiss = ::dismissSnackBar),
        showSnackBar = ::showSnackBar
    ))
    val uiState = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun showSnackBar(message: String) {
        _uiState.update {
            it.copy(screenState = it.screenState.copy(snackbarMessage = message))
        }
    }

    private fun dismissSnackBar() {
        _uiState.update {
            it.copy(screenState = it.screenState.copy(snackbarMessage = null))
        }
    }

    private fun loadData() {
        viewModelScope.tryLaunch {
            _uiState.update {
                it.copy(screenState = it.screenState.copy(isLoading = true))
            }

            val user = repository.getUser(apiPrefs.user!!.id)
            val icsFile = downloadIcsToCache(context, user.calendar!!.ics)!!
            val icsUri = FileProvider.getUriForFile(context, context.applicationContext.packageName + Const.FILE_PROVIDER_AUTHORITY, icsFile)
            _uiState.update {
                it.copy(
                    calendarUrl = user.calendar!!.ics,
                    localCalendarUri = icsUri,
                    calendarOptions = getCalendarApps(icsUri),
                )
            }
            _uiState.update {
                it.copy(screenState = it.screenState.copy(isLoading = false))
            }
        } catch {
            _uiState.update {
                it.copy(screenState = it.screenState.copy(isLoading = false, isError = true, errorMessage = context.getString(
                    R.string.accountCalendarFeedErrorMessage
                )))
            }
        }
    }

    private suspend fun downloadIcsToCache(context: Context, url: String): File? {
        return withContext(Dispatchers.IO) {
            try {
                val inputStream = URL(url).openStream()
                val file = File(context.cacheDir, "event.ics")

                FileOutputStream(file).use { output ->
                    inputStream.copyTo(output)
                }

                file
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    private fun getCalendarApps(icsUri: Uri): List<AccountCalendarOption> {
        val intent = Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
        }

        val packageManager = context.packageManager
        return packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY).map {
            AccountCalendarOption(
                name = it.loadLabel(packageManager).toString(),
                icon = it.loadIcon(context.packageManager),
                intent = Intent(Intent.ACTION_VIEW).apply {
                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    `package` = it.activityInfo.packageName
                    setDataAndType(icsUri, "text/calendar")
                }
            )
        }
    }
}