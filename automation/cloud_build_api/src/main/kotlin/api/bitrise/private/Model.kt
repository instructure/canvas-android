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


package api.bitrise.private

//  {
//    "user": {
//      "login": "example@example.com",
//      "password": "123",
//      "remember_me": 1
//    }
//  }
data class Credential(
        val login: String,
        val password: String,
        val remember_me: Int = 1
)

data class User(
        val user: Credential
)

// {
//    "pr": true,
//    "push": true,
//    "running": true
// }
//
// may also return:
//  {
//    "error_msg": "Rolling builds addon not connected."
//  }
data class RollingBuildsConfig(
        val pr: Boolean,
        val push: Boolean,
        val running: Boolean
)

//  {
//    "build_type": "pr",
//    "should_enable": true
//  }

data class RollingBuildsPatch(
        val build_type: String,
        val should_enable: Boolean
)

data class Status(
        val status: String?,
        val message: String?
)

// [{"the_cache_item_key":"master","file_size_bytes":3072,"created_at":"2018-04-18T16:37:54.673Z","updated_at":"2018-04-18T16:37:54.673Z"}]

data class CacheItem(
        val the_cache_item_key: String,
        val file_size_bytes: Long,
        val created_at: String,
        val updated_at: String
)
