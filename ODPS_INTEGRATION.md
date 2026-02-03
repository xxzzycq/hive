# ODPS Natural Language Query Service

[English](#english) | [中文](#中文)

---

## English

### Overview

This implementation adds a Natural Language Query service for Alibaba Cloud ODPS (Open Data Processing Service) to Apache Hive. Users can submit queries in natural language (English or Chinese) and get results from ODPS.

### Features

- **Natural Language Processing**: Convert English and Chinese natural language queries to SQL
- **RESTful API**: Easy-to-use REST endpoints
- **Flexible Configuration**: Simple XML-based configuration
- **Mock Implementation**: Includes mock ODPS client for testing (can be replaced with real ODPS SDK)

### Quick Start

#### 1. Configuration

Add the following configuration to `conf/hive-site.xml` or `conf/odps-site.xml`:

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

#### 2. Build and Start HiveServer2

```bash
# Build Hive
mvn clean package -DskipTests

# Start HiveServer2
./bin/hiveserver2
```

#### 3. Test the API

```bash
# Check service status
curl http://localhost:10002/odps/v1/odps/status

# Execute a natural language query
curl -X POST http://localhost:10002/odps/v1/odps/nlp \
  -H "Content-Type: application/json" \
  -d '{"query": "show tables"}'
```

### API Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/odps/v1/odps/status` | GET | Check ODPS service status |
| `/odps/v1/odps/config` | GET | View ODPS configuration |
| `/odps/v1/odps/nlp` | POST | Execute natural language query |

### Supported Query Patterns

#### English
- "show tables" → `SHOW TABLES`
- "describe table users" → `DESC users`
- "select all from users" → `SELECT * FROM users LIMIT 100`
- "count records in users" → `SELECT COUNT(*) FROM users`
- "show first 10 rows from users" → `SELECT * FROM users LIMIT 10`

#### Chinese (中文)
- "显示表" → `SHOW TABLES`
- "描述表 users" → `DESC users`
- "查询表 users 的所有数据" → `SELECT * FROM users LIMIT 100`
- "统计表 users 的记录数" → `SELECT COUNT(*) FROM users`
- "显示表 users 的前 10 行" → `SELECT * FROM users LIMIT 10`

### Examples

See the `service/src/examples/odps/` directory for complete examples:
- `test_odps_api.sh` - Bash script with all API examples
- `odps_client_example.py` - Python client example
- `ODPSNLPClientExample.java` - Java client example

### Running Tests

```bash
# Run unit tests
mvn test -pl service -Dtest=TestNaturalLanguageParser
mvn test -pl service -Dtest=TestODPSConfig
mvn test -pl service -Dtest=TestODPSClient
```

### Architecture

```
┌─────────────┐
│   Client    │
└──────┬──────┘
       │ HTTP POST/GET
       ▼
┌─────────────────────────────┐
│  ODPSNLPServlet             │
│  (RESTful API)              │
└──────┬──────────────────────┘
       │
       ├──► NaturalLanguageParser
       │    (Convert NL to SQL)
       │
       └──► ODPSClient
            (Execute on ODPS)
```

### Production Deployment

**Important**: The current implementation uses a mock ODPS client. For production:

1. Add the Aliyun ODPS SDK dependency to `service/pom.xml`:
```xml
<dependency>
  <groupId>com.aliyun.odps</groupId>
  <artifactId>odps-sdk-core</artifactId>
  <version>LATEST_VERSION</version>
</dependency>
```

2. Update `ODPSClient.java` to use the real ODPS SDK:
```java
import com.aliyun.odps.Odps;
import com.aliyun.odps.account.AliyunAccount;
import com.aliyun.odps.task.SQLTask;

// In connect() method:
Odps odps = new Odps(new AliyunAccount(accessId, accessKey));
odps.setEndpoint(endpoint);
odps.setDefaultProject(project);

// In executeQuery() method:
Instance instance = SQLTask.run(odps, sql);
instance.waitForSuccess();
List<String> result = SQLTask.getResult(instance);
```

3. Implement proper error handling and connection pooling

### Documentation

For detailed documentation, see:
- [ODPS Service README](service/src/java/org/apache/hive/service/odps/README.md)
- [Configuration Template](conf/odps-site.xml)

---

## 中文

### 概述

本实现为 Apache Hive 添加了阿里云 ODPS（Open Data Processing Service）的自然语言查询服务。用户可以使用自然语言（中文或英文）提交查询并获取 ODPS 的结果。

### 功能特性

- **自然语言处理**：将中英文自然语言查询转换为 SQL
- **RESTful API**：易于使用的 REST 接口
- **灵活配置**：基于 XML 的简单配置
- **模拟实现**：包含用于测试的模拟 ODPS 客户端（可替换为真实的 ODPS SDK）

### 快速开始

#### 1. 配置

在 `conf/hive-site.xml` 或 `conf/odps-site.xml` 中添加以下配置：

```xml
<property>
  <name>odps.enabled</name>
  <value>true</value>
</property>

<property>
  <name>odps.access.id</name>
  <value>您的访问密钥ID</value>
</property>

<property>
  <name>odps.access.key</name>
  <value>您的访问密钥</value>
</property>

<property>
  <name>odps.endpoint</name>
  <value>http://service.odps.aliyun.com/api</value>
</property>

<property>
  <name>odps.project</name>
  <value>您的项目名称</value>
</property>
```

#### 2. 构建并启动 HiveServer2

```bash
# 构建 Hive
mvn clean package -DskipTests

# 启动 HiveServer2
./bin/hiveserver2
```

#### 3. 测试 API

```bash
# 检查服务状态
curl http://localhost:10002/odps/v1/odps/status

# 执行自然语言查询
curl -X POST http://localhost:10002/odps/v1/odps/nlp \
  -H "Content-Type: application/json" \
  -d '{"query": "显示表"}'
```

### API 接口

| 接口 | 方法 | 描述 |
|------|------|------|
| `/odps/v1/odps/status` | GET | 检查 ODPS 服务状态 |
| `/odps/v1/odps/config` | GET | 查看 ODPS 配置 |
| `/odps/v1/odps/nlp` | POST | 执行自然语言查询 |

### 支持的查询模式

#### 中文
- "显示表" → `SHOW TABLES`
- "描述表 users" → `DESC users`
- "查询表 users 的所有数据" → `SELECT * FROM users LIMIT 100`
- "统计表 users 的记录数" → `SELECT COUNT(*) FROM users`
- "显示表 users 的前 10 行" → `SELECT * FROM users LIMIT 10`

#### English
- "show tables" → `SHOW TABLES`
- "describe table users" → `DESC users`
- "select all from users" → `SELECT * FROM users LIMIT 100`
- "count records in users" → `SELECT COUNT(*) FROM users`
- "show first 10 rows from users" → `SELECT * FROM users LIMIT 10`

### 示例

查看 `service/src/examples/odps/` 目录中的完整示例：
- `test_odps_api.sh` - 包含所有 API 示例的 Bash 脚本
- `odps_client_example.py` - Python 客户端示例
- `ODPSNLPClientExample.java` - Java 客户端示例

### 运行测试

```bash
# 运行单元测试
mvn test -pl service -Dtest=TestNaturalLanguageParser
mvn test -pl service -Dtest=TestODPSConfig
mvn test -pl service -Dtest=TestODPSClient
```

### 架构

```
┌─────────────┐
│   客户端     │
└──────┬──────┘
       │ HTTP POST/GET
       ▼
┌─────────────────────────────┐
│  ODPSNLPServlet             │
│  (RESTful API)              │
└──────┬──────────────────────┘
       │
       ├──► NaturalLanguageParser
       │    (将自然语言转为SQL)
       │
       └──► ODPSClient
            (在ODPS上执行)
```

### 生产部署

**重要提示**：当前实现使用模拟的 ODPS 客户端。对于生产环境：

1. 在 `service/pom.xml` 中添加阿里云 ODPS SDK 依赖：
```xml
<dependency>
  <groupId>com.aliyun.odps</groupId>
  <artifactId>odps-sdk-core</artifactId>
  <version>最新版本</version>
</dependency>
```

2. 更新 `ODPSClient.java` 以使用真实的 ODPS SDK

3. 实现适当的错误处理和连接池

### 文档

详细文档请参阅：
- [ODPS 服务 README](service/src/java/org/apache/hive/service/odps/README.md)
- [配置模板](conf/odps-site.xml)

### 支持

如有问题，请在项目仓库中提交 issue。
