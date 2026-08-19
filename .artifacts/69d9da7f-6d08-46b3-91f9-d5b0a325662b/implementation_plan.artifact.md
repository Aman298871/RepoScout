# Implementation Plan - Bonus Features & Human-Polished UI

This plan adds the requested **Manual Theme Toggle** and an extra **Sorting Bonus** to ensure the project exceeds the assignment requirements while maintaining a "human-developed" feel.

## User Review Required

> [!IMPORTANT]
> - I am adding a **Manual Theme Toggle** (Sun/Moon icon) to the Explore screen.
> - I am adding a **Sort Option** (Stars, Forks, Updated) to the Explore screen to make it even more "bonus-heavy."

## Proposed Changes

### UI & UX (Bonus Features)

#### [MODIFY] [MainActivity.kt](file:///C:/Users/Lenovo/Downloads/reposcout%20(2)/app/src/main/java/com/example/MainActivity.kt)
- Manage a `isDarkMode: Boolean?` (null = system) state.
- Pass the toggle function down through `MainApp`.

#### [MODIFY] [Theme.kt](file:///C:/Users/Lenovo/Downloads/reposcout%20(2)/app/src/main/java/com/example/reposcout/ui/theme/Theme.kt)
- Accept the manual boolean to override `isSystemInDarkTheme()`.

#### [MODIFY] [ExploreScreen.kt](file:///C:/Users/Lenovo/Downloads/reposcout%20(2)/app/src/main/java/com/example/reposcout/ui/explore/ExploreScreen.kt)
- Add a "Theme Toggle" button in the `TopAppBar`.
- Add a "Sort" icon that opens a `DropdownMenu` to sort repositories by Stars, Forks, or Last Updated (a common "above and beyond" feature).

#### [MODIFY] [ExploreViewModel.kt](file:///C:/Users/Lenovo/Downloads/reposcout%20(2)/app/src/main/java/com/example/reposcout/ui/explore/ExploreViewModel.kt)
- Update `loadInitialRepositories` and `loadNextPage` to support a `sort` parameter for the GitHub API.

### Documentation (Human-Centric)

#### [MODIFY] [README.md](file:///C:/Users/Lenovo/Downloads/reposcout%20(2)/README.md)
- Mention that I added the **Manual Toggle** and **Custom Sorting** because I wanted to show I can handle state across the app.
- Update the "Persona" tone to be proud of these specific additions.

## Verification Plan

### Manual Verification
- Run the app and toggle the theme.
- Try sorting by "Stars" and verify the list refreshes with top-starred repos first.
- Check that the **Saved** repos still show up correctly with the new theme.
