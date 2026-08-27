# The Gradle Wrapper is always the recommended way to execute a build to ensure a reliable, controlled, and standardized execution of the build.
./gradlew build

./gradlew clean build

./gradlew test

./gradlew --version

./gradlew build --build-cache

./gradlew build --no-build-cache

./gradlew --help

./gradlew -h

# Gradle will output the dependency tree, grouped by configuration:
./gradlew :app:dependencies
