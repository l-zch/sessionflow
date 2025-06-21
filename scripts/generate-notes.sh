#!/bin/bash

# Generates release notes markdown for the GitHub Action.
#
# This script reads the following environment variables, which should be
# set by the 'update-webapp.sh --ci' step in the workflow:
# - FRONTEND_VERSION: The version tag of the included webapp release.
# - FRONTEND_RELEASE_URL: The URL to the webapp release page on GitHub.

set -e # Exit immediately if a command exits with a non-zero status.

# --- Validation ---
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
This is an automated release.

This build incorporates webapp assets from the **[$WEBAPP_VERSION]($FRONTEND_RELEASE_URL)** release.
The attached JAR file contains the built application with the updated webapp assets.
EOF