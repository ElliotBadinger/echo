#!/bin/bash
# Simple automated logging - just the basics

# Get recent changes
echo "## $(date '+%Y-%m-%d %H:%M') - Auto-generated from git" >> docs/daily-activity.md
echo "" >> docs/daily-activity.md
echo "### Recent Changes" >> docs/daily-activity.md
git log --oneline --since="1 day ago" | head -5 >> docs/daily-activity.md
echo "" >> docs/daily-activity.md
echo "### Files Changed" >> docs/daily-activity.md
git diff --name-only HEAD~1 HEAD | head -10 >> docs/daily-activity.md
echo "" >> docs/daily-activity.md
echo "---" >> docs/daily-activity.md
echo "" >> docs/daily-activity.md