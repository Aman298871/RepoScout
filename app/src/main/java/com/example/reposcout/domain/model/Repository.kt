package com.example.reposcout.domain.model

data class Owner(
    val id: Long,
    val login: String,
    val avatarUrl: String,
    val htmlUrl: String
)

data class License(
    val key: String,
    val name: String,
    val spdxId: String? = null
)

data class Repository(
    val id: Long,
    val name: String,
    val fullName: String,
    val owner: Owner,
    val description: String?,
    val htmlUrl: String,
    val language: String?,
    val stargazersCount: Int,
    val forksCount: Int,
    val watchersCount: Int,
    val openIssuesCount: Int,
    val license: License?,
    val createdAt: String?,
    val updatedAt: String?,
    val isBookmarked: Boolean = false
)
