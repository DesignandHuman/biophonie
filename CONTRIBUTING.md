# Contributing to Biophonie
We welcome contributions to this project.

If you would like to suggest a [new feature](https://github.com/DesignandHuman/biophonie/issues/new?assignees=Haransis&labels=%3Asparkles%3A+enhancement&projects=&template=feature_request.md&title=) or report a [bug](https://github.com/DesignandHuman/biophonie/issues/new?assignees=Haransis&labels=%3Abug%3A++bug&projects=&template=bug_report.md&title=), fill the provided template when creating an issue and take a look at the contributing section.

If you would like to contribute code:
1. Please note that any contribution should follow the [code of conduct](https://github.com/DesignandHuman/biophonie/blob/master/CODE_OF_CONDUCT.md)
2. Please familiarize yourself with the install process, the development process and the deployment at the bottom of this document. All contributions to the project must reflect its development standards. As such, respect of `ktfmt` and `detekt` is required.
3. Ensure that existing pull requests and issues don’t already cover your contribution or question. If your planned contribution involves significant changes to the application, consider opening a new issue to discuss your design before beginning work.
4. Pull requests are gladly accepted. If your PR makes changes that developers should be aware of, please be sure to update the [CHANGELOG](https://github.com/DesignandHuman/biophonie/blob/master/CHANGELOG.md)
5. When contributing to this project, you must agree that you have authored 100% of the content, that you have the necessary rights to the content and that the content you contribute may be provided under the project license.

## Build locally

### Prerequisite
The Biophonie application heavily relies on Mapbox SDK. 
Thus, building this repository requires to have an account on [Mapbox](https://account.mapbox.com/).

### Setting up the repository
Clone the git repository:

```
$ git clone git@github.com:DesignandHuman/biophonie.git
```

### Using soundwave
Soundwave is a library used to play and record sounds in the shape of wave forms. It is mandatory to build it alongside Biophonie so, you will need to clone the repository:
```
$ git clone git@github.com:Haransis/WaveFormPlayer.git
```
Then you will need to set the [Soundwave Directory](#soundwave-directory).

_Note :construction: : In the future, this dependency will be made available in a Maven Repository._ 

### Configuring the project
Before building, the project needs to be configured. A couple of configurations need to be in place before
a successful build and test run can be made. Define the following environment variables in the global Gradle Properties (located in `$USER_HOME/.gradle/gradle.properties`).
The following steps describe how to define the related environmental variables needed to build the project:
* [`MAPBOX_DOWNLOAD_TOKEN`](#mapbox-download-token)
* [`MAPBOX_ACCESS_TOKEN`](#mapbox-access-token)
* [`STYLE_URL`](#mapbox-style-url)
* [`SOUNDWAVE_DIRECTORY`](#soundwave-directory)
* [`BIOPHONIE_DEBUG_API_URL`](#debug-api-url)

#### Mapbox Download Token
The `MAPBOX_DOWNLOAD_TOKEN` is a Mapbox access token, used during compile time, with a scope set to `DOWNLOADS:READ`.
This token allows to download all required Mapbox dependencies from a Mapbox Maven instance.
The `DOWNLOADS:READ` scope can be set when creating a new access token on https://account.mapbox.com/.
The token configuration can be found in the root `build.gradle.kts` of the project.

#### Mapbox Access token
The `MAPBOX_ACCESS_TOKEN` is a mapbox access token, packaged as part of test application, to load Mapbox tiles and resources.
This token can be configured on https://account.mapbox.com/ and doesn't require any specific scopes.

#### Mapbox style URL
The `STYLE_URL` is a URL pointing to a mapbox style. It is of the form: `mapbox://styles/OWNER/HASH`
The style used in the production app is based upon the [creation of Geoffrey Dorne](https://api.mapbox.com/styles/v1/geoffreydorne/cjy30xoii1tid1crv9bz19hct.html?title=view&access_token=pk.eyJ1IjoiZ2VvZmZyZXlkb3JuZSIsImEiOiJpTHBzT3l3In0.9emvgijE_t5EXwWrfemapA&zoomwheel=true&fresh=true#14.04/48.88576/7.1762)
but you can use any style such as a standard one: `mapbox://styles/mapbox/light-v11`.

#### Soundwave Directory
The `SOUNDWAVE_DIR` is the path to the directory containing the soundwave library. 
It can be found in the [WaveFormPlayer repository](https://github.com/Haransis/WaveFormPlayer/tree/master/soundwave) (see also [Installing the dependency](#installing-the-dependency)).

#### Debug API URL
This `BIOPHONIE_DEBUG_API_URL` is a URL pointing to the external API you want to use for debug version (see [Communicate with the remote API](#communicate-with-the-remote-api) to learn how to use your own).
The API is needed to find geolocated sounds, record and upload them. You can find the code of the api [here](https://github.com/Haransis/biophonie-api).

#### example global `gradle.properties`
```properties
MAPBOX_DOWNLOAD_TOKEN=sk.eyxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
MAPBOX_ACCESS_TOKEN=pk.eyxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
BIOPHONIE_STYLE_URL=mapbox://styles/user/xxxxxxxxxxxxxxxxxxxxx
SOUNDWAVE_DIR=/home/user/AndroidStudioProjects/WaveFormPlayer/soundwave
# use a local instance of biophonie-api from an emulator
BIOPHONIE_DEBUG_API_URL=http://10.0.2.2:8080
```

## Communicate with the remote API
The production API is located at https://biophonie.fr/.
The preproduction API will be soon available (track progress [here](https://github.com/DesignandHuman/biophonie/issues/33)).
To do your own testing, please clone the [biophonie-api repository](https://github.com/Haransis/biophonie-api) and launch your own instance of it using docker.

## Checks
A precommit hook checks for the respect of [`ktfmt`](https://github.com/facebook/ktfmt) and [`detekt`](https://github.com/detekt/detekt).
Automatic formatting is done using the command:
```
$ ./gradlew ktfmtFormat
```
You can add exceptions to `detekt` with: `app/detekt-baseline.xml` or with `@SupressWarnings("RULENAME")`.
:warning: Beware, changes that do not justify any exception to linters will be automatically rejected.

Other than that, the project do not have any testing implemented yet (track progress [here](https://github.com/DesignandHuman/biophonie/issues/34)).
In the meantime, you should test your modifications by hand.

## Architecture
The project follows the recommended Android architecture guidelines and especially the type of modules described [here](https://developer.android.com/topic/modularization/patterns#types-of-modules).
Module dependency graphs are available in the README of each module.
If architecture changes are required, execution of `scripts/generateModuleGraphs.sh` regenerates the corresponding graphs.

## Commit
This repository follows the [gitmoji](https://github.com/carloscuesta/gitmoji) convention. 
A commit title should start with an emoji. See the [website](https://gitmoji.dev/) or use the [gitmoji-cli](https://github.com/carloscuesta/gitmoji-cli) to find the adequate emoji. Here are the main emojis that should be used but feel free to use the ones [listed](https://gitmoji.dev/) that fit better your needs:
* :sparkles: : new feature
* :lipstick: : ui change
* :recycle: : refactoring
* :bug: : bug fix
* :memo: : documentation change

Next is a sentence starting with a verb starting with a minuscule letter and ending without any dot. The title should be as precise as possible.

Optionnaly, a commit message can be added. It should tell a story about the what, how and why a change is happening. It can also contain the issue that it solves.

Wrong commit title `Fix issue with crash of application.`

Correct commit title `:bug: init imageUri to prevent NullPointerEx`

## Pull Requests
Pull requests should follow the [corresponding template](https://github.com/DesignandHuman/biophonie/tree/master/.github/PULL_REQUEST/pull_request_template.md).
Their title should follow the same convention as the [commits](#commit).
