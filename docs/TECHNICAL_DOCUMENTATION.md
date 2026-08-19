# RepoScout — Technical Breakdown (My Approach)

Building RepoScout was a great way to put my 2 years of Android practice into a "production-ready" project. Here's a deeper look at how I implemented the technical requirements.

## 🏗️ Architecture Choices
I went with **MVVM**. I've tried other patterns in the past, but MVVM with **Jetpack Compose** just feels natural. 
- **View**: 100% Compose. No XML here. I used `Scaffold` to manage the bottom bar and top bars.
- **ViewModel**: I used `collectAsStateWithLifecycle` in the UI to make sure I'm not wasting resources when the app is in the background.
- **Domain**: I kept my models (`Repository`, `Owner`) clean so they don't depend on libraries like Moshi or Room directly.

## 📡 The Networking Layer
- **Retrofit**: I've got a single `GitHubApiService` that handles searching and detail fetching.
- **Error Handling**: I created a sealed class called `AppResult`. It was a bit complex to set up, but it makes handling "No Internet" vs "Rate Limit" vs "Server Error" much cleaner in the ViewModels.
- **Debouncing**: In `SearchViewModel`, I used Flow's `debounce(450L)`. It makes the search feel really smooth and saves the user's data/API quota.

## 💾 Local Persistence (The Offline Feature)
For the **Saved** feature, I used **Room**.
- I've got a `RepositoryEntity` that stores just enough data to show the repository details offline.
- The `Saved` screen is "reactive"—it listens to the database `Flow`. So the moment you tap that bookmark button on the Search screen, the Saved screen is already updated.

## 🧪 How I Tested
- **ViewModel Testing**: This was a great learning experience. I had to learn how to use `StandardTestDispatcher` and `advanceTimeBy` to test the search debounce logic.
- **Room Testing**: I used **Robolectric** to run my DAO tests on the JVM, which is much faster than running them on an emulator.
- **AI Collaboration**: I used AI tools to help me debug a weird "Unresolved Reference" error I had in my `NavGraph` and to help me write some of the more repetitive unit test assertions. It really helped me move faster!

## 🚀 Key Features
- **Pagination**: I implemented this by checking the scroll position in the `LazyColumn`. If the user is near the bottom, I trigger the next page load.
- **Connectivity**: I implemented a `ConnectivityObserver` using the system's `ConnectivityManager`. It was a bit more work than just checking a flag, but it's the right way to do it for modern Android.
