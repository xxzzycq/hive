# ODPS Natural Language Query Service

## 概述 (Overview)

本服务为 Apache Hive 提供了与阿里云 ODPS（Open Data Processing Service）的集成，支持使用自然语言查询 ODPS 数据。

This service provides integration between Apache Hive and Aliyun ODPS (Open Data Processing Service), with support for natural language queries.

## 功能特性 (Features)

- **自然语言查询 (Natural Language Queries)**: 支持中英文自然语言输入
- **自动 SQL 转换 (Automatic SQL Conversion)**: 将自然语言转换为 ODPS SQL 查询
- **RESTful API**: 提供标准的 REST API 接口
- **配置管理 (Configuration Management)**: 灵活的配置选项

## 配置 (Configuration)

### 1. 启用 ODPS 服务 (Enable ODPS Service)

在 `hive-site.xml` 或 `odps-site.xml` 中添加以下配置：

```xml
<property>
  <name>odps.enabled</name>
  <value>true</value>
</property>

<property>
  <name>odps.access.id</name>
  <value>YOUR_ACCESS_KEY_ID</value>
</property>

<property>
  <name>odps.access.key</name>
  <value>YOUR_ACCESS_KEY_SECRET</value>
</property>

<property>
  <name>odps.endpoint</name>
  <value>http://service.odps.aliyun.com/api</value>
</property>

<property>
  <name>odps.project</name>
  <value>YOUR_PROJECT_NAME</value>
</property>
```

### 2. 重启 HiveServer2

配置完成后，重启 HiveServer2 以加载新的配置。

## API 端点 (API Endpoints)

### 1. 执行自然语言查询 (Execute Natural Language Query)

**Endpoint**: `POST /odps/v1/odps/nlp`

**Request Body**:
```json
{
  "query": "show all tables"
}
```

**Response**:
```json
{
  "status": "success",
  "originalQuery": "show all tables",
  "convertedSQL": "SHOW TABLES",
  "rows": [
    {"tableName": "users"},
    {"tableName": "orders"}
  ],
  "rowCount": 2,
  "executionTime": 1234567890
}
```

### 2. 检查服务状态 (Check Service Status)

**Endpoint**: `GET /odps/v1/odps/status`

**Response**:
```json
{
  "enabled": true,
  "configured": true,
  "connected": true
}
```

### 3. 查看配置信息 (View Configuration)

**Endpoint**: `GET /odps/v1/odps/config`

**Response**:
```json
{
  "enabled": true,
  "endpoint": "http://service.odps.aliyun.com/api",
  "project": "your_project",
  "hasAccessId": true,
  "hasAccessKey": true
}
```

## 支持的查询模式 (Supported Query Patterns)

### 中文查询示例 (Chinese Query Examples)

1. **显示所有表**:
   - "显示表" / "列出表" / "show tables"
   - 转换为: `SHOW TABLES`

2. **查看表结构**:
   - "描述表 users" / "查看表 users"
   - 转换为: `DESC users`

3. **查询所有数据**:
   - "查询表 users 的所有数据" / "获取表 users 的全部数据"
   - 转换为: `SELECT * FROM users LIMIT 100`

4. **统计记录数**:
   - "统计表 users 的记录数" / "count records in users"
   - 转换为: `SELECT COUNT(*) FROM users`

5. **限制结果数量**:
   - "显示表 users 的前 10 行"
   - 转换为: `SELECT * FROM users LIMIT 10`

### English Query Examples

1. **Show tables**:
   - "show tables" / "list all tables"
   - Converts to: `SHOW TABLES`

2. **Describe table**:
   - "describe table users" / "desc users"
   - Converts to: `DESC users`

3. **Select all data**:
   - "select all from users" / "get all data from users"
   - Converts to: `SELECT * FROM users LIMIT 100`

4. **Count records**:
   - "count records in users" / "how many rows in users"
   - Converts to: `SELECT COUNT(*) FROM users`

5. **Limit results**:
   - "show first 10 rows from users"
   - Converts to: `SELECT * FROM users LIMIT 10`

## 使用示例 (Usage Examples)

### cURL 示例

```bash
# 执行自然语言查询
curl -X POST http://localhost:10002/odps/v1/odps/nlp \
  -H "Content-Type: application/json" \
  -d '{"query": "show all tables"}'

# 检查服务状态
curl http://localhost:10002/odps/v1/odps/status

# 查看配置信息
curl http://localhost:10002/odps/v1/odps/config
```

### Java 客户端示例

```java
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ODPSNLPClient {
    public static void main(String[] args) throws Exception {
        String serverUrl = "http://localhost:10002/odps/v1/odps/nlp";
        String query = "show all tables";
        
        URL url = new URL(serverUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        
        String jsonInput = "{\"query\": \"" + query + "\"}";
        
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonInput.getBytes("utf-8");
            os.write(input, 0, input.length);
        }
        
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), "utf-8"))) {
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            System.out.println(response.toString());
        }
    }
}
```

### Python 客户端示例

```python
import requests
import json

# 执行自然语言查询
def query_odps_nlp(query):
    url = "http://localhost:10002/odps/v1/odps/nlp"
    headers = {"Content-Type": "application/json"}
    data = {"query": query}
    
    response = requests.post(url, headers=headers, json=data)
    return response.json()

# 示例
result = query_odps_nlp("show all tables")
print(json.dumps(result, indent=2))
```

## 注意事项 (Important Notes)

1. **安全性 (Security)**: 
   - 不要在代码中硬编码 AccessKey
   - 使用环境变量或安全的配置管理系统存储凭证
   - 限制 API 访问权限

2. **当前实现 (Current Implementation)**:
   - 这是一个演示实现，使用模拟数据
   - 生产环境需要集成真实的 Aliyun ODPS SDK
   - 需要添加 `aliyun-sdk-odps` 依赖

3. **性能 (Performance)**:
   - 默认查询限制返回 100 行
   - 大数据量查询应使用分页

4. **错误处理 (Error Handling)**:
   - API 会返回详细的错误信息
   - 检查响应状态码以确定请求是否成功

## 后续开发 (Future Development)

1. 集成真实的 Aliyun ODPS SDK
2. 添加更多的自然语言查询模式
3. 支持 AI/ML 模型进行更智能的查询转换
4. 添加查询历史和缓存
5. 支持复杂的 JOIN 和聚合查询

## 故障排除 (Troubleshooting)

### 服务未启用
错误: "ODPS service is not enabled"
解决: 在配置文件中设置 `odps.enabled=true`

### 配置不完整
错误: "ODPS configuration is incomplete"
解决: 检查所有必需的配置项（accessId, accessKey, endpoint, project）

### 连接失败
错误: "Failed to connect to ODPS"
解决: 
- 检查网络连接
- 验证 endpoint URL
- 确认 AccessKey 有效

### 查询解析失败
错误: "Unable to parse natural language query"
解决: 
- 使用支持的查询模式
- 或直接提供 SQL 查询

## 支持 (Support)

如有问题，请提交 issue 到项目仓库。

For issues, please submit to the project repository.
