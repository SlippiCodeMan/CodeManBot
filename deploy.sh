#!/bin/bash
# This script is meant to be accessed by github actions.

rootDir="$(git rev-parse --show-toplevel)"
gitBranch="$(git branch --show-current)"

"$rootDir"/gradlew shadowjar
if [ $? -eq 0 ]; then
    echo ":: ($gitBranch) OK"
    "$rootDir"/gradlew docker
    "$rootDir"/gradlew dockerStop
    "$rootDir"/gradlew dockerRun
    exit 0
else
    echo ":: ($gitBranch) FAIL"
    exit 1
fi