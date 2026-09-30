# Upgrade plan

## Current state

Score: 5/10 (was 4/10) — tested pure-Kotlin domain core and honest CI; the
Compose UI is still a placeholder and the Android build is unverified locally.

## Backlog

- P0: Confirm the `android` CI job is green (AGP 8.5 + Kotlin 2.0.21 Compose
  plugin + material dependency were fixed statically, without an Android SDK).
- P0: Replace the placeholder Home/Explore screens with UI backed by `core`
  (ViewModel + state holder calling the `core` API).
- P1: Load the project catalog from the portfolio site's JSON (or bundle a snapshot) and run `validate` in a unit test against it.
- P1: Project detail screen with links opened via Custom Tabs.
- P2: Compose UI tests (`androidx.compose.ui:ui-test-junit4`) for the main flow.
- P2: Pin `distributionSha256Sum` in `gradle-wrapper.properties`.

## Done in this pass (pass 1)

- Added `core/` (included Gradle build) with the app's domain logic and 4 unit tests.
- Committed the Gradle wrapper (8.10.2).
- Fixed Kotlin 2.0 Compose setup: `org.jetbrains.kotlin.plugin.compose`
  replaces the removed `composeOptions.kotlinCompilerExtensionVersion`; Java/Kotlin
  target 17; added `com.google.android.material` for the manifest theme.
- CI: removed `|| true`; separate `core` and `android` jobs.
- README now states what is verified and what is not.

## Done in this pass (pass 2)

- Fixed validation: `https://` with no host (or with spaces) passed the link check; blank-only tech tags passed "no tech tags". Links are now parsed as URIs and must be https with a host.
- Fixed showcase filter: a blank tech filter hid every project, and padded values (" kotlin") matched nothing; blank now means no filter and tags are compared trimmed.
- 6 more edge-case tests (10 total). Verified with `./gradlew -p core test --offline`.
- Still not verified locally: the Compose `app/` (no Android SDK; dl.google.com is blocked here).
