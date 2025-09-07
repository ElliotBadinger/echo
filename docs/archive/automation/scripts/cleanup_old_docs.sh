#!/bin/bash

# Documentation Migration Cleanup Script
# Version: 2.0 - Unified Documentation System
# Purpose: Clean up old documentation files after successful migration

set -e

echo "🧹 Starting Documentation Migration Cleanup..."
echo "================================================"

# Define the project root
PROJECT_ROOT="/home/siya/git/echo"
cd "$PROJECT_ROOT"

# List of old documentation files to remove
OLD_DOCS=(
    "AGENT_WORKFLOW_GUIDE.md"
    "AGENT_DOCUMENTATION.md"
    "AGENT_SESSION_CHECKLIST.md"
    "KOTLIN_MIGRATION_FRAMEWORK.md"
    "UI_UX_ENHANCEMENT_FRAMEWORK.md"
    "RESEARCH_FRAMEWORK.md"
    "ML_STRATEGY_FRAMEWORK.md"
    "PERFORMANCE_RESEARCH_FRAMEWORK.md"
)

# Backup old files before removal (safety measure)
BACKUP_DIR="docs/automation/backup/old-docs-$(date +%Y%m%d-%H%M%S)"
echo "📦 Creating backup directory: $BACKUP_DIR"
mkdir -p "$BACKUP_DIR"

# Backup and remove old documentation files
echo "🔄 Processing old documentation files..."
for file in "${OLD_DOCS[@]}"; do
    if [ -f "$file" ]; then
        echo "  📋 Backing up: $file"
        cp "$file" "$BACKUP_DIR/"
        
        echo "  🗑️  Removing: $file"
        rm "$file"
        
        echo "  ✅ Completed: $file"
    else
        echo "  ⚠️  Not found: $file (already removed)"
    fi
done

# Update README.md to reference new documentation structure
echo "📝 Updating project README.md..."
if [ -f "README.md" ]; then
    # Create backup of README
    cp "README.md" "$BACKUP_DIR/README.md.backup"
    
    # Update README with new documentation references
    sed -i 's|AGENT_WORKFLOW_GUIDE\.md|docs/agent-workflow/detailed-guide.md|g' README.md
    sed -i 's|AGENT_DOCUMENTATION\.md|docs/project-state/current-status.md|g' README.md
    sed -i 's|AGENT_SESSION_CHECKLIST\.md|docs/agent-workflow/session-checklist.md|g' README.md
    
    # Add documentation section if not present
    if ! grep -q "## 📚 Documentation" README.md; then
        cat >> README.md << 'EOF'

## 📚 Documentation

This project uses a unified documentation system for efficient knowledge management:

- **[Documentation Hub](docs/README.md)** - Central navigation and overview
- **[Agent Quick Start](docs/agent-workflow/quick-start.md)** - 15-minute agent onboarding
- **[Current Status](docs/project-state/current-status.md)** - Live project state
- **[Development Frameworks](docs/frameworks/)** - Technical frameworks and methodologies
- **[MCP Integration](docs/mcp-integration/)** - MCP server optimization guides

### Quick Navigation
- 🚀 **New to the project?** Start with [Agent Quick Start](docs/agent-workflow/quick-start.md)
- 📊 **Current project state?** Check [Current Status](docs/project-state/current-status.md)
- 🔧 **Development workflow?** See [Session Checklist](docs/agent-workflow/session-checklist.md)
- 🤖 **MCP optimization?** Review [MCP Integration](docs/mcp-integration/mcp-optimization.md)

EOF
    fi
    
    echo "  ✅ README.md updated with new documentation structure"
else
    echo "  ⚠️  README.md not found - skipping update"
fi

# Check for any remaining references to old documentation files
echo "🔍 Checking for remaining references to old documentation files..."
REMAINING_REFS=0

for file in "${OLD_DOCS[@]}"; do
    # Search for references in all text files (excluding backup directory)
    refs=$(find . -type f \( -name "*.md" -o -name "*.kt" -o -name "*.java" -o -name "*.gradle" -o -name "*.yml" -o -name "*.yaml" \) \
           -not -path "./docs/automation/backup/*" \
           -exec grep -l "$file" {} \; 2>/dev/null || true)
    
    if [ -n "$refs" ]; then
        echo "  ⚠️  References to $file found in:"
        echo "$refs" | sed 's/^/    - /'
        REMAINING_REFS=$((REMAINING_REFS + 1))
    fi
done

if [ $REMAINING_REFS -eq 0 ]; then
    echo "  ✅ No remaining references found"
else
    echo "  ⚠️  Found $REMAINING_REFS files with references to old documentation"
    echo "     These should be manually reviewed and updated"
fi

# Validate new documentation structure
echo "🔍 Validating new documentation structure..."
REQUIRED_DIRS=(
    "docs/agent-workflow"
    "docs/project-state"
    "docs/frameworks"
    "docs/mcp-integration"
    "docs/templates"
    "docs/automation"
)

REQUIRED_FILES=(
    "docs/README.md"
    "docs/agent-workflow/quick-start.md"
    "docs/agent-workflow/session-checklist.md"
    "docs/agent-workflow/detailed-guide.md"
    "docs/project-state/current-status.md"
    "docs/project-state/change-log.md"
    "docs/project-state/priorities.md"
    "docs/frameworks/kotlin-migration.md"
    "docs/frameworks/ui-ux-framework.md"
    "docs/frameworks/research-framework.md"
    "docs/mcp-integration/context7-guide.md"
    "docs/mcp-integration/brave-search-guide.md"
    "docs/mcp-integration/mcp-optimization.md"
)

echo "📁 Checking required directories..."
for dir in "${REQUIRED_DIRS[@]}"; do
    if [ -d "$dir" ]; then
        echo "  ✅ $dir"
    else
        echo "  ❌ $dir (MISSING)"
    fi
done

echo "📄 Checking required files..."
for file in "${REQUIRED_FILES[@]}"; do
    if [ -f "$file" ]; then
        echo "  ✅ $file"
    else
        echo "  ❌ $file (MISSING)"
    fi
done

# Generate cleanup summary
echo ""
echo "🎉 Documentation Migration Cleanup Summary"
echo "=========================================="
echo "📦 Backup created: $BACKUP_DIR"
echo "🗑️  Old files removed: ${#OLD_DOCS[@]}"
echo "📝 README.md updated: ✅"
echo "🔍 Remaining references: $REMAINING_REFS"
echo ""

if [ $REMAINING_REFS -eq 0 ]; then
    echo "✅ CLEANUP COMPLETED SUCCESSFULLY"
    echo "   All old documentation files removed and references updated"
    echo "   New unified documentation system is ready for use"
else
    echo "⚠️  CLEANUP PARTIALLY COMPLETED"
    echo "   Manual review required for remaining references"
    echo "   Check the files listed above and update references to new structure"
fi

echo ""
echo "📚 New Documentation Structure:"
echo "   - Central Hub: docs/README.md"
echo "   - Agent Onboarding: docs/agent-workflow/quick-start.md"
echo "   - Current Status: docs/project-state/current-status.md"
echo "   - Development Frameworks: docs/frameworks/"
echo "   - MCP Integration: docs/mcp-integration/"
echo ""
echo "🚀 Next Steps:"
echo "   1. Review any remaining references and update them"
echo "   2. Start using the new documentation system"
echo "   3. Begin implementing MCP optimization targets"
echo "   4. Train team on new documentation structure"
echo ""
echo "Migration cleanup completed at $(date)"