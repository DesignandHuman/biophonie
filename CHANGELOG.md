# Changelog for Biophonie for Android

We welcome participation and contributions from everyone (see [CONTRIBUTING.md](https://github.com/DesignandHuman/biophonie/blob/master/CONTRIBUTING.md)).

# December 3rd, 2024

## Features ✨ and improvements 🏁
* Refactor map ui layer of :feature:exploregeopoints to use mapbox compose extensions
* Update Mapbox SDK
* Use a location data source with callbackFlow
* Save CameraState in user preferences when leaving the screen
* Add unitTests for viewModel
* Add instrumentedTests using screenshot testing for Map exploration

# July 1, 2024

## Features ✨ and improvements 🏁
* modularize Biophonie application
* add module dependency graphs (generated using `scripts/generateModuleGraphs.sh` from https://github.com/android/nowinandroid/)
* add documentation for modularization graphs
* create convention plugins for:
  * Android Application
  * Android Library
  * BuildConfig
  * Feature
  * Hilt
  * Lint
* refactor TutorialRepository to use a proper UserRepository
* improve performance of ktfmt hook

# May 1, 2024

## Features ✨ and improvements 🏁
* use Hilt for dependency injection

# April 2, 2024

## Bug fixes 🐞
* fix wrong order on first launch fragments

# March 8, 2024

## Features ✨ and improvements 🏁
* harmonize `GalleryFragment` margins

# February 12, 2024

## Features ✨ and improvements 🏁
* add ktfmt
* add detekt
* add git precommit hook to check respect of ktfmt and detekt standard rules
* format code to meet ktfmt standards
* refactor code to respect detekt rules
* add baseline file to temporarily exclude `TooManyFunctions` from detekt
* add mentions to linters in `CONTRIBUTING.md`

## Dependencies
* gradle (8.2 -> 8.3)

# Draft - January 25, 2024
## Breaking changes ⚠️
* Delete section if empty

## Features ✨ and improvements 🏁
* add a `CONTRIBUTING.md`
* add a `CHANGELOG.md`
* add a `CODE_OF_CONDUCT.md`
* add a PR template
* update README to link to issue templates

## Bug fixes 🐞
* Delete section if empty

## Dependencies
* Delete section if empty