#!/usr/bin/env bash


if test "$TRAVIS_PULL_REQUEST" = "false"
then
    if [ -z "$TRAVIS_TAG" ];
    then
        echo "Running tests ..."
        ./gradlew test
    else
        echo "Deploying to bintray"
        ./gradlew bintrayUpload
    fi
else
    echo "Running tests ..."
    ./gradlew test
fi
