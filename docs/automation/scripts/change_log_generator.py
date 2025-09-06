#!/usr/bin/env python3
"""
Change Log Generator - Auto-generate change logs from git commits and MCP usage
Version: 2.0 - Unified Documentation System
"""

import os
import sys
import json
import yaml
import argparse
import logging
import subprocess
import re
from datetime import datetime, timedelta
from pathlib import Path
from typing import Dict, List, Optional, Any
from loguru import logger
import requests
from github import Github

# Configure logging
logger.remove()
logger.add(
    sys.stderr,
    format="<green>{time:YYYY-MM-DD HH:mm:ss}</green> | <level>{level: <8}</level> | <cyan>{name}</cyan>:<cyan>{function}</cyan>:<cyan>{line}</cyan> - <level>{message}</level>",
    level="INFO"
)

class ChangeLogGenerator:
    """Generate change logs from git commits and MCP usage data"""
    
    def __init__(self, config_path: str = "docs/automation/docs-config.yaml"):
        self.config_path = Path(config_path)
        self.config = self._load_config()
        self.repo_path = Path(".")
        self.change_log_path = Path("docs/project-state/change-log.md")
        
    def _load_config(self) -> Dict[str, Any]:
        """Load automation configuration"""
        try:
            with open(self.config_path, 'r') as f:
                return yaml.safe_load(f)
        except FileNotFoundError:
            logger.error(f"Configuration file not found: {self.config_path}")
            return {}
        except yaml.YAMLError as e:
            logger.error(f"Error parsing configuration: {e}")
            return {}
    
    def get_git_commits(self, since: str = "1 week ago") -> List[Dict[str, Any]]:
        """Get git commits since specified time"""
        try:
            # Get commits with detailed information
            cmd = [
                "git", "log",
                f"--since={since}",
                "--pretty=format:%H|%an|%ae|%ad|%s|%b",
                "--date=iso"
            ]
            
            result = subprocess.run(cmd, capture_output=True, text=True, cwd=self.repo_path)
            
            if result.returncode != 0:
                logger.error(f"Git command failed: {result.stderr}")
                return []
            
            commits = []
            for line in result.stdout.strip().split('\n'):
                if not line:
                    continue
                
                parts = line.split('|')
                if len(parts) >= 6:
                    commit_data = {
                        "hash": parts[0],
                        "author": parts[1],
                        "email": parts[2],
                        "date": parts[3],
                        "subject": parts[4],
                        "body": parts[5] if len(parts) > 5 else "",
                        "files_changed": self._get_commit_files(parts[0])
                    }
                    commits.append(commit_data)
            
            return commits
            
        except Exception as e:
            logger.error(f"Error getting git commits: {e}")
            return []
    
    def _get_commit_files(self, commit_hash: str) -> List[str]:
        """Get files changed in a specific commit"""
        try:
            cmd = ["git", "show", "--name-only", "--pretty=format:", commit_hash]
            result = subprocess.run(cmd, capture_output=True, text=True, cwd=self.repo_path)
            
            if result.returncode == 0:
                return [f.strip() for f in result.stdout.strip().split('\n') if f.strip()]
            return []
        except Exception:
            return []
    
    def analyze_commit_message(self, commit: Dict[str, Any]) -> Dict[str, Any]:
        """Analyze commit message for categorization and MCP usage"""
        analysis = {
            "category": "other",
            "priority": "low",
            "mcp_usage": [],
            "research_conducted": False,
            "testing_done": False
        }
        
        subject = commit["subject"].lower()
        body = commit["body"].lower()
        full_text = f"{subject} {body}"
        
        # Categorize based on keywords
        if any(word in full_text for word in ["fix", "bug", "error", "issue"]):
            analysis["category"] = "bug_fix"
            analysis["priority"] = "high"
        elif any(word in full_text for word in ["feature", "add", "implement"]):
            analysis["category"] = "feature"
            analysis["priority"] = "medium"
        elif any(word in full_text for word in ["test", "testing"]):
            analysis["category"] = "testing"
            analysis["priority"] = "medium"
        elif any(word in full_text for word in ["doc", "documentation", "readme"]):
            analysis["category"] = "documentation"
            analysis["priority"] = "low"
        elif any(word in full_text for word in ["refactor", "cleanup", "optimize"]):
            analysis["category"] = "refactoring"
            analysis["priority"] = "medium"
        
        # Check for MCP usage indicators
        mcp_indicators = {
            "brave search": "Brave Search MCP",
            "context7": "Context7 MCP", 
            "github mcp": "GitHub MCP",
            "playwright": "Playwright MCP"
        }
        
        for indicator, server in mcp_indicators.items():
            if indicator in full_text:
                analysis["mcp_usage"].append(server)
        
        # Check for research indicators
        research_indicators = ["research", "study", "investigate", "analyze", "sota"]
        if any(indicator in full_text for indicator in research_indicators):
            analysis["research_conducted"] = True
        
        # Check for testing indicators
        testing_indicators = ["test", "verify", "validate", "check"]
        if any(indicator in full_text for indicator in testing_indicators):
            analysis["testing_done"] = True
        
        return analysis
    
    def categorize_change(self, commit: Dict[str, Any], analysis: Dict[str, Any]) -> str:
        """Generate a categorized description of the change"""
        category = analysis["category"]
        subject = commit["subject"]
        
        # Generate category-specific description
        if category == "bug_fix":
            return f"Fixed issue: {subject}"
        elif category == "feature":
            return f"Added feature: {subject}"
        elif category == "testing":
            return f"Enhanced testing: {subject}"
        elif category == "documentation":
            return f"Updated documentation: {subject}"
        elif category == "refactoring":
            return f"Refactored: {subject}"
        else:
            return f"Change: {subject}"
    
    def generate_change_log_entry(self, commit: Dict[str, Any], analysis: Dict[str, Any]) -> str:
        """Generate a formatted change log entry"""
        # Format date
        try:
            commit_date = datetime.fromisoformat(commit["date"].replace(' ', 'T'))
            formatted_date = commit_date.strftime('%Y-%m-%d %H:%M')
        except:
            formatted_date = commit["date"]
        
        # Generate entry
        entry = f"""## Change {formatted_date} - {commit["hash"][:8]} - {analysis["category"].replace('_', ' ').title()}

### Goal
{self.categorize_change(commit, analysis)}

### Research Conducted
"""
        
        if analysis["research_conducted"]:
            entry += "- ✅ Research was conducted to inform this change\n"
            if analysis["mcp_usage"]:
                entry += f"- MCP Tools Used: {', '.join(analysis['mcp_usage'])}\n"
        else:
            entry += "- 🟡 No formal research documented\n"
        
        entry += f"""
### Files Modified
"""
        
        if commit["files_changed"]:
            for file_path in commit["files_changed"][:5]:  # Show first 5 files
                entry += f"- `{file_path}`\n"
            if len(commit["files_changed"]) > 5:
                entry += f"- ... and {len(commit['files_changed']) - 5} more files\n"
        else:
            entry += "- No files listed (commit analysis limitation)\n"
        
        entry += f"""
### Testing Done
"""
        
        if analysis["testing_done"]:
            entry += "- ✅ Testing was performed to validate the change\n"
        else:
            entry += "- 🟡 No testing explicitly documented\n"
        
        entry += f"""
### Result
- ✅ **SUCCESS**: {commit["subject"]}
- 📝 **Details**: {commit["body"] if commit["body"] else "No additional details"}
- 🔗 **Commit**: [{commit["hash"][:8]}](https://github.com/ElliotBadinger/echo/commit/{commit["hash"]})

### Next Steps
- Monitor for any issues related to this change
- Document any follow-up work needed

---
"""
        
        return entry
    
    def update_change_log(self, new_entries: List[str]) -> None:
        """Update the change log with new entries"""
        if not self.change_log_path.exists():
            # Create new change log file
            header = f"""# Project Change Log

**Version:** 2.0 - Unified Documentation System  
**Last Updated:** {datetime.utcnow().strftime('%Y-%m-%d %H:%M:%S')} UTC  
**Auto-generated:** This file is automatically updated by documentation automation

---

## Recent Changes

"""
            with open(self.change_log_path, 'w') as f:
                f.write(header)
        
        # Read existing content
        with open(self.change_log_path, 'r') as f:
            existing_content = f.read()
        
        # Insert new entries after the header
        header_end = existing_content.find("## Recent Changes")
        if header_end == -1:
            header_end = existing_content.find("---", existing_content.find("---") + 3)  # Find second ---
        
        if header_end != -1:
            # Find the end of the header section
            section_end = existing_content.find("\n## ", header_end + 1)
            if section_end == -1:
                section_end = len(existing_content)
            
            # Insert new entries
            new_content = (
                existing_content[:section_end] +
                "\n" + "\n".join(new_entries) +
                existing_content[section_end:]
            )
        else:
            # Append to end
            new_content = existing_content + "\n" + "\n".join(new_entries)
        
        # Update the file
        with open(self.change_log_path, 'w') as f:
            f.write(new_content)
        
        logger.info(f"Updated change log with {len(new_entries)} new entries")
    
    def update_project_status(self) -> None:
        """Update current project status based on recent changes"""
        status_file = Path("docs/project-state/current-status.md")
        
        if not status_file.exists():
            logger.error("Current status file not found")
            return
        
        # Get recent commits for status update
        recent_commits = self.get_git_commits("24 hours ago")
        
        if not recent_commits:
            logger.info("No recent commits to update status")
            return
        
        # Analyze recent activity
        latest_commit = recent_commits[0]
        analysis = self.analyze_commit_message(latest_commit)
        
        # Read current status file
        with open(status_file, 'r') as f:
            content = f.read()
        
        # Update last updated timestamp
        content = re.sub(
            r'^\*\*Last Updated:\*\*\s+.*$',
            f'**Last Updated:** {datetime.utcnow().strftime("%Y-%m-%d %H:%M:%S")} UTC',
            content,
            flags=re.MULTILINE
        )
        
        # Update current session workspace if found
        if "## 📋 Current Session Workspace" in content:
            # Find the section and update it
            section_start = content.find("## 📋 Current Session Workspace")
            section_end = content.find("## ", section_start + 1)
            if section_end == -1:
                section_end = len(content)
            
            new_section = f"""## 📋 Current Session Workspace

- **Last Activity**: {latest_commit["date"]}
- **Latest Change**: {latest_commit["subject"]}
- **Category**: {analysis["category"].replace("_", " ").title()}
- **Files Modified**: {len(latest_commit["files_changed"])} files
- **MCP Usage**: {", ".join(analysis["mcp_usage"]) if analysis["mcp_usage"] else "None documented"}
"""
            
            content = content[:section_start] + new_section + content[section_end:]
        
        # Write updated content
        with open(status_file, 'w') as f:
            f.write(content)
        
        logger.info("Updated project status with latest activity")
    
    def auto_update_change_log(self, since: str = "1 week ago") -> None:
        """Automatically update change log with recent commits"""
        commits = self.get_git_commits(since)
        
        if not commits:
            logger.info("No commits found in the specified period")
            return
        
        new_entries = []
        for commit in commits:
            analysis = self.analyze_commit_message(commit)
            entry = self.generate_change_log_entry(commit, analysis)
            new_entries.append(entry)
        
        if new_entries:
            self.update_change_log(new_entries)
            logger.info(f"Added {len(new_entries)} new entries to change log")
        else:
            logger.info("No new entries to add to change log")

def main():
    """Main entry point for change log generation"""
    parser = argparse.ArgumentParser(description="Change Log Generator")
    parser.add_argument("--config", default="docs/automation/docs-config.yaml", 
                       help="Configuration file path")
    parser.add_argument("--auto-update", action="store_true", 
                       help="Auto-update change log from recent commits")
    parser.add_argument("--update-status", action="store_true", 
                       help="Update current project status")
    parser.add_argument("--since", default="1 week ago", 
                       help="Time period for commit analysis")
    parser.add_argument("--debug", action="store_true", 
                       help="Enable debug logging")
    
    args = parser.parse_args()
    
    if args.debug:
        logger.remove()
        logger.add(sys.stderr, level="DEBUG")
    
    generator = ChangeLogGenerator(args.config)
    
    if args.auto_update:
        generator.auto_update_change_log(args.since)
    
    elif args.update_status:
        generator.update_project_status()
    
    else:
        # Interactive mode
        print("Change Log Generator - Interactive Mode")
        
        # Get commits for specified period
        commits = generator.get_git_commits(args.since)
        
        if not commits:
            print("No commits found in the specified period")
            return
        
        print(f"Found {len(commits)} commits since {args.since}")
        
        for i, commit in enumerate(commits):
            analysis = generator.analyze_commit_message(commit)
            print(f"\nCommit {i+1}: {commit['subject']}")
            print(f"Category: {analysis['category']}")
            print(f"MCP Usage: {', '.join(analysis['mcp_usage']) if analysis['mcp_usage'] else 'None'}")
            
            entry = generator.generate_change_log_entry(commit, analysis)
            print("\nGenerated entry:")
            print(entry)
            
            if input("Add this entry? (y/n): ").lower() == 'y':
                generator.update_change_log([entry])
                print("Entry added to change log")

if __name__ == "__main__":
    main()