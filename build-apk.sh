#!/usr/bin/env bash
set -euo pipefail

# Build LoanConnect APK on any Linux CI/build service with Java 21 + Gradle 8.13.
gradle --no-daemon assembleDebug

echo "APK created at: app/build/outputs/apk/debug/app-debug.apk"
