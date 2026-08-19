package com.example.reposcout

import com.example.reposcout.data.mapper.toDomain
import com.example.reposcout.data.mapper.toEntity
import com.example.reposcout.data.remote.dto.GitHubLicenseDto
import com.example.reposcout.data.remote.dto.GitHubOwnerDto
import com.example.reposcout.data.remote.dto.GitHubRepositoryDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RepositoryMapperTest {

    @Test
    fun `map DTO to Domain model correctly with all fields`() {
        val dto = GitHubRepositoryDto(
            id = 12345L,
            name = "RepoScout",
            fullName = "Aman/RepoScout",
            owner = GitHubOwnerDto(id = 1L, login = "Aman", avatarUrl = "https://example.com/avatar.png", htmlUrl = "https://github.com/Aman"),
            description = "GitHub Explorer Android App",
            htmlUrl = "https://github.com/Aman/RepoScout",
            language = "Kotlin",
            stargazersCount = 1500,
            forksCount = 230,
            watchersCount = 1500,
            openIssuesCount = 5,
            license = GitHubLicenseDto(key = "mit", name = "MIT License", spdxId = "MIT"),
            createdAt = "2026-01-01T00:00:00Z",
            updatedAt = "2026-08-15T00:00:00Z"
        )

        val domain = dto.toDomain(isBookmarked = false)

        assertEquals(12345L, domain.id)
        assertEquals("RepoScout", domain.name)
        assertEquals("Aman/RepoScout", domain.fullName)
        assertEquals("Aman", domain.owner.login)
        assertEquals("Kotlin", domain.language)
        assertEquals(1500, domain.stargazersCount)
        assertEquals("MIT", domain.license?.spdxId)
    }

    @Test
    fun `map Domain to Entity and back to Domain retains data fidelity`() {
        val dto = GitHubRepositoryDto(
            id = 999L,
            name = "TestRepo",
            fullName = "user/TestRepo",
            owner = GitHubOwnerDto(id = 2L, login = "user", avatarUrl = "https://example.com/u.png", htmlUrl = "https://github.com/user"),
            description = "Test description",
            htmlUrl = "https://github.com/user/TestRepo",
            language = "Kotlin",
            stargazersCount = 50,
            forksCount = 10,
            watchersCount = 50,
            openIssuesCount = 0,
            license = null,
            createdAt = "2026-02-02T00:00:00Z",
            updatedAt = "2026-03-03T00:00:00Z"
        )

        val domain = dto.toDomain()
        val entity = domain.toEntity()
        val restoredDomain = entity.toDomain()

        assertEquals(domain.id, restoredDomain.id)
        assertEquals(domain.name, restoredDomain.name)
        assertEquals(domain.owner.login, restoredDomain.owner.login)
        assertTrue(restoredDomain.isBookmarked)
    }
}
