package com.example.reposcout

import android.app.Application
import com.example.reposcout.data.local.database.RepoScoutDatabase
import com.example.reposcout.data.remote.api.ApiClient
import com.example.reposcout.data.repository.GitHubRepositoryImpl
import com.example.reposcout.domain.repository.GitHubRepository
import com.example.reposcout.util.ConnectivityObserver
import com.example.reposcout.util.NetworkConnectivityObserver

class RepoScoutApplication : Application() {

    lateinit var database: RepoScoutDatabase
        private set

    lateinit var gitHubRepository: GitHubRepository
        private set

    lateinit var connectivityObserver: ConnectivityObserver
        private set

    override fun onCreate() {
        super.onCreate()
        database = RepoScoutDatabase.getInstance(this)
        gitHubRepository = GitHubRepositoryImpl(
            apiService = ApiClient.gitHubApiService,
            repositoryDao = database.repositoryDao()
        )
        connectivityObserver = NetworkConnectivityObserver(this)
    }
}
