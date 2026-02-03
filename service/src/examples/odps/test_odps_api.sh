#!/bin/bash
# Example script for testing ODPS NLP API endpoints

# Set the HiveServer2 URL
HIVE_SERVER_URL="${HIVE_SERVER_URL:-http://localhost:10002}"
ODPS_API_BASE="${HIVE_SERVER_URL}/odps/v1/odps"

echo "========================================"
echo "ODPS Natural Language Query API Examples"
echo "========================================"
echo ""

# Function to make API calls with nice formatting
call_api() {
    local method=$1
    local endpoint=$2
    local data=$3
    
    echo ">>> $method $endpoint"
    if [ -n "$data" ]; then
        echo "Request: $data"
    fi
    echo ""
    
    if [ "$method" = "GET" ]; then
        curl -s -X GET "$endpoint" | python3 -m json.tool 2>/dev/null || curl -s -X GET "$endpoint"
    else
        curl -s -X POST "$endpoint" \
            -H "Content-Type: application/json" \
            -d "$data" | python3 -m json.tool 2>/dev/null || curl -s -X POST "$endpoint" -H "Content-Type: application/json" -d "$data"
    fi
    echo ""
    echo "----------------------------------------"
    echo ""
}

# 1. Check ODPS service status
echo "1. Checking ODPS service status..."
call_api "GET" "${ODPS_API_BASE}/status"

# 2. View ODPS configuration
echo "2. Viewing ODPS configuration..."
call_api "GET" "${ODPS_API_BASE}/config"

# 3. Execute natural language queries
echo "3. Executing natural language queries..."

# English queries
echo "3.1. Show tables (English)"
call_api "POST" "${ODPS_API_BASE}/nlp" '{"query": "show tables"}'

echo "3.2. Describe a table (English)"
call_api "POST" "${ODPS_API_BASE}/nlp" '{"query": "describe table users"}'

echo "3.3. Select all data (English)"
call_api "POST" "${ODPS_API_BASE}/nlp" '{"query": "select all from users"}'

echo "3.4. Count records (English)"
call_api "POST" "${ODPS_API_BASE}/nlp" '{"query": "count records in users"}'

echo "3.5. Limit results (English)"
call_api "POST" "${ODPS_API_BASE}/nlp" '{"query": "show first 10 rows from users"}'

# Chinese queries
echo "3.6. Show tables (Chinese)"
call_api "POST" "${ODPS_API_BASE}/nlp" '{"query": "显示表"}'

echo "3.7. Describe a table (Chinese)"
call_api "POST" "${ODPS_API_BASE}/nlp" '{"query": "描述表 users"}'

echo "3.8. Select all data (Chinese)"
call_api "POST" "${ODPS_API_BASE}/nlp" '{"query": "查询表 users 的所有数据"}'

echo "3.9. Count records (Chinese)"
call_api "POST" "${ODPS_API_BASE}/nlp" '{"query": "统计表 users 的记录数"}'

echo "3.10. Limit results (Chinese)"
call_api "POST" "${ODPS_API_BASE}/nlp" '{"query": "显示表 users 的前 20 行"}'

# SQL passthrough
echo "3.11. Direct SQL query"
call_api "POST" "${ODPS_API_BASE}/nlp" '{"query": "SELECT * FROM users WHERE id > 100 LIMIT 50"}'

echo ""
echo "========================================"
echo "All examples completed!"
echo "========================================"
