package com.example.reposcout.data.mapper

import com.example.reposcout.data.local.entity.RepositoryEntity
import com.example.reposcout.data.remote.dto.GitHubRepositoryDto
import com.example.reposcout.domain.model.License
import com.example.reposcout.domain.model.Owner
import com.example.reposcout.domain.model.Repository

fun GitHubRepositoryDto.toDomain(isBookmarked: Boolean = false): Repository {
    return Repository(
        id = id,
        name = name,
        fullName = fullName ?: name,
        owner = Owner(
            id = owner?.id ?: 0L,
            login = owner?.login ?: "Unknown",
            avatarUrl = owner?.avatarUrl ?: "",
            htmlUrl = owner?.htmlUrl ?: ""
        ),
        description = description,
        htmlUrl = htmlUrl ?: "https://github.com",
        language = language,
        stargazersCount = stargazersCount ?: 0,
        forksCount = forksCount ?: 0,
        watchersCount = watchersCount ?: stargazersCount ?: 0,
        openIssuesCount = openIssuesCount ?: 0,
        license = license?.let {
            License(
                key = it.key,
                name = it.name,
                spdxId = it.spdxId
            )
        },
        createdAt = createdAt,
        updatedAt = updatedAt,
        isBookmarked = isBookmarked
    )
}

fun RepositoryEntity.toDomain(): Repository {
    return Repository(
        id = id,
        name = name,
        fullName = fullName,
        owner = Owner(
            id = ownerId,
            login = ownerLogin,
            avatarUrl = ownerAvatarUrl,
            htmlUrl = ownerHtmlUrl
        ),
        description = description,
        htmlUrl = htmlUrl,
        language = language,
        stargazersCount = stargazersCount,
        forksCount = forksCount,
        watchersCount = watchersCount,
        openIssuesCount = openIssuesCount,
        license = if (licenseKey != null && licenseName != null) {
            License(
                key = licenseKey,
                name = licenseName,
                spdxId = licenseSpdxId
            )
        } else null,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isBookmarked = true
    )
}

fun Repository.toEntity(): RepositoryEntity {
    return RepositoryEntity(
        id = id,
        name = name,
        fullName = fullName,
        ownerId = owner.id,
        ownerLogin = owner.login,
        ownerAvatarUrl = owner.avatarUrl,
        ownerHtmlUrl = owner.htmlUrl,
        description = description,
        htmlUrl = htmlUrl,
        language = language,
        stargazersCount = stargazersCount,
        forksCount = forksCount,
        watchersCount = watchersCount,
        openIssuesCount = openIssuesCount,
        licenseKey = license?.key,
        licenseName = license?.name,
        licenseSpdxId = license?.spdxId,
        createdAt = createdAt,
        updatedAt = updatedAt,
        savedAt = System.currentTimeMillis()
    )
}
