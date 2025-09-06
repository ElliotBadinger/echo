#!/usr/bin/env python3
"""
Research Synthesizer - Synthesize research findings across sessions
Version: 2.0 - Unified Documentation System
"""

import os
import sys
import json
import yaml
import argparse
import logging
from datetime import datetime, timedelta
from pathlib import Path
from typing import Dict, List, Optional, Any
import pandas as pd
from loguru import logger
import re

# Configure logging
logger.remove()
logger.add(
    sys.stderr,
    format="<green>{time:YYYY-MM-DD HH:mm:ss}</green> | <level>{level: <8}</level> | <cyan>{name}</cyan>:<cyan>{function}</cyan>:<cyan>{line}</cyan> - <level>{message}</level>",
    level="INFO"
)

class ResearchSynthesizer:
    """Synthesize research findings across sessions"""
    
    def __init__(self, config_path: str = "docs/automation/docs-config.yaml"):
        self.config_path = Path(config_path)
        self.config = self._load_config()
        self.data_dir = Path("docs/automation/data")
        self.data_dir.mkdir(exist_ok=True)
        self.research_file = self.data_dir / "research_findings.json"
        self.findings_file = Path("docs/project-state/research-findings.md")
        
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
    
    def extract_research_from_change_logs(self) -> List[Dict[str, Any]]:
        """Extract research findings from change log entries"""
        change_log_path = Path("docs/project-state/change-log.md")
        
        if not change_log_path.exists():
            logger.warning("Change log file not found")
            return []
        
        with open(change_log_path, 'r') as f:
            content = f.read()
        
        findings = []
        
        # Parse change log entries
        entry_pattern = r'## Change (\d{4}-\d{2}-\d{2} \d{2}:\d{2}) - ([a-f0-9]{8}) - (.+?)\n\n(.*?)(?=\n## |\n---|\Z)'
        entries = re.findall(entry_pattern, content, re.DOTALL)
        
        for date, hash_id, category, entry_content in entries:
            # Extract research section
            research_match = re.search(r'### Research Conducted\n(.*?)(?=\n###|\Z)', entry_content, re.DOTALL)
            if research_match:
                research_content = research_match.group(1).strip()
                
                # Extract MCP usage
                mcp_usage = []
                mcp_pattern = r'MCP (?:Tool|Server) Used:?\s*(.+)'
                mcp_match = re.search(mcp_pattern, research_content, re.IGNORECASE)
                if mcp_match:
                    mcp_usage = [tool.strip() for tool in mcp_match.group(1).split(',')]
                
                # Extract research findings
                findings_text = research_content.replace("MCP Tool Used:", "").replace("MCP Server Used:", "").strip()
                
                finding = {
                    "timestamp": date,
                    "commit_hash": hash_id,
                    "category": category,
                    "source": "change_log",
                    "mcp_tools": mcp_usage,
                    "findings": findings_text,
                    "files_modified": self._extract_files_from_entry(entry_content),
                    "research_quality": self._assess_research_quality(research_content)
                }
                
                findings.append(finding)
        
        return findings
    
    def _extract_files_from_entry(self, entry_content: str) -> List[str]:
        """Extract modified files from change log entry"""
        files_match = re.search(r'### Files Modified\n(.*?)(?=\n###|\Z)', entry_content, re.DOTALL)
        if files_match:
            files_content = files_match.group(1)
            # Extract file paths from markdown list
            file_pattern = r'- `([^`]+)`'
            files = re.findall(file_pattern, files_content)
            return files
        return []
    
    def _assess_research_quality(self, research_content: str) -> int:
        """Assess the quality of research documentation (1-10 scale)"""
        quality_score = 5  # Base score
        
        # Check for specific research elements
        if "research" in research_content.lower():
            quality_score += 1
        if any(tool in research_content.lower() for tool in ["brave search", "context7", "github", "playwright"]):
            quality_score += 2
        if any(term in research_content.lower() for term in ["sota", "state of the art", "best practice", "benchmark"]):
            quality_score += 2
        if len(research_content.split()) > 20:  # Substantial research description
            quality_score += 1
        
        return min(quality_score, 10)
    
    def extract_research_from_mcp_usage(self) -> List[Dict[str, Any]]:
        """Extract research findings from MCP usage data"""
        mcp_usage_file = Path("docs/automation/data/mcp_usage.json")
        
        if not mcp_usage_file.exists():
            logger.warning("MCP usage data file not found")
            return []
        
        try:
            with open(mcp_usage_file, 'r') as f:
                usage_data = json.load(f)
        except json.JSONDecodeError:
            logger.error("Failed to parse MCP usage data")
            return []
        
        findings = []
        
        for entry in usage_data:
            # Only include entries with research value
            if entry.get("result_quality", 0) >= 7 and entry.get("implementation_success", False):
                finding = {
                    "timestamp": entry["timestamp"],
                    "commit_hash": entry.get("session_id", "unknown"),
                    "category": "mcp_research",
                    "source": "mcp_usage",
                    "mcp_tools": [entry["server"]],
                    "findings": f"Query: {entry['query']}\nQuality: {entry['result_quality']}/10\nTime saved: {entry['time_saved_minutes']} minutes",
                    "implementation_success": entry["implementation_success"],
                    "research_quality": entry["result_quality"]
                }
                findings.append(finding)
        
        return findings
    
    def synthesize_findings(self, findings: List[Dict[str, Any]]) -> Dict[str, Any]:
        """Synthesize research findings into themes and insights"""
        if not findings:
            return {"error": "No research findings available"}
        
        synthesis = {
            "generated_at": datetime.utcnow().isoformat(),
            "total_findings": len(findings),
            "time_period": {
                "start": min(f["timestamp"] for f in findings),
                "end": max(f["timestamp"] for f in findings)
            },
            "themes": {},
            "mcp_effectiveness": {},
            "key_insights": [],
            "recommendations": []
        }
        
        # Analyze by themes
        themes = self._analyze_themes(findings)
        synthesis["themes"] = themes
        
        # Analyze MCP effectiveness
        mcp_analysis = self._analyze_mcp_effectiveness(findings)
        synthesis["mcp_effectiveness"] = mcp_analysis
        
        # Generate key insights
        insights = self._generate_insights(findings, themes, mcp_analysis)
        synthesis["key_insights"] = insights
        
        # Generate recommendations
        recommendations = self._generate_recommendations(findings, themes, mcp_analysis)
        synthesis["recommendations"] = recommendations
        
        return synthesis
    
    def _analyze_themes(self, findings: List[Dict[str, Any]]) -> Dict[str, Any]:
        """Analyze research findings by themes"""
        themes = {
            "android_development": {
                "count": 0,
                "quality_scores": [],
                "findings": []
            },
            "performance_optimization": {
                "count": 0,
                "quality_scores": [],
                "findings": []
            },
            "ui_ux_design": {
                "count": 0,
                "quality_scores": [],
                "findings": []
            },
            "testing_methodology": {
                "count": 0,
                "quality_scores": [],
                "findings": []
            },
            "ml_integration": {
                "count": 0,
                "quality_scores": [],
                "findings": []
            },
            "documentation_practices": {
                "count": 0,
                "quality_scores": [],
                "findings": []
            }
        }
        
        # Categorize findings
        for finding in findings:
            content = f"{finding.get('findings', '')} {finding.get('category', '')}".lower()
            
            # Android development
            if any(term in content for term in ["android", "kotlin", "java", "gradle", "sdk"]):
                themes["android_development"]["count"] += 1
                themes["android_development"]["quality_scores"].append(finding.get("research_quality", 5))
                themes["android_development"]["findings"].append(finding)
            
            # Performance optimization
            if any(term in content for term in ["performance", "optimization", "efficiency", "benchmark"]):
                themes["performance_optimization"]["count"] += 1
                themes["performance_optimization"]["quality_scores"].append(finding.get("research_quality", 5))
                themes["performance_optimization"]["findings"].append(finding)
            
            # UI/UX design
            if any(term in content for term in ["ui", "ux", "design", "interface", "material"]):
                themes["ui_ux_design"]["count"] += 1
                themes["ui_ux_design"]["quality_scores"].append(finding.get("research_quality", 5))
                themes["ui_ux_design"]["findings"].append(finding)
            
            # Testing methodology
            if any(term in content for term in ["test", "testing", "validation", "verification"]):
                themes["testing_methodology"]["count"] += 1
                themes["testing_methodology"]["quality_scores"].append(finding.get("research_quality", 5))
                themes["testing_methodology"]["findings"].append(finding)
            
            # ML integration
            if any(term in content for term in ["ml", "machine learning", "ai", "model", "whisper", "vad"]):
                themes["ml_integration"]["count"] += 1
                themes["ml_integration"]["quality_scores"].append(finding.get("research_quality", 5))
                themes["ml_integration"]["findings"].append(finding)
            
            # Documentation practices
            if any(term in content for term in ["documentation", "readme", "guide", "template"]):
                themes["documentation_practices"]["count"] += 1
                themes["documentation_practices"]["quality_scores"].append(finding.get("research_quality", 5))
                themes["documentation_practices"]["findings"].append(finding)
        
        # Calculate average quality scores
        for theme, data in themes.items():
            if data["quality_scores"]:
                data["average_quality"] = sum(data["quality_scores"]) / len(data["quality_scores"])
            else:
                data["average_quality"] = 0
        
        return themes
    
    def _analyze_mcp_effectiveness(self, findings: List[Dict[str, Any]]) -> Dict[str, Any]:
        """Analyze MCP server effectiveness"""
        mcp_stats = {
            "brave_search": {"count": 0, "avg_quality": 0, "success_rate": 0},
            "context7": {"count": 0, "avg_quality": 0, "success_rate": 0},
            "github_mcp": {"count": 0, "avg_quality": 0, "success_rate": 0},
            "playwright": {"count": 0, "avg_quality": 0, "success_rate": 0}
        }
        
        # Collect MCP usage statistics
        for finding in findings:
            if "mcp_tools" in finding:
                for tool in finding["mcp_tools"]:
                    tool_key = tool.lower().replace(" ", "_")
                    if tool_key in mcp_stats:
                        mcp_stats[tool_key]["count"] += 1
                        mcp_stats[tool_key]["avg_quality"] += finding.get("research_quality", 5)
                        if finding.get("implementation_success", False):
                            mcp_stats[tool_key]["success_rate"] += 1
        
        # Calculate averages
        for tool, stats in mcp_stats.items():
            if stats["count"] > 0:
                stats["avg_quality"] = stats["avg_quality"] / stats["count"]
                stats["success_rate"] = (stats["success_rate"] / stats["count"]) * 100
            else:
                stats["avg_quality"] = 0
                stats["success_rate"] = 0
        
        return mcp_stats
    
    def _generate_insights(self, findings: List[Dict[str, Any]], themes: Dict[str, Any], 
                          mcp_analysis: Dict[str, Any]) -> List[str]:
        """Generate key insights from research findings"""
        insights = []
        
        # Overall research activity
        total_findings = len(findings)
        if total_findings > 0:
            avg_quality = sum(f.get("research_quality", 5) for f in findings) / total_findings
            insights.append(f"Research activity shows {total_findings} documented findings with average quality of {avg_quality:.1f}/10")
        
        # Theme insights
        for theme, data in themes.items():
            if data["count"] > 0:
                insights.append(f"{theme.replace('_', ' ').title()}: {data['count']} findings, avg quality {data['average_quality']:.1f}/10")
        
        # MCP effectiveness insights
        most_effective = max(mcp_analysis.items(), key=lambda x: x[1]["avg_quality"] if x[1]["count"] > 0 else 0)
        if most_effective[1]["count"] > 0:
            insights.append(f"Most effective MCP tool: {most_effective[0].replace('_', ' ').title()} "
                          f"({most_effective[1]['avg_quality']:.1f}/10 quality, {most_effective[1]['success_rate']:.1f}% success rate)")
        
        # Quality trend insights
        high_quality_findings = sum(1 for f in findings if f.get("research_quality", 0) >= 8)
        if high_quality_findings > 0:
            insights.append(f"{high_quality_findings} high-quality research findings (≥8/10) demonstrate strong research practices")
        
        return insights
    
    def _generate_recommendations(self, findings: List[Dict[str, Any]], themes: Dict[str, Any], 
                                mcp_analysis: Dict[str, Any]) -> List[str]:
        """Generate recommendations based on research analysis"""
        recommendations = []
        
        # MCP usage recommendations
        for tool, stats in mcp_analysis.items():
            if stats["count"] == 0:
                recommendations.append(f"Consider using {tool.replace('_', ' ').title()} for research to improve documentation quality")
            elif stats["avg_quality"] < 7:
                recommendations.append(f"Improve {tool.replace('_', ' ').title()} query quality - current average is {stats['avg_quality']:.1f}/10")
        
        # Theme-based recommendations
        for theme, data in themes.items():
            if data["count"] > 0 and data["average_quality"] < 6:
                recommendations.append(f"Enhance research quality in {theme.replace('_', ' ').title()} - current average is {data['average_quality']:.1f}/10")
        
        # Overall recommendations
        if len(findings) < 5:
            recommendations.append("Increase research documentation frequency to build stronger knowledge base")
        
        avg_quality = sum(f.get("research_quality", 5) for f in findings) / len(findings) if findings else 0
        if avg_quality < 7:
            recommendations.append("Focus on improving research documentation quality - target 7+ for all findings")
        
        return recommendations
    
    def save_findings(self, findings: List[Dict[str, Any]]) -> None:
        """Save research findings to file"""
        with open(self.research_file, 'w') as f:
            json.dump(findings, f, indent=2)
        
        logger.info(f"Saved {len(findings)} research findings")
    
    def update_research_findings_doc(self, synthesis: Dict[str, Any]) -> None:
        """Update the research findings documentation"""
        if "error" in synthesis:
            logger.error(f"Cannot update research findings: {synthesis['error']}")
            return
        
        content = f"""# Research Findings and Synthesis

**Version:** 2.0 - Unified Documentation System  
**Last Updated:** {datetime.utcnow().strftime('%Y-%m-%d %H:%M:%S')} UTC  
**Auto-generated:** This document is automatically updated by research synthesis

---

## 📊 Research Overview

**Total Findings**: {synthesis['total_findings']}  
**Analysis Period**: {synthesis['time_period']['start']} to {synthesis['time_period']['end']}  
**Generated**: {synthesis['generated_at']}

## 🎯 Key Insights

"""
        
        for insight in synthesis["key_insights"]:
            content += f"- {insight}\n"
        
        content += f"""
## 🔍 Research Themes Analysis

"""
        
        for theme, data in synthesis["themes"].items():
            if data["count"] > 0:
                content += f"""### {theme.replace('_', ' ').title()}

- **Findings Count**: {data['count']}
- **Average Quality**: {data['average_quality']:.1f}/10
- **Key Areas**: Research in this theme focuses on {theme.replace('_', ' ')} development and optimization

"""
        
        content += f"""
## 🤖 MCP Server Effectiveness

"""
        
        for tool, stats in synthesis["mcp_effectiveness"].items():
            if stats["count"] > 0:
                content += f"""### {tool.replace('_', ' ').title()}

- **Usage Count**: {stats['count']}
- **Average Quality**: {stats['avg_quality']:.1f}/10
- **Success Rate**: {stats['success_rate']:.1f}%
- **Effectiveness**: {"High" if stats['avg_quality'] >= 8 else "Good" if stats['avg_quality'] >= 6 else "Needs Improvement"}

"""
        
        content += f"""
## 💡 Recommendations

"""
        
        for recommendation in synthesis["recommendations"]:
            content += f"- {recommendation}\n"
        
        content += f"""
## 📈 Research Quality Trends

Research quality is measured on a 1-10 scale based on:
- Documentation completeness
- MCP tool integration
- Implementation success tracking
- Research methodology clarity

### Quality Distribution

- **High Quality (8-10)**: Findings with comprehensive documentation and successful implementation
- **Good Quality (6-7)**: Findings with adequate documentation and partial implementation success
- **Needs Improvement (1-5)**: Findings requiring better documentation and research methodology

## 🔧 Research Methodology

Our research-driven development approach includes:

1. **Problem Definition**: Clear articulation of technical challenges
2. **Literature Review**: Comprehensive search using Brave Search MCP for SOTA solutions
3. **Technical Analysis**: Android-specific guidance using Context7 MCP
4. **Implementation Validation**: CI monitoring using GitHub MCP
5. **Documentation**: Comprehensive recording of findings and decisions

## 📋 Recent Research Activities

[Research findings are automatically extracted from change logs and MCP usage data]

---

*This research synthesis is automatically generated from documented findings across all project activities. For detailed change history, see `docs/project-state/change-log.md`.*
"""
        
        with open(self.findings_file, 'w') as f:
            f.write(content)
        
        logger.info("Updated research findings documentation")

def main():
    """Main entry point for research synthesis"""
    parser = argparse.ArgumentParser(description="Research Synthesizer")
    parser.add_argument("--config", default="docs/automation/docs-config.yaml", 
                       help="Configuration file path")
    parser.add_argument("--update-findings", action="store_true", 
                       help="Extract and synthesize research findings")
    parser.add_argument("--comprehensive-report", action="store_true", 
                       help="Generate comprehensive research report")
    parser.add_argument("--debug", action="store_true", 
                       help="Enable debug logging")
    
    args = parser.parse_args()
    
    if args.debug:
        logger.remove()
        logger.add(sys.stderr, level="DEBUG")
    
    synthesizer = ResearchSynthesizer(args.config)
    
    if args.update_findings:
        # Extract research from various sources
        change_log_findings = synthesizer.extract_research_from_change_logs()
        mcp_findings = synthesizer.extract_research_from_mcp_usage()
        
        all_findings = change_log_findings + mcp_findings
        
        if all_findings:
            # Save findings
            synthesizer.save_findings(all_findings)
            
            # Synthesize and update documentation
            synthesis = synthesizer.synthesize_findings(all_findings)
            synthesizer.update_research_findings_doc(synthesis)
            
            print(f"Processed {len(all_findings)} research findings")
        else:
            print("No research findings found to process")
    
    elif args.comprehensive_report:
        # Load existing findings and generate comprehensive report
        try:
            with open(synthesizer.research_file, 'r') as f:
                findings = json.load(f)
        except FileNotFoundError:
            print("No research findings file found. Run --update-findings first.")
            return
        
        synthesis = synthesizer.synthesize_findings(findings)
        report = synthesizer.generate_comprehensive_report(synthesis)
        print(report)
    
    else:
        # Interactive mode
        print("Research Synthesizer - Interactive Mode")
        
        # Extract research from change logs
        print("Extracting research from change logs...")
        change_log_findings = synthesizer.extract_research_from_change_logs()
        print(f"Found {len(change_log_findings)} findings from change logs")
        
        # Extract research from MCP usage
        print("Extracting research from MCP usage...")
        mcp_findings = synthesizer.extract_research_from_mcp_usage()
        print(f"Found {len(mcp_findings)} findings from MCP usage")
        
        all_findings = change_log_findings + mcp_findings
        
        if all_findings:
            # Show summary
            print(f"\nTotal findings: {len(all_findings)}")
            
            # Synthesize
            synthesis = synthesizer.synthesize_findings(all_findings)
            
            if "error" not in synthesis:
                print("\nSynthesis complete!")
                print(f"Key insights: {len(synthesis['key_insights'])}")
                print(f"Recommendations: {len(synthesis['recommendations'])}")
                
                if input("Update documentation? (y/n): ").lower() == 'y':
                    synthesizer.save_findings(all_findings)
                    synthesizer.update_research_findings_doc(synthesis)
                    print("Documentation updated!")
            else:
                print(f"Error: {synthesis['error']}")
        else:
            print("No research findings found")

if __name__ == "__main__":
    main()