---
name: android-ai-builder
description: Build, modify, and ship native Android apps in Kotlin using Material 3 Expressive, entirely through AI-driven development with zero local toolchain — no Android Studio, no local Gradle/SDK/JDK installs, no local package managers. All builds, tests, and artifact generation happen on GitHub Actions. Use this skill whenever the user wants to create a new Android app, add a feature to an existing one, fix a build, or set up/debug CI for an Android project, especially when they mention Kotlin, Jetpack Compose, Material 3 / Material Expressive, or building "without installing anything locally."
---

# Android AI Builder (Kotlin + Material 3 Expressive, CI-only)

Build native Android apps end-to-end by editing source files directly and letting
GitHub Actions do 100% of the compiling, linting, and packaging. No local JDK,
Android SDK, Gradle wrapper execution, or `pip`/`npm` installs of build tooling.
The dev loop is: **edit → commit → push → watch Actions → pull artifact.**

## Core constraints (never violate)

- Never run `./gradlew`, `sdkmanager`, `avdmanager`, or install JDK/Android SDK locally.
- Never run an emulator or instrumented tests locally.
- The only thing done locally is editing text files (Kotlin, XML, Gradle Kotlin DSL,
  YAML) and `git` operations.
- All compilation, unit tests, lint, and APK/AAB generation happen inside
  `.github/workflows/*.yml` runners.
- If a build error needs debugging, debug it by reading the Actions log output
  (via `gh run view --log-failed` or the Actions UI), not by reproducing locally.

## Project setup

1. **Scaffold the project structure by hand** (no `Android Studio > New Project`
   wizard available). Use the standard Gradle Kotlin DSL layout:
   ```
   app/
     build.gradle.kts
     src/main/
       AndroidManifest.xml
       java/com/<pkg>/...       (Kotlin sources)
       res/                     (values, drawable, mipmap, etc.)
   build.gradle.kts              (root)
   settings.gradle.kts
   gradle/wrapper/gradle-wrapper.properties
   gradle.properties
   .github/workflows/android.yml
   ```
2. **Gradle wrapper files must still be committed** (`gradlew`, `gradlew.bat`,
   `gradle/wrapper/gradle-wrapper.jar`, `gradle-wrapper.properties`) — the CI
   runner executes them, you just never invoke them yourself. Generate the
   wrapper jar via the CI's first run or by downloading the known-good jar for
   the pinned Gradle version rather than running `gradle wrapper` locally.
3. Pin exact versions everywhere (AGP, Kotlin, Compose BOM, Gradle) — see
   "Version pinning" below. Floating versions are the #1 cause of a build that
   passed yesterday failing today.

## Material 3 Expressive setup

- Use Jetpack Compose (not legacy Views/XML themes) — Material 3 Expressive
  ships as part of `androidx.compose.material3` starting with the versions
  that introduced expressive motion/shape/color specs.
- Declare the Compose BOM in `app/build.gradle.kts` and pull `material3` from
  it — don't hand-pin the material3 version separately from the BOM.
- Enable the expressive theming APIs (expressive typography scale, shape
  scale, motion scheme) via the `MaterialExpressiveTheme` /
  `ExpressiveTheme` entry points if available in the pinned Compose Material3
  version — check the exact API name against the version you pinned, it has
  shifted across alpha/beta releases.
- Keep a single `Theme.kt` with color scheme (support dynamic color +
  light/dark), typography, and shape definitions, so the whole app reads from
  one source of truth.
- Because Material 3 Expressive APIs have moved fast across alpha releases,
  **always pin the exact Compose Material3 version** rather than `+` or
  no-version ranges, and note the version in a comment next to the dependency
  so future-you knows which API surface is in play.

## Kotlin conventions

- Kotlin-only source (no Java files) unless the user's existing project
  already has Java to interop with.
- Prefer Compose + `ViewModel` + `StateFlow`/`State` for UI state, standard
  unidirectional data flow.
- Use `kotlinx.serialization` or `kotlinx.coroutines` as needed; declare them
  explicitly in `build.gradle.kts` rather than assuming a version catalog
  entry exists.
- If the project is non-trivial, set up a **Gradle version catalog**
  (`gradle/libs.versions.toml`) up front — it centralizes every version
  number in one file, which matters a lot when you can't locally
  `./gradlew --refresh-dependencies` to sanity-check a bump.

## The GitHub Actions workflow

Create `.github/workflows/android.yml` covering, at minimum:

```yaml
name: Android CI

on:
  push:
    branches: [main]
  pull_request:
  workflow_dispatch:

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK
        uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '17'

      - name: Setup Gradle
        uses: gradle/actions/setup-gradle@v4
        # this action alone gives you dependency caching, no manual cache: key needed

      - name: Lint
        run: bash gradlew lint

      - name: Unit tests
        run: bash gradlew testDebugUnitTest

      - name: Assemble debug APK
        run: bash gradlew assembleDebug

      - name: Upload APK
        uses: actions/upload-artifact@v4
        with:
          name: app-debug
          path: app/build/outputs/apk/debug/*.apk
```

Notes specific to this project's history:
- Invoke `bash gradlew <task>`, not `./gradlew <task>` — avoids exec-bit /
  noexec filesystem issues seen previously on ARM64 runners.
- `gradle/actions/setup-gradle@v4` already handles Gradle caching; don't
  hand-roll `actions/cache` for `~/.gradle` unless you need something custom —
  duplicate caching layers cause stale-cache bugs.
- Add a `workflow_dispatch` trigger always — lets you re-run a build on demand
  from the Actions UI without pushing a dummy commit.

### Release workflow (separate from CI)

Keep debug CI and release signing in separate workflow files. A second
`.github/workflows/release.yml`, triggered on tag push (`v*`), should:
- Decode a base64-encoded keystore from a GitHub Secret into a file.
- Run `bundleRelease` (AAB) and/or `assembleRelease` (signed APK).
- Upload the signed artifact and delete the decoded keystore file as a final
  step (`if: always()`), so it never lingers on the runner disk.
- Never echo secret values into logs; mask with `::add-mask::` if a secret
  must be derived/transformed mid-workflow.

## Workflow suggestions for a smooth, robust, error-free loop

**Fast feedback first.** Put lint + unit tests in a job that runs before (or
in parallel with, if you then gate the APK upload on both) the full assemble
step. Failing fast on a lint error saves minutes vs. waiting for a full
assemble to fail on something trivial.

**Version pinning, everywhere.** AGP, Kotlin, Compose BOM, Gradle wrapper
version, and any CI action (`actions/checkout@v4`, not `@v4.1.2`-then-drift)
should be pinned to exact or minor-locked versions. Since you can't locally
verify a version bump works before pushing, an unpinned dependency resolving
differently between two CI runs is a silent, hard-to-diagnose failure mode.

**One change, one push, one watch.** Because the feedback loop is push→CI
(minutes, not seconds), batch trivial edits but keep logically distinct
changes in separate commits/pushes so a failure is easy to bisect. Avoid
pushing five unrelated changes and then puzzling over which one broke the
build.

**Use `gh` CLI to close the loop without leaving the terminal**, if available:
```bash
gh run watch                     # live-tail the current run
gh run view --log-failed         # pull just the failing step's log
```
This avoids the human needing to switch to a browser for every iteration.

**Cache aggressively but scope it correctly.** `setup-gradle@v4` caches
dependencies and build outputs across runs keyed by branch/lockfile hash —
good default, don't disable it. If you add a custom cache (e.g. for a
local `.kotlin` compiler daemon dir), key it on the Gradle/Kotlin version so
a toolchain bump can't serve a stale cache.

**Concurrency control**, to avoid wasted/overlapping runs on rapid pushes:
```yaml
concurrency:
  group: ${{ github.workflow }}-${{ github.ref }}
  cancel-in-progress: true
```

**Matrix only when you need it.** Don't add a multi-API-level or
multi-ABI test matrix until the app actually needs to verify against more
than one target — it multiplies CI minutes and multiplies the number of
possible flaky failures for no signal in an early-stage project.

**Keep a `CHANGELOG.md` updated per meaningful push** and, if working in
autonomous/agentic loops, a `FUTURE_PLANS.md` (or similar) regenerated each
cycle — this project has used that pattern before and it materially helps
resume context across sessions without local build state to inspect.

**Treat the Actions log as the only debugger you have.** When a build fails:
1. Read the failing step's full output, not just the last few lines —
   Gradle often prints the real cause several lines above the final
   "BUILD FAILED".
2. Reproduce narrowly: re-run just the failing Gradle task via
   `workflow_dispatch` with a task-name input, rather than re-running the
   whole pipeline, if you've wired that in.
3. Prefer fixing root cause (version mismatch, missing dependency, wrong
   API for the pinned library version) over broad try/catch-style workarounds
   like blanket `--continue` or suppressing lint categories.

**Guard against flaky infra, not just code.** Add `timeout-minutes` to every
job (a hang otherwise burns Actions minutes silently) and consider a retry
step (`nick-invision/retry` or similar) only around genuinely flaky external
steps (e.g. dependency downloads), never around the actual compile/test
step — a flaky compile step is a real bug, not infra noise.

## When extending an existing app

- Read `app/build.gradle.kts`, `gradle/libs.versions.toml` (or root
  `build.gradle.kts` if no version catalog), and the existing `Theme.kt`
  before adding anything, so new code matches pinned versions and existing
  Material 3 Expressive theming rather than introducing a second theme
  source or a conflicting library version.
- Check `.github/workflows/` for existing job names/triggers before adding a
  new workflow file — prefer extending the existing one over creating a
  parallel, slightly-different pipeline.
