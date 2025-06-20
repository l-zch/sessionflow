#!/bin/bash

# Generates release notes markdown for the GitHub Action.
#
# This script reads the following environment variables, which should be
# set by the 'update-frontend.sh --ci' step in the workflow:
# - GITHUB_REF_NAME: The name of the tag being released (e.g., v1.2.3).
# - FRONTEND_VERSION: The version tag of the included frontend release.
# - FRONTEND_RELEASE_URL: The URL to the frontend release page on GitHub.

set -e # Exit immediately if a command exits with a non-zero status.

# --- Validation ---
if [ -z "$GITHUB_REF_NAME" ]; then
  echo "Error: GITHUB_REF_NAME environment variable is not set." >&2
  exit 1
fi
if [ -z "$FRONTEND_VERSION" ]; then
  echo "Error: FRONTEND_VERSION environment variable is not set." >&2
  exit 1
fi
if [ -z "$FRONTEND_RELEASE_URL" ]; then
  echo "Error: FRONTEND_RELEASE_URL environment variable is not set." >&2
  exit 1
fi

# --- Generate Markdown Output ---
cat << EOF
## Release: $GITHUB_REF_NAME

This is an automated release.

This build incorporates frontend assets from the **[$FRONTEND_VERSION]($FRONTEND_RELEASE_URL)** release.
The attached JAR file contains the built application with the updated frontend assets.
EOF