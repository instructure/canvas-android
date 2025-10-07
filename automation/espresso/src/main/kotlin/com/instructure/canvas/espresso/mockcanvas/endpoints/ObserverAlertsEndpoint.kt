/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
 */    package com.instructure.canvas.espresso.mockcanvas.endpoints

import com.instructure.canvas.espresso.mockcanvas.Endpoint
import com.instructure.canvas.espresso.mockcanvas.utils.LongId
import com.instructure.canvas.espresso.mockcanvas.utils.PathVars
import com.instructure.canvas.espresso.mockcanvas.utils.StringId
import com.instructure.canvas.espresso.mockcanvas.utils.successResponse
import com.instructure.canvas.espresso.mockcanvas.utils.unauthorizedResponse
import com.instructure.canvasapi2.models.Alert
import com.instructure.canvasapi2.models.AlertWorkflowState

object ObserverAlertsEndpoint : Endpoint(
    LongId(PathVars::studentId) to Endpoint(
        StringId(PathVars::workflowState) to Endpoint(
            response = {
                PUT {
                    var updatedAlert: Alert? = null
                    data.observerAlerts = data.observerAlerts.mapValues { (_, alerts) ->
                        alerts.map { alert ->
                            if (alert.id == pathVars.studentId) {
                                updatedAlert = alert.copy(workflowState = AlertWorkflowState.valueOf(pathVars.workflowState.uppercase()))
                                updatedAlert!!
                            } else {
                                alert
                            }
                        }
                    }.toMutableMap()
                    updatedAlert?.let {
                        request.successResponse(it)
                    } ?: request.unauthorizedResponse()
                }
            }
        ),
        response = {
            GET {
                val alerts = data.observerAlerts[pathVars.studentId] ?: emptyList()
                val filtered = alerts.filter { listOf(AlertWorkflowState.READ, AlertWorkflowState.UNREAD).contains(it.workflowState) }
                request.successResponse(filtered)
            }
        }
    )
)