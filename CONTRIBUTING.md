# Contributing to FastZen 🧘

Thank you for your interest in contributing! Please follow these guidelines.

## Branch Naming
- `fix/<short-description>` — bug fixes
- `feat/<short-description>` — new features
- `perf/<short-description>` — performance improvements
- `docs/<short-description>` — documentation only

## Commit Style
Follow [Conventional Commits](https://www.conventionalcommits.org):
```
fix(timer): correct elapsed seconds on resume
feat(analytics): add monthly heatmap view
perf(navigation): cache tab list with remember
```

## Code Standards
- All UI must use **Jetpack Compose** with Material 3
- State must flow **unidirectionally** through `MainLayout` — no side-channel mutations
- All data persistence goes through `FastZenStorage` — no direct `SharedPreferences` access elsewhere
- Wrap `enum.values()` calls inside `remember { }` in composables
- Use `CoroutineScope(Dispatchers.IO + SupervisorJob())` for long-lived background work — never raw `Thread`
- No hardcoded credentials or API keys — use `BuildConfig` fields or environment variables

## Pull Request Checklist
- [ ] Builds without warnings (`./gradlew assembleDebug`)
- [ ] No new lint errors introduced
- [ ] Tested on both light and dark themes
- [ ] Tested on API 26 (min SDK) and API 34 (target SDK)
- [ ] PR description explains *what* changed and *why*

## Architecture
See [README.md](README.md) for the full system architecture diagram before making structural changes.
