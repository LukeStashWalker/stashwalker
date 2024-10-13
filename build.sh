#!/bin/bash

# Run the Gradle clean build command
./gradlew clean build

# Set the source and destination directories
SOURCE_DIR="./build/libs/"
DEST_DIR="$APPDATA/.minecraft/mods/"

# Delete any files containing 'stashwalker' in the destination directory
for file in "$DEST_DIR"*; do
  if [[ "$file" == *stashwalker* ]]; then
    rm "$file"
    echo "Deleted $file"
  fi
done

# Copy files from SOURCE_DIR to DEST_DIR, excluding those with 'sources' in the filename
for file in "$SOURCE_DIR"*; do
  if [[ "$file" != *sources* ]]; then
    cp "$file" "$DEST_DIR"
  fi
done

echo "Files copied successfully to $DEST_DIR"
