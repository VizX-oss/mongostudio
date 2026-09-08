# Changelog

All notable changes to the MongoStudio Android App will be documented in this file.

## [1.0.0] - 2026-09-08

### Added
- **Project Scaffolding**: Gradle Kotlin DSL project with Android Gradle Plugin (AGP) 8.7.3, Kotlin 2.0.21, and Compose BOM 2024.10.01.
- **Gradle Version Catalog**: Centralized version dependencies in `gradle/libs.versions.toml`.
- **Material 3 Expressive UI**: Obsidian dark palette with vibrant emerald accents, glassmorphic card containers, and expressive typography.
- **Connection Management**: Direct URI connection (`mongodb+srv://...` and `mongodb://...`), server health ping, and encrypted vault with masked URIs.
- **Cluster Dashboard**: Live stats for total storage size, database list, collections tally, uptime, memory, and version information.
- **Database & Collection Explorer**: Drill-down view into databases, collection stats (size, document count, indexes), and collection creation/dropping.
- **Document Browser & Direct Push**:
  - Filter, Sort, and Projection JSON queries.
  - Page navigation and item counting.
  - Formatted JSON cards with real-time Compose syntax highlighting.
  - Direct Push inline document editor with JSON validation.
  - Document insertion and deletion.
  - Clipboard JSON copy.
- **Aggregation Pipeline Runner**: Multi-stage aggregation builder with preset shortcuts (Group by Status, Recent 10) and output viewers.
- **Indexes Management**: Index lister, creator (keys, sort direction, unique constraints), and drop index functionality.
- **Raw Command Console**: Interactive database console for arbitrary administrative commands (`ping`, `buildInfo`, `dbStats`).
- **Settings & Host Configuration**: Configurable MongoStudio server endpoint with real-time ping diagnostic.
- **GitHub Actions CI Pipeline**: Automated build, lint, unit testing, and debug APK artifact packaging in `.github/workflows/android.yml`.
- **Unit Tests**: Test suites for format utilities, JSON syntax highlighting, and data model serialization.
