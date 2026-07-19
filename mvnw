#!/usr/bin/env sh
# Licensed to the Apache Software Foundation (ASF) under the Apache License, Version 2.0.
set -eu

BASE_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
PROPERTIES_FILE="$BASE_DIR/.mvn/wrapper/maven-wrapper.properties"

if [ ! -r "$PROPERTIES_FILE" ]; then
  echo "Missing Maven Wrapper properties: $PROPERTIES_FILE" >&2
  exit 1
fi

DISTRIBUTION_URL=$(sed -n 's/^distributionUrl=//p' "$PROPERTIES_FILE" | head -n 1)
if [ -z "$DISTRIBUTION_URL" ]; then
  echo "distributionUrl is missing from $PROPERTIES_FILE" >&2
  exit 1
fi

DISTRIBUTION_NAME=$(basename "$DISTRIBUTION_URL" .zip)
MAVEN_DIRECTORY_NAME=$(printf '%s' "$DISTRIBUTION_NAME" | sed 's/-bin$//')
WRAPPER_HOME="${MAVEN_USER_HOME:-$HOME/.m2}/wrapper/dists/$DISTRIBUTION_NAME"
MAVEN_HOME="$WRAPPER_HOME/$MAVEN_DIRECTORY_NAME"

if [ ! -x "$MAVEN_HOME/bin/mvn" ]; then
  mkdir -p "$WRAPPER_HOME"
  ARCHIVE="$WRAPPER_HOME/$DISTRIBUTION_NAME.zip"
  TEMP_DIR=$(mktemp -d "$WRAPPER_HOME/.download.XXXXXX")
  trap 'rm -rf "$TEMP_DIR"' EXIT HUP INT TERM

  if command -v curl >/dev/null 2>&1; then
    curl --fail --location --retry 3 --output "$ARCHIVE" "$DISTRIBUTION_URL"
  elif command -v wget >/dev/null 2>&1; then
    wget --tries=3 --output-document="$ARCHIVE" "$DISTRIBUTION_URL"
  else
    echo "Maven Wrapper requires curl or wget to download Maven." >&2
    exit 1
  fi

  unzip -q "$ARCHIVE" -d "$TEMP_DIR"
  mv "$TEMP_DIR/$MAVEN_DIRECTORY_NAME" "$MAVEN_HOME"
fi

exec "$MAVEN_HOME/bin/mvn" "$@"
