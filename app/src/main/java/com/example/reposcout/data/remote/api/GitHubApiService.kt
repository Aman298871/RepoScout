package com.example.reposcout.data.remote.api

import com.example.reposcout.data.remote.dto.GitHubRepositoryDto
import com.example.reposcout.data.remote.dto.GitHubSearchResponseDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface GitHubApiService {

    @GET("search/repositories")
    suspend fun searchRepositories(
        @Query("q") query: String,
        @Query("page") page: Int,
        @Query("per_page") perPage: Int = 20,
        @Query("sort") sort: String? = null
    ): Response<GitHubSearchResponseDto>

    @GET("repos/{owner}/{repo}")
    suspend fun getRepositoryDetails(
        @Path("owner") owner: String,
        @Path("repo") repo: String
    ): Response<GitHubRepositoryDto>
}
