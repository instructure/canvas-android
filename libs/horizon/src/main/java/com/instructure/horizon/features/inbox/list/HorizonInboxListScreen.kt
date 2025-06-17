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
package com.instructure.horizon.features.inbox.list

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.instructure.horizon.features.inbox.navigation.HorizonInboxRoute
import com.instructure.horizon.horizonui.molecules.Button

@Composable
fun HorizonInboxListScreen(navController: NavHostController) {
    Column {
        Text("Inbox List")

        Button(
            "Details",
            onClick = {
                navController.navigate(HorizonInboxRoute.InboxDetails.route(5))
            }
        )

        Button(
            "Compose",
            onClick = {
                navController.navigate(HorizonInboxRoute.InboxCompose.route)
            }
        )
    }
}