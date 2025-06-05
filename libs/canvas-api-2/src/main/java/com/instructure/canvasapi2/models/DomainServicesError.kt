package com.instructure.canvasapi2.models

data class DomainServicesError(
    val errors: List<Error>,
    val data: Any?,
)

data class Error(
    val message: String,
    val locations: List<Location>,
    val path: List<String>,
    val extensions: Extensions,
)

data class Location(
    val line: Long,
    val column: Long,
)

data class Extensions(
    val code: String,
    val originalError: OriginalError,
)

data class OriginalError(
    val message: String,
    val statusCode: Long,
)
