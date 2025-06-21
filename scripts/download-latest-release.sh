#!/bin/bash
# This script downloads the latest release of sessionflow.jar from the l-zch/sessionflow GitHub repository.

set -e

REPO="l-zch/sessionflow"
API_URL="https://api.github.com/repos/$REPO/releases/latest"
ASSET_NAME="sessionflow.jar"

echo "Fetching the latest release information from $REPO..."

# Use curl to get the latest release data, then use grep and cut to extract the download URL.
# The 'jq' tool would be cleaner, but this approach avoids an extra dependency.
DOWNLOAD_URL=$(curl -s "$API_URL" | grep "browser_download_url" | grep "$ASSET_NAME" | head -n 1 | cut -d '"' -f 4)

if [ -z "$DOWNLOAD_URL" ]; then
  echo "Error: Could not find a download URL for '$ASSET_NAME'."
  echo "Please check the releases page manually: https://github.com/$REPO/releases"
  exit 1
fi

echo "Found download URL: $DOWNLOAD_URL"
echo "Downloading '$ASSET_NAME'..."

# Download the file using curl, with -L to follow redirects.
curl -L -o "$ASSET_NAME" "$DOWNLOAD_URL"

echo ""
echo "Download complete!"
echo "You can now run the application with the following command:"
echo "  java -jar $ASSET_NAME" 