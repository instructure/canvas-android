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


package api.bitrise

import normal.ToNormalApp
import util.getEnv

/*
{
    "data": [
        "slug": "4c2f997f51ab7b62",
        "title": "Test Job for Cloud Build Metrics",
        "project_type": "other",
        "provider": "github",
        "repo_owner": "TestArmada",
        "repo_url": "https://github.com/TestArmada/flank.git",
        "repo_slug": "flank",
        "is_disabled": false,
        "status": 1,
        "is_public": false,
        "owner": {
            "account_type": "organization",
            "name": "Instructure",
            "slug": "324bc32b776abdbf"
        }
    },
    ],
    "paging": {
        "total_item_count": 44,
        "page_item_limit": 50
    }
}
*/
data class Owner(
        val account_type: String,
        val name: String,
        val slug: String
)

data class BitriseAppObject(
        val slug: String,
        val title: String,
        val project_type: String,
        val provider: String,
        val repo_owner: String,
        val repo_url: String,
        val repo_slug: String,
        val is_disabled: Boolean,
        val status: Int,
        val is_public: Boolean,
        val owner: Owner
) : ToNormalApp

data class BitrisePaging(
        val total_item_count: Int,
        val page_item_limit: Int,
        val next: String?
)

data class BitriseAppData(
        val data: List<BitriseAppObject>,
        val paging: BitrisePaging
)

//    {
//        "status": "ok",
//        "message": "webhook processed",
//        "slug": "6ec29cf3b6e62901",
//        "service": "bitrise",
//        "build_slug": "9864dce889fad02d",
//        "build_number": 26,
//        "build_url": "https://www.bitrise.io/build/9ca4bce889fad02d",
//        "triggered_workflow": "x86_emulator"
//    }
data class BitriseTriggerBuildResponse(
        val status: String,
        val message: String,
        val slug: String,
        val service: String,
        val build_slug: String,
        val build_number: Int,
        val build_url: String,
        val triggered_workflow: String
)

//    {
//        "hook_info": {
//          "type": "bitrise",
//          "api_token": "123"
//        },
//        "build_params": {
//          "branch": "master",
//          "workflow_id": "x86_emulator"
//        },
//        "triggered_by": "curl"
//    }

data class HookInfo(
        val type: String = "bitrise",
        val api_token: String = getEnv("BITRISE_TOKEN")
)

data class Environment(
        val mapped_to: String,
        val value: String,
        val is_expand: Boolean = true
)

data class BuildParams(
        val branch: String = "master",
        val workflow_id: String? = null,
        val environments: List<Environment>? = null
)

data class BitriseTriggerBuildRequest(
        val hook_info: HookInfo = HookInfo(),
        val build_params: BuildParams,
        val triggered_by: String = "cloud_build_metrics"
)

/*
{
    "data": [
        {
            "triggered_at": "2017-10-11T20:25:44Z",
            "started_on_worker_at": "2017-10-11T20:25:44Z",
            "environment_prepare_finished_at": "2017-10-11T20:27:13Z",
            "finished_at": "2017-10-11T20:43:11Z",
            "slug": "492b3b077d88a643",
            "status": 1,
            "status_text": "success",
            "abort_reason": null,
            "is_on_hold": false,
            "branch": "migrate-MBL-8890",
            "build_number": 2741,
            "commit_hash": "170da8a73f88e60e2e45bb38ecce6f76696c7693",
            "commit_message": "hello",
            "tag": null,
            "triggered_workflow": "debug",
            "triggered_by": "webhook",
            "stack_config_type": "elite1",
            "stack_identifier": "linux-docker-android",
            "original_build_params": {
                "commit_hash": "170da8a73f88e60e2e45bb38ecce6f76696c7693",
                "commit_message": "hello",
                "branch": "migrate_hello",
                "branch_dest": "migrate-models",
                "pull_request_id": 904,
                "pull_request_repository_url": "git@github.com:instructure/example.git",
                "pull_request_merge_branch": "pull/904/merge",
                "pull_request_head_branch": "pull/904/head"
            },
            "pull_request_id": 904,
            "pull_request_target_branch": "migrate-models",
            "pull_request_view_url": "https://github.com/instructure/example/pull/904",
            "commit_view_url": "https://github.com/instructure/example/commit/170da8a73f88e60e2e45bb38ecce6f76696c7693"
        },
    ],
    "paging": {
        "total_item_count": 2741,
        "page_item_limit": 50,
        "next": "8e05b92b818a982f"
    }

*/

data class BitriseBuildData(
        val data: List<BitriseBuildObject>,
        val paging: BitrisePaging
)

data class BitriseSingleBuildData(
        val data: BitriseBuildObject
)

data class BitriseBuildObject(
        val triggered_at: String,
        val started_on_worker_at: String,
        val environment_prepare_finished_at: String?,
        val finished_at: String?,
        val slug: String,
        val status: Int,
        val status_text: String,
        val abort_reason: String,
        val is_on_hold: Boolean,
        val branch: String,
        val build_number: Int,
        val commit_hash: String,
        val commit_message: String,
        val tag: String,
        val triggered_workflow: String,
        val triggered_by: String,
        val stack_config_type: String,
        val stack_identifier: String,
        val original_build_params: BitriseOriginalBuildParams,
        val pull_request_id: Int,
        val pull_request_target_branch: String,
        val pull_request_view_url: String,
        val commit_view_url: String
)

data class BitriseOriginalBuildParams(
        val commit_hash: String,
        val commit_message: String,
        val branch: String,
        val branch_dest: String,
        val pull_request_id: Int,
        val pull_request_repository_url: String,
        val pull_request_merge_branch: String,
        val pull_request_head_branch: String
)

// build log

//{
//    "generated_log_chunks_num": 41,
//    "is_archived": true,
//    "timestamp": "2017-11-29T18:27:44.094+00:00",
//    "log_chunks": [
//    {
//        {
//            "position": 38,
//            "chunk": ""
//        },
//        {
//            "position": 39,
//            "chunk": ""
//        },
//        {
//            "position": 40,
//            "chunk": ""
//        }
//        ],
//        "expiring_raw_log_url": "url"
//    }

data class LogChunk(
        val position: Int,
        val chunk: String
)

data class BitriseLogData(
        val generated_log_chunks_num: Int,
        val is_archived: Boolean,
        val timestamp: String,
        val log_chunks: List<LogChunk>,
        val expiring_raw_log_url: String? // randomly null
)

data class YamlConfig(
        val app_config_datastore_yaml: String
)
