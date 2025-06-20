#!/bin/bash

# A script to automatically download and update the frontend assets
# from the latest release of the sessionflow-frontend GitHub repository.
#
# Dependencies: curl, jq, unzip
# Usage: ./update-frontend.sh

# --- Configuration ---
REPO="l-zch/sessionflow-frontend"
API_URL="https://api.github.com/repos/$REPO/releases/latest"
TARGET_DIR="src/main/resources/static/sessionflowapp"
VERSION_FILE="$TARGET_DIR/version.txt"
TMP_DIR=$(mktemp -d) # Create a temporary directory for our work

# --- Color Codes for Output ---
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# --- Cleanup function to be called on exit ---
cleanup() {
  echo -e "${YELLOW}✨ Cleaning up temporary directory...${NC}"
  rm -rf "$TMP_DIR"
}
trap cleanup EXIT # Register cleanup function to run on script exit

# --- Function to check for required commands ---
check_command() {
    if ! command -v "$1" &> /dev/null; then
        echo -e "${RED}❌ Error: Required command '$1' is not installed. Please install it to continue.${NC}"
        exit 1
    fi
}

# --- Main Script Logic ---
echo -e "${YELLOW}🚀 Starting frontend update process for repository: $REPO...${NC}"

# 1. Check for dependencies
check_command curl
check_command jq
check_command unzip

# 2. Ensure the script is run from the project root
if [ ! -d "src/main/resources/static" ]; then
    echo -e "${RED}❌ Error: This script must be run from the root directory of the 'sessionflow' project.${NC}"
    exit 1
fi
echo -e "✅ Running from project root."

# 3. Fetch latest release information from GitHub API
echo -e "${YELLOW}📡 Fetching latest release information...${NC}"
RELEASE_INFO=$(curl -sL "$API_URL")

# Check if API call was successful
if [ -z "$RELEASE_INFO" ] || echo "$RELEASE_INFO" | jq -e '.message' > /dev/null; then
    ERROR_MSG=$(echo "$RELEASE_INFO" | jq -r '.message')
    echo -e "${RED}❌ Error fetching release info: $ERROR_MSG${NC}"
    echo -e "${RED}   Please check the repository name or your network connection.${NC}"
    exit 1
fi

# 4. Get the latest release tag and compare with the local version
LATEST_TAG=$(echo "$RELEASE_INFO" | jq -r '.tag_name')
CURRENT_VERSION=""
if [ -f "$VERSION_FILE" ]; then
    CURRENT_VERSION=$(cat "$VERSION_FILE")
fi

if [ "$LATEST_TAG" == "$CURRENT_VERSION" ]; then
    echo -e "${GREEN}✅ Frontend is already up to date (version $LATEST_TAG). Nothing to do.${NC}"
    exit 0
fi

echo -e "ℹ️  New version available. Current: ${YELLOW}${CURRENT_VERSION:-'none'}${NC}, Latest: ${GREEN}$LATEST_TAG${NC}."

# 5. Parse the download URL for the zip asset
DOWNLOAD_URL=$(echo "$RELEASE_INFO" | jq -r '.assets[] | select(.name | endswith(".zip")) | .browser_download_url')

if [ -z "$DOWNLOAD_URL" ] || [ "$DOWNLOAD_URL" == "null" ]; then
    echo -e "${RED}❌ Error: No .zip asset found in the latest release ($LATEST_TAG).${NC}"
    exit 1
fi
echo -e "✅ Found release asset for tag: ${GREEN}$LATEST_TAG${NC}"

# 6. Download the release asset
ZIP_FILE="$TMP_DIR/frontend.zip"
echo -e "${YELLOW}⏬ Downloading asset...${NC}"
curl -sL "$DOWNLOAD_URL" -o "$ZIP_FILE"

# Verify download
if [ ! -s "$ZIP_FILE" ]; then
    echo -e "${RED}❌ Error: Failed to download the asset or the file is empty.${NC}"
    exit 1
fi

# 7. Clean the target directory
echo -e "${YELLOW}🧹 Cleaning target directory: $TARGET_DIR...${NC}"
mkdir -p "$TARGET_DIR" # Ensure target directory exists just in case
rm -rf "${TARGET_DIR:?}"/* # The :? protects from deleting root if var is empty

# 8. Unzip the asset into the temp directory
echo -e "${YELLOW}📦 Unzipping asset...${NC}"
unzip -q "$ZIP_FILE" -d "$TMP_DIR"

# Per user spec, the content is in a 'dist' directory inside the zip.
SOURCE_PATH="$TMP_DIR/dist"
if [ ! -d "$SOURCE_PATH" ]; then
    echo -e "${RED}❌ Error: Unzipped content does not contain the expected 'dist' directory.${NC}"
    echo "Contents of temporary directory:"
    ls -la "$TMP_DIR"
    exit 1
fi

# 9. Copy the new files to the target directory
echo -e "${YELLOW}🚚 Copying new frontend files to $TARGET_DIR...${NC}"
# Use `.` at the end of source path to copy contents, including hidden files.
cp -r "$SOURCE_PATH"/. "$TARGET_DIR"/

# 10. Write new version to file
echo -e "${YELLOW}📝 Writing new version tag to $VERSION_FILE...${NC}"
echo -n "$LATEST_TAG" > "$VERSION_FILE"

echo -e "\n${GREEN}🎉 Frontend update complete! Successfully updated to version $LATEST_TAG.${NC}"

exit 0 