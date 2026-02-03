#!/usr/bin/env python3
"""
Python client example for ODPS Natural Language Query API
"""

import requests
import json
import sys

class ODPSNLPClient:
    """Client for interacting with ODPS Natural Language Query API"""
    
    def __init__(self, base_url="http://localhost:10002"):
        self.base_url = base_url
        self.api_base = f"{base_url}/odps/v1/odps"
        
    def get_status(self):
        """Check ODPS service status"""
        url = f"{self.api_base}/status"
        response = requests.get(url)
        return response.json()
    
    def get_config(self):
        """Get ODPS configuration"""
        url = f"{self.api_base}/config"
        response = requests.get(url)
        return response.json()
    
    def query(self, natural_language_query):
        """Execute a natural language query"""
        url = f"{self.api_base}/nlp"
        headers = {"Content-Type": "application/json"}
        data = {"query": natural_language_query}
        
        response = requests.post(url, headers=headers, json=data)
        return response.json()


def print_result(title, result):
    """Pretty print a result"""
    print(f"\n{'='*60}")
    print(f"{title}")
    print('='*60)
    print(json.dumps(result, indent=2, ensure_ascii=False))


def main():
    """Run example queries"""
    print("ODPS Natural Language Query API - Python Client Example")
    print("="*60)
    
    # Create client
    client = ODPSNLPClient()
    
    try:
        # Check status
        status = client.get_status()
        print_result("Service Status", status)
        
        # Get configuration
        config = client.get_config()
        print_result("Configuration", config)
        
        # Execute English queries
        print("\n" + "="*60)
        print("English Query Examples")
        print("="*60)
        
        queries_en = [
            "show tables",
            "describe table users",
            "select all from users",
            "count records in users",
            "show first 10 rows from users",
        ]
        
        for query in queries_en:
            result = client.query(query)
            print_result(f"Query: {query}", result)
        
        # Execute Chinese queries
        print("\n" + "="*60)
        print("Chinese Query Examples (中文查询示例)")
        print("="*60)
        
        queries_cn = [
            "显示表",
            "描述表 users",
            "查询表 users 的所有数据",
            "统计表 users 的记录数",
            "显示表 users 的前 20 行",
        ]
        
        for query in queries_cn:
            result = client.query(query)
            print_result(f"Query: {query}", result)
        
        # Direct SQL query
        print("\n" + "="*60)
        print("Direct SQL Query Example")
        print("="*60)
        
        sql_query = "SELECT * FROM users WHERE id > 100 LIMIT 50"
        result = client.query(sql_query)
        print_result(f"SQL: {sql_query}", result)
        
        print("\n" + "="*60)
        print("All examples completed successfully!")
        print("="*60)
        
    except requests.exceptions.ConnectionError:
        print("\nError: Could not connect to HiveServer2.")
        print("Please ensure HiveServer2 is running and the URL is correct.")
        sys.exit(1)
    except Exception as e:
        print(f"\nError: {e}")
        sys.exit(1)


if __name__ == "__main__":
    main()
