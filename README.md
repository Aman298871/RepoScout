# RepoScout 🔍 - GitHub Explorer for Android

Hey! Thanks for checking out my RepoScout project.

I'm a junior Android developer with around 2 years of personal/practical experience in Native Android development, and I have recently started my professional journey with around 2 months of professional app-development experience.

I built RepoScout as a take-home Android project to demonstrate my understanding of modern native Android development using **Kotlin, Jetpack Compose, Material 3, Coroutines, Retrofit, and Room**.

My main focus was to keep the application simple enough for me to understand and explain, while still following a clean Android architecture and handling real-world cases such as network failures, pagination, search debouncing, and offline saved repositories.

---

## 📱 Screenshots

### Explore — Dark Theme
![image alt](https://github.com/Aman298871/RepoScout/blob/47cea9be64c5fdd0ac3e040b75684b98164846f8/explore_dark.png.jpg)

### Explore — Light Theme
![image alt](https://github.com/Aman298871/RepoScout/blob/47cea9be64c5fdd0ac3e040b75684b98164846f8/explore_light.png.jpg)

### Sorting Options
![image alt](https://github.com/Aman298871/RepoScout/blob/47cea9be64c5fdd0ac3e040b75684b98164846f8/sorting.jpg)

### Search
![image alt](https://github.com/Aman298871/RepoScout/blob/47cea9be64c5fdd0ac3e040b75684b98164846f8/search.jpg)

### Repository Details
![image alt](https://github.com/Aman298871/RepoScout/blob/47cea9be64c5fdd0ac3e040b75684b98164846f8/Repository_detail.png.jpg)

### Bookmarked Repositories
![image alt](https://github.com/Aman298871/RepoScout/blob/47cea9be64c5fdd0ac3e040b75684b98164846f8/Bookmarked_Repository.jpg)

### Saved Repositories
![image alt](https://github.com/Aman298871/RepoScout/blob/47cea9be64c5fdd0ac3e040b75684b98164846f8/Saved.jpg)

---

# ✨ Features

- 🔍 Search GitHub repositories
- 🏠 Explore repositories with `android` as the default query
- 📄 Pagination / infinite scrolling
- 🔄 Pull-to-refresh
- ⏱️ Search debouncing
- 📊 Repository statistics and details
- ⭐ Stars and forks
- 👀 Watchers
- 🐛 Open issues
- 💻 Primary language
- 📜 License information
- 📅 Created and updated dates
- 🔖 Bookmark repositories
- 💾 Offline access to saved repositories
- 🌐 Open repositories on GitHub
- 🔗 Share repository links
- ⭐ Sort repositories by Stars, Forks, or Updated
- 🔎 Repository filtering
- 🌙 Light / Dark theme
- ⚠️ Loading, empty, error, and retry states

---

# 🏗️ Architecture

## Why did I choose this architecture?

I chose **MVVM (Model-View-ViewModel)** because it gives me a clear separation between the UI and the rest of the application.

Since the project uses Jetpack Compose, I wanted the Composable functions to mainly focus on displaying the current state instead of directly performing API calls or handling database operations.

The overall structure is:

```text
                 ┌─────────────────┐
                 │   Compose UI    │
                 └────────┬────────┘
                          │
                          ▼
                 ┌─────────────────┐
                 │    ViewModel    │
                 └────────┬────────┘
                          │
                          ▼
                 ┌─────────────────┐
                 │    Repository   │
                 └───────┬─┬───────┘
                         │ │
              ┌──────────┘ └──────────┐
              ▼                       ▼
       ┌──────────────┐       ┌──────────────┐
       │ GitHub API   │       │  Room DB     │
       │ Retrofit     │       │  Local Data  │

I chose this approach because it is something I am comfortable working with and it is suitable for the size of this application.

I considered using a more complex setup with additional abstraction layers or a dependency-injection framework such as Hilt/Koin, but I felt that would add complexity without providing much benefit for this particular assignment.

The main priority was to keep the code understandable and maintainable.

🔄 Data Flow
How does data move from the API to the UI?

For GitHub API data, the flow is:

GitHub REST API
       ↓
Retrofit + OkHttp
       ↓
     DTOs
       ↓
Moshi JSON Parsing
       ↓
     Mapping
       ↓
 Repository
       ↓
 ViewModel
       ↓
 StateFlow / UI State
       ↓
Jetpack Compose

When the user opens Explore or performs a search:

The UI sends the user's action to the ViewModel.
The ViewModel requests the required data through the repository.
The repository calls the GitHub REST API using Retrofit.
Moshi parses the JSON response into DTOs.
The repository maps the DTOs into application models.
The ViewModel exposes the resulting state using Kotlin Flow/StateFlow.
Compose observes that state and updates the UI.

For saved repositories, the data flow is slightly different:

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

This separation allows remote GitHub data and locally saved data to be handled independently.

🌐 Error Handling
How are API errors and network failures handled?

I tried to treat network failures as normal application states instead of allowing them to crash the application.

The application handles common situations such as:

No internet connection
Connection/DNS failures
Request timeouts
GitHub API errors
HTTP 5xx server errors
GitHub rate limits
Empty search results
Pagination failures

The UI has different states for:

Loading
   ↓
Success
   ↓
Empty / Error
   ↓
Retry

If a request fails while useful data is already displayed, I try to preserve the existing data instead of replacing the entire screen with a blank error state.

For example, if page 1 and page 2 have already loaded and page 3 fails, the repositories from pages 1 and 2 remain visible and the user can retry loading page 3.

I also use a NetworkConnectivityObserver to detect connectivity changes and provide feedback when the device is offline.

The goal was to make network failures feel like part of the application flow rather than unexpected crashes.

💾 Offline Saved Repositories
How does offline saved-repository access work?

For the Saved feature, I use Room for local persistence.

When the user bookmarks a repository, the required repository information is stored in the local Room database.

The Saved screen reads the saved repositories from Room instead of depending on another GitHub API request.

The flow is:

Internet Available
       ↓
User bookmarks repository
       ↓
Repository saved to Room
       ↓
Internet becomes unavailable
       ↓
User opens Saved
       ↓
Room provides saved repository
       ↓
Repository is still displayed

This means previously saved repositories remain accessible even without an internet connection.

Explore and Search still require internet access because they need to retrieve new repository data from GitHub.

I intentionally focused offline support on the Saved requirement rather than implementing a complete offline cache for every GitHub API response.

🔎 Search
How does search debouncing work?

The search field uses Kotlin Flow with approximately 450ms of debounce.

Without debouncing, if the user types:

a
an
and
andr
andro
android

the application could potentially make a request for every change.

Instead, the application waits for the user to stop typing for approximately 450ms.

User types
    ↓
Input changes
    ↓
Wait ~450ms
    ↓
No new input?
    ↓
Make API request

I also use distinctUntilChanged() so the same query is not unnecessarily processed again.

This reduces unnecessary API calls and makes the search experience smoother.

📄 Pagination

Both Explore and Search support pagination.

The application keeps track of the current page and requests additional repositories when the user reaches the appropriate point in the list.

The implementation also handles:

Preventing duplicate page requests
Loading indicators for additional pages
Pagination failures
Retrying failed pages
Keeping previously loaded repositories
Resetting pagination when a new search starts
Resetting pagination during refresh

Explore also supports pull-to-refresh.

🎨 UI and Additional Features

After implementing the core assignment requirements, I added a few small usability improvements.

Sorting

Repositories can be sorted by:

⭐ Stars
🍴 Forks
🕐 Updated
Filtering

The Explore screen also provides filtering options to narrow down the displayed repositories.

Theme

The application supports:

☀️ Light theme
🌙 Dark theme
Sharing

Repository links can be shared using Android's native sharing functionality.

These features were kept relatively small so they would not interfere with the required assignment functionality.

🧪 Testing

I included automated tests for important parts of the application, including repository/data mapping and search-related behavior.

Unit tests can be executed using:

./gradlew testDebugUnitTest

I focused on testing meaningful application behavior instead of simply trying to maximize the number of test files.

With additional time, I would expand testing around ViewModel state transitions, pagination edge cases, Room operations, bookmarking, and offline behavior.

⏳ Development Trade-offs
What trade-offs did I make because of the time limit?

The assignment had a limited expected effort, so I prioritized the required functionality first.

1. Simple MVVM

I used MVVM but avoided adding unnecessary use-case and abstraction layers.

For this project, I felt the simpler structure was easier to understand and maintain.

2. No Hilt/Koin

I did not introduce Hilt or Koin just for the sake of using a dependency-injection framework.

For an application of this size, manual dependency/ViewModel factory setup was sufficient for me.

3. Focused offline support

Instead of caching every GitHub API response locally, I focused on making the Saved repository feature work properly offline using Room.

4. Public GitHub API

I used the public GitHub REST API without authentication because a GitHub token was not required for the assignment.

The trade-off is that unauthenticated requests are subject to GitHub's rate limits.

5. Extra features were secondary

Sorting, filtering, theme switching, and sharing were implemented after the core requirements were completed.

I preferred to make the required functionality reliable rather than adding a large number of optional features.

Overall, I chose simplicity and understandability over unnecessary complexity.

🚀 If I Had Another 1–2 Days
What would I improve if I had another 1–2 days?

If I had another 1–2 days, I would focus mainly on improving reliability, testing, accessibility, and UI polish rather than adding many new screens.

1. More automated tests

I would increase coverage for:

ViewModel state changes
Pagination
Room database operations
Bookmark behavior
Offline Saved behavior
Network error scenarios
2. Better offline/network UX

I would make the difference between online, offline, loading, and cached states even clearer to the user.

3. Accessibility improvements

I would spend more time on:

Content descriptions
Text readability
Touch target sizes
Screen-reader support
Better handling of edge-case states
4. UI polish

I would further refine:

Sorting/filtering interactions
Dark/light theme transitions
Loading states
Empty states
Error messages
5. Caching improvements

If the scope allowed it, I would explore a more complete caching strategy while keeping the architecture simple.

I would rather spend the extra time making the existing features reliable and polished than simply adding more features.

🔌 GitHub API

RepoScout uses the public GitHub REST API.

Main API endpoints used by the application include:

GET /search/repositories?q={query}&page={page}&per_page={perPage}


GET /repos/{owner}/{repo}

The application does not require a custom backend or GitHub personal access token.

Because the public API is unauthenticated, GitHub rate limits can apply. Relevant rate-limit and API errors are handled by the application.

🧰 Tech Stack
Technology	Usage
Kotlin	Main programming language
Jetpack Compose	UI
Material 3	Design system
ViewModel	UI state and lifecycle-aware logic
Coroutines / Flow	Asynchronous operations and reactive state
Retrofit	REST API communication
OkHttp	HTTP client
Moshi	JSON parsing
Room	Local persistence
Navigation Compose	Screen navigation
Coil	Image loading
📁 Project Structure

The project follows a simple separation of responsibilities:

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

The exact package and class names in the source code are the source of truth for the current implementation.

⚠️ Known Limitations
Explore and Search require access to the public GitHub API.
Unauthenticated GitHub API usage is subject to GitHub rate limits.
Saved repositories work offline through Room, while new Explore/Search data requires the network.
Filtering is focused on the current assignment scope rather than being a full GitHub discovery system.
The project intentionally uses a relatively small architecture suitable for the assignment rather than a production-scale multi-module setup.
🚀 Build and Run
Requirements
Android Studio
Android SDK
JDK compatible with the project's Gradle configuration
Android device or emulator
Run
Clone the repository.
Open the project in Android Studio.
Allow Gradle to sync.
Connect an Android device or start an emulator.
Run the app configuration.
Debug Build
./gradlew assembleDebug
Unit Tests
./gradlew testDebugUnitTest
Release Build
./gradlew assembleRelease

The generated APK will be available in the project's build output directory.

👨‍💻 Final Note

RepoScout was built as a focused Android take-home assignment.

My main goal was not to make the project unnecessarily large. I wanted to demonstrate that I can take a defined Android problem, work with a REST API, manage UI state, persist data locally, handle common failure cases, and make reasonable architectural decisions.

I also wanted the final implementation to be something I can actually walk through and explain, rather than adding complexity just to make the project look more advanced.

## 🚀 Building the project
1. Open in Android Studio.
2. Build: `./gradlew assembleDebug`.
3. The Release APK (unsigned) can be found in the `app/build/outputs/apk/release` folder.

Hope you like it! I learned a ton building this.
