#!/bin/bash

# Start logging
echo "Starting to search for .env or env files in the repository's history..."

# Counter for found instances
found_count=0

# Loop through each commit hash from the git reflog
for hash in $(git reflog --pretty=format:'%h'); do
    # Checkout each commit quietly
    git checkout $hash --quiet
    
    # Log current action
    echo "Checking commit $hash..."

    # Check for .env file
    if [ -f ".env" ]; then
        echo "Found .env in commit $hash"
        ((found_count++))
    fi

    # Check for env file
    if [ -f "env" ]; then
        echo "Found env in commit $hash"
        ((found_count++))
    fi
done

# Log the total count of found instances
echo "Total instances found: $found_count"

# Checkout back to the master branch
git checkout master --quiet

# End logging
echo "Search completed. Returning to the master branch."

