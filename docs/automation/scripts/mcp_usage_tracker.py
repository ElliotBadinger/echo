#!/usr/bin/env python3
"""
MCP Usage Tracker - Track and optimize MCP server usage effectiveness
Version: 2.0 - Unified Documentation System
"""

import os
import sys
import json
import yaml
import argparse
import logging
import io
from datetime import datetime, timedelta
from pathlib import Path
from typing import Dict, List, Optional, Any
import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns
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

class MCPUsageTracker:
    """Track and analyze MCP server usage effectiveness"""
    
    def __init__(self, config_path: str = "docs/automation/docs-config.yaml"):
        self.config_path = Path(config_path)
        self.config = self._load_config()
        self.data_dir = Path("docs/automation/data")
        self.data_dir.mkdir(exist_ok=True)
        self.usage_file = self.data_dir / "mcp_usage.json"
        self.effectiveness_file = self.data_dir / "mcp_effectiveness.json"
        
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
    
    def track_usage(self, server_name: str, query: str, result_quality: int, 
                   time_saved: float, implementation_success: bool) -> None:
        """Track individual MCP server usage"""
        usage_data = {
            "timestamp": datetime.utcnow().isoformat(),
            "server": server_name,
            "query": query,
            "result_quality": result_quality,
            "time_saved_minutes": time_saved,
            "implementation_success": implementation_success,
            "session_id": os.environ.get("SESSION_ID", "unknown")
        }
        
        # Load existing usage data
        existing_data = self._load_usage_data()
        existing_data.append(usage_data)
        
        # Save updated data
        self._save_usage_data(existing_data)
        logger.info(f"Tracked MCP usage: {server_name} - Quality: {result_quality}/10")
    
    def _load_usage_data(self) -> List[Dict[str, Any]]:
        """Load existing usage data"""
        if self.usage_file.exists():
            try:
                with open(self.usage_file, 'r') as f:
                    return json.load(f)
            except json.JSONDecodeError:
                logger.warning("Corrupted usage data file, starting fresh")
                return []
        return []
    
    def _save_usage_data(self, data: List[Dict[str, Any]]) -> None:
        """Save usage data to file"""
        with open(self.usage_file, 'w') as f:
            json.dump(data, f, indent=2)
    
    def get_weekly_stats(self) -> Dict[str, Any]:
        """Get weekly MCP usage statistics"""
        usage_data = self._load_usage_data()
        if not usage_data:
            return {"error": "No usage data available"}
        
        # Filter data for last 7 days
        cutoff_date = datetime.utcnow() - timedelta(days=7)
        weekly_data = [
            entry for entry in usage_data
            if datetime.fromisoformat(entry["timestamp"]) >= cutoff_date
        ]
        
        if not weekly_data:
            return {"error": "No usage data in last 7 days"}
        
        # Calculate statistics by server
        server_stats = {}
        for entry in weekly_data:
            server = entry["server"]
            if server not in server_stats:
                server_stats[server] = {
                    "count": 0,
                    "total_quality": 0,
                    "total_time_saved": 0,
                    "successful_implementations": 0,
                    "queries": []
                }
            
            server_stats[server]["count"] += 1
            server_stats[server]["total_quality"] += entry["result_quality"]
            server_stats[server]["total_time_saved"] += entry["time_saved_minutes"]
            if entry["implementation_success"]:
                server_stats[server]["successful_implementations"] += 1
            server_stats[server]["queries"].append({
                "query": entry["query"],
                "quality": entry["result_quality"],
                "time_saved": entry["time_saved_minutes"],
                "success": entry["implementation_success"]
            })
        
        # Calculate averages and effectiveness
        for server, stats in server_stats.items():
            count = stats["count"]
            stats["average_quality"] = stats["total_quality"] / count
            stats["average_time_saved"] = stats["total_time_saved"] / count
            stats["success_rate"] = (stats["successful_implementations"] / count) * 100
            stats["effectiveness_score"] = (stats["average_quality"] * stats["success_rate"]) / 100
        
        return {
            "period": "7_days",
            "total_usage": len(weekly_data),
            "server_stats": server_stats,
            "generated_at": datetime.utcnow().isoformat()
        }
    
    def analyze_effectiveness(self) -> Dict[str, Any]:
        """Analyze MCP server effectiveness and provide recommendations"""
        weekly_stats = self.get_weekly_stats()
        
        if "error" in weekly_stats:
            return weekly_stats
        
        recommendations = []
        target_config = self.config.get("mcp_integration", {}).get("optimization_targets", {})
        
        for server, stats in weekly_stats["server_stats"].items():
            server_config = target_config.get(server, {})
            target_usage = server_config.get("target_usage", "unknown")
            
            # Check usage against targets
            if isinstance(target_usage, str) and target_usage.isdigit():
                target_usage = int(target_usage)
                if stats["count"] < target_usage:
                    recommendations.append({
                        "server": server,
                        "issue": "low_usage",
                        "current": stats["count"],
                        "target": target_usage,
                        "recommendation": f"Increase {server} usage to reach target of {target_usage} weekly uses"
                    })
            
            # Check effectiveness
            if stats["average_quality"] < 7.0:
                recommendations.append({
                    "server": server,
                    "issue": "low_quality",
                    "current": stats["average_quality"],
                    "target": 7.0,
                    "recommendation": f"Improve {server} query quality - consider refining search terms or documentation requests"
                })
            
            if stats["success_rate"] < 80:
                recommendations.append({
                    "server": server,
                    "issue": "low_success_rate",
                    "current": stats["success_rate"],
                    "target": 80,
                    "recommendation": f"Improve {server} implementation success rate - better align research with implementation"
                })
        
        return {
            "analysis_date": datetime.utcnow().isoformat(),
            "weekly_stats": weekly_stats,
            "recommendations": recommendations,
            "overall_effectiveness": self._calculate_overall_effectiveness(weekly_stats)
        }
    
    def _calculate_overall_effectiveness(self, weekly_stats: Dict[str, Any]) -> float:
        """Calculate overall MCP effectiveness score"""
        if "error" in weekly_stats or not weekly_stats.get("server_stats"):
            return 0.0
        
        total_score = 0
        total_weight = 0
        
        for server, stats in weekly_stats["server_stats"].items():
            # Weight by usage count and effectiveness
            weight = stats["count"]
            effectiveness = (stats["average_quality"] * stats["success_rate"]) / 100
            total_score += weight * effectiveness
            total_weight += weight
        
        return total_score / total_weight if total_weight > 0 else 0.0
    
    def generate_weekly_report(self) -> str:
        """Generate formatted weekly usage report"""
        stats = self.get_weekly_stats()
        analysis = self.analyze_effectiveness()
        
        if "error" in stats:
            return f"# MCP Usage Report - Weekly\n\n**Error**: {stats['error']}\n"
        
        report = f"""# MCP Usage Report - Weekly

**Generated**: {datetime.utcnow().strftime('%Y-%m-%d %H:%M:%S')} UTC
**Period**: Last 7 days
**Total Usage**: {stats['total_usage']} interactions

## Usage Statistics by Server

"""
        
        for server, server_stats in stats["server_stats"].items():
            report += f"""
### {server.title().replace('_', ' ')}

- **Usage Count**: {server_stats['count']} times
- **Average Quality**: {server_stats['average_quality']:.1f}/10
- **Average Time Saved**: {server_stats['average_time_saved']:.1f} minutes
- **Success Rate**: {server_stats['success_rate']:.1f}%
- **Effectiveness Score**: {server_stats['effectiveness_score']:.1f}/10

**Top Queries**:
"""
            # Show top 3 queries
            top_queries = sorted(server_stats['queries'], 
                               key=lambda x: x['quality'], reverse=True)[:3]
            for i, query in enumerate(top_queries, 1):
                report += f"{i}. `{query['query'][:50]}...` (Quality: {query['quality']}/10)\n"
        
        # Add recommendations
        if analysis["recommendations"]:
            report += "\n## Optimization Recommendations\n\n"
            for rec in analysis["recommendations"]:
                report += f"- **{rec['server']}**: {rec['recommendation']} "
                report += f"(Current: {rec['current']:.1f}, Target: {rec['target']})\n"
        
        report += f"\n## Overall Effectiveness\n\n"
        report += f"**Overall Score**: {analysis['overall_effectiveness']:.1f}/10\n\n"
        
        if analysis['overall_effectiveness'] >= 8.0:
            report += "🟢 **Excellent**: MCP servers are being used effectively\n"
        elif analysis['overall_effectiveness'] >= 6.0:
            report += "🟡 **Good**: MCP usage is effective with room for improvement\n"
        else:
            report += "🔴 **Needs Improvement**: MCP usage effectiveness is below target\n"
        
        return report
    
    def update_optimization_guide(self) -> None:
        """Update the MCP optimization guide with latest statistics"""
        analysis = self.analyze_effectiveness()
        
        if "error" in analysis:
            logger.error(f"Cannot update optimization guide: {analysis['error']}")
            return
        
        # Generate updated content for optimization guide
        weekly_report = self.generate_weekly_report()
        
        # Update the optimization guide file
        optimization_guide_path = Path("docs/mcp-integration/mcp-optimization.md")
        
        if optimization_guide_path.exists():
            with open(optimization_guide_path, 'r') as f:
                content = f.read()
            
            # Find the section to update (between markers)
            start_marker = "## 📊 Current Usage Statistics"
            end_marker = "## 🎯 Optimization Targets"
            
            start_idx = content.find(start_marker)
            end_idx = content.find(end_marker)
            
            if start_idx != -1 and end_idx != -1:
                # Replace the statistics section
                new_content = (
                    content[:start_idx + len(start_marker)] + 
                    "\n\n" + weekly_report + "\n\n" +
                    content[end_idx:]
                )
                
                with open(optimization_guide_path, 'w') as f:
                    f.write(new_content)
                
                logger.info("Updated MCP optimization guide with latest statistics")
            else:
                logger.warning("Could not find section markers in optimization guide")
        else:
            logger.error("MCP optimization guide file not found")
    
    def export_usage_data(self, format: str = "json") -> str:
        """Export usage data in specified format"""
        usage_data = self._load_usage_data()
        
        if format == "json":
            return json.dumps(usage_data, indent=2)
        elif format == "csv":
            if not usage_data:
                return "timestamp,server,query,result_quality,time_saved_minutes,implementation_success\n"
            
            df = pd.DataFrame(usage_data)
            return df.to_csv(index=False)
        elif format == "excel":
            if not usage_data:
                return "No data available"
            
            df = pd.DataFrame(usage_data)
            output = io.StringIO()
            df.to_excel(output, index=False)
            return output.getvalue()
        else:
            raise ValueError(f"Unsupported format: {format}")

def main():
    """Main entry point for MCP usage tracking"""
    parser = argparse.ArgumentParser(description="MCP Usage Tracker")
    parser.add_argument("--config", default="docs/automation/docs-config.yaml", 
                       help="Configuration file path")
    parser.add_argument("--weekly-report", action="store_true", 
                       help="Generate weekly usage report")
    parser.add_argument("--optimization-analysis", action="store_true", 
                       help="Analyze MCP effectiveness and provide recommendations")
    parser.add_argument("--update-guide", action="store_true", 
                       help="Update MCP optimization guide with latest statistics")
    parser.add_argument("--export", choices=["json", "csv", "excel"], 
                       help="Export usage data in specified format")
    parser.add_argument("--debug", action="store_true", 
                       help="Enable debug logging")
    
    args = parser.parse_args()
    
    if args.debug:
        logger.remove()
        logger.add(sys.stderr, level="DEBUG")
    
    tracker = MCPUsageTracker(args.config)
    
    if args.weekly_report:
        report = tracker.generate_weekly_report()
        print(report)
    
    elif args.optimization_analysis:
        analysis = tracker.analyze_effectiveness()
        print(json.dumps(analysis, indent=2))
    
    elif args.update_guide:
        tracker.update_optimization_guide()
        print("Updated MCP optimization guide")
    
    elif args.export:
        data = tracker.export_usage_data(args.export)
        print(data)
    
    else:
        # Interactive mode - track a single usage
        print("MCP Usage Tracker - Interactive Mode")
        server = input("Server name: ")
        query = input("Query: ")
        quality = int(input("Result quality (1-10): "))
        time_saved = float(input("Time saved (minutes): "))
        success = input("Implementation successful? (y/n): ").lower() == 'y'
        
        tracker.track_usage(server, query, quality, time_saved, success)
        print("Usage tracked successfully!")

if __name__ == "__main__":
    main()