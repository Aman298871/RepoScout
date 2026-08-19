package com.example.reposcout.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GitHubOwnerDto(
    @Json(name = "id") val id: Long,
    @Json(name = "login") val login: String,
    @Json(name = "avatar_url") val avatarUrl: String?,
    @Json(name = "html_url") val htmlUrl: String?
)

@JsonClass(generateAdapter = true)
data class GitHubLicenseDto(
    @Json(name = "key") val key: String,
    @Json(name = "name") val name: String,
    @Json(name = "spdx_id") val spdxId: String?
)

@JsonClass(generateAdapter = true)
data class GitHubRepositoryDto(
    @Json(name = "id") val id: Long,
    @Json(name = "name") val name: String,
    @Json(name = "full_name") val fullName: String?,
    @Json(name = "owner") val owner: GitHubOwnerDto?,
    @Json(name = "description") val description: String?,
    @Json(name = "html_url") val htmlUrl: String?,
    @Json(name = "language") val language: String?,
    @Json(name = "stargazers_count") val stargazersCount: Int?,
    @Json(name = "forks_count") val forksCount: Int?,
    @Json(name = "watchers_count") val watchersCount: Int?,
    @Json(name = "open_issues_count") val openIssuesCount: Int?,
    @Json(name = "license") val license: GitHubLicenseDto?,
    @Json(name = "created_at") val createdAt: String?,
    @Json(name = "updated_at") val updatedAt: String?
)

@JsonClass(generateAdapter = true)
data class GitHubSearchResponseDto(
    @Json(name = "total_count") val totalCount: Int,
    @Json(name = "incomplete_results") val incompleteResults: Boolean,
    @Json(name = "items") val items: List<GitHubRepositoryDto>
)
