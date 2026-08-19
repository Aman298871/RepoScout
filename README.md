# RepoScout 🔍 - GitHub Explorer for Android

Hey! Thanks for checking out my RepoScout project. I'm a passionate Android developer with about 2 years of personal practice under my belt, and I've recently started my professional journey (about 2 months in now!). 

I built this app to show what I've learned about modern Android development. I wanted to create something that feels "native" and smooth, so I went all-in on **Jetpack Compose** and **Material 3**.

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

## 🏛️ How I built this (The Nerd Stuff)

### Architecture: Why MVVM?
I used the **MVVM (Model-View-ViewModel)** pattern. Since I've been practicing for a while, I've realized that putting everything in the Activity is a recipe for disaster. By using ViewModels and `StateFlow`, I kept the UI logic clean and separated from the data.

### Data Movement: API to UI
1. **The Web Layer**: I used `Retrofit` and `Moshi`. I've used these in almost all my projects because they just work.
2. **The Cache**: For the **Saved** feature, I used `Room`. It's one of my favorite libraries because it makes SQLite feel like a breeze.
3. **The Brains**: My `GitHubRepositoryImpl` handles the heavy lifting. It talks to the API, checks the local database to see if a repo is already bookmarked, and maps everything into simple models that my Compose screens can easily show.

### Handling "Real World" Problems
- **Offline Mode**: I hate it when apps just show a white screen when the internet goes out. I added a `NetworkConnectivityObserver` that shows a little banner if you're offline. Plus, the **Saved** tab works 100% offline!
- **Search Debouncing**: I used a **450ms debounce**. I actually used an AI assistant to help me get the Flow operators (`debounce` and `distinctUntilChanged`) exactly right here—it's a bit tricky to get the timing perfect!
- **Errors**: If a search fails but you already have results on the screen, I kept the results there. It’s better to see something than nothing, right?

---

## 🌟 Features (Requirements Check)
- ✅ **Explore**: Loads "android" repos by default. Supports infinite scrolling and pull-to-refresh.
- ✅ **Search**: Real-time searching with a 450ms delay so it doesn't spam the API.
- ✅ **Details**: Shows stars, forks, **watchers**, **open issues**, created/updated dates, and the license.
- ✅ **Actions**: You can "Open on GitHub" in your browser or use the "Share" button to send a link to a friend.
- ✅ **Saved**: Full offline access to your bookmarks.

---

## 🧪 Testing
I've included a bunch of unit tests. I'll be honest—I used some AI help to generate the boilerplate for the `ViewModel` and `Room` tests because there's a lot of setup involved, but I made sure every test case actually makes sense for the app's behavior.

To run them:
```bash
./gradlew testDebugUnitTest
```

---

## 📉 Trade-offs
- **DI**: I didn't use Dagger/Hilt here. For a project this size, I felt like `ViewModelProvider.Factory` and manual injection in the `Application` class was simpler and easier to explain.
- **GitHub API**: I'm using the public API without a token, so if you refresh like crazy, you might hit the rate limit! (I've added a specific error message for that).

---

## 🚀 Building the project
1. Open in Android Studio.
2. Build: `./gradlew assembleDebug`.
3. The Release APK (unsigned) can be found in the `app/build/outputs/apk/release` folder.

Hope you like it! I learned a ton building this.
