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
package com.instructure.canvasapi2.utils

//right now this is expecting a url in the format of:
//"/1/sessions/eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJjIjoxNDk1NzM1ODM2MDU2LCJkIjoicV9vdlBnTnpxVVh0MTY3UVJjUDFyYVhwMlJydEpPIiwiZSI6MTQ5NTczOTQzNiwiciI6InBkZmpzIiwiYSI6eyJjIjoiZGVmYXVsdCIsInAiOiJyZWFkd3JpdGUiLCJ1IjoiMTAwMDAwMDU4MTQ3ODkiLCJuIjoiVHJldm9yIiwiciI6IiJ9LCJpYXQiOjE0OTU3MzU4MzZ9.IKn4kV-mrseE4INa8niX8A6rMxWS9f798bFeWtUkFIA/file/file.pdf"
fun extractSessionId(url: String) = url.substringAfter("sessions/", "").substringBefore('/', "")

fun extractCanvaDocsDomain(url: String) = url.substringBefore("/1/sessions")
