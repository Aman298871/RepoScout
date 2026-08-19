package com.example.reposcout

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.reposcout.data.local.database.RepoScoutDatabase
import com.example.reposcout.data.local.entity.RepositoryEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class RepositoryDaoTest {

    private lateinit var database: RepoScoutDatabase
    private lateinit var dao: com.example.reposcout.data.local.dao.RepositoryDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RepoScoutDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = database.repositoryDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `insert and get repository success`() = runBlocking {
        val entity = createFakeEntity(1L, "test-repo")
        dao.insertRepository(entity)

        val retrieved = dao.getSavedRepositoryById(1L)
        assertNotNull(retrieved)
        assertEquals("test-repo", retrieved?.name)
    }

    @Test
    fun `delete repository removes it from database`() = runBlocking {
        val entity = createFakeEntity(1L, "to-delete")
        dao.insertRepository(entity)
        dao.deleteRepositoryById(1L)

        val retrieved = dao.getSavedRepositoryById(1L)
        assertNull(retrieved)
    }

    @Test
    fun `getAllSavedRepositories returns all saved items`() = runBlocking {
        dao.insertRepository(createFakeEntity(1L, "repo-1"))
        dao.insertRepository(createFakeEntity(2L, "repo-2"))

        val all = dao.getAllSavedRepositories().first()
        assertEquals(2, all.size)
    }

    private fun createFakeEntity(id: Long, name: String) = RepositoryEntity(
        id = id,
        name = name,
        fullName = "user/$name",
        ownerId = 1L,
        ownerLogin = "user",
        ownerAvatarUrl = "",
        ownerHtmlUrl = "",
        description = "desc",
        htmlUrl = "https://github.com",
        language = "Kotlin",
        stargazersCount = 10,
        forksCount = 1,
        watchersCount = 10,
        openIssuesCount = 0,
        licenseKey = null,
        licenseName = null,
        licenseSpdxId = null,
        createdAt = null,
        updatedAt = null
    )
}
