package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.reposcout.domain.model.License
import com.example.reposcout.domain.model.Owner
import com.example.reposcout.domain.model.Repository
import com.example.reposcout.ui.components.RepoCard
import com.example.reposcout.ui.theme.RepoScoutTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

    @get:Rule val composeTestRule = createComposeRule()

    @Test
    fun greeting_screenshot() {
        val sampleRepo = Repository(
            id = 1L,
            name = "RepoScout",
            fullName = "Aman298871/RepoScout",
            owner = Owner(1L, "Aman298871", "", ""),
            description = "Native Android GitHub Repository Explorer with Jetpack Compose, Retrofit, and Room.",
            htmlUrl = "https://github.com/Aman298871/RepoScout",
            language = "Kotlin",
            stargazersCount = 340,
            forksCount = 42,
            watchersCount = 340,
            openIssuesCount = 2,
            license = License("mit", "MIT License", "MIT"),
            createdAt = "2026-01-01T00:00:00Z",
            updatedAt = "2026-08-19T00:00:00Z",
            isBookmarked = true
        )

        composeTestRule.setContent {
            RepoScoutTheme {
                RepoCard(
                    repository = sampleRepo,
                    onClick = {},
                    onBookmarkToggle = {}
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
    }
}
