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

package org.apache.hive.service.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hive.service.odps.NaturalLanguageParser;
import org.apache.hive.service.odps.ODPSClient;
import org.apache.hive.service.odps.ODPSConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * Servlet for handling ODPS Natural Language Query API.
 * Provides endpoints for:
 * - POST /api/v1/odps/nlp - Execute natural language query
 * - GET /api/v1/odps/status - Check ODPS connection status
 * - GET /api/v1/odps/config - Get ODPS configuration info
 */
public class ODPSNLPServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;
  private static final Logger LOG = LoggerFactory.getLogger(ODPSNLPServlet.class);

  private static final String API_V1 = "v1";
  private static final String REQ_ODPS = "odps";
  private static final String REQ_NLP = "nlp";
  private static final String REQ_STATUS = "status";
  private static final String REQ_CONFIG = "config";

  private NaturalLanguageParser nlpParser;
  private ObjectMapper objectMapper;

  @Override
  public void init() throws ServletException {
    super.init();
    this.nlpParser = new NaturalLanguageParser();
    this.objectMapper = new ObjectMapper();
    LOG.info("ODPSNLPServlet initialized");
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    String pathInfo = request.getPathInfo();
    if (pathInfo == null || "/".equals(pathInfo)) {
      sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Path to the endpoint is missing");
      return;
    }

    String[] splits = pathInfo.split("/");
    if (splits.length < 3) {
      sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Expecting at least 2 parts in the path");
      return;
    }

    String apiVersion = splits[1];
    if (!apiVersion.equals(API_V1)) {
      sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Only API v1 is supported");
      return;
    }

    String reqType = splits[2];
    if (!reqType.equals(REQ_ODPS)) {
      sendError(response, HttpServletResponse.SC_NOT_FOUND, "Unknown request type: " + reqType);
      return;
    }

    if (splits.length < 4) {
      sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Expecting endpoint specification");
      return;
    }

    String endpoint = splits[3];
    
    try {
      if (endpoint.equals(REQ_STATUS)) {
        handleStatusRequest(request, response);
      } else if (endpoint.equals(REQ_CONFIG)) {
        handleConfigRequest(request, response);
      } else {
        sendError(response, HttpServletResponse.SC_NOT_FOUND, "Unknown endpoint: " + endpoint);
      }
    } catch (Exception e) {
      LOG.error("Error handling GET request", e);
      sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                "Error: " + e.getMessage());
    }
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    String pathInfo = request.getPathInfo();
    if (pathInfo == null || "/".equals(pathInfo)) {
      sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Path to the endpoint is missing");
      return;
    }

    String[] splits = pathInfo.split("/");
    if (splits.length < 4) {
      sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid path format");
      return;
    }

    String apiVersion = splits[1];
    String reqType = splits[2];
    String endpoint = splits[3];

    if (!apiVersion.equals(API_V1) || !reqType.equals(REQ_ODPS) || !endpoint.equals(REQ_NLP)) {
      sendError(response, HttpServletResponse.SC_NOT_FOUND, "Invalid endpoint");
      return;
    }

    try {
      handleNLPQueryRequest(request, response);
    } catch (Exception e) {
      LOG.error("Error handling NLP query", e);
      sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                "Error executing query: " + e.getMessage());
    }
  }

  private void handleNLPQueryRequest(HttpServletRequest request, HttpServletResponse response)
      throws Exception {
    // Read request body
    StringBuilder sb = new StringBuilder();
    BufferedReader reader = request.getReader();
    String line;
    while ((line = reader.readLine()) != null) {
      sb.append(line);
    }
    
    String requestBody = sb.toString();
    if (requestBody.isEmpty()) {
      sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Request body is empty");
      return;
    }

    // Parse JSON request
    Map<String, Object> requestMap = objectMapper.readValue(requestBody, Map.class);
    String naturalLanguageQuery = (String) requestMap.get("query");
    
    if (naturalLanguageQuery == null || naturalLanguageQuery.trim().isEmpty()) {
      sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Query field is required");
      return;
    }

    LOG.info("Processing natural language query: {}", naturalLanguageQuery);

    // Convert natural language to SQL
    String sql;
    try {
      sql = nlpParser.convertToSQL(naturalLanguageQuery);
      LOG.info("Converted to SQL: {}", sql);
    } catch (IllegalArgumentException e) {
      sendError(response, HttpServletResponse.SC_BAD_REQUEST, 
                "Failed to parse query: " + e.getMessage());
      return;
    }

    // Get ODPS configuration
    ServletContext ctx = getServletContext();
    HiveConf hiveConf = (HiveConf) ctx.getAttribute("hiveconf");
    if (hiveConf == null) {
      // Fallback to creating a new configuration
      hiveConf = new HiveConf();
    }
    
    ODPSConfig odpsConfig = new ODPSConfig(hiveConf);
    
    if (!odpsConfig.isEnabled()) {
      sendError(response, HttpServletResponse.SC_SERVICE_UNAVAILABLE, 
                "ODPS service is not enabled. Set odps.enabled=true in configuration.");
      return;
    }

    if (!odpsConfig.isValid()) {
      sendError(response, HttpServletResponse.SC_SERVICE_UNAVAILABLE, 
                "ODPS configuration is incomplete. Check access credentials and project settings.");
      return;
    }

    // Execute query on ODPS
    ODPSClient odpsClient = new ODPSClient(odpsConfig);
    try {
      odpsClient.connect();
      Map<String, Object> queryResult = odpsClient.executeQuery(sql);
      
      // Add the converted SQL to the response
      queryResult.put("originalQuery", naturalLanguageQuery);
      queryResult.put("convertedSQL", sql);
      
      sendAsJson(response, queryResult);
    } finally {
      odpsClient.close();
    }
  }

  private void handleStatusRequest(HttpServletRequest request, HttpServletResponse response)
      throws Exception {
    ServletContext ctx = getServletContext();
    HiveConf hiveConf = (HiveConf) ctx.getAttribute("hiveconf");
    if (hiveConf == null) {
      hiveConf = new HiveConf();
    }
    
    ODPSConfig odpsConfig = new ODPSConfig(hiveConf);
    
    Map<String, Object> status = new HashMap<>();
    status.put("enabled", odpsConfig.isEnabled());
    status.put("configured", odpsConfig.isValid());
    
    if (odpsConfig.isEnabled() && odpsConfig.isValid()) {
      ODPSClient odpsClient = new ODPSClient(odpsConfig);
      try {
        odpsClient.connect();
        status.put("connected", odpsClient.isConnected());
        odpsClient.close();
      } catch (Exception e) {
        status.put("connected", false);
        status.put("error", e.getMessage());
      }
    } else {
      status.put("connected", false);
    }
    
    sendAsJson(response, status);
  }

  private void handleConfigRequest(HttpServletRequest request, HttpServletResponse response)
      throws Exception {
    ServletContext ctx = getServletContext();
    HiveConf hiveConf = (HiveConf) ctx.getAttribute("hiveconf");
    if (hiveConf == null) {
      hiveConf = new HiveConf();
    }
    
    ODPSConfig odpsConfig = new ODPSConfig(hiveConf);
    
    Map<String, Object> config = new HashMap<>();
    config.put("enabled", odpsConfig.isEnabled());
    config.put("endpoint", odpsConfig.getEndpoint());
    config.put("project", odpsConfig.getProject());
    config.put("hasAccessId", odpsConfig.getAccessId() != null && !odpsConfig.getAccessId().isEmpty());
    config.put("hasAccessKey", odpsConfig.getAccessKey() != null && !odpsConfig.getAccessKey().isEmpty());
    
    sendAsJson(response, config);
  }

  private void sendError(HttpServletResponse response, Integer errorCode, String message) {
    response.setStatus(errorCode);
    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");
    try {
      Map<String, String> error = new HashMap<>();
      error.put("error", message);
      response.getWriter().write(objectMapper.writeValueAsString(error));
    } catch (IOException e) {
      LOG.error("Caught an exception while writing an HTTP error status", e);
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  private void sendAsJson(HttpServletResponse response, Object obj) {
    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");
    response.setStatus(HttpServletResponse.SC_OK);

    try {
      PrintWriter out = response.getWriter();
      String objectAsJson = objectMapper.writeValueAsString(obj);
      out.print(objectAsJson);
      out.flush();
    } catch (IOException e) {
      LOG.error("Caught an exception while writing an HTTP response", e);
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }
}
