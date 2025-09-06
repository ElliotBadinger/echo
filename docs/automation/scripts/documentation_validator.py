#!/usr/bin/env python3
"""
Documentation Validator - Validate documentation quality and compliance
Version: 2.0 - Unified Documentation System
"""

import os
import sys
import json
import yaml
import argparse
import logging
import re
from datetime import datetime, timezone
from pathlib import Path
from typing import Dict, List, Optional, Any, Tuple
import pandas as pd
from loguru import logger
import jsonschema

# Configure logging
logger.remove()
logger.add(
    sys.stderr,
    format="<green>{time:YYYY-MM-DD HH:mm:ss}</green> | <level>{level: <8}</level> | <cyan>{name}</cyan>:<cyan>{function}</cyan>:<cyan>{line}</cyan> - <level>{message}</level>",
    level="INFO"
)

class DocumentationValidator:
    """Validate documentation quality and compliance"""
    
    def __init__(self, config_path: str = "../docs-config.yaml"):
        self.config_path = Path(__file__).parent / config_path
        self.config = self._load_config()
        self.docs_dir = Path(__file__).parent.parent  # Go up to docs directory
        self.validation_rules = self._load_validation_rules()
        self.project_root = Path(__file__).parent.parent.parent  # Go up to project root
        
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
    
    def _load_validation_rules(self) -> Dict[str, Any]:
        """Load validation rules from configuration"""
        validation_config = self.config.get("validation", {})
        return {
            "documentation": validation_config.get("documentation", {}),
            "mcp_integration": validation_config.get("mcp_integration", {}),
            "research": validation_config.get("research", {}),
            "testing": validation_config.get("testing", {})
        }
    
    def validate_all_docs(self) -> Dict[str, Any]:
        """Validate all documentation files"""
        results = {
            "timestamp": datetime.now(timezone.utc).isoformat(),
            "overall_score": 0.0,
            "files_validated": 0,
            "files_passed": 0,
            "files_failed": 0,
            "validation_results": {},
            "recommendations": []
        }
        
        # Get all markdown files in docs directory
        md_files = list(self.docs_dir.rglob("*.md"))
        
        for file_path in md_files:
            if file_path.name == "README.md":  # Skip root README
                continue
            if "venv" in str(file_path):  # Skip virtual environment files
                continue
                
            file_result = self.validate_single_doc(file_path)
            results["validation_results"][str(file_path)] = file_result
            results["files_validated"] += 1
            
            if file_result["passed"]:
                results["files_passed"] += 1
            else:
                results["files_failed"] += 1
        
        # Calculate overall score
        if results["files_validated"] > 0:
            results["overall_score"] = (results["files_passed"] / results["files_validated"]) * 10
        
        # Generate recommendations
        results["recommendations"] = self._generate_recommendations(results)
        
        return results
    
    def validate_single_doc(self, file_path: Path) -> Dict[str, Any]:
        """Validate a single documentation file"""
        logger.info(f"Validating: {file_path}")
        
        result = {
            "file": str(file_path),
            "passed": False,
            "score": 0.0,
            "checks": {},
            "issues": [],
            "recommendations": []
        }
        
        try:
            with open(file_path, 'r', encoding='utf-8') as f:
                content = f.read()
        except Exception as e:
            result["issues"].append(f"Failed to read file: {e}")
            return result
        
        # Run validation checks
        checks = {
            "structure": self._check_structure(content, file_path),
            "required_sections": self._check_required_sections(content, file_path),
            "mcp_integration": self._check_mcp_integration(content, file_path),
            "template_compliance": self._check_template_compliance(content, file_path),
            "cross_references": self._check_cross_references(content, file_path),
            "formatting": self._check_formatting(content, file_path),
            "timestamps": self._check_timestamps(content, file_path)
        }
        
        result["checks"] = checks
        
        # Calculate score
        passed_checks = sum(1 for check in checks.values() if check["passed"])
        total_checks = len(checks)
        result["score"] = (passed_checks / total_checks) * 10 if total_checks > 0 else 0
        
        # Collect issues
        for check_name, check_result in checks.items():
            if not check_result["passed"]:
                result["issues"].extend(check_result.get("issues", []))
        
        # Determine pass/fail
        result["passed"] = result["score"] >= 7.0  # Minimum passing score
        
        # Generate recommendations
        result["recommendations"] = self._generate_file_recommendations(result)
        
        return result
    
    def _check_structure(self, content: str, file_path: Path) -> Dict[str, Any]:
        """Check documentation structure compliance"""
        result = {"passed": True, "issues": []}
        
        # Check for basic markdown structure
        if not content.strip():
            result["passed"] = False
            result["issues"].append("File is empty")
            return result
        
        # Check for title/header
        if not re.search(r'^#\s+', content, re.MULTILINE):
            result["passed"] = False
            result["issues"].append("Missing main title (H1)")
        
        # Check for version information
        if not re.search(r'^\*\*Version:\*\*\s+', content, re.MULTILINE):
            result["passed"] = False
            result["issues"].append("Missing version information")
        
        # Check for last updated timestamp
        if not re.search(r'^\*\*Last Updated:\*\*\s+', content, re.MULTILINE):
            result["passed"] = False
            result["issues"].append("Missing last updated timestamp")
        
        return result
    
    def _check_required_sections(self, content: str, file_path: Path) -> Dict[str, Any]:
        """Check for required sections based on file type"""
        result = {"passed": True, "issues": []}
        
        # Define required sections by file type
        required_sections = self._get_required_sections(file_path)
        
        for section in required_sections:
            if not re.search(rf'^##\s+{re.escape(section)}', content, re.MULTILINE | re.IGNORECASE):
                result["passed"] = False
                result["issues"].append(f"Missing required section: {section}")
        
        return result
    
    def _get_required_sections(self, file_path: Path) -> List[str]:
        """Get required sections for specific file types"""
        file_name = file_path.name
        
        if "change-log" in file_name:
            return ["Change", "Goal", "Research Conducted", "Files Modified", "Testing Done", "Result", "Next Steps"]
        elif "current-status" in file_name:
            return ["Project Overview", "Current Critical Status", "Development Phase", "Success Metrics", "Next Session Focus"]
        elif "mcp-usage" in file_name or "mcp-integration" in file_name:
            return ["MCP Server Usage", "Effectiveness Analysis", "Optimization Recommendations"]
        elif "research" in file_name:
            return ["Research Questions", "Methodology", "Findings", "Implementation"]
        else:
            return []  # No specific required sections for general docs
    
    def _check_mcp_integration(self, content: str, file_path: Path) -> Dict[str, Any]:
        """Check MCP integration documentation"""
        result = {"passed": True, "issues": []}
        
        # Check if this is a change log or research document that should have MCP usage
        if "change-log" in file_path.name or "research" in file_path.name:
            # Check for MCP usage documentation
            if not re.search(r'MCP Tool Used|MCP Server Used', content, re.IGNORECASE):
                result["passed"] = False
                result["issues"].append("Missing MCP usage documentation in change log")
            
            # Check for specific MCP server mentions
            mcp_servers = ["Brave Search", "Context7", "GitHub MCP", "Playwright"]
            found_servers = []
            for server in mcp_servers:
                if re.search(re.escape(server), content, re.IGNORECASE):
                    found_servers.append(server)
            
            if not found_servers and ("change-log" in file_path.name or "research" in file_path.name):
                result["passed"] = False
                result["issues"].append("No MCP servers documented in change log")
        
        return result
    
    def _check_template_compliance(self, content: str, file_path: Path) -> Dict[str, Any]:
        """Check compliance with documentation templates"""
        result = {"passed": True, "issues": []}
        
        # Check for template usage indicators
        template_indicators = [
            "MCP Server Usage Summary",
            "Research Queries Conducted",
            "Library Documentation Accessed",
            "Content Extractions Performed"
        ]
        
        # If this looks like it should use a template, check for indicators
        if any(indicator in content for indicator in template_indicators):
            # Check for proper table formatting
            table_pattern = r'\|.*\|.*\|'
            tables = re.findall(table_pattern, content)
            
            if len(tables) < 2:  # Should have header and at least one data row
                result["passed"] = False
                result["issues"].append("Template tables appear incomplete or malformed")
        
        return result
    
    def _check_cross_references(self, content: str, file_path: Path) -> Dict[str, Any]:
        """Check cross-reference validity"""
        result = {"passed": True, "issues": []}
        
        # Find all markdown links
        link_pattern = r'\[([^\]]+)\]\(([^)]+)\)'
        links = re.findall(link_pattern, content)
        
        for link_text, link_url in links:
            # Check for relative links to documentation files
            if link_url.startswith('../') or link_url.startswith('./'):
                # Resolve relative path
                base_dir = file_path.parent
                try:
                    resolved_path = (base_dir / link_url).resolve()
                    if not resolved_path.exists():
                        result["passed"] = False
                        result["issues"].append(f"Broken cross-reference: [{link_text}]({link_url})")
                except Exception:
                    result["passed"] = False
                    result["issues"].append(f"Invalid cross-reference path: [{link_text}]({link_url})")
        
        return result
    
    def _check_formatting(self, content: str, file_path: Path) -> Dict[str, Any]:
        """Check markdown formatting consistency"""
        result = {"passed": True, "issues": []}
        
        # Check for consistent header levels
        headers = re.findall(r'^(#{1,6})\s+(.+)$', content, re.MULTILINE)
        if headers:
            header_levels = [len(h[0]) for h in headers]
            if not all(level <= 3 for level in header_levels[:3]):  # First few headers should be H1-H3
                result["passed"] = False
                result["issues"].append("Inconsistent header hierarchy")
        
        # Check for proper list formatting
        if re.search(r'^\s*[-*+]\s*\S', content, re.MULTILINE):
            # Has bullet lists, check for consistency
            bullet_styles = re.findall(r'^\s*([-+*])\s+', content, re.MULTILINE)
            if len(set(bullet_styles)) > 1:
                result["passed"] = False
                result["issues"].append("Inconsistent bullet list markers")
        
        return result
    
    def _check_timestamps(self, content: str, file_path: Path) -> Dict[str, Any]:
        """Check timestamp validity and format"""
        result = {"passed": True, "issues": []}
        
        # Find all timestamps
        timestamp_pattern = r'\d{4}-\d{2}-\d{2}\s+\d{2}:\d{2}(?::\d{2})?'
        timestamps = re.findall(timestamp_pattern, content)
        
        for timestamp in timestamps:
            try:
                # Try to parse the timestamp
                if len(timestamp) == 16:  # YYYY-MM-DD HH:MM
                    datetime.strptime(timestamp, '%Y-%m-%d %H:%M')
                elif len(timestamp) == 19:  # YYYY-MM-DD HH:MM:SS
                    datetime.strptime(timestamp, '%Y-%m-%d %H:%M:%S')
                else:
                    result["passed"] = False
                    result["issues"].append(f"Invalid timestamp format: {timestamp}")
            except ValueError:
                result["passed"] = False
                result["issues"].append(f"Invalid timestamp: {timestamp}")
        
        return result
    
    def _generate_recommendations(self, results: Dict[str, Any]) -> List[str]:
        """Generate overall recommendations based on validation results"""
        recommendations = []
        
        if results["overall_score"] < 7.0:
            recommendations.append("Overall documentation quality needs improvement. Focus on fixing validation issues.")
        
        if results["files_failed"] > 0:
            recommendations.append(f"{results['files_failed']} files failed validation. Review and fix issues in these files.")
        
        # Check for common issues across files
        common_issues = {}
        for file_result in results["validation_results"].values():
            for issue in file_result.get("issues", []):
                common_issues[issue] = common_issues.get(issue, 0) + 1
        
        # Recommend fixes for common issues
        for issue, count in common_issues.items():
            if count > 2:  # Issue appears in multiple files
                if "Missing required section" in issue:
                    recommendations.append(f"Multiple files missing required sections. Review template requirements.")
                elif "MCP usage documentation" in issue:
                    recommendations.append("Several change logs missing MCP usage documentation. Ensure all changes include MCP research details.")
                elif "cross-reference" in issue:
                    recommendations.append("Multiple broken cross-references found. Verify all internal links.")
        
        return recommendations
    
    def _generate_file_recommendations(self, result: Dict[str, Any]) -> List[str]:
        """Generate recommendations for a specific file"""
        recommendations = []
        
        if result["score"] < 7.0:
            recommendations.append("Improve documentation quality to meet minimum standards")
        
        # Check specific issues
        for issue in result.get("issues", []):
            if "Missing required section" in issue:
                recommendations.append("Add missing required sections based on file type")
            elif "MCP usage documentation" in issue:
                recommendations.append("Document MCP server usage for research and implementation")
            elif "cross-reference" in issue:
                recommendations.append("Fix broken cross-references or use absolute paths")
            elif "formatting" in issue:
                recommendations.append("Fix markdown formatting inconsistencies")
            elif "timestamp" in issue:
                recommendations.append("Correct timestamp format to YYYY-MM-DD HH:MM:SS")
        
        return recommendations
    
    def generate_validation_report(self, results: Dict[str, Any]) -> str:
        """Generate formatted validation report"""
        report = f"""# Documentation Validation Report

**Generated**: {datetime.now(timezone.utc).strftime('%Y-%m-%d %H:%M:%S')} UTC
**Files Validated**: {results['files_validated']}
**Overall Score**: {results['overall_score']:.1f}/10
**Pass Rate**: {(results['files_passed'] / results['files_validated'] * 100):.1f}%

## Summary

- ✅ **Passed**: {results['files_passed']} files
- ❌ **Failed**: {results['files_failed']} files
- 📊 **Overall Quality**: {"Excellent" if results['overall_score'] >= 9 else "Good" if results['overall_score'] >= 7 else "Needs Improvement"}

## Detailed Results

"""
        
        # Add detailed results for each file
        for file_path, file_result in results["validation_results"].items():
            status = "✅ PASS" if file_result["passed"] else "❌ FAIL"
            report += f"### {status} - {Path(file_path).name}\n\n"
            report += f"- **Score**: {file_result['score']:.1f}/10\n"
            report += f"- **Issues**: {len(file_result['issues'])}\n\n"
            
            if file_result["issues"]:
                report += "**Issues Found**:\n"
                for issue in file_result["issues"]:
                    report += f"- {issue}\n"
                report += "\n"
            
            if file_result["recommendations"]:
                report += "**Recommendations**:\n"
                for rec in file_result["recommendations"]:
                    report += f"- {rec}\n"
                report += "\n"
        
        # Add overall recommendations
        if results["recommendations"]:
            report += "## Overall Recommendations\n\n"
            for rec in results["recommendations"]:
                report += f"- {rec}\n"
        
        return report

def main():
    """Main entry point for documentation validation"""
    parser = argparse.ArgumentParser(description="Documentation Validator")
    parser.add_argument("--config", default="docs/automation/docs-config.yaml", 
                       help="Configuration file path")
    parser.add_argument("--check-all", action="store_true", 
                       help="Validate all documentation files")
    parser.add_argument("--check-structure", action="store_true", 
                       help="Check documentation structure only")
    parser.add_argument("--check-templates", action="store_true", 
                       help="Check template compliance only")
    parser.add_argument("--check-mcp-integration", action="store_true", 
                       help="Check MCP integration only")
    parser.add_argument("--generate-report", action="store_true", 
                       help="Generate validation report")
    parser.add_argument("--file", help="Validate specific file")
    parser.add_argument("--debug", action="store_true", 
                       help="Enable debug logging")
    
    args = parser.parse_args()
    
    if args.debug:
        logger.remove()
        logger.add(sys.stderr, level="DEBUG")
    
    validator = DocumentationValidator(args.config)
    
    if args.check_all:
        results = validator.validate_all_docs()
        print(json.dumps(results, indent=2))
    
    elif args.file:
        file_path = Path(args.file)
        if file_path.exists():
            result = validator.validate_single_doc(file_path)
            print(json.dumps(result, indent=2))
        else:
            print(f"File not found: {args.file}")
            sys.exit(1)
    
    elif args.generate_report:
        results = validator.validate_all_docs()
        report = validator.generate_validation_report(results)

        # Save report to file
        report_dir = validator.project_root / "docs" / "automation" / "reports"
        report_dir.mkdir(exist_ok=True)
        report_file = report_dir / f"validation-report-{datetime.now(timezone.utc).strftime('%Y%m%d-%H%M%S')}.md"
        
        with open(report_file, 'w') as f:
            f.write(report)
        
        print(f"Validation report saved to: {report_file}")
        print(report)
    
    else:
        # Run specific checks
        results = validator.validate_all_docs()
        
        if args.check_structure:
            print("Structure validation results:")
            for file_path, result in results["validation_results"].items():
                structure_result = result["checks"].get("structure", {})
                print(f"{file_path}: {'PASS' if structure_result.get('passed') else 'FAIL'}")
        
        if args.check_templates:
            print("Template compliance results:")
            for file_path, result in results["validation_results"].items():
                template_result = result["checks"].get("template_compliance", {})
                print(f"{file_path}: {'PASS' if template_result.get('passed') else 'FAIL'}")
        
        if args.check_mcp_integration:
            print("MCP integration results:")
            for file_path, result in results["validation_results"].items():
                mcp_result = result["checks"].get("mcp_integration", {})
                print(f"{file_path}: {'PASS' if mcp_result.get('passed') else 'FAIL'}")

if __name__ == "__main__":
    main()