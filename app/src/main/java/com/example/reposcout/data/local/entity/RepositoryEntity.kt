package com.example.reposcout.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_repositories")
data class RepositoryEntity(
    @PrimaryKey val id: Long,
    val name: String,
    val fullName: String,
    val ownerId: Long,
    val ownerLogin: String,
    val ownerAvatarUrl: String,
    val ownerHtmlUrl: String,
    val description: String?,
    val htmlUrl: String,
    val language: String?,
    val stargazersCount: Int,
    val forksCount: Int,
    val watchersCount: Int,
    val openIssuesCount: Int,
    val licenseKey: String?,
    val licenseName: String?,
    val licenseSpdxId: String?,
    val createdAt: String?,
    val updatedAt: String?,
    val savedAt: Long = System.currentTimeMillis()
)
