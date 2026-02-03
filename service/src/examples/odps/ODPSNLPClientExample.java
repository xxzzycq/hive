/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.hive.service.odps.examples;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

/**
 * Java client example for ODPS Natural Language Query API.
 */
public class ODPSNLPClientExample {

  private static final String DEFAULT_BASE_URL = "http://localhost:10002";
  private static final ObjectMapper MAPPER = new ObjectMapper();

  private String baseUrl;

  public ODPSNLPClientExample(String baseUrl) {
    this.baseUrl = baseUrl != null ? baseUrl : DEFAULT_BASE_URL;
  }

  /**
   * Check ODPS service status.
   */
  public Map<String, Object> getStatus() throws Exception {
    String url = baseUrl + "/odps/v1/odps/status";
    return executeGetRequest(url);
  }

  /**
   * Get ODPS configuration.
   */
  public Map<String, Object> getConfig() throws Exception {
    String url = baseUrl + "/odps/v1/odps/config";
    return executeGetRequest(url);
  }

  /**
   * Execute a natural language query.
   */
  public Map<String, Object> query(String naturalLanguageQuery) throws Exception {
    String url = baseUrl + "/odps/v1/odps/nlp";
    String jsonInput = "{\"query\": \"" + escapeJson(naturalLanguageQuery) + "\"}";
    return executePostRequest(url, jsonInput);
  }

  private Map<String, Object> executeGetRequest(String urlString) throws Exception {
    URL url = new URL(urlString);
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setRequestMethod("GET");

    int responseCode = conn.getResponseCode();
    if (responseCode != 200) {
      throw new Exception("HTTP GET request failed with response code: " + responseCode);
    }

    try (BufferedReader br = new BufferedReader(
        new InputStreamReader(conn.getInputStream(), "utf-8"))) {
      StringBuilder response = new StringBuilder();
      String responseLine;
      while ((responseLine = br.readLine()) != null) {
        response.append(responseLine.trim());
      }
      return MAPPER.readValue(response.toString(), Map.class);
    }
  }

  private Map<String, Object> executePostRequest(String urlString, String jsonInput) throws Exception {
    URL url = new URL(urlString);
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setRequestMethod("POST");
    conn.setRequestProperty("Content-Type", "application/json");
    conn.setDoOutput(true);

    try (OutputStream os = conn.getOutputStream()) {
      byte[] input = jsonInput.getBytes("utf-8");
      os.write(input, 0, input.length);
    }

    int responseCode = conn.getResponseCode();
    if (responseCode != 200) {
      throw new Exception("HTTP POST request failed with response code: " + responseCode);
    }

    try (BufferedReader br = new BufferedReader(
        new InputStreamReader(conn.getInputStream(), "utf-8"))) {
      StringBuilder response = new StringBuilder();
      String responseLine;
      while ((responseLine = br.readLine()) != null) {
        response.append(responseLine.trim());
      }
      return MAPPER.readValue(response.toString(), Map.class);
    }
  }

  private String escapeJson(String str) {
    return str.replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\n", "\\n")
        .replace("\r", "\\r")
        .replace("\t", "\\t");
  }

  private static void printResult(String title, Map<String, Object> result) throws Exception {
    System.out.println("\n" + "=".repeat(60));
    System.out.println(title);
    System.out.println("=".repeat(60));
    System.out.println(MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(result));
  }

  public static void main(String[] args) {
    System.out.println("ODPS Natural Language Query API - Java Client Example");
    System.out.println("=".repeat(60));

    try {
      // Create client
      ODPSNLPClientExample client = new ODPSNLPClientExample(DEFAULT_BASE_URL);

      // Check status
      Map<String, Object> status = client.getStatus();
      printResult("Service Status", status);

      // Get configuration
      Map<String, Object> config = client.getConfig();
      printResult("Configuration", config);

      // Execute English queries
      System.out.println("\n" + "=".repeat(60));
      System.out.println("English Query Examples");
      System.out.println("=".repeat(60));

      String[] queriesEn = {
          "show tables",
          "describe table users",
          "select all from users",
          "count records in users",
          "show first 10 rows from users"
      };

      for (String query : queriesEn) {
        Map<String, Object> result = client.query(query);
        printResult("Query: " + query, result);
      }

      // Execute Chinese queries
      System.out.println("\n" + "=".repeat(60));
      System.out.println("Chinese Query Examples (中文查询示例)");
      System.out.println("=".repeat(60));

      String[] queriesCn = {
          "显示表",
          "描述表 users",
          "查询表 users 的所有数据",
          "统计表 users 的记录数",
          "显示表 users 的前 20 行"
      };

      for (String query : queriesCn) {
        Map<String, Object> result = client.query(query);
        printResult("Query: " + query, result);
      }

      // Direct SQL query
      System.out.println("\n" + "=".repeat(60));
      System.out.println("Direct SQL Query Example");
      System.out.println("=".repeat(60));

      String sqlQuery = "SELECT * FROM users WHERE id > 100 LIMIT 50";
      Map<String, Object> result = client.query(sqlQuery);
      printResult("SQL: " + sqlQuery, result);

      System.out.println("\n" + "=".repeat(60));
      System.out.println("All examples completed successfully!");
      System.out.println("=".repeat(60));

    } catch (Exception e) {
      System.err.println("\nError: " + e.getMessage());
      e.printStackTrace();
      System.exit(1);
    }
  }
}
