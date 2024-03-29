# FileLoader

[![Maven Central](https://img.shields.io/maven-central/v/io.github.mobile-development-group/fileloader.svg?label=Maven%20Central)](https://central.sonatype.com/namespace/io.github.mobile-development-group)

Library for convenient downloading of files on Android

## Dependency

Make sure to add Maven Central to your repositories declarations:

```groovy
repositories {
    mavenCentral()
}
```

then add the latest ComposeCalendar version to your `app/build.gradle.kts` file dependencies:

```groovy
dependencies {
    implementation("io.github.mobile-development-group:fileloader:$version")
}
```

## How to use

```kotlin
val fileDownloader = FileLoader(this)

val uuid = fileDownloader.load(
    url = "https://raw.githubusercontent.com/mobile-development-group/fileloader/main/assets/kittens.jpeg",
    directoryName = Environment.DIRECTORY_DOWNLOADS,
    directoryType = FileDownloader.DIR_EXTERNAL_PUBLIC
)

// Flow
fileDownloader.getWorkInfoByIdFlow(uuid)
    .onEach {
        val uris = it.outputData.getStringArray(FileLoader.OUTPUT_URIS)?.toList()
            ?: emptyList()
    }
    .launchIn(this)

// LiveData
fileDownloader.getWorkInfoByIdLiveData(uuid).observe(this) {
    val uris = it.outputData.getStringArray(FileLoader.OUTPUT_URIS)?.toList()
}
```

## License
This project is licensed under the Apache-2.0 License - see the [LICENSE](LICENSE.txt) file for details.

```
Copyright 2024 FileLoader Contributors

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```