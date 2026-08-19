# RepoScout 🔍

A native Android app for discovering, searching, viewing, sorting, filtering, and saving GitHub repositories.

I built RepoScout as a take-home Android project to practice the things I have been working with in Native Android development, especially Kotlin, Jetpack Compose, ViewModel, Coroutines/Flow, Retrofit, and Room. I have around 2 years of personal/practical Android development practice and around 2 months of professional app-development experience, so I kept the architecture simple enough to understand and explain while still following good Android practices.

## Features

- **Explore:** Loads GitHub repositories using `android` as the initial query.
- **Pagination:** Loads more repositories while scrolling.
- **Pull-to-refresh:** Refreshes the current Explore results.
- **Search:** Search GitHub repositories using the public GitHub REST API.
- **Search debouncing:** Prevents an API request from being made for every keystroke.
- **Sorting & Filtering:** Sort Explore results and filter repositories using the available filter/topic options.
- **Light / Dark Theme:** Switch between light and dark themes from the app UI.
- **Repository Details:** Shows important repository information such as stars, forks, watchers, open issues, language, license, created date, and updated date.
- **Open on GitHub:** Opens the repository in the appropriate external browser/app.
- **Bookmarking:** Save repositories locally.
- **Offline Saved:** Previously saved repositories remain available when the device has no internet connection.
- **Loading / Empty / Error / Retry states:** The UI handles the main network and data states instead of simply showing a blank screen.

## Tech Stack

- **Kotlin** — Main programming language.
- **Jetpack Compose + Material 3** — UI.
- **ViewModel** — Keeps UI-related state separate from the Composable functions.
- **Coroutines / Flow** — Asynchronous work, state observation, and search debouncing.
- **Retrofit + OkHttp** — Communication with the GitHub REST API.
- **Moshi** — JSON parsing.
- **Room** — Local persistence for saved repositories.
- **Navigation Compose** — Navigation between Explore, Search, Saved, and Details.
- **Coil** — Loading repository/owner images.

I intentionally did not add a dependency-injection framework such as Koin or Hilt just for the sake of using one. For a project of this size, I felt that the existing ViewModel factory/manual dependency approach was simpler and easier for me to understand and maintain.

## Architecture

### Why MVVM?

I chose **MVVM (Model-View-ViewModel)** because I wanted the Compose UI to mainly be responsible for displaying state, instead of putting API calls and application logic directly inside Composables.

The basic structure is:

```text
Compose UI
    ↓
ViewModel
    ↓
Repository
    ↓
Retrofit / Room
```

The ViewModel exposes lifecycle-aware UI state, and the UI observes that state. This also makes the main logic easier to test without depending on the screen itself.

I kept the architecture relatively lightweight because this is a bounded take-home project. I did not add a large number of extra abstraction layers such as separate use-case/interactor classes when they were not necessary for the current scope.

## How Data Moves from the API to the UI

For GitHub data, the flow is approximately:

```text
GitHub REST API
      ↓
Retrofit + OkHttp
      ↓
API DTOs
      ↓
Mapping
      ↓
Repository
      ↓
ViewModel
      ↓
StateFlow / UI State
      ↓
Jetpack Compose UI
```

When the user searches or opens a repository, the ViewModel requests the required data through the repository. The repository communicates with the GitHub API and maps the response into the application's models. The ViewModel then exposes the resulting state to Compose, and Compose updates the UI.

For saved repositories, Room is used as the local data source:

```text
User bookmarks repository
          ↓
       Room DB
          ↓
       DAO / Flow
          ↓
      Repository
          ↓
       ViewModel
          ↓
     Compose UI
```

This keeps the Saved screen independent from the GitHub API once a repository has been saved.

## GitHub API Usage

RepoScout uses the **public GitHub REST API** and does not require a custom backend or a GitHub personal access token.

The main API operations are:

```text
GET /search/repositories?q={query}&page={page}&per_page={perPage}

GET /repos/{owner}/{repo}
```

The application uses the public API for Explore, Search, and Repository Details.

Because the API is being used without authentication, GitHub's unauthenticated rate limits can apply. The app handles relevant rate-limit/API errors and shows an appropriate error state instead of crashing.

## API Errors and Network Failures

I tried to keep network failures separate from the normal success state.

The app handles situations such as:

- No internet connection
- Connection/DNS failures
- Request timeouts
- GitHub HTTP errors
- Server-side errors
- GitHub rate limiting
- Empty search results
- Pagination failures

When a request fails, the UI can show an error and retry action instead of crashing.

For search, I also tried to avoid unnecessarily destroying useful content. If there are already results on the screen and a later request fails, the existing results can remain visible while the error is communicated to the user.

For pagination, an error on a later page should not remove repositories that were already loaded.

## Offline Saved Repositories

The Saved feature uses **Room** for local persistence.

When a repository is bookmarked, the required repository information is stored in the local database. The Saved screen observes the locally stored repositories instead of depending on another GitHub API request just to display them.

This means:

```text
Internet ON
    ↓
Repository bookmarked
    ↓
Saved in Room
    ↓
Internet OFF
    ↓
Saved screen
    ↓
Previously saved repository is still available
```

Explore and Search still depend on the GitHub API for new remote data. The offline requirement is focused on keeping saved repositories accessible without a network connection.

## Search Debouncing

I used Kotlin Flow debouncing for the search input.

The current implementation uses approximately **450 ms** of debounce time.

The idea is:

```text
User types:
a → an → and → andro → android

        ↓

Wait for the input to settle

        ↓

Make the search request
```

Without debouncing, every keystroke could cause another API request. Debouncing gives the user a short time to finish typing before making the request.

Where appropriate, the search flow also avoids processing unnecessary duplicate/outdated requests.

## Sorting, Filtering, and Theme

After completing the core assignment requirements, I also added a few small usability improvements.

### Sorting & Filtering

The Explore screen provides sorting and filtering options so the user can change how repositories are displayed and narrow the visible results using the available options/topics.

These are implemented as actual application behavior rather than only being visual controls.

### Light / Dark Theme

The app also provides a light/dark theme option. The theme is handled through the Compose Material 3 theme setup, with separate light and dark color schemes.

I kept these additions relatively small so they would not take priority over the required API, pagination, Saved/offline, error handling, and navigation functionality.

## Pagination and Refresh

Explore and Search support pagination.

The app keeps track of the current page and requests the next page when the user reaches the appropriate point in the list.

Important behavior includes:

- Avoiding duplicate page requests.
- Keeping previously loaded repositories when a later page fails.
- Showing a loading indicator while another page is being requested.
- Allowing a failed pagination request to be retried.
- Resetting the relevant page state when a full refresh/new search is performed.

Explore also supports pull-to-refresh.

## Repository Details

The Details screen displays the information required by the assignment, including:

- Repository name
- Owner
- Description
- Stars
- Forks
- Watchers
- Open issues
- Primary language
- License
- Created date
- Last updated date

There is also an **Open on GitHub** action that uses Android's external app/browser handling.

## Testing

I included automated tests for important pieces of the application, including repository/data mapping and search debounce behavior.

The test suite can be run with:

```bash
./gradlew testDebugUnitTest
```

The goal of the tests is to check actual application behavior rather than simply increasing the number of test files.

The areas I would prioritize for additional test coverage are ViewModel state transitions, pagination edge cases, Room DAO behavior, bookmark behavior, and offline Saved behavior.

## Trade-offs Because of the Time Limit

The assignment had a limited expected effort, so I focused first on the required features instead of trying to make the application much larger than necessary.

Some decisions I made were:

- I kept the MVVM structure relatively simple instead of adding a large dependency-injection or use-case layer.
- I used manual dependency/ViewModel factory setup because it was enough for the size of this application.
- I focused on reliable Explore, Search, Details, Bookmark, and Offline Saved behavior before optional features.
- I used the public GitHub API without authentication because the assignment does not require a GitHub token.
- I spent more time on loading, empty, error, retry, pagination, and offline states instead of adding a lot of visual effects.
- After the required functionality was working, I added small usability improvements such as sorting/filtering and a light/dark theme toggle.

The main trade-off was choosing a smaller implementation that I could understand and explain rather than adding architecture or features just to make the project look bigger.

## What I Would Improve With Another 1–2 Days

If I had another 1–2 days, I would mainly spend that time improving reliability, testing, and polish rather than adding a large number of new features.

My priorities would be:

1. **Increase automated test coverage**, especially for ViewModels, pagination failures, bookmark behavior, and offline Saved behavior.
2. **Improve offline/network UX**, for example by making it even clearer when the user is viewing locally saved information because the network is unavailable.
3. **Polish accessibility and UI details**, including content descriptions, spacing, and a few more edge-case states.
4. **Improve the caching strategy** if the scope allowed it, while keeping the Saved/offline behavior simple and reliable.
5. **Further polish the existing sorting/filtering and theme experience** based on user feedback instead of adding many unrelated features.

I would not use the extra time simply to add more screens. I would prefer to make the existing screens more reliable, easier to test, and more polished.

## Known Limitations

- Explore and Search require access to the public GitHub API.
- Unauthenticated GitHub API usage is subject to GitHub's rate limits.
- Saved repositories are available offline because they are stored locally in Room; new Explore/Search data still requires the network.
- The sorting/filtering options are intentionally focused on the current assignment scope rather than trying to build a full advanced repository discovery system.
- The project is intentionally kept relatively small and does not try to implement a production-scale backend or a large multi-module architecture.

## Project Structure

The project follows a simple separation of responsibilities around:

```text
ui/
    screens
    navigation
    components

data/
    remote
    local
    repository

domain/
    models
    repository contracts

util/
```

The exact package/class names in the source code are the source of truth for the current implementation.

## Assignment Requirement Coverage

### Explore
- ✅ Default `android` repository query
- ✅ Repository name
- ✅ Owner/avatar
- ✅ Description
- ✅ Stars
- ✅ Forks
- ✅ Primary language
- ✅ Last updated date
- ✅ License when available
- ✅ Pagination / infinite scrolling
- ✅ Pull-to-refresh
- ✅ Loading state
- ✅ Empty state
- ✅ Error state
- ✅ Retry

### Search
- ✅ GitHub repository search
- ✅ Search debouncing
- ✅ Search pagination
- ✅ Empty results
- ✅ Network/API error handling
- ✅ Loading state
- ✅ Existing useful content preserved where applicable

### Repository Details
- ✅ Repository name
- ✅ Owner
- ✅ Description
- ✅ Stars
- ✅ Forks
- ✅ Watchers
- ✅ Open issues
- ✅ Language
- ✅ License
- ✅ Created date
- ✅ Last updated date
- ✅ Open on GitHub

### Saved / Offline
- ✅ Bookmark repositories
- ✅ Local persistence using Room
- ✅ Saved repositories available offline

### Additional UX
- ✅ Repository sorting/filtering
- ✅ Light/dark theme toggle

### Technical
- ✅ Kotlin
- ✅ Jetpack Compose
- ✅ Coroutines / Flow
- ✅ ViewModel and lifecycle-aware state
- ✅ Retrofit / OkHttp
- ✅ Moshi
- ✅ Room
- ✅ Navigation Compose
- ✅ Coil
- ✅ Automated tests for important existing logic

## Build and Run

1. Clone the repository.
2. Open the project in Android Studio.
3. Allow Gradle to sync.
4. Connect an Android device or start an emulator.
5. Run the `app` configuration.

For a debug build:

```bash
./gradlew assembleDebug
```

For unit tests:

```bash
./gradlew testDebugUnitTest
```

For a release build:

```bash
./gradlew assembleRelease
```

Use the generated release APK from the project's release output directory when providing the APK for submission.

## Final Note

This project was built as a focused Android take-home assignment. My main goal was to demonstrate that I can take a defined Android problem, choose a reasonable architecture, work with a REST API, manage UI state, persist data locally, handle common failure cases, and explain the decisions I made.

I have tried to keep the implementation simple enough that I can explain the important parts of it rather than adding complexity just for the sake of it.
