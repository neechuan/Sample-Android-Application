# The Gradle Wrapper is always the recommended way to execute a build to ensure a reliable, controlled, and standardized execution of the build.
./gradlew build

# List available tasks
./gradlew tasks

./gradlew run

./gradlew clean build

./gradlew compileDebugAndroidTestJavaWithJavac --console=verbose

./gradlew compileDebugJavaWithJavac --console=verbose

./gradlew compileDebugUnitTestJavaWithJavac --console=verbose

./gradlew compileReleaseJavaWithJavac --console=verbose

./gradlew compileReleaseUnitTestJavaWithJavac --console=verbose

./gradlew test

./gradlew --version

./gradlew build --build-cache

./gradlew build --no-build-cache

./gradlew --help

./gradlew -h

# Gradle will output the dependency tree, grouped by configuration:
./gradlew :app:dependencies
