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

package org.apache.hive.service.odps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Natural Language to SQL Parser for ODPS queries.
 * This class converts natural language queries into ODPS SQL statements.
 */
public class NaturalLanguageParser {
  private static final Logger LOG = LoggerFactory.getLogger(NaturalLanguageParser.class);

  /**
   * Converts a natural language query to SQL.
   * 
   * @param naturalLanguageQuery the natural language input
   * @return the generated SQL query
   * @throws IllegalArgumentException if the query cannot be parsed
   */
  public String convertToSQL(String naturalLanguageQuery) throws IllegalArgumentException {
    if (naturalLanguageQuery == null || naturalLanguageQuery.trim().isEmpty()) {
      throw new IllegalArgumentException("Query cannot be empty");
    }

    String query = naturalLanguageQuery.trim().toLowerCase();
    
    // If the input already looks like SQL, return it as-is
    if (looksLikeSQL(query)) {
      LOG.info("Input appears to be SQL, returning as-is");
      return naturalLanguageQuery.trim();
    }

    // Pattern: "show tables" or "显示表" or "列出表"
    if (query.matches(".*(?:show|显示|列出).*(?:tables?|表).*") ||
        query.matches(".*(?:tables?|表).*(?:list|列表).*")) {
      return "SHOW TABLES";
    }

    // Pattern: "describe table X" or "描述表 X"
    Pattern describePattern = Pattern.compile(
        ".*(?:describe|desc|描述|查看).*(?:table|表)\\s+([\\w_]+).*",
        Pattern.CASE_INSENSITIVE
    );
    Matcher describeMatcher = describePattern.matcher(query);
    if (describeMatcher.matches()) {
      String tableName = describeMatcher.group(1);
      return "DESC " + tableName;
    }

    // Pattern: "select all from table X" or "查询表 X 的所有数据"
    Pattern selectAllPattern = Pattern.compile(
        ".*(?:select|查询|获取).*(?:all|所有|全部).*(?:from|来自)?.*(?:table|表)?\\s+([\\w_]+).*",
        Pattern.CASE_INSENSITIVE
    );
    Matcher selectAllMatcher = selectAllPattern.matcher(query);
    if (selectAllMatcher.matches()) {
      String tableName = selectAllMatcher.group(1);
      return "SELECT * FROM " + tableName + " LIMIT 100";
    }

    // Pattern: "get/select data from table X where Y" or "查询表 X 条件 Y"
    Pattern selectWherePattern = Pattern.compile(
        ".*(?:select|get|query|查询|获取).*(?:from|来自)?.*(?:table|表)?\\s+([\\w_]+).*(?:where|条件|满足)\\s+(.+)",
        Pattern.CASE_INSENSITIVE
    );
    Matcher selectWhereMatcher = selectWherePattern.matcher(query);
    if (selectWhereMatcher.matches()) {
      String tableName = selectWhereMatcher.group(1);
      String condition = selectWhereMatcher.group(2).trim();
      return "SELECT * FROM " + tableName + " WHERE " + condition + " LIMIT 100";
    }

    // Pattern: "count records in table X" or "统计表 X 的记录数"
    Pattern countPattern = Pattern.compile(
        ".*(?:count|统计|计数).*(?:records?|rows?|记录|行数).*(?:in|from|来自)?.*(?:table|表)?\\s+([\\w_]+).*",
        Pattern.CASE_INSENSITIVE
    );
    Matcher countMatcher = countPattern.matcher(query);
    if (countMatcher.matches()) {
      String tableName = countMatcher.group(1);
      return "SELECT COUNT(*) FROM " + tableName;
    }

    // Pattern: "show first N rows from table X" or "显示表 X 的前 N 行"
    Pattern limitPattern = Pattern.compile(
        ".*(?:show|display|显示|查看).*(?:first|top|前)?\\s*(\\d+).*(?:rows?|records?|行|记录).*(?:from|来自)?.*(?:table|表)?\\s+([\\w_]+).*",
        Pattern.CASE_INSENSITIVE
    );
    Matcher limitMatcher = limitPattern.matcher(query);
    if (limitMatcher.matches()) {
      String limit = limitMatcher.group(1);
      String tableName = limitMatcher.group(2);
      return "SELECT * FROM " + tableName + " LIMIT " + limit;
    }

    // If no pattern matches, throw an exception
    throw new IllegalArgumentException(
        "Unable to parse natural language query. Supported patterns:\n" +
        "- 'show tables' or '显示表'\n" +
        "- 'describe table <name>' or '描述表 <name>'\n" +
        "- 'select all from <table>' or '查询表 <table> 的所有数据'\n" +
        "- 'count records in <table>' or '统计表 <table> 的记录数'\n" +
        "- 'show first N rows from <table>' or '显示表 <table> 的前 N 行'\n" +
        "Or provide a valid SQL query directly."
    );
  }

  /**
   * Checks if the input looks like SQL.
   */
  private boolean looksLikeSQL(String query) {
    // Check for common SQL keywords at the start
    String[] sqlKeywords = {"select", "insert", "update", "delete", "create", 
                            "drop", "alter", "show", "desc", "describe"};
    
    for (String keyword : sqlKeywords) {
      if (query.startsWith(keyword + " ")) {
        return true;
      }
    }
    return false;
  }
}
