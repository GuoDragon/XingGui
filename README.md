# XingGUI Android Template

Static Android app scaffold for the ?? project. This first delivery focuses on the shared shell that the team can extend in parallel.

## Latest Update (2026-04-07 Shared MVP Project Skeleton)

Added the initial Android Studio project framework for ?? with a shared MVP structure:

1. Created a runnable single-module Android project using Kotlin, Jetpack Compose, Navigation Compose, Gson, and MVP-style presentation layers.
2. Added shared data models and repository-backed seed assets for:
   - session state
   - users and roles
   - children
   - goals
   - reports
   - resource items
3. Added the public shell pages owned by the project lead:
   - Login
   - Register
   - Role Select
   - Main shell with bottom navigation for ??? / ??? / ??? / ???
4. Added placeholder-but-usable module pages so the four teammates can extend their own areas without changing the global scaffold.
5. Added reusable UI components, theme files, and basic unit tests for core role/session presentation behavior.

## Project Structure

```text
XingGUI/
??? app/
?   ??? src/main/assets/data/
?   ??? src/main/java/com/example/xinggui/
?   ?   ??? data/
?   ?   ??? navigation/
?   ?   ??? presentation/
?   ?   ??? ui/
?   ??? src/test/java/com/example/xinggui/
??? gradle/
??? build.gradle.kts
??? settings.gradle.kts
```

## Seed Data Notes

- All demo content is local and static.
- Runtime session state is stored in `session_state.json` under the app internal files directory.
- Parent and teacher views share the same module pages and switch by role state.
- The 9 growth dimensions are defined once in code and reused across report/archive placeholders.

## Next Extension Points

1. Each teammate can continue inside their assigned `presentation/<module>/` package.
2. Shared models should be extended incrementally instead of replaced.
3. New runtime signals or persisted files should go through `data/repository/DataRepository.kt`.

## Open in Android Studio

1. Open the XingGui folder in Android Studio.
2. Let Gradle sync complete. If Android Studio asks for an SDK, point it to your local Android SDK.
3. Run the app configuration on a device or emulator.

Notes:
- local.properties is intentionally not committed; Android Studio usually recreates it automatically.
- The Gradle wrapper and required project files are included in this repo.
